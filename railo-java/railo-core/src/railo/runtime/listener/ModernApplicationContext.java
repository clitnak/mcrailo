package railo.runtime.listener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import railo.print;
import railo.commons.io.res.Resource;
import railo.commons.lang.StringUtil;
import railo.commons.lang.types.RefBoolean;
import railo.runtime.Component;
import railo.runtime.ComponentWrap;
import railo.runtime.Mapping;
import railo.runtime.PageContext;
import railo.runtime.component.Member;
import railo.runtime.config.Config;
import railo.runtime.config.ConfigWeb;
import railo.runtime.config.ConfigWebImpl;
import railo.runtime.exp.PageException;
import railo.runtime.net.s3.Properties;
import railo.runtime.op.Caster;
import railo.runtime.op.Decision;
import railo.runtime.orm.ORMConfiguration;
import railo.runtime.orm.ORMConfigurationImpl;
import railo.runtime.rest.RestSetting;
import railo.runtime.rest.RestSettingImpl;
import railo.runtime.type.Collection;
import railo.runtime.type.Collection.Key;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.cfc.ComponentAccess;
import railo.runtime.type.dt.TimeSpan;
import railo.runtime.type.scope.Scope;
import railo.runtime.type.util.KeyConstants;

public class ModernApplicationContext extends ApplicationContextSupport {

	private static final long serialVersionUID = -8230105685329758613L;

	private static final Collection.Key APPLICATION_TIMEOUT = KeyImpl.intern("applicationTimeout");
	private static final Collection.Key CLIENT_MANAGEMENT = KeyImpl.intern("clientManagement");
	private static final Collection.Key CLIENT_STORAGE = KeyImpl.intern("clientStorage");
	private static final Collection.Key SESSION_STORAGE = KeyImpl.intern("sessionStorage");
	private static final Collection.Key LOGIN_STORAGE = KeyImpl.intern("loginStorage");
	private static final Collection.Key SESSION_TYPE = KeyImpl.intern("sessionType");
	private static final Collection.Key TRIGGER_DATA_MEMBER = KeyImpl.intern("triggerDataMember");
	private static final Collection.Key INVOKE_IMPLICIT_ACCESSOR = KeyImpl.intern("InvokeImplicitAccessor");
	private static final Collection.Key SESSION_MANAGEMENT = KeyImpl.intern("sessionManagement");
	private static final Collection.Key SESSION_TIMEOUT = KeyImpl.intern("sessionTimeout");
	private static final Collection.Key CLIENT_TIMEOUT = KeyImpl.intern("clientTimeout");
	private static final Collection.Key SET_CLIENT_COOKIES = KeyImpl.intern("setClientCookies");
	private static final Collection.Key SET_DOMAIN_COOKIES = KeyImpl.intern("setDomainCookies");
	private static final Collection.Key SCRIPT_PROTECT = KeyImpl.intern("scriptProtect");
	private static final Collection.Key MAPPINGS = KeyImpl.intern("mappings");
	private static final Collection.Key CUSTOM_TAG_PATHS = KeyImpl.intern("customtagpaths");
	private static final Collection.Key COMPONENT_PATHS = KeyImpl.intern("componentpaths");
	private static final Collection.Key SECURE_JSON_PREFIX = KeyImpl.intern("secureJsonPrefix");
	private static final Collection.Key SECURE_JSON = KeyImpl.intern("secureJson");
	private static final Collection.Key LOCAL_MODE = KeyImpl.intern("localMode");
	private static final Collection.Key SESSION_CLUSTER = KeyImpl.intern("sessionCluster");
	private static final Collection.Key CLIENT_CLUSTER = KeyImpl.intern("clientCluster");
	

	private static final Collection.Key DEFAULT_DATA_SOURCE = KeyImpl.intern("defaultdatasource");
	private static final Collection.Key ORM_ENABLED = KeyImpl.intern("ormenabled");
	private static final Collection.Key ORM_SETTINGS = KeyImpl.intern("ormsettings");
	private static final Collection.Key IN_MEMORY_FILESYSTEM = KeyImpl.intern("inmemoryfilesystem");
	private static final Collection.Key REST_SETTING = KeyImpl.intern("restsettings");

	
	private ComponentAccess component;
	private ConfigWeb config;

	private String name=null;
	
