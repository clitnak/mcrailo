package railo.runtime.type.scope;

import java.util.Map;

public abstract class SessionManager {
	
	private static SessionManager instance = null;
	
	public static SessionManager getInstance(String className) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
		if (instance == null) {
			Class<?> clazz = Class.forName(className);
			instance = (SessionManager) clazz.newInstance(); 
		}
		
		return instance;
	}
	
	public static SessionManager getInstance() {
		return instance;
	}
	
	public abstract Session getSession(String CFID);
    public abstract void removeSession(String CFID);
    public abstract void clear();
    public abstract Map<String, Session> getAllSessions();
}
