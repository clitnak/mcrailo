package com.mastercontrol.railo.session.mongo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import com.mongodb.BasicDBObject;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.mongodb.ReadPreference;
import com.mongodb.WriteConcern;
import com.mongodb.WriteResult;

import railo.commons.lang.CFTypes;
import railo.loader.engine.CFMLEngineFactory;
import railo.runtime.PageContext;
import railo.runtime.dump.DumpData;
import railo.runtime.dump.DumpProperties;
import railo.runtime.dump.DumpUtil;
import railo.runtime.exp.ExpressionException;
import railo.runtime.exp.PageException;
import railo.runtime.type.Collection;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.UDF;
import railo.runtime.type.dt.DateTime;
import railo.runtime.type.scope.Session;
import railo.runtime.type.util.KeyConstants;
import railo.runtime.type.util.MemberUtil;
import railo.runtime.functions.dynamicEvaluation.Evaluate;
import railo.runtime.functions.dynamicEvaluation.Serialize;

public class MongoSession implements Session {
	
	//STATICS
	private static final int SESSION_TYPE = -1;
	private static final String SESSION_TYPE_STR = "MongoSessionScope";
	private static final long serialVersionUID = -6799439320357915754L;
	private static final String STARTING_HIT_COUNT = "1";
	private static final String ID_KEY = "_id";
	
	private static final Collection.Key CFID=KeyConstants._cfid;
	private static final Collection.Key CFTOKEN=KeyConstants._cftoken;
	private static final Collection.Key URL_TOKEN=KeyConstants._urltoken;
	private static final Collection.Key LAST_VISIT=KeyConstants._lastvisit;
	private static final Collection.Key HIT_COUNT=KeyConstants._hitcount;
	private static final Collection.Key TIME_CREATED=KeyConstants._timecreated;
	
	private static final Set<Collection.Key> EXTRA_KEYS = new HashSet<Collection.Key>();
	static {
		EXTRA_KEYS.add(CFID);
		EXTRA_KEYS.add(CFTOKEN);
		EXTRA_KEYS.add(URL_TOKEN);
		EXTRA_KEYS.add(LAST_VISIT);
		EXTRA_KEYS.add(HIT_COUNT);
		EXTRA_KEYS.add(TIME_CREATED);
	}
	
	private static int idCounter = 0;
	
	private static int nextId() {
		return ++idCounter;
	}

	//INSTANCE VARIABLES
	private final int id;
	private final String cfid;
	private final String cftoken;
	private final String urlToken;
	private final DBCollection collection;
	private final DBObject sessionQuery;
	private final long sessionTimeout;
	
	private boolean isInit = true;
	
	//CONSTRUCTORS AND INSTANCE METHODS
	public MongoSession(String cfid, String cftoken, String urlToken, long sessionTimeout, DBCollection collection) {
		this.cfid = cfid;
		this.cftoken = cftoken;
		this.urlToken = urlToken;
		this.sessionTimeout = sessionTimeout;
		this.collection = setupCollection(collection);
		this.sessionQuery = makeDBSessionQuery(cfid);
		this.id = nextId();
		
		if (!sessionExists()) {
			insertSession(makeNewSession());
		}
	}
		
	private DBObject makeDBSessionQuery(String cfid) {
		try {
			return QueryBuilder.start(toDBKey(CFID)).is(serializeToString(cfid)).get();
		}
		catch (PageException e) {
			e.printStackTrace();
			return QueryBuilder.start(toDBKey(CFID)).is("'" + cfid + "'").get();
		}
	}
	
	private DBCollection setupCollection(DBCollection coll) {
		coll.ensureIndex(new BasicDBObject(toDBKey(CFID),""));
		coll.setWriteConcern(WriteConcern.SAFE); // this is the most consistent but has tradeoffs.  See mongogdb docs.
		coll.setReadPreference(ReadPreference.PRIMARY);
		return coll;
	}
	
	private static void log(String stuff) {
		System.out.println("STORE: " + stuff);
	}
	
	private boolean sessionExists() {
		return collection.findOne(sessionQuery)  != null;
	}
	
	private void insertSession(Map<String, String> session) {
		//instead of constructing a DBObject to insert here
		//the following is done to ensure the default 
		//session key value pairs and serialized like 
		//all other session values.
		collection.insert(sessionQuery);
		putAll(session);
	}
	
