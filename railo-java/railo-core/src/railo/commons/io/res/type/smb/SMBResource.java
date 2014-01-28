package railo.commons.io.res.type.smb;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

import jcifs.smb.NtlmPasswordAuthentication;
import jcifs.smb.SmbException;
import jcifs.smb.SmbFile;
import jcifs.smb.SmbFileOutputStream;
import railo.commons.io.IOUtil;
import railo.commons.io.res.Resource;
import railo.commons.io.res.ResourceProvider;
import railo.commons.io.res.util.ResourceOutputStream;
import railo.commons.io.res.util.ResourceSupport;
import railo.commons.io.res.util.ResourceUtil;

public class SMBResource extends ResourceSupport implements Resource{

	private SMBResourceProvider provider;
	private String path;
	private NtlmPasswordAuthentication auth;
	private SmbFile _smbFile;
	private SmbFile _smbDir;
	
	
	private SMBResource(SMBResourceProvider provider) {
		this.provider = provider;
	}

	public SMBResource(SMBResourceProvider provider, String path) {
		this(provider);
		_init(_stripAuth(path), _extractAuth(path));
	}
	
	public SMBResource(SMBResourceProvider provider, String path, NtlmPasswordAuthentication auth) {
		this(provider);
		_init(path, auth);
	}
	
	public SMBResource(SMBResourceProvider provider, String parent, String child) {
		this(provider);
		_init(ResourceUtil.merge(_stripAuth(parent), child), _extractAuth(parent));
	}

	public SMBResource(SMBResourceProvider provider, String parent, String child, NtlmPasswordAuthentication auth) {
		this(provider);
		_init(ResourceUtil.merge(_stripAuth(parent), child), auth);
		
	}
	
	private void _init (String path, NtlmPasswordAuthentication auth ) {
		//String[] pathName=ResourceUtil.translatePathName(path);
		this.path = path;
		this.auth = auth;
		
	}
	
	
	private String _userInfo (String path) {
		
		try {
			//use http scheme just so we can parse the url and get the user info out
			String result = new URL("http://".concat(path)).getUserInfo();
			return SMBResourceProvider.unencryptUserInfo(result);
		}
		catch (MalformedURLException e) {
			return "";
		}
	}
	
	/*
	private static String _userInfo (NtlmPasswordAuthentication auth,boolean addAtSign) {
		String result = "";
		if( auth != null) {
			if( !StringUtils.isEmpty( auth.getDomain() ) ) {
				result += auth.getDomain() + ";";
			}
			if( !StringUtils.isEmpty( auth.getUsername() ) ) {
				result += auth.getUsername() + ":";
			}
			if( !StringUtils.isEmpty( auth.getPassword() ) ) {
				result += auth.getPassword();
			}
			if( addAtSign && !StringUtils.isEmpty( result ) ) {
				result += "@";
			}
		}
		return result;
	}*/
	
	private NtlmPasswordAuthentication _extractAuth(String path) {
		return new NtlmPasswordAuthentication( _userInfo(path) );
	}
	
	private String _stripAuth(String path) {
		return _calculatePath(path).replaceFirst(_scheme().concat("[^/]*@"),"");
	}
	
	private SmbFile _file() {
		return _file(false);
	}
	
	private SmbFile _file( boolean expectDirectory ) {
		String _path = _calculatePath(path);
		SmbFile result;
		if(expectDirectory) {
			if(!_path.endsWith("/")) _path += "/";
			if(_smbDir == null) {
				_smbDir = provider.getFile(_path,auth);
			}
			result = _smbDir;
		} else {
			if(_smbFile == null) {
				_smbFile = provider.getFile(_path,auth);
			}
			result = _smbFile;
		}
		return result;
	}
	
	private String _calculatePath(String path) {
		if ( !path.startsWith( _scheme() ) ) path = _scheme().concat( path );
		return path;
	}
	
	private String _scheme() {
		return provider.getScheme().concat("://");
		
	}
	
