package railo.runtime.type.scope;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import railo.runtime.PageContext;
import railo.runtime.dump.DumpData;
import railo.runtime.dump.DumpProperties;
import railo.runtime.exp.PageException;
import railo.runtime.type.Collection;
import railo.runtime.type.Struct;
import railo.runtime.type.dt.DateTime;

public class MongoSessionScope implements Session {

	@Override
	public final Object clone(){
		// TODO This might not be what we want, but taking it out causes an error
		// Check on this
		return duplicate(true);
	}
	
	@Override
	public boolean isInitalized() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void initialize(PageContext pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void release() {
		// TODO Auto-generated method stub

	}

	@Override
	public void release(PageContext pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public int getType() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public String getTypeAsString() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int size() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Key[] keys() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(Key key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object removeEL(Key key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clear() {
		// TODO Auto-generated method stub

	}

	@Override
	public Object get(String key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(Key key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(String key, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(Key key, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(String key, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(Key key, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(String key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(Key key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Collection duplicate(boolean deepCopy) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean containsKey(String key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Key key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public DumpData toDumpData(PageContext pageContext, int maxlevel, DumpProperties properties) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Key> keyIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<String> keysAsStringIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Object> valueIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Iterator<Entry<Key, Object>> entryIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String castToString() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String castToString(String defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean castToBooleanValue() throws PageException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Boolean castToBoolean(Boolean defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public double castToDoubleValue() throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public double castToDoubleValue(double defaultValue) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public DateTime castToDateTime() throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public DateTime castToDateTime(DateTime defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int compareTo(String str) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(boolean b) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(double d) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int compareTo(DateTime dt) throws PageException {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Iterator<?> getIterator() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean isEmpty() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsKey(Object key) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean containsValue(Object value) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Object get(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object put(Object key, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object remove(Object key) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void putAll(Map m) {
		// TODO Auto-generated method stub

	}

	@Override
	public Set keySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public java.util.Collection values() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Set entrySet() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public long sizeOf() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Object get(PageContext pc, Key key, Object defaultValue) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object get(PageContext pc, Key key) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object set(PageContext pc, Key propertyName, Object value) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object setEL(PageContext pc, Key propertyName, Object value) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object call(PageContext pc, Key methodName, Object[] arguments) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Object callWithNamedValues(PageContext pc, Key methodName, Struct args) throws PageException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void resetEnv(PageContext pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void touchBeforeRequest(PageContext pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public void touchAfterRequest(PageContext pc) {
		// TODO Auto-generated method stub

	}

	@Override
	public long getLastAccess() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getTimeSpan() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public long getCreated() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isExpired() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void touch() {
		// TODO Auto-generated method stub

	}

	@Override
	public int _getId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public Key[] pureKeys() {
		// TODO Auto-generated method stub
		return null;
	}

}
