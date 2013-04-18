package com.mastercontrol.railo.session.mongo;

import java.net.UnknownHostException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.mongodb.DBCollection;
import com.mongodb.Mongo;
import com.mongodb.MongoException;


import railo.runtime.PageContext;
import railo.runtime.type.Collection;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.Struct;
import railo.runtime.type.scope.Session;
import railo.runtime.type.scope.SessionManager;

/**
 * Holds all the MongoSession objects for a single Railo application
 * @author Jason Kapp 
 *
 */
public class MongoSessionManager extends SessionManager {
	
	private static final Collection.Key COLLECTION_NAME = KeyImpl.init("collection"); 
	private static final Collection.Key DB_NAME = KeyImpl.init("db");
	private static final Collection.Key DB_PORT = KeyImpl.init("port");
	private static final Collection.Key DB_HOST = KeyImpl.init("host");
	
	private final ConcurrentHashMap<String, MongoSession> sessions = new ConcurrentHashMap<String, MongoSession>();
	private final Mongo dbConnection; 

	public MongoSessionManager(Struct config) {
		super(config);
		dbConnection = makeConnection();
	}
	
	private Mongo makeConnection() { 
		String host = getDBHost();
		int port = getDBPort();
		
		try {
			return new Mongo(host, port);
		} catch (UnknownHostException e) {
			doConnectionError(e, host, port);
			return null;
		} catch  (MongoException e) {
			doConnectionError(e, host, port);
			return null;
		} 
	}
	
	private void doConnectionError(Exception e, String host, int port) {
		System.err.println("MongoSessionManager was not able to make a connection with MongoDB at " + host + ":" + port);
		e.printStackTrace();
	}

	@Override
	public Session getSession(PageContext pageContext) {
		MongoSession session = getSession(pageContext.getCFID());
		if (session == null) {
			session = newSession(pageContext);
			sessions.put(pageContext.getCFID(), session); 
		}
		
		session.touch();
		return session;
	}
	
	private MongoSession getSession(String cfid) {
		return sessions.get(cfid);
	}
	
	private MongoSession newSession(PageContext pageContext) {
		return new MongoSession(
				pageContext.getCFID(),
				pageContext.getCFToken(),
				pageContext.getURLToken(),
				pageContext.getApplicationContext().getSessionTimeout().getMillis(),
				getDBCollection());
	}
	
	private DBCollection getDBCollection() {
		return getDBConnection().getDB(getDBName()).getCollection(getCollectionName());
	}
	
	private Mongo getDBConnection() {
		return dbConnection; 
	}

	private String getDBName() {
		return getConfigValue(DB_NAME);
	}

	private String getCollectionName() {
		return getConfigValue(COLLECTION_NAME);
	}
	
	private int getDBPort() {
		return Integer.parseInt(getConfigValue(DB_PORT));
	}
	
	private String getDBHost() {
		return getConfigValue(DB_HOST);
	}
	
	private String getConfigValue(Collection.Key key) {
		return getConfig().get(key, null).toString();
	}
	

	@Override
	public void removeSession(String cfid) {
		getSession(cfid).delete();
		sessions.remove(cfid);
	}

	@Override
	public void clear() {
		for(String cfid : sessions.keySet()) {
			removeSession(cfid);
		}
	}

	@Override
	public Map getAllSessions() {
		return new ConcurrentHashMap<String, MongoSession>(sessions);
	}
}
