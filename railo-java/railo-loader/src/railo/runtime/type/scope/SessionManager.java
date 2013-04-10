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
	
	public abstract Session getSession(String applicationName, String CFID);
	public abstract Map<String, Scope> getSessionScopesForApplication(String applicationName);
    public abstract void removeSession(String applicationName, String CFID);
}
