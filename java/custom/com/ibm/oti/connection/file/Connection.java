package com.ibm.oti.connection.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.microedition.io.Connector;
import javax.microedition.io.file.ConnectionClosedException;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.oti.connection.CreateConnection;

import com.sun.midp.log.*;

public class Connection implements FileConnection, CreateConnection { 
	
	/* The url string that was sent to Connector.open(),
	 * without the preceeding "://" and host name. 
	 * file separator is "/".
	 */
	private String fullPath;
	
	/* String representation of the path to this file
	 * on the file system. Includes the host name if there is any.
	 * file separator is the platform file separator. "/", "\\", etc.
	 */ 
	private String platformPath;
	
	/* Byte array representation of platformPath. 
	 */
	private byte[] properPath;
	
	private String host;
	private String root;
	
	/* The directory containing this file or dir. 
	 * Might be null, if the connection is opened on a file system root.
	 */
	private String parentPath;
	
	/* The name of the file or directory, without the path info.
	 */
	private String name;	
	
	/** boolean value indicating if the connection is open.
	 */
	private boolean open = false;
	
	/** The access mode the connection was opened with.
	 *  possible values are : READ, WRITE or READ_WRITE
	 */
	private int accessMode;
	
	/** System dependant file separator character.
	 */
	public static final char separatorChar;
	
	/** System dependant file separator String. The initial value
	 * of this field is the System property "file.separator".
	 */
	public static final String separator;	

	private FCInputStream inputStream = null;
	private FCOutputStream outputStream = null;
	
	static {
		com.ibm.oti.connection.file.Util.init();
		separator = Util.getSeparator();
		separatorChar= separator.charAt(0);
	}

/**
 * Passes the parameters from the Connector.open() method to this
 * object. Protocol used by MIDP 2.0
 *
 * @author		IBM
 * @version		initial
 *
 * @param		spec String
 *					The address passed to Connector.open()
 * @param		access int
 *					The type of access this Connection is
 *					granted (READ, WRITE, READ_WRITE)
 * @param		timeout boolean
 *					A boolean indicating wether or not the
 *					caller to Connector.open() wants timeout
 *					exceptions or not
 * @exception	IOException
 *					If an error occured opening and configuring
 *					serial port.
 *
 * @see javax.microedition.io.Connector
 */
public javax.microedition.io.Connection setParameters2(String spec, int access, boolean timeout) throws IOException {
	setParameters(spec, access, timeout);
	return this;
}

	
/**
 * Passes the parameters from the Connector.open() method to this
 * object. 
 *
 * @author		IBM
 * @version		initial
 *
 * @param		spec String
 *					The address passed to Connector.open()
 * @param		access int
 *					The type of access this Connection is
 *					granted (READ, WRITE, READ_WRITE)
 * @param		timeout boolean
 *					A boolean indicating whether or not the
 *					caller to Connector.open() wants timeout
 *					exceptions
 * @exception	IOException
 *					If an error occured opening and configuring
 *					serial port.
 *
 * @see javax.microedition.io.Connector
 */
public void setParameters(String spec, int accessMode, boolean timeout) throws IOException {
	spec = decode(spec);
	fullPath = validateSpec(spec);
	setPaths(fullPath);

	if (name!="")
		if (!isValidFilenameImpl(name.getBytes()))
			throw new IllegalArgumentException("Invalid file name in FileConnection Url: " + spec);

	this.accessMode = accessMode;
	checkSecurity(platformPath);
	open = true;
}

private void setPaths(String path) {
	this.fullPath = path;
	this.platformPath = getPlatformPath(fullPath);
	this.properPath = platformPath.getBytes();
	parseDirectory();
}

private String getPlatformPath(String path) {
	String platformPath =path.replace('/', separatorChar);
	if (!host.equals(""))
		platformPath = "\\\\" + host + separatorChar + platformPath;
	return platformPath;
}

private void checkSecurity(String path) {
	// This code is commented out because in J2ME there isn't the
	// System::getSecurityManager function.
	/*SecurityManager security = System.getSecurityManager();
	if (security != null) {
		if (accessMode == Connector.READ_WRITE) {
			security.checkWrite(path);
			security.checkRead(path);
		} else if (accessMode == Connector.READ)
			security.checkRead(path);
		else
			security.checkWrite(path);
	}*/
}	

private void parseDirectory () {
	String temp = fullPath;

	if(temp.substring(temp.length()-1,temp.length() ).equals("/")) {
		temp = temp.substring(0, temp.length()-1);
	}	
	
	int idxforRoot = temp.indexOf("/");
	if (idxforRoot==-1) idxforRoot = temp.length();
	root= temp.substring(0,idxforRoot);

	if (root.equals("")) root = separator;
	
	if (idxforRoot==temp.length()) {
		name = "";
		if (root.equals(separator))
			parentPath = "/" ;
		else	
			parentPath = root + "/";
	} else {
		int idx=-1;
		while (temp.indexOf("/",idx+1)!=-1) {
			idx = temp.indexOf("/",idx +1);		
		}
		name = temp.substring(idx+1, temp.length());
		parentPath = temp.substring(0,idx+1);
	}
}


/**
 * Returns an indication of whether the file connection is currently
 * open or not.
 * 
 * @return	true if the file connection is open, false otherwise.
 */
public boolean isOpen() {
	return open;
}


public void close() throws IOException {
	open = false;
	inputStream = null;
	outputStream = null;
}

private void closeIOStreams() throws IOException {
	if (outputStream != null)
		outputStream.close();
		
	if (inputStream != null) 
		inputStream.close();
}
	
public InputStream openInputStream() throws IOException {
	if (!isOpen()) throw new IOException("Connection is closed");
	if (inputStream != null) throw new IOException("input stream already open");	
	if (!existsInternal()) throw new IOException("File does not exist.");
	if (isDirectoryInternal()) throw new IOException("Can not open InputStream on a directory.");

	checkRead();

	inputStream = new FCInputStream(properPath, this);
	return inputStream;
}