	@Override
	public boolean isReadable() {
		SmbFile file = _file();
		try {
			return file != null && file.canRead();
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public boolean isWriteable() {
		SmbFile file = _file();
		try {
			return file != null && file.canWrite();
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public void remove(boolean alsoRemoveChildren) throws IOException {
		if(alsoRemoveChildren)ResourceUtil.removeChildren(this);
		
		_delete();
	}

	private void _delete() throws IOException{
		provider.lock(this);
		try {
			SmbFile file = _file();
			if (file == null) throw new IOException("Can't delete [" + path + "], SMB path is invalid or inaccessable");
			if (file.isDirectory()) {
				file = _file(true);
			}
			file.delete();
		} finally {
			provider.unlock(this);
		}
	}

	@Override
	public boolean exists() {
		SmbFile file = _file();
		try {
			return file != null && file.exists();
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public String getName() {
		SmbFile file = _file();
		if(file == null)
			return "";
		return file.getName().replaceFirst("/$", ""); //remote trailing slash for directories
	}

	@Override
	public String getParent() {
		SmbFile file = _file();
		if(file == null)
			return "";
		return file.getParent();
		
	}

	@Override
	public Resource getParentResource() {
		String p = getParent();
		if(p==null) return null;
		return new SMBResource(provider,p,auth);
	}

	@Override
	public Resource getRealResource(String realpath) {
		realpath=ResourceUtil.merge(path +"/", realpath);
		
		if(realpath.startsWith("../"))return null;
		return new SMBResource(provider,realpath,auth);
	}

	@Override
	public String getPath() {
		return _calculatePath(path);
	}

	@Override
	public boolean isAbsolute() {
		return _file() != null;
	}

	@Override
	public boolean isDirectory() {
		SmbFile file = _file();
		try {
			return file != null && _file().isDirectory();
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public boolean isFile() {
		SmbFile file = _file();
		try {
			return file != null && file.isFile();
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public boolean isHidden() {
		return _isFlagSet(_file(), SmbFile.ATTR_HIDDEN);
	}

	@Override
	public boolean isArchive() {
		return _isFlagSet(_file(), SmbFile.ATTR_ARCHIVE);
	}

	@Override
	public boolean isSystem() {
		return _isFlagSet(_file(), SmbFile.ATTR_SYSTEM);
	}
	
	private boolean _isFlagSet(SmbFile file, int flag) {
		if (file == null) return false;
		try {
			return (file.getAttributes() & flag) == flag;
		}
		catch (SmbException e) {
			return false;
		}
	}

	@Override
	public long lastModified() {
		SmbFile file = _file();
		if (file == null) return 0;
		try {
			return file.lastModified();
		}
		catch (SmbException e) {
			return 0;
		}

	}

	@Override
	public long length() {
		SmbFile file = _file();
		if (file == null) return 0;
		try {
			return file.length();
		}
		catch (SmbException e) {
			return 0;
		}
	}

	@Override
	public Resource[] listResources() {
		if(isFile()) return null;
		try {
			SmbFile dir = _file(true);
			System.out.println("dir:" + dir.getPath());
			System.out.println("dir-isdir:" + dir.isDirectory());
			SmbFile[] files = dir.listFiles();
			System.out.println("dir-files:" + files.length);
			
			Resource[] result = new Resource[files.length];
			for(int i = 0; i < files.length ; i++) {
				SmbFile file = files[i];
				result[i] = new SMBResource(provider,file.getCanonicalPath(),auth);
			}
			return result;
		}
		catch (SmbException e) {
			e.printStackTrace();
			return new Resource[0];
		}
		
		
	}

	@Override
	public boolean setLastModified(long time){
		SmbFile file = _file();
		if (file == null) return false;
		try {
			provider.lock(this);
			file.setLastModified(time);
		}
		catch (SmbException e) {
			return false;
		}
		catch (IOException e) {
			return false;
		} finally {
			provider.unlock(this);
		}
		return true;
	}

	@Override
	public boolean setWritable(boolean writable) {
		SmbFile file = _file();
		if( file == null) return false;
		try {
			setAttribute((short)SmbFile.ATTR_READONLY, !writable);
		}
		catch (IOException e1) {
			return false;
		}
		return true;

	}

	@Override
	public boolean setReadable(boolean readable) {
		return setWritable(!readable);
	}

	@Override
	public void createFile(boolean createParentWhenNotExists) throws IOException {
		ResourceUtil.checkCreateFileOK(this, createParentWhenNotExists);
		//client.unregisterFTPFile(this);
		IOUtil.copy(new ByteArrayInputStream(new byte[0]), getOutputStream(), true, true);
	
	}

	@Override
	public void createDirectory(boolean createParentWhenNotExists) throws IOException {
		SmbFile file= _file(true);
		if (file == null) throw new IOException("SMBFile is inaccessible");
		ResourceUtil.checkCreateDirectoryOK(this, createParentWhenNotExists);
		try {
			provider.lock(this);
			file.mkdir();
		}
		finally {
			provider.unlock(this);
		}
		
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return _file().getInputStream();
	}

	@Override
	public OutputStream getOutputStream(boolean append) throws IOException {
		ResourceUtil.checkGetOutputStreamOK(this);
		try {
			provider.lock(this);
			SmbFile file =_file();
			OutputStream os = new SmbFileOutputStream(file, append);
			return IOUtil.toBufferedOutputStream(new ResourceOutputStream(this,os));
		}
		catch (IOException e) {
			provider.unlock(this);
			throw e;
		}
	}

	@Override
	public ResourceProvider getResourceProvider() {
		return provider;
	}

	@Override
	public int getMode() {
		return 0;
	}

	@Override
	public void setMode(int mode) throws IOException {
		// TODO
	}

	@Override
	public void setHidden(boolean value) throws IOException {
		setAttribute((short)SmbFile.ATTR_SYSTEM, value);
	}

	@Override
	public void setSystem(boolean value) throws IOException {
		setAttribute((short)SmbFile.ATTR_SYSTEM, value);
	}

	@Override
	public void setArchive(boolean value) throws IOException {
		setAttribute((short)SmbFile.ATTR_ARCHIVE, value);
	}

	@Override
	public void setAttribute(short attribute, boolean value) throws IOException {
		int newAttribute = _lookupAttribute(attribute);
		SmbFile file = _file();
		if (file == null) throw new IOException("SMB File is not valid");
		try {
			provider.lock(this);
			int atts = file.getAttributes();
			if (value) {
				atts = atts | newAttribute;
			} else {
				atts = atts & (~newAttribute);
			}
			file.setAttributes(atts);
		} finally {
			provider.unlock(this);
		}	
	}

	@Override
	public void moveTo(Resource dest) throws IOException {
		if(dest instanceof SMBResource) {
			SMBResource destination = (SMBResource)dest;
			SmbFile file = _file();
			file.renameTo(destination._file());
		} else {
			ResourceUtil.moveTo(this, dest,false);
		}
	}
	
	@Override
	public boolean getAttribute(short attribute) {
		try {
			int newAttribute = _lookupAttribute(attribute);
			return (_file().getAttributes() & newAttribute) != 0;
		}
		catch (SmbException e) {
			return false;
		}
		
	}
	
	private int _lookupAttribute(short attribute) {
		int result = attribute;
		switch (attribute) {
			case Resource.ATTRIBUTE_ARCHIVE:
				result = SmbFile.ATTR_ARCHIVE;
				break;
			case Resource.ATTRIBUTE_SYSTEM:
				result = SmbFile.ATTR_SYSTEM;
				break;
			case Resource.ATTRIBUTE_HIDDEN:
				result = SmbFile.ATTR_HIDDEN;
				break;
		}
		return result;
	}

}