	private boolean setClientCookies;
	private boolean setDomainCookies;
	private boolean setSessionManagement;
	private boolean setClientManagement;
	private TimeSpan sessionTimeout;
	private TimeSpan clientTimeout;
	private TimeSpan applicationTimeout;
	private int loginStorage=Scope.SCOPE_COOKIE;
	private int scriptProtect;
	private String defaultDataSource;
	private int localMode;
	private short sessionType;
	private boolean sessionCluster;
	private boolean clientCluster;
	

	private String clientStorage;
	private String sessionStorage;
	private String secureJsonPrefix="//";
	private boolean secureJson; 
	private Mapping[] mappings;
	private Mapping[] ctmappings;
	private Mapping[] cmappings;
	private Properties s3;
	private boolean triggerComponentDataMember;
	private Map<Integer,String> defaultCaches;
	private Map<Integer,Boolean> sameFieldAsArrays;
	
	private boolean initApplicationTimeout;
	private boolean initSessionTimeout;
	private boolean initClientTimeout;
	private boolean initSetClientCookies;
	private boolean initSetClientManagement;
	private boolean initSetDomainCookies;
	private boolean initSetSessionManagement;
	private boolean initScriptProtect;
	private boolean initClientStorage;
	private boolean initSecureJsonPrefix;
	private boolean initSecureJson;
	private boolean initSessionStorage;
	private boolean initSessionCluster;
	private boolean initClientCluster;
	private boolean initLoginStorage;
	private boolean initSessionType;
	private boolean initTriggerComponentDataMember;
	private boolean initMappings;
	private boolean initDefaultCaches;
	private boolean initSameFieldAsArrays;
	private boolean initCTMappings;
	private boolean initCMappings;
	private boolean initLocalMode;
	private boolean initS3;
	private boolean ormEnabled;
	private ORMConfiguration ormConfig;
	private boolean initRestSetting;
	private RestSetting restSetting;
	private String ormDatasource;
		
	public ModernApplicationContext(PageContext pc, ComponentAccess cfc, RefBoolean throwsErrorWhileInit) {
		config = pc.getConfig();
    	setClientCookies=config.isClientCookies();
        setDomainCookies=config.isDomainCookies();
        setSessionManagement=config.isSessionManagement();
        setClientManagement=config.isClientManagement();
        sessionTimeout=config.getSessionTimeout();
        clientTimeout=config.getClientTimeout();
        applicationTimeout=config.getApplicationTimeout();
        scriptProtect=config.getScriptProtect();
        this.defaultDataSource=config.getDefaultDataSource();
        this.localMode=config.getLocalMode();
        this.sessionType=config.getSessionType();
        this.sessionCluster=config.getSessionCluster();
        this.clientCluster=config.getClientCluster();
        this.triggerComponentDataMember=config.getTriggerComponentDataMember();
        
        
		this.component=cfc;
		
		pc.addPageSource(component.getPageSource(), true);
		try {
			
		


			
			/////////// ORM /////////////////////////////////
			reinitORM(pc);
			
			
			throwsErrorWhileInit.setValue(false);
		}
		catch(Throwable t) {
			throwsErrorWhileInit.setValue(true);
			pc.removeLastPageSource(true);
		}
	}


	
	public void reinitORM(PageContext pc) throws PageException {

		// datasource
		Object o = get(component,KeyImpl.DATA_SOURCE,null);
		if(o!=null) {
			String ds;
			if(Decision.isStruct(o)) {
				Struct sct = Caster.toStruct(o);
				ds=Caster.toString(sct.get(KeyConstants._name));
			}
			else ds = Caster.toString(o);
			this.defaultDataSource = ds;
			this.ormDatasource = ds;
		}

		// default datasource
		o=get(component,DEFAULT_DATA_SOURCE,null);
		if(o!=null) this.defaultDataSource =Caster.toString(o);
		
		// ormenabled
		o = get(component,ORM_ENABLED,null);
		if(o!=null && Caster.toBooleanValue(o,false)){
			this.ormEnabled=true;
			
			// settings
			o=get(component,ORM_SETTINGS,null);
			Struct settings;
			if(o instanceof Struct)	settings=(Struct) o;
			else	settings=new StructImpl();
			AppListenerUtil.setORMConfiguration(pc, this, settings);
		}
	}



	/**
	 * @see railo.runtime.util.ApplicationContext#hasName()
	 */
	public boolean hasName() {
		return true;//!StringUtil.isEmpty(getName());
	}
	
