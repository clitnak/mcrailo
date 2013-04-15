package railo.runtime.type.scope;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import railo.runtime.PageContext;
import railo.runtime.type.Struct;

public abstract class SessionManager {
	
	private static SessionManager instance = null;
	private final Struct config;
	
	public synchronized static SessionManager getInstance(String className, Struct config) 
			throws ClassNotFoundException, SecurityException, NoSuchMethodException, 
			IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
		
		if (instance == null) {
			Class<?> clazz = Class.forName(className);
			//instance = (SessionManager) clazz.newInstance(); 
			Constructor<?> ctor =  clazz.getConstructor(config.getClass());
			instance = (SessionManager) ctor.newInstance(config);
		}
		
		return instance;
	}

	public static SessionManager getInstance() {
		return instance;
	}
	
	public SessionManager(Struct config) {
		this.config = config; 
	}
	
	public Struct getConfig() {
		return this.config;
	}
	
	public abstract Session getSession(PageContext pageContext);
    public abstract void removeSession(String CFID);
    public abstract void clear();
    public abstract Map<String, Session> getAllSessions();
}
