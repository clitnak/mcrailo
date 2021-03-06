package railo.runtime; 

import java.net.URL;
import java.util.Iterator;
import java.util.Stack;

import javax.servlet.Servlet;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.JspEngineInfo;

import railo.commons.io.SystemUtil;
import railo.commons.io.log.Log;
import railo.commons.io.res.util.ResourceUtil;
import railo.commons.lang.SizeOf;
import railo.commons.lang.SystemOut;
import railo.runtime.config.ConfigWeb;
import railo.runtime.config.ConfigWebImpl;
import railo.runtime.engine.CFMLEngineImpl;
import railo.runtime.engine.ThreadLocalPageContext;
import railo.runtime.exp.Abort;
import railo.runtime.exp.PageException;
import railo.runtime.exp.PageExceptionImpl;
import railo.runtime.exp.RequestTimeoutException;
import railo.runtime.functions.string.Hash;
import railo.runtime.lock.LockManager;
import railo.runtime.op.Caster;
import railo.runtime.query.QueryCache;
import railo.runtime.type.Array;
import railo.runtime.type.ArrayImpl;
import railo.runtime.type.Collection;
import railo.runtime.type.KeyImpl;
import railo.runtime.type.List;
import railo.runtime.type.Struct;
import railo.runtime.type.StructImpl;
import railo.runtime.type.dt.DateTimeImpl;
import railo.runtime.type.scope.ArgumentIntKey;
import railo.runtime.type.scope.LocalNotSupportedScope;
import railo.runtime.type.scope.ScopeContext;
import railo.runtime.type.util.ArrayUtil;

/**
 * implements a JSP Factory, this class produce JSP Compatible PageContext Object
 * this object holds also the must interfaces to coldfusion specified functionlity
 */
public final class CFMLFactoryImpl extends CFMLFactory {
	
	private static JspEngineInfo info=new JspEngineInfoImpl("1.0");
	private ConfigWebImpl config;
	Stack<PageContext> pcs=new Stack<PageContext>();
    private Struct runningPcs=new StructImpl();
    int idCounter=1;
    private QueryCache queryCache;
    private ScopeContext scopeContext=new ScopeContext(this);
    private HttpServlet servlet;
	private URL url=null;
	private CFMLEngineImpl engine;

	/**
	 * constructor of the JspFactory
	 * @param config Railo specified Configuration
	 * @param compiler Cold Fusion compiler
	 * @param engine
	 */
	public CFMLFactoryImpl(CFMLEngineImpl engine,QueryCache queryCache) {
		this.engine=engine; 
		this.queryCache=queryCache; 
	}
    
    /**
     * reset the PageContexes
     */
    public void resetPageContext() {
        SystemOut.printDate(config.getOutWriter(),"Reset "+pcs.size()+" Unused PageContexts");
        synchronized(pcs) {
            pcs.clear();
        }
    }
    
	/**
	 * @see javax.servlet.jsp.JspFactory#getPageContext(javax.servlet.Servlet, javax.servlet.ServletRequest, javax.servlet.ServletResponse, java.lang.String, boolean, int, boolean)
	 */
	public javax.servlet.jsp.PageContext getPageContext(
		Servlet servlet,
		ServletRequest req,
		ServletResponse rsp,
		String errorPageURL,
		boolean needsSession,
		int bufferSize,
		boolean autoflush) {
			return getPageContextImpl((HttpServlet)servlet,(HttpServletRequest)req,(HttpServletResponse)rsp,errorPageURL,needsSession,bufferSize,autoflush,true,false);
	}
	
	/**
	 * similar to getPageContext Method but return the concrete implementation of the railo PageCOntext
	 * and take the HTTP Version of the Servlet Objects
	 * @param servlet
	 * @param req
	 * @param rsp
	 * @param errorPageURL
	 * @param needsSession
	 * @param bufferSize
	 * @param autoflush
	 * @return return the page<context
	 */
	public PageContext getRailoPageContext(
	HttpServlet servlet,
	HttpServletRequest req,
	HttpServletResponse rsp,
        String errorPageURL,
		boolean needsSession,
		int bufferSize,
		boolean autoflush)  {
        //runningCount++;
        return getPageContextImpl(servlet, req, rsp, errorPageURL, needsSession, bufferSize, autoflush,true,false);
	}
	
	public PageContextImpl getPageContextImpl(
			HttpServlet servlet,
			HttpServletRequest req,
			HttpServletResponse rsp,
		        String errorPageURL,
				boolean needsSession,
				int bufferSize,
				boolean autoflush,boolean registerPageContext2Thread,boolean isChild)  {
		        //runningCount++;
				PageContextImpl pc;
        		synchronized (pcs) {
		            if(pcs.isEmpty()) pc=new PageContextImpl(scopeContext,config,queryCache,idCounter++,servlet);
		            else pc=((PageContextImpl)pcs.pop());
		            runningPcs.setEL(ArgumentIntKey.init(pc.getId()),pc);
		            this.servlet=servlet;
		            if(registerPageContext2Thread)ThreadLocalPageContext.register(pc);
		    		
		        }
		        pc.initialize(servlet,req,rsp,errorPageURL,needsSession,bufferSize,autoflush,isChild);
		        return pc;
			}