	private Map<String, String> makeNewSession() {
		Map<String, String> newSession = new HashMap<String, String>();
		newSession.put(toDBKey(CFID), this.cfid);
		newSession.put(toDBKey(CFTOKEN), this.cftoken);
		newSession.put(toDBKey(URL_TOKEN), this.urlToken);
		newSession.put(toDBKey(HIT_COUNT), STARTING_HIT_COUNT);
		newSession.put(toDBKey(TIME_CREATED), nowAsString());
		newSession.put(toDBKey(LAST_VISIT), nowAsString());
		
		return newSession;
	}
	
	public WriteResult delete() {
		return collection.remove(this.sessionQuery);
	}
	
	// MONGODB METHODS
	private DBObject getDBSession() {
		return collection.findOne(sessionQuery);
	}
	
	private Object getDBValue(String key) {
		key = toDBKey(key);
		DBObject dbSession = collection.findOne(sessionQuery, new BasicDBObject(key, ""));
		Object value = null;
		if (dbSession != null) {
			value = dbSession.get(key);
		}
		
		return value;
	}
	
	//throws PageException for serialization
	private WriteResult updateDBKey(String key, Object val) throws PageException {
		return collection.update(sessionQuery, makeSetObject(toDBKey(key), val));
	}
	
	//throws PageException for serialization
	private WriteResult updateDBMulti(Map<?,?> data) throws PageException {
		return collection.update(sessionQuery, makeSetMultiObject(data));
	}
	
	private WriteResult removeDBKey(String key) {
		return collection.update(sessionQuery, makeUnsetObject(toDBKey(key)));
	}
	
	private WriteResult removeDBKeys(String[] keys) {
		return collection.update(sessionQuery, makeUnsetMultiObject(keys));
	}
	
	//throws PageException for serialization
	private DBObject makeSetObject(String key, Object val) throws PageException {
		return new BasicDBObject("$set", new BasicDBObject(toDBKey(key), serializeToString(val)));
	}
	
	private DBObject makeSetMultiObject(Map<?,?> data) throws PageException {
		return new BasicDBObject("$set", new BasicDBObject(serializeAllValues(data)));
	}
	
	private Map<String, String> serializeAllValues(Map<?,?> data) throws PageException {
		Map<String, String> serializedData = new HashMap<String, String>();
		
		for (Object key : data.keySet()) {
			String strKey = (key instanceof String) ? toDBKey((String) key) : serializeToString(key);
			serializedData.put(strKey, serializeToString(data.get(key)));
		}
		
		return serializedData;
	}
	
	private DBObject makeUnsetObject(String key) {
		return new BasicDBObject("$unset", new BasicDBObject(key, ""));
	}
	
	private DBObject makeUnsetMultiObject(String[] keys) {
		HashMap<String, String> deleteMap = new HashMap<String, String>();
		for (String k : keys) {
			deleteMap.put(toDBKey(k), "");
		}
		
		return new BasicDBObject("$unset", new BasicDBObject(deleteMap));
	}
	
	//UTIL METHODS
	private PageContext getThreadPageContext() {
		return CFMLEngineFactory.getInstance().getThreadPageContext();
	}
	
	private String serializeToString(Object val) throws PageException {
		return Serialize.call(getThreadPageContext(), val);
	}
	
	private Object deserializeFromString(Object val) throws PageException {
		return Evaluate.call(getThreadPageContext(), new Object[]{ val });
	}
	
	private String toDBKey(Collection.Key key) {
		return key.getUpperString();
	}
	
	private String toDBKey(String key) {
		return key.toUpperCase();
	}
	
	private Double getAsDouble(Collection.Key key) {
		return getAsDouble(toDBKey(key));
	}