	public  DataInputStream openDataInputStream() throws IOException {
		return new DataInputStream(openInputStream());
	}
	
	public OutputStream openOutputStream() throws IOException {
		if (outputStream != null) throw new IOException("output stream already open");
		if (!isOpen()) throw new IOException("Connection is closed");
		if (!existsInternal()) throw new IOException("File does not exist.");
		if (isDirectoryInternal()) throw new IOException("Can not perform this operation on a directory.");

		checkWrite();
	
		outputStream = new FCOutputStream(properPath, this);
		return outputStream;
	}

	public  DataOutputStream openDataOutputStream() throws IOException {
		return new DataOutputStream(openOutputStream());
	}

	public  OutputStream openOutputStream(long offset) throws IOException{
		if (outputStream != null) throw new IOException("output stream already open");
		if (!isOpen()) throw new IOException("Connection is closed");
		if (!existsInternal()) throw new IOException("File does not exist.");	
		if (isDirectoryInternal()) throw new IOException("Can not perform this operation on a directory.");

		checkWrite();

		if (offset<0)
			throw new java.lang.IllegalArgumentException("offset can not be a negative value: " + offset);

		long fileSize = fileSizeImpl(properPath);
		if (offset>fileSize)
			offset= fileSize;

		outputStream = new FCOutputStream(properPath, offset, this);
		return outputStream;
	}

	/**
	 * This method is called from FCInputStream.close() to notify the connection that it is closed,
	 *  and that a new InputStream could be opened again.
	 */	
	protected synchronized void notifyInputStreamClosed() {
		inputStream = null;
	}

	/**
	 * This method is called from FCOutputStream.close() to notify the connection that it is closed,
	 *  and that a new OutputStream could be opened again.
	 */		
	protected synchronized void notifyOutputStreamClosed() {
		outputStream = null;
	}

	public  long totalSize() {
		if (!isOpen()) throw new ConnectionClosedException();
		// we need to check read access to the root, not the path
		checkRead(root);
		
		return totalSizeImpl(root.getBytes()); 	
	}

	private native long totalSizeImpl(byte[] root);
	