    /**
	 * @see javax.servlet.jsp.JspFactory#releasePageContext(javax.servlet.jsp.PageContext)
	 */
	public void releasePageContext(javax.servlet.jsp.PageContext pc) {
		releaseRailoPageContext((PageContext)pc);
	}
	
	/**
	 * Similar to the releasePageContext Method, but take railo PageContext as entry
	 * @param pc
	 */
	public void releaseRailoPageContext(PageContext pc) {
		if(pc.getId()<0)return;
        pc.release();
        ThreadLocalPageContext.release();
        //if(!pc.hasFamily()){
			synchronized (runningPcs) {
	            runningPcs.removeEL(ArgumentIntKey.init(pc.getId()));
	            if(pcs.size()<100)// not more than 100 PCs
	            	pcs.push(pc);
	            SystemOut.printDate(config.getOutWriter(),"Release: ("+pc.getId()+")");
	        }
       /*}
        else {
        	 SystemOut.printDate(config.getOutWriter(),"Unlink: ("+pc.getId()+")");
        }*/
	}
    
    /**
	 * check timeout of all running threads, downgrade also priority from all thread run longer than 10 seconds
	 */
	public void checkTimeout() {
		if(!engine.allowRequestTimeout())return;
		synchronized (runningPcs) {
            //int len=runningPcs.size();
			Iterator it = runningPcs.keyIterator();
            PageContext pc;
            Collection.Key key;
            while(it.hasNext()) {
            	key=KeyImpl.toKey(it.next(),null);
                //print.out("key:"+key);
                pc=(PageContext) runningPcs.get(key,null);
                if(pc==null) {
                	runningPcs.removeEL(key);
                	continue;
                }
                
                long timeout=pc.getRequestTimeout();
                if(pc.getStartTime()+timeout<System.currentTimeMillis()) {
                    terminate(pc);
                }
                // after 10 seconds downgrade priority of the thread
                else if(pc.getStartTime()+10000<System.currentTimeMillis() && pc.getThread().getPriority()!=Thread.MIN_PRIORITY) {
                    Log log = config.getRequestTimeoutLogger();
                    if(log!=null)log.warn("controler","downgrade priority of the a thread at "+getPath(pc));
                    try {
                    	pc.getThread().setPriority(Thread.MIN_PRIORITY);
                    }
                    catch(Throwable t) {}
                }
            }
        }
	}
	
	public static void terminate(PageContext pc) {
		Log log = pc.getConfig().getRequestTimeoutLogger();
        
		String strLocks="";
		try{
			LockManager manager = pc.getConfig().getLockManager();
	        String[] locks = manager.getOpenLockNames();
	        if(!ArrayUtil.isEmpty(locks)) 
	        	strLocks=" open locks at this time ("+List.arrayToList(locks, ", ")+").";
	        //LockManagerImpl.unlockAll(pc.getId());
		}
		catch(Throwable t){}
        
        if(log!=null)log.error("controler",
        		"stop thread ("+pc.getId()+") because run into a timeout "+getPath(pc)+"."+strLocks);
        pc.getThread().stop(new RequestTimeoutException(pc,"request ("+getPath(pc)+":"+pc.getId()+") is run into a timeout ("+(pc.getRequestTimeout()/1000)+" seconds) and has been stopped."+strLocks));
        
	}

	private static String getPath(PageContext pc) {
		try {
			String base=ResourceUtil.getResource(pc, pc.getBasePageSource()).getAbsolutePath();
			String current=ResourceUtil.getResource(pc, pc.getCurrentPageSource()).getAbsolutePath();
			if(base.equals(current)) return "path: "+base;
			return "path: "+base+" ("+current+")";
		}
		catch(Throwable t) {
			return "";
		}
	}
	
	/**
	 * @see javax.servlet.jsp.JspFactory#getEngineInfo()
	 */
	public JspEngineInfo getEngineInfo() {
		return info;
	}

	/**
	 * @return returns the query cache
	 */
	public QueryCache getQueryCache() {
		return queryCache;
	}

	/**
	 * @return returns count of pagecontext in use
	 */
	public int getUsedPageContextLength() { 
		int length=0;
		try{
		Iterator it = runningPcs.values().iterator();
		while(it.hasNext()){
			PageContextImpl pc=(PageContextImpl) it.next();
			if(!pc.isGatewayContext()) length++;
		}
		}
		catch(Throwable t){
			return length;
		}
	    return length;
	}
    /**
     * @return Returns the config.
     */
    public ConfigWeb getConfig() {
        return config;
    }
    public ConfigWebImpl getConfigWebImpl() {
        return config;
    }
    /**
     * @return Returns the scopeContext.
     */
    public ScopeContext getScopeContext() {
        return scopeContext;
    }