	/**
	 * @see railo.runtime.util.ApplicationContext#getName()
	 */
	public String getName() {
		if(this.name==null) {
			this.name=Caster.toString(get(component,KeyConstants._name,""),"");
		}
		return name;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getLoginStorage()
	 */
	public int getLoginStorage() {
		if(!initLoginStorage) {
			String str=null;
			Object o = get(component,LOGIN_STORAGE,null);
			if(o!=null){ 
				str=Caster.toString(o,null);
				if(str!=null)loginStorage=AppListenerUtil.translateLoginStorage(str,loginStorage);
			}
			initLoginStorage=true; 
		}
		return loginStorage;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getApplicationTimeout()
	 */
	public TimeSpan getApplicationTimeout() {
		if(!initApplicationTimeout) {
			Object o=get(component,APPLICATION_TIMEOUT,null);
			if(o!=null)applicationTimeout=Caster.toTimespan(o,applicationTimeout);
			initApplicationTimeout=true;
		}
		return applicationTimeout;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getSessionTimeout()
	 */
	public TimeSpan getSessionTimeout() {
		if(!initSessionTimeout) {
			Object o=get(component,SESSION_TIMEOUT,null);
			if(o!=null)sessionTimeout=Caster.toTimespan(o,sessionTimeout);
			initSessionTimeout=true;
		}
		return sessionTimeout;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getClientTimeout()
	 */
	public TimeSpan getClientTimeout() {
		if(!initClientTimeout) {
			Object o=get(component,CLIENT_TIMEOUT,null);
			if(o!=null)clientTimeout=Caster.toTimespan(o,clientTimeout);
			initClientTimeout=true;
		}
		return clientTimeout;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#isSetClientCookies()
	 */
	public boolean isSetClientCookies() {
		if(!initSetClientCookies) {
			Object o = get(component,SET_CLIENT_COOKIES,null);
			if(o!=null)setClientCookies=Caster.toBooleanValue(o,setClientCookies);
			initSetClientCookies=true;
		}
		return setClientCookies;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#isSetClientManagement()
	 */
	public boolean isSetClientManagement() {
		if(!initSetClientManagement) {
			Object o = get(component,CLIENT_MANAGEMENT,null);
			if(o!=null)setClientManagement=Caster.toBooleanValue(o,setClientManagement);
			initSetClientManagement=true;
		}
		return setClientManagement;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#isSetDomainCookies()
	 */
	public boolean isSetDomainCookies() {
		if(!initSetDomainCookies) {
			Object o = get(component,SET_DOMAIN_COOKIES,null);
			if(o!=null)setDomainCookies=Caster.toBooleanValue(o,setDomainCookies);
			initSetDomainCookies=true;
		}
		return setDomainCookies;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#isSetSessionManagement()
	 */
	public boolean isSetSessionManagement() {
		if(!initSetSessionManagement) {
			Object o = get(component,SESSION_MANAGEMENT,null);
			if(o!=null)setSessionManagement=Caster.toBooleanValue(o,setSessionManagement);
			initSetSessionManagement=true; 
		}
		return setSessionManagement;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getClientstorage()
	 */
	public String getClientstorage() {
		if(!initClientStorage) {
			Object o=get(component,CLIENT_STORAGE,null);
			if(o!=null)clientStorage=Caster.toString(o,clientStorage);
			initClientStorage=true;
		}
		return clientStorage;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getScriptProtect()
	 */
	public int getScriptProtect() {
		if(!initScriptProtect) {
			String str=null;
			Object o = get(component,SCRIPT_PROTECT,null);
			if(o!=null){ 
				str=Caster.toString(o,null);
				if(str!=null)scriptProtect=AppListenerUtil.translateScriptProtect(str);
			}
			initScriptProtect=true; 
		}
		return scriptProtect;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getSecureJsonPrefix()
	 */
	public String getSecureJsonPrefix() {
		if(!initSecureJsonPrefix) {
			Object o=get(component,SECURE_JSON_PREFIX,null);
			if(o!=null)secureJsonPrefix=Caster.toString(o,secureJsonPrefix);
			initSecureJsonPrefix=true;
		}
		return secureJsonPrefix;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getSecureJson()
	 */
	public boolean getSecureJson() {
		if(!initSecureJson) {
			Object o = get(component,SECURE_JSON,null);
			if(o!=null)secureJson=Caster.toBooleanValue(o,secureJson);
			initSecureJson=true; 
		}
		return secureJson;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getSessionstorage()
	 */
	public String getSessionstorage() {
		if(!initSessionStorage) {
			Object o=get(component,SESSION_STORAGE,null);
			if(o!=null)sessionStorage=Caster.toString(o,sessionStorage);
			initSessionStorage=true;
		}
		return sessionStorage;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getSessionCluster()
	 */
	public boolean getSessionCluster() {
		if(!initSessionCluster) {
			Object o = get(component,SESSION_CLUSTER,null);
			if(o!=null)sessionCluster=Caster.toBooleanValue(o,sessionCluster);
			initSessionCluster=true; 
		}
		return sessionCluster;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getClientCluster()
	 */
	public boolean getClientCluster() {
		if(!initClientCluster) {
			Object o = get(component,CLIENT_CLUSTER,null);
			if(o!=null)clientCluster=Caster.toBooleanValue(o,clientCluster);
			initClientCluster=true; 
		}
		return clientCluster;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getSessionType()
	 */
	public short getSessionType() {
		if(!initSessionType) {
			String str=null;
			Object o = get(component,SESSION_TYPE,null);
			if(o!=null){ 
				str=Caster.toString(o,null);
				if(str!=null)sessionType=AppListenerUtil.toSessionType(str, sessionType);
			}
			initSessionType=true; 
		}
		return sessionType;
	}
	




	@Override
	public boolean getTriggerComponentDataMember() {
		if(!initTriggerComponentDataMember) {
			Boolean b=null;
			Object o = get(component,INVOKE_IMPLICIT_ACCESSOR,null);
			if(o==null)o = get(component,TRIGGER_DATA_MEMBER,null);
			if(o!=null){ 
				b=Caster.toBoolean(o,null);
				if(b!=null)triggerComponentDataMember=b.booleanValue();
			}
			initTriggerComponentDataMember=true; 
		}
		return triggerComponentDataMember;
	}



	@Override
	public void setTriggerComponentDataMember(boolean triggerComponentDataMember) {
		initTriggerComponentDataMember=true;
		this.triggerComponentDataMember=triggerComponentDataMember;
	}
	


	@Override
	public boolean getSameFieldAsArray(int scope) {
		if(!initSameFieldAsArrays) {
			if(sameFieldAsArrays==null)sameFieldAsArrays=new HashMap<Integer, Boolean>();
			
			// Form
			Object o = get(component,KeyImpl.init("sameformfieldsasarray"),null);
			if(o!=null && Decision.isBoolean(o))
				sameFieldAsArrays.put(Scope.SCOPE_FORM, Caster.toBooleanValue(o,false));
			
			// URL
			o = get(component,KeyImpl.init("sameurlfieldsasarray"),null);
			if(o!=null && Decision.isBoolean(o))
				sameFieldAsArrays.put(Scope.SCOPE_URL, Caster.toBooleanValue(o,false));
			
			initSameFieldAsArrays=true; 
		}
		return Caster.toBooleanValue(sameFieldAsArrays.get(scope),false);
	}
	

	@Override
	public String getDefaultCacheName(int type) {
		if(!initDefaultCaches) {
			boolean hasResource=false;
			if(defaultCaches==null)defaultCaches=new HashMap<Integer, String>();
			Object o = get(component,KeyConstants._cache,null);
			if(o!=null && Decision.isStruct(o)){ 
				Struct sct = Caster.toStruct(o,null);
				if(sct!=null){
					// Function
					String name=Caster.toString(sct.get(KeyConstants._function,null),null);
					if(!StringUtil.isEmpty(name,true)) defaultCaches.put(Config.CACHE_DEFAULT_FUNCTION, name.trim());
					// Query
					name=Caster.toString(sct.get(KeyConstants._query,null),null);
					if(!StringUtil.isEmpty(name,true)) defaultCaches.put(Config.CACHE_DEFAULT_QUERY, name.trim());
					// Template
					name=Caster.toString(sct.get(KeyConstants._template,null),null);
					if(!StringUtil.isEmpty(name,true)) defaultCaches.put(Config.CACHE_DEFAULT_TEMPLATE, name.trim());
					// Object
					name=Caster.toString(sct.get(KeyConstants._object,null),null);
					if(!StringUtil.isEmpty(name,true)) defaultCaches.put(Config.CACHE_DEFAULT_OBJECT, name.trim());
					// Resource
					name=Caster.toString(sct.get(KeyConstants._resource,null),null);
					if(!StringUtil.isEmpty(name,true)) {
						defaultCaches.put(Config.CACHE_DEFAULT_RESOURCE, name.trim());
						hasResource=true;
					}
					
				}
			}
			
			// check alias inmemoryfilesystem 
			if(!hasResource) {
				String str = Caster.toString(get(component,IN_MEMORY_FILESYSTEM,null),null);
				if(!StringUtil.isEmpty(str,true)) {
					defaultCaches.put(Config.CACHE_DEFAULT_RESOURCE, str.trim());
				}
				
			}
			
			
			
			initDefaultCaches=true; 
		}
		return defaultCaches.get(type);
	}



	@Override
	public void setDefaultCacheName(int type, String cacheName) {
		if(StringUtil.isEmpty(cacheName,true)) return;
		
		initDefaultCaches=true;
		if(defaultCaches==null)defaultCaches=new HashMap<Integer, String>();
		defaultCaches.put(type, cacheName.trim());
	}
	
	
	

	/**
	 * @see railo.runtime.util.ApplicationContext#getMappings()
	 */
	public Mapping[] getMappings() {
		if(!initMappings) {
			Object o = get(component,MAPPINGS,null);
			if(o!=null)mappings=AppListenerUtil.toMappings(config,o,mappings);
			initMappings=true; 
		}
		return mappings;
	}

	/**
	 * @see railo.runtime.util.ApplicationContext#getCustomTagMappings()
	 */
	public Mapping[] getCustomTagMappings() {
		if(!initCTMappings) {
			Object o = get(component,CUSTOM_TAG_PATHS,null);
			if(o!=null)ctmappings=AppListenerUtil.toCustomTagMappings(config,o,ctmappings);
			initCTMappings=true; 
		}
		return ctmappings;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getComponentMappings()
	 */
	public Mapping[] getComponentMappings() {
		if(!initCMappings) {
			Object o = get(component,COMPONENT_PATHS,null);
			if(o!=null)cmappings=AppListenerUtil.toCustomTagMappings(config,o,cmappings);
			initCMappings=true; 
		}
		return cmappings;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getLocalMode()
	 */
	public int getLocalMode() {
		if(!initLocalMode) {
			Object o = get(component,LOCAL_MODE,null);
			if(o!=null)localMode=AppListenerUtil.toLocalMode(o, localMode);
			initLocalMode=true; 
		}
		return localMode;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getS3()
	 */
	public Properties getS3() {
		if(!initS3) {
			Object o = get(component,KeyImpl.S3,null);
			if(o!=null && Decision.isStruct(o))s3=AppListenerUtil.toS3(Caster.toStruct(o,null));
			initS3=true; 
		}
		return s3;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getDefaultDataSource()
	 */
	public String getDefaultDataSource() {
		return defaultDataSource;
	}

	@Override
	public boolean isORMEnabled() {
		return this.ormEnabled;
	}

	@Override
	public String getORMDatasource() {
		return ormDatasource;
	}

	@Override
	public ORMConfiguration getORMConfiguration() {
		return ormConfig;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getComponent()
	 */
	public ComponentAccess getComponent() {
		return component;
	}

	/**
	 * @see railo.runtime.listener.ApplicationContext#getCustom(railo.runtime.type.Collection.Key)
	 */
	public Object getCustom(Key key) {
		try {
			ComponentWrap cw=ComponentWrap.toComponentWrap(Component.ACCESS_PRIVATE, component); 
			return cw.get(key,null);
		} 
		catch (Throwable t) {}
		
		return null;
	}
	
	
	



	private static Object get(ComponentAccess app, Key name,String defaultValue) {
		Member mem = app.getMember(Component.ACCESS_PRIVATE, name, true, false);
		if(mem==null) return defaultValue;
		return mem.getValue();
	}

	
//////////////////////// SETTERS /////////////////////////
	
	
	
	@Override
	public void setApplicationTimeout(TimeSpan applicationTimeout) {
		initApplicationTimeout=true;
		this.applicationTimeout=applicationTimeout;
	}

	@Override
	public void setSessionTimeout(TimeSpan sessionTimeout) {
		initSessionTimeout=true;
		this.sessionTimeout=sessionTimeout;
	}

	@Override
	public void setClientTimeout(TimeSpan clientTimeout) {
		initClientTimeout=true;
		this.clientTimeout=clientTimeout;
	}

	@Override
	public void setClientstorage(String clientstorage) {
		initClientStorage=true;
		this.clientStorage=clientstorage;
	}

	@Override
	public void setSessionstorage(String sessionstorage) {
		initSessionStorage=true;
		this.sessionStorage=sessionstorage;
	}

	@Override
	public void setCustomTagMappings(Mapping[] customTagMappings) {
		initCTMappings=true;
		this.ctmappings=customTagMappings;
	}

	@Override
	public void setComponentMappings(Mapping[] componentMappings) {
		initCMappings=true;
		this.cmappings=componentMappings;
	}

	@Override
	public void setMappings(Mapping[] mappings) {
		initMappings=true;
		this.mappings=mappings;
	}

	@Override
	public void setLoginStorage(int loginStorage) {
		initLoginStorage=true;
		this.loginStorage=loginStorage;
	}

	@Override
	public void setDefaultDataSource(String datasource) {
		this.defaultDataSource=datasource;
	}

	@Override
	public void setScriptProtect(int scriptrotect) {
		initScriptProtect=true;
		this.scriptProtect=scriptrotect;
	}

	@Override
	public void setSecureJson(boolean secureJson) {
		initSecureJson=true;
		this.secureJson=secureJson;
	}

	@Override
	public void setSecureJsonPrefix(String secureJsonPrefix) {
		initSecureJsonPrefix=true;
		this.secureJsonPrefix=secureJsonPrefix;
	}

	@Override
	public void setSetClientCookies(boolean setClientCookies) {
		initSetClientCookies=true;
		this.setClientCookies=setClientCookies;
	}

	@Override
	public void setSetClientManagement(boolean setClientManagement) {
		initSetClientManagement=true;
		this.setClientManagement=setClientManagement;
	}

	@Override
	public void setSetDomainCookies(boolean setDomainCookies) {
		initSetDomainCookies=true;
		this.setDomainCookies=setDomainCookies;
	}

	@Override
	public void setSetSessionManagement(boolean setSessionManagement) {
		initSetSessionManagement=true;
		this.setSessionManagement=setSessionManagement;
	}

	@Override
	public void setLocalMode(int localMode) {
		initLocalMode=true;
		this.localMode=localMode;
	}

	@Override
	public void setSessionType(short sessionType) {
		initSessionType=true;
		this.sessionType=sessionType;
	}

	@Override
	public void setClientCluster(boolean clientCluster) {
		initClientCluster=true;
		this.clientCluster=clientCluster;
	}

	@Override
	public void setSessionCluster(boolean sessionCluster) {
		initSessionCluster=true;
		this.sessionCluster=sessionCluster;
	}

	@Override
	public void setS3(Properties s3) {
		initS3=true;
		this.s3=s3;
	}

	@Override
	public void setORMEnabled(boolean ormEnabled) {
		this.ormEnabled=ormEnabled;
	}

	@Override
	public void setORMConfiguration(ORMConfiguration ormConfig) {
		this.ormConfig=ormConfig;
	}

	@Override
	public void setORMDatasource(String ormDatasource) {
		this.ormDatasource=ormDatasource;
	}

	@Override
	public Resource getSource() {
		return component.getPageSource().getPhyscalFile();
	}



	@Override
	public RestSetting getRestSettings() {
		if(!initRestSetting) {
			Object o = get(component,REST_SETTING,null);
			if(o!=null && Decision.isStruct(o)){
				Struct sct = Caster.toStruct(o,null);
				
				// cfclocation
				Object obj = sct.get(KeyConstants._cfcLocation,null);
				List<Resource> list = ORMConfigurationImpl.loadCFCLocation(config, null, obj);
				Resource[] cfcLocations=list==null?new Resource[0]:list.toArray(new Resource[list.size()]);
				
				// skipCFCWithError
				boolean skipCFCWithError=Caster.toBooleanValue(sct.get(KeyConstants._skipCFCWithError,null),true);
				
				restSetting=new RestSettingImpl(cfcLocations,skipCFCWithError);
				
			}
			initRestSetting=true; 
		}
		return restSetting;
	}



	@Override
	public void setRestSettings(RestSetting restSetting) {
		initRestSetting=true;
		this.restSetting=restSetting;
	}
}