	public  long availableSize() {
		if (!isOpen()) throw new ConnectionClosedException();
		// we need to check read access to the root, not the path		
		checkRead(root);
		
		return availableSizeImpl(root.getBytes()); 	
	}


	
	private native long availableSizeImpl(byte[] root);

	public long usedSize() {
		if (!isOpen()) throw new ConnectionClosedException();
		// we need to check read access to the root, not the path
		checkRead(root);
		
		return usedSizeImpl(root.getBytes()); 
	}

	private native long usedSizeImpl(byte[] root);


	public long directorySize(boolean includeSubDirs) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();

		if (!existsInternal()) return -1;
		if (!isDirectoryInternal()) throw new IOException("Can not perform this operation on a file: " + getURL());

		return directorySizeImpl(properPath, includeSubDirs);
	}

	/*
	 * This method returns -1 if directory does not exist.
	 */	
	private native long directorySizeImpl(byte[] path, boolean includeSubDirs);

	public  long fileSize() throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		if (isDirectoryInternal()) throw new IOException("Can not perform this operation on a directory: " + getURL());

		return fileSizeImpl(properPath);
	}

	/*
	 * This method returns -1 if file does not exist.
	 */
	private native long fileSizeImpl(byte[] path);


	public boolean canRead() {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		return existsInternal() && !isWriteOnlyImpl(properPath);
	}

	public boolean canWrite() {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		return existsInternal() && !isReadOnlyImpl(properPath);
	}
	
	public boolean isHidden() {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		return existsInternal() && isHiddenImpl(properPath);
	}

	public boolean isDirectory() {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		return isDirectoryInternal();
	}
	
	private boolean isDirectoryInternal() {
		return isDirectoryImpl(properPath);
	}

	private boolean isDirectoryInternal1() {
		if (existsInternal()) {
			return isDirectoryInternal();
		} else {
			return fullPath.endsWith("/");
		}
	}
	
	private native boolean isDirectoryImpl(byte[] path);

	public void setReadable(boolean readable) throws IOException {
	if (!isOpen()) throw new ConnectionClosedException();
	checkWrite();
	if (!existsInternal()) throw new IOException("File does not exist: " + getURL());

	setWriteOnlyImpl(properPath, !readable);
	}

	public void setWritable(boolean writable) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();
		if (!existsInternal()) throw new IOException("File does not exist: " + getURL());
	
		setReadOnlyImpl(properPath, !writable);
	}

	public void setHidden(boolean hidden) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();
		if (!existsInternal()) throw new IOException("File does not exist: " + getURL());
		
		setHiddenImpl(properPath, hidden);
	}

	public Enumeration list() throws IOException {
		return listInternal(null, false);
	}
	
	public Enumeration list(String filter, boolean includeHidden) throws IOException {
		if (filter==null) throw new NullPointerException();
		
		//accept escaped filter string
		filter = decode(filter);	
		
		// replace the filter characters with a 'a' and see if it is a vaild filename
		String filterWithoutWildcards = filter.replace('*', 'a');
		if (!isValidFilenameImpl(filterWithoutWildcards.getBytes()))
			throw new IllegalArgumentException("filter contains an invalid character or path specification: " + filter);

 		return listInternal(filter.getBytes(), includeHidden);
	}

	private Enumeration listInternal(byte[] filter, boolean includeHidden) throws IOException {
		// make the necessary checks
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		if (!existsInternal()) throw new IOException("Directory does not exist: " + getURL());
		if (!isDirectoryInternal()) throw new IOException("Connection is open on a file: " + getURL());

		// find the list of files and directories matching the filter
		byte[][] implList = listImpl(properPath, filter, includeHidden );

		// create an enumaration that will contain the list of files and directories in String form
		int resultCount = implList==null ? 0 : implList.length;
		final String result[] = new String[resultCount];
		
		for (int index = 0; index < resultCount; index++)
			result[index] = new String(implList[index], 0, implList[index].length);
	
		return new Enumeration() {
			int pos = 0;
			public boolean hasMoreElements() {return pos < result.length;}
			public Object nextElement() {
				if (pos < result.length) return result[pos++];
				throw new NoSuchElementException();
			}
		};
	}

	/*
	 * @param path
	 * @param filter	The filter to use when looking for files.  It can contain valid filename characters
	 * 					and the wildcard character ('*').  If null is passed, then the default filter is
	 * 					used ("*")
	 * @param includeHidden
	 */
	private synchronized static native byte[][] listImpl(byte[] path, byte[] filter, boolean includeHidden);

	public void create() throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();

		if (fullPath.endsWith("/")) throw new IOException("Connection is open on a directory: " + getURL());

		int result = newFileImpl(properPath);
		switch (result) {
			case 0 : 
				return;
			case 1 :
				throw new IOException("Connection is open on an existing file: " + getURL());				
			case 3 :
				throw new IOException("Connection is open on a directory: "+ getURL());
			default:
				throw new IOException("Con not create: "+ getURL());
		}
	}

	private native int newFileImpl(byte[] path);

	public void mkdir() throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();
		
		int result = mkdirImpl(properPath);
		switch (result) {
			case 0 :
				return;
			case 1 :
				throw new IOException("Connection is open on an existing directory. " + getURL());
			case 3 :
				throw new IOException("Connection is open on a file. " + getURL() );
			default : 
				throw new IOException("Can not mkdir: " + getURL());
		}
	}

	private native int mkdirImpl(byte[] path);

	public boolean exists() {
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		return existsInternal();
	}
	
	private boolean existsInternal() {
		return existsImpl(properPath);
	}

	private native boolean existsImpl(byte[] path);

	public  void delete() throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();
		
		if (isDirectoryInternal()) {
			if (!deleteDirImpl(properPath))
				throw new IOException("Can not delete: " + getURL());
		} else {
			closeIOStreams();
			if (!deleteFileImpl(properPath))
				throw new IOException("Can not delete: " + getURL());
		}
	}

	private native boolean deleteDirImpl(byte[] path);

	private native boolean deleteFileImpl(byte[] path);

	public  void rename(String newName) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();
		if (!existsInternal()) throw new IOException("File does not exist: " + getURL());

		if (newName==null) throw new NullPointerException();
		
		//decode the file name before validation checks
		newName = decode(newName);	
		
		String newPath;
		if (name.equals("")) {
			newPath = newName;
		} else {			
			// check for invalid characters
			int idx = newName.indexOf('/');
			if (idx>-1 && (idx<newName.length()-1 || !isDirectoryInternal())) {
				// '/' is allowed as the last character, if the target is a directory
				throw new IllegalArgumentException("newName can not contain any path specification: " + newName);
			} 
			String strippedFileName = idx==-1 ? newName : newName.substring(0, idx);
			if (!isValidFilenameImpl(strippedFileName.getBytes())) {
				throw new IOException("newName contains an invalid character: " + newName);
			}	
			newPath = parentPath + newName;
		}
		
		String newPlatformPath = getPlatformPath(newPath);
		if (existsImpl(newPlatformPath.getBytes())) 
			throw new IOException("File already exists: " + newName);
		
		// check if the destination path is accessible.
		checkSecurity(newPlatformPath);

		closeIOStreams();

		renameImpl(properPath, newPlatformPath.getBytes());

		//since renameImpl has completed successfully, we can set the fields related to paths
		setPaths(newPath);
	}

	private native void renameImpl(byte[] pathExist, byte[] pathNew);

	public void truncate(long offset) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		checkWrite();

		if (!existsInternal()) throw new IOException("File does not exist: " + getURL());
		if (isDirectoryInternal()) throw new IOException("Can not perform truncate operation on a directory: " + getURL());

		try {
			if (outputStream!=null)
				outputStream.flush();
		} catch (IOException e) {} 
			
		if (offset >= fileSize())
			return;
		if (offset < 0)
			throw new IllegalArgumentException("Truncate offset can not be a negative value: " + offset);

		truncateImpl(properPath, offset);
	}

	private native void truncateImpl(byte[] b, long offset);
	
	/**
	 * Checks the read access for this file connection's path
	 */
	private void checkRead() {
		checkRead(platformPath);
	}

	/**
	 * Checks the read access for a given path 
	 */
	private void checkRead(String path) {
		/*SecurityManager security = System.getSecurityManager();
		if (security != null)
			security.checkRead(path);*/
		if (accessMode == Connector.WRITE)
			throw new IllegalModeException("Not open for read");
	}

	/**
	 * Checks the write access for this file connection's path
	 */	
	private void checkWrite() {
		/*SecurityManager security = System.getSecurityManager();
		if (security != null)
			security.checkWrite(platformPath);*/
		if (accessMode == Connector.READ)
			throw new IllegalModeException("Not open for write");
	}	

	public void setFileConnection(String fileName) throws IOException {
		if (!isOpen()) throw new ConnectionClosedException();
		if (!isDirectoryInternal()) throw new IOException("Connection is not open on a directory: " + getURL());
		if (fileName==null) throw new NullPointerException();
		if (fileName.equals(".")) return;
				
		//decode the file name before validation checks
		fileName = decode(fileName);
		
		// check for invalid characters
		int idx = fileName.indexOf('/');
		if (idx>-1 && idx<fileName.length()-1) {
			// '/' is allowed as the last character because this indicates a directory
			throw new IllegalArgumentException("fileName can not contain any path specification: " + fileName);
		} 
		String strippedFileName = idx==-1 ? fileName : fileName.substring(0, idx);
		if (!isValidFilenameImpl(strippedFileName.getBytes())) {
			throw new IOException("fileName contains an invalid character: " + fileName);
		}
	
		String newPath=null;
		if (fileName.equals("..")) {
			//optimization, if the connection was opened on a filesystem root, return.
			if (name.equals("")) return;			
	
			// set as parent directory
			newPath = parentPath;
		} else {
			for(Enumeration e=list(); e.hasMoreElements();) {
				if (e.nextElement().equals(fileName)) {
				// if the connection was opened on a file system root, avoid the addition of an extra slash.
					if (name.equals(""))
						newPath = parentPath + fileName;
					else
						newPath = parentPath + name + "/" + fileName;						
					break;
				}
			}
			
			// the file is not in the directory
			if (newPath==null)
				throw new IllegalArgumentException("File is not in the directory: " + fileName);
		}
		
		String newPlatformPath = getPlatformPath(newPath);
		checkSecurity(newPlatformPath);
		setPaths(newPath);
	}

	public String getName() {
		if (name.equals(""))
			return name;

		if (isDirectoryInternal1())
			return name + "/";
		else
			return name;
	}

	public String getPath() {
		return "/" + parentPath;	
	}

	public String getURL() {
		return "file://" + host + encode(getPath() + getName());
	}

	public  long lastModified() {		
		if (!isOpen()) throw new ConnectionClosedException();
		checkRead();
		
		long lastModified = lastModifiedImpl(properPath);

		// The FileConnection docs say Connection.lastModified() returns "a long
		// value representing the time the file was last modified, measured in
		// milliseconds since the epoch," and the code comment on lastModifiedImpl()
		// below says it "returns the last modification time in milliseconds."
		//
		// But this method was taking that value and multiplying it by 1000!  As if
		// it expected it to be in seconds (which it may well be on some native
		// implementations).  Which caused it to return microseconds rather than
		// milliseconds.  So I modified this line to stop multiplying the value.
		//
		return lastModified;
}

	/* returns the last modification time in milliseconds	
	 */
	private native long lastModifiedImpl(byte[] path);	