	private Double getAsDouble(String key) {
		try {
			Object val = get(key);
			if (val instanceof Double) {
				return ((Double) val).doubleValue();
			} 
			
			return Double.parseDouble(val.toString());
		} catch (PageException e) {
			e.printStackTrace();
			return null; 
		} catch (NumberFormatException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	private Long now() {
		return new Long(System.currentTimeMillis());
	}
	
	private String nowAsString() {
		return now().toString();
	}
	
	private Long getAsLong(Collection.Key key, Long nullPatch) {
		Double val = getAsDouble(key);
		if (val == null) {
			return nullPatch;
		}
		
		return val.longValue();
	}
	
	//RAILO'S SESSION INTERFACE METHODS BELOW
	@Override
	public final Object clone(){
		return duplicate(true);
	}
	
	@Override
	public boolean isInitalized() {
		return isInit; 
	}

	@Override
	public void initialize(PageContext pc) {
		//all initialization is done in constructors for now.
	}

	@Override
	public void release() {
		clear();
		isInit = false;
	}

	@Override
	public void release(PageContext pc) {
		release();
	}

	@Override
	public int getType() {
		// return a type that doesn't exist in railo,
		// which makes railo think it's a cfml session
		return SESSION_TYPE; 
	}

	@Override
	public String getTypeAsString() {
		return SESSION_TYPE_STR; 
	}

	@Override
	public int size() {
		return keySet().size(); 
	}

	@Override
	public Collection.Key[] keys() {
		HashSet<Collection.Key> keys = new HashSet<Collection.Key>();
		
		for (String strKey : getDBSession().keySet()) {
			keys.add(KeyImpl.init(strKey));
		}
		
		return keys.toArray(new Collection.Key[]{});
	}

	@Override
	public Object remove(Collection.Key key) throws PageException {
		Object val = get(key);
		removeDBKey(toDBKey(key));
		return val;
	}

	@Override
	public Object removeEL(Collection.Key key) {
		try {
			return remove(key);
		} catch (PageException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void clear() {
		Set<String> keysToClear = this.keySet();
		for (Collection.Key k : EXTRA_KEYS) {
			keysToClear.remove(toDBKey(k));
		}
		
		keysToClear.remove(ID_KEY);
		
		this.removeDBKeys(keysToClear.toArray(new String[]{}));
	}
	
	@Override
	public Object get(String key) throws PageException {
		Object val = getDBValue(key);
		if (val == null) {
			return null;
		}
		log(val.toString());
		return deserializeFromString(getDBValue(key));
	}

	@Override
	public Object get(Collection.Key key) throws PageException {
		return get(toDBKey(key));
	}

	@Override
	public Object get(String key, Object defaultValue) {
		try {
			Object val = get(key);
			if (val == null) {
				set(key, defaultValue);
				val = defaultValue;
			}
			
			return val;
		} catch (PageException e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Object get(Collection.Key key, Object defaultValue) {
		return get(toDBKey(key), defaultValue);
	}
	
	@Override
	public Object set(String key, Object value) throws PageException {
		updateDBKey(key, value);
		return value;
	}

	@Override
	public Object set(Collection.Key key, Object value) throws PageException {
		return set(toDBKey(key), value);
	}

	@Override
	public Object setEL(String key, Object value) {
		try {
			set(key, value);
		} catch (PageException e) {
			e.printStackTrace();
			return null;
		}
		
		return value;
	}

	@Override
	public Object setEL(Collection.Key key, Object value) {
		return setEL(toDBKey(key), value);
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		if (deepCopy) {
			return deepCopy();
		}
		
		return new MongoSession(this.cfid, this.cftoken, this.urlToken, this.sessionTimeout, this.collection);
	}
	
	private Struct deepCopy() {
		// "deep copy" here just means get everything 
		// from mongodb and put it into a struct
		Struct sct = new StructImpl();
		sct.putAll(toMap());
		
		return sct;
	}
	
	public Map toMap() {
		Map map = new HashMap();
		for (Key k : keys()) {
			map.put(k, get(k, null));
		}
		
		return map;
	}

	@Override
	public boolean containsKey(String key) {
		return keySet().contains(key); 
	}

	@Override
	public boolean containsKey(Collection.Key key) {
		return containsKey(toDBKey(key));
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		return DumpUtil.toDumpData(this, pageContext, maxlevel, properties);
	}

	@Override
	public Iterator<Collection.Key> keyIterator() {
		LinkedList<Collection.Key> keys = new LinkedList<Collection.Key>();
		for (Collection.Key k : keys()) {
			keys.add(k);
		}
		
		return keys.iterator();
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		return this.keySet().iterator(); 
	}

	@Override
	public Iterator<Object> valueIterator() {
		return this.values().iterator();
	}

	@Override
	public Iterator<Entry<Collection.Key, Object>> entryIterator() {
		return entrySet().iterator(); 
	}

	@Override
	public String castToString() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Session Scope to String");
	}

	@Override
	public String castToString(String defaultValue) {
		return defaultValue;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Session Scope to Boolean");
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		return defaultValue;
	}

	@Override
	public double castToDoubleValue() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Session Scope to Double");
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		return defaultValue;
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		throw new ExpressionException("Can't cast Complex Object Type Session Scope to DateTime");
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		return defaultValue;
	}

	@Override
	public int compareTo(String str) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Session with a String value");
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Session Scope with a boolean value");
	}

	@Override
	public int compareTo(double d) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Session Scope with a double value");
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		throw new ExpressionException("can't compare Complex Object Type Session Scope with a DateTime value");
	}

	@Override
	public Iterator<?> getIterator() {
		return this.entryIterator();
	}

	@Override
	public boolean isEmpty() {
		return size() == 0;
	}
	
	@Override
	public boolean containsKey(Object key) {
		return keySet().contains(toDBKey(KeyImpl.toKey(key, null)));
	}

	@Override
	public boolean containsValue(Object value) {
		//copied from railo.runtime.type.util.StructSupport
		return values().contains(value);
	}

	@Override
	public Object get(Object key) {
		return get(KeyImpl.toKey(key,null), null);
	}

	@Override
	public Object put(Object key, Object value) {
		//copied from railo.runtime.type.util.StructSupport
		return setEL(KeyImpl.toKey(key,null), value);
	}

	@Override
	public Object remove(Object key) {
		//copied from railo.runtime.type.util.StructSupport
		return removeEL(KeyImpl.toKey(key,null));
	}

	@Override
	public void putAll(Map data) {
		try {
			this.updateDBMulti(data);
		} catch (PageException e) {
			//not really sure what to do here
			e.printStackTrace();
		}
	}

	@Override
	public Set keySet() {
		//railo.runtime.type.util.StructUtil returns Set<String> 
		//so we can do that same 
		return getDBSession().keySet();
	}

	@Override
	public java.util.Collection values() {
		return toMap().values();
	}

	@Override
	public Set entrySet() {
		return toMap().entrySet();
	}

	@Override
	public long sizeOf() {
		return size();
	}

	@Override
	public Object get(PageContext pc, Collection.Key key, Object defaultValue) {
		return get(key, defaultValue);
	}

	@Override
	public Object get(PageContext pc, Collection.Key key) throws PageException {
		return get(key);
	}

	@Override
	public Object set(PageContext pc, Collection.Key propertyName, Object value) throws PageException {
		return set(propertyName, value);
	}

	@Override
	public Object setEL(PageContext pc, Collection.Key propertyName, Object value) {
		return setEL(propertyName, value);
	}

	@Override
	//copied from railo.runtime.type.util.StructSupport
	public Object call(PageContext pc, Collection.Key methodName, Object[] args) throws PageException {
		Object obj = get(methodName,null);
		if(obj instanceof UDF) {
			return ((UDF)obj).call(pc,args,false);
		}
		return MemberUtil.call(pc, this, methodName, args, CFTypes.TYPE_STRUCT, "struct");
	}

    @Override
	//copied from railo.runtime.type.util.StructSupport
	public Object callWithNamedValues(PageContext pc, Collection.Key methodName, Struct args) throws PageException {
		Object obj = get(methodName,null);
		if(obj instanceof UDF) {
			return ((UDF)obj).callWithNamedValues(pc,args,false);
		}
		return MemberUtil.callWithNamedValues(pc,this,methodName,args, CFTypes.TYPE_STRUCT, "struct");
	}

	@Override
	public void resetEnv(PageContext pc) {
		//change to set multi???
		setEL(LAST_VISIT, now()); 
		setEL(TIME_CREATED, now()); 
		touch();
	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		touch();
	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		touch();
	}

	@Override
	public long getLastAccess() {
		return getAsLong(LAST_VISIT, now());
	}

	@Override
	public long getTimeSpan() {
		return sessionTimeout;
	}

	@Override
	public long getCreated() {
		return getAsLong(TIME_CREATED, now());
	}
	
	@Override
	public boolean isExpired() {
		return (getLastAccess() + getTimeSpan()) < now(); 
	}

	@Override
	public void touch() {
		Map<String, Object> data = new HashMap<String, Object>();
		data.put(toDBKey(HIT_COUNT), getIncrementedHitCount()); 
		data.put(toDBKey(LAST_VISIT), now()); 
		this.putAll(data);
	}
	
	private Long getIncrementedHitCount() {
		Double hitCount = getAsDouble(HIT_COUNT);
		if (hitCount == null) {
			hitCount = 1.0;
		} else {
			hitCount++;
		}
		
		return hitCount.longValue();
	}

	@Override
	public int _getId() {
		return this.id;
	}

	@Override
	public Collection.Key[] pureKeys() {
		LinkedList<Collection.Key> pureKeys = new LinkedList<Collection.Key>();
		for (Collection.Key k : this.keys()) {
			if (!EXTRA_KEYS.contains(k)) {
				pureKeys.add(k);
			}
		}
		
		return (Collection.Key[]) pureKeys.toArray();
	}
}