    /**
     * @return label of the factory
     */
    public Object getLabel() {
    	return ((ConfigWebImpl)getConfig()).getLabel();
    }
    /**
     * @param label
     */
    public void setLabel(String label) {
        // deprecated
    }

	/**
	 * @return the hostName
	 */
	public URL getURL() {
		return url;
	}
    

	public void setURL(URL url) {
		this.url=url;
	}

	/**
	 * @return the servlet
	 */
	public HttpServlet getServlet() {
		return servlet;
	}

	public void setConfig(ConfigWebImpl config) {
		this.config=config;
	}

	public Struct getRunningPageContextes() {
		return runningPcs;
	}

	public long getPageContextesSize() {
		return SizeOf.size(pcs);
	}
	
	// FUTURE add to interface
	public Array getInfo() {
		Array info=new ArrayImpl();
		
		synchronized (runningPcs) {
            //int len=runningPcs.size();
			Iterator it = runningPcs.keyIterator();
            PageContextImpl pc;
            Struct data,sctThread,scopes;
    		Collection.Key key;
            Thread thread;
    		while(it.hasNext()) {
            	data=new StructImpl();
            	sctThread=new StructImpl();
            	scopes=new StructImpl();
            	data.setEL("thread", sctThread);
                data.setEL("scopes", scopes);
                
            	
            	key=KeyImpl.toKey(it.next(),null);
                //print.out("key:"+key);
                pc=(PageContextImpl) runningPcs.get(key,null);
                if(pc==null || pc.isGatewayContext()) continue;
                thread=pc.getThread();
                if(thread==Thread.currentThread()) continue;
                
                
               
                
                data.setEL("startTime", new DateTimeImpl(pc.getStartTime(),false));
                data.setEL("endTime", new DateTimeImpl(pc.getStartTime()+pc.getRequestTimeout(),false));
                data.setEL("timeout", Caster.toDouble(pc.getRequestTimeout()));
                
                // thread
                sctThread.setEL(KeyImpl.NAME,thread.getName());
                sctThread.setEL("priority",Caster.toDouble(thread.getPriority()));
                data.setEL("TagContext",PageExceptionImpl.getTagContext(pc.getConfig(),thread.getStackTrace() ));

                data.setEL("urlToken", pc.getURLToken());
                data.setEL("debugger", pc.getDebugger().getDebuggingData());
                try {
					data.setEL("id", Hash.call(pc, pc.getId()+":"+pc.getStartTime()));
				} catch (PageException e1) {}
                data.setEL("requestid", pc.getId());

                // Scopes
                scopes.setEL(KeyImpl.NAME, pc.getApplicationContext().getName());
                try {
					scopes.setEL("application", pc.applicationScope());
				} catch (PageException e) {}

                try {
					scopes.setEL("session", pc.sessionScope());
				} catch (PageException e) {}
                
                try {
					scopes.setEL("client", pc.clientScope());
				} catch (PageException e) {}
                scopes.setEL("cookie", pc.cookieScope());
                scopes.setEL("variables", pc.variablesScope());
                if(!(pc.localScope() instanceof LocalNotSupportedScope)){
                	scopes.setEL("local", pc.localScope());
                	scopes.setEL("arguments", pc.argumentsScope());
                }
                scopes.setEL("cgi", pc.cgiScope());
                scopes.setEL("form", pc.formScope());
                scopes.setEL("url", pc.urlScope());
                scopes.setEL("request", pc.requestScope());
                
                info.appendEL(data);
                
                
            }
            return info;
        }
	}

	
	

	public void stopThread(String threadId, String stopType) {
		synchronized (runningPcs) {
            //int len=runningPcs.size();
			Iterator it = runningPcs.keyIterator();
            PageContext pc;
    		while(it.hasNext()) {
            	
            	pc=(PageContext) runningPcs.get(KeyImpl.toKey(it.next(),null),null);
                if(pc==null) continue;
                try {
					String id = Hash.call(pc, pc.getId()+":"+pc.getStartTime());
					if(id.equals(threadId)){
						stopType=stopType.trim();
						Throwable t;
						if("abort".equalsIgnoreCase(stopType) || "cfabort".equalsIgnoreCase(stopType))
							t=new Abort(Abort.SCOPE_REQUEST);
						else
							t=new RequestTimeoutException(pc,"request has been forced to stop.");
						
		                pc.getThread().stop(t);
		                SystemUtil.sleep(10);
						break;
					}
				} catch (PageException e1) {}
                
            }
        }
	}
}