/**
 * 
 * All characters except for the following are escaped (converted into their 
 * hexadecimal value prepended by '%'):
 *     letters ('a'..'z', 'A'..'Z'),
 *     numbers ('0'..'9'),
 *     unreserved characters ('-', '_', '.', '!', '~', '*', '\'', '(', ')'), and
 *     reserved characters ('/', ':')
 * <p>
 * For example: '#' -> %23
 *
 * @author		IBM
 * @version		initial
 *
 * @return java.lang.String		the string to be converted
 * @param s java.lang.String	the converted string
 */
public static String encode(String s) {
	final String digits = "0123456789ABCDEF";
	StringBuffer buf = new StringBuffer(s.length());
	for (int i = 0; i < s.length(); i++) {
		char ch = s.charAt(i);
		if ((ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z') ||
			(ch >= '0' && ch <= '9') || "-_.!~*\'()//:".indexOf(ch) > -1)
				buf.append(ch);
		else {
			byte[] bytes = new String(new char[]{ch}).getBytes();
			for (int j=0; j<bytes.length; j++) {
				buf.append('%');
				buf.append(digits.charAt((bytes[j] & 0xf0) >> 4));
				buf.append(digits.charAt(bytes[j] & 0xf));
			}
		}
	}
	return buf.toString();
}

/**
 * <p>
 * '%' and two following hex digit characters are converted
 *     to the equivalent byte value.
 * All other characters are passed through unmodified.
 * <p>
 * e.g. "ABC %24%25" -> "ABC $%"
 *
 * @author		IBM
 * @version		initial
 *
 * @param		s java.lang.String
 *					The encoded string.
 * @return		java.lang.String
 *					The decoded version.
 */
public static String decode(String s) {
	StringBuffer result = new StringBuffer(s.length());
	ByteArrayOutputStream out = new ByteArrayOutputStream();
	for (int i = 0; i < s.length();) {
		char c = s.charAt(i);
		if (c == '%') {
			out.reset();
			do {
				if (i + 2 >= s.length())
					throw new IllegalArgumentException("Incomplete % sequence at: " + i);
				int d1 = Character.digit(s.charAt(i+1), 16);
				int d2 = Character.digit(s.charAt(i+2), 16);
				if (d1 == -1 || d2 == -1)
					throw new IllegalArgumentException("Invalid % sequence (" + s.substring(i, i+3)+ ") at: " + String.valueOf(i));
				out.write((byte)((d1 << 4) + d2));
				i += 3;
			} while (i < s.length() && s.charAt(i) == '%');
			result.append(out.toString());
			continue;
		} else result.append(c);
		i++;
	}
	return result.toString();
}

private native boolean isAbsoluteImpl(byte[] path);

private native boolean isHiddenImpl(byte[] path);

private native boolean isReadOnlyImpl(byte[] path);

private native boolean isWriteOnlyImpl(byte[] path);

private native void setReadOnlyImpl(byte[] path, boolean value);

private native void setWriteOnlyImpl(byte[] path, boolean value);

private native void setHiddenImpl(byte[] path, boolean value);

private native boolean isValidFilenameImpl(byte[] filename);

/* 
 * Sets fullPath and host
 * 
 */
private String validateSpec(String spec) {
	String fullPath;
	
	if (!spec.startsWith("//")) 
		throw new IllegalArgumentException("File connection url should start with 'file://': " + spec);
	
	spec = spec.substring(2);
	
	if (separatorChar!='/'){
		if (spec.indexOf(separatorChar) != -1) 
			throw new IllegalArgumentException("File Connection url can not include the file separator: " + spec);
	}
			
	int idx = spec.indexOf("/");
	if (idx==-1) 
		throw new IllegalArgumentException(spec);
		
	if (idx==0) {
		// no host is specified, since the URL started as "file:///"
		host="";		
		fullPath=spec.substring(0,spec.length()) ;
	} else {
		// There is a host specified
		// make sure it is a valid specification.
		
		int idx2 = spec.indexOf(":");
		int idx3 = spec.indexOf(" ");
		if ((idx2!=-1 && idx2<idx ) ||  (idx3!=-1 && idx3<idx ))
			throw new  IllegalArgumentException(spec);
	
		fullPath = spec.substring(idx,spec.length());
		host = spec.substring(0,idx);	
	}
	
	int idx4=spec.indexOf("/../");
	int idx5=spec.indexOf("/./");
	
	int idx6=spec.indexOf("../");
	int idx7=spec.indexOf("./");
	
	int idx8=spec.indexOf("/..");
	int idx9=spec.indexOf("/.");	
	
	if (idx4!=-1 || idx5!=-1 || idx6==0 || idx7==0 || (idx8!=-1 &&idx8==spec.length()-3) || (idx9!=-1 && idx9==spec.length()-2))
		throw new IllegalArgumentException("File Connection URL can not have relative path: " + spec);
		
	return fullPath;
}

}
