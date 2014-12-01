package javax.microedition.io.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

/**
 * This interface is intended to access files or directories that are located on
 * removeable media and/or file systems on a device.
 *
 * <P>
 * Device internal filesystems in memory may also be accessed through
 * this class as well, provided there is underlying hardware and OS support.
 * If file connections are not supported to a particular media or file system,
 * attempts to open a file connection to the media or file system through
 * <code>Connector.open()</code> results in an
 * <code>javax.microedition.io.IOException</code> being thrown.
 * </P>
 * <H2>Establishing a Connection</H2>
 * <P>The format of the input string used to access a FileConnection through 
 * <code>Connector.open()</code> must follow the format of a fully-qualified,
 * absolute path file name as described by the file 
 * URL format in IETF RFCs 1738 & 2396.  Further detail for the File 
 * URL format can be found in the javax.microedition.io.file package description. 
 * </P>
 * <p>A single connection object only references a single file or
 * directory at a time. In some cases, a connection object can be reused to
 * refer to a different file or directory than it was originally associated with
 * (see {@link #setFileConnection}).  In general, the best approach to reference
 * a different file or directory is by establishing a completely separate
 * connection through the <code>Connector.open()</code> method.</p>
 *
 * <H2>FileConnection Behavior</H2>
 *
 * <p>File connection is different from other Generic Connection Framework 
 * connections in that a connection object can be successfully returned from
 * the <code>Connector.open()</code> method without actually referencing an
 * existing entity (in this case, a file or directory). This behavior
 * allows the creation of new files and directories on a file system.
 * For example, the following code can be used to create
 * a new file on a file system, where <I>CFCard</I> is a valid existing file
 * system root name for a given implementation:</p>
 * <code>
 * <pre>
 * try {
 *     FileConnection fconn = (FileConnection)Connector.open(&quot;file:///CFCard/newfile.txt&quot;);
 *     // If no exception is thrown, then the URI is valid, but the file may or may not exist.
 *     if (!fconn.exists())
 *         fconn.create();  // create the file if it doesn't exist
 *
 *     fconn.close();
 * }
 * catch (IOException ioe) {
 * }</pre>
 * </code>
 * <p>
 * Developers should always check for the file's or directory's existence
 * after a connection is established to determine if the file or directory
 * actually exists. Similarly, files or directories can be deleted using the
 * {@link #delete} method, and developers should close the connection
 * immediately after deletion to prevent exceptions from accessing a connection
 * to a non-existent file or directory.
 * </p>
 * <P>
 * A file connection's open status is unaffected by the opening and closing of
 * input and output streams from the file connection; the file connection stays
 * open until <code>close()</code> is invoked on the FileConnection instance.
 * Input and output streams may be opened and closed multiple times on a
 * FileConnection instance.
 * </P>
 * <p>
 * All <code>FileConnection</code> instances have one underlying 
 * <code>InputStream</code> and one <code>OutputStream</code>. Opening a 
 * <code>DataInputStream</code> counts as opening an <code>InputStream,</code>
 * and opening a <code>DataOutputStream</code> counts as opening an 
 * <code>OutputStream</code>. A <code>FileConnection</code> instance can have 
 * only one <code>InputStream</code> and one <code>OutputStream</code> open at
 * any one time. Trying to open more than one <code>InputStream</code> or more
 * than one <code>OutputStream</code> from a <code>StreamConnection</code> 
 * causes an <code>IOException</code>. Trying to open an <code>InputStream</code>
 * or an <code>OutputStream</code> after the <code>FileConnection</code> has 
 * been closed causes an <code>IOException</code>. 
 * </p>
 * <p>
 * The inherited <code>StreamConnection</code> methods in a 
 * <code>FileConnection</code> instance are not synchronized. The only stream 
 * method that can be called safely from another thread is <code>close</code>. 
 * When <code>close</code> is invoked on a stream that is executing in another 
 * thread, any pending I/O method MUST throw an 
 * <code>InterruptedIOException</code>. In the above case, implementations 
 * SHOULD try to throw the exception in a timely manner. When all open streams 
 * have been closed, and when the <code>FileConnection</code> is closed, any
 * pending I/O operations MUST be interrupted in a timely manner. 
 * </p>
 * <p>
 * Data written to the output streams of these <code>FileConnection</code> 
 * objects is not guaranteed to be flushed to the stream's destination 
 * (and subsequently made available to any input streams) until either 
 * <code>flush()</code> or <code>close()</code> is invoked on the stream. 
 * </p>
 * <h2>Security</h2>
 * <P>
 * Access to file connections is restricted to prevent unauthorized manipulation
 * of data. The access security model applied to the file connection
 * is defined by the implementing profile. The security model is applied on
 * the invocation of the <code>Connector.open()</code> method with a valid file
 * connection string.  The mode provided in the <code>open()</code> method
 * (<code>Connector.READ_WRITE</code> by default) indicates the application's
 * request for access rights for the indicated file or directory and is
 * therefore checked against the security scheme.  All three connections modes
 * (<code>READ_WRITE, WRITE_ONLY,</code> and <code>READ_ONLY</code>) are
 * supported for a file connection and determine the access requested from the
 * security model.
 * </P>
 * <P>
 * The security model is also applied during use of the returned
 * FileConnection, specifically when the methods
 * <code>openInputStream()</code>, <code>openDataInputStream()</code>,
 * <code>openOutputStream()</code>, and <code>openDataOutputStream()</code> are
 * invoked. These methods have implied request for access rights (i.e.
 * input stream access is requesting read access, and output stream access is
 * requesting write access).  Should the application not be granted the
 * appropriate read or write access to the file or file system by the profile
 * authorization scheme, a <code>java.lang.SecurityException</code> is thrown.
 * </P>
 * <P>
 * File access through the File Connection API may be restricted to files that
 * are within a public context and not deemed private or sensitive. This
 * restriction is intended to protect the device's and other users' files and
 * data from both malicious and unintentional access. RMS databases cannot be
 * accessed using the File Connection API. Access to files and directories that
 * are private to another application, files and directories that are private to
 * a different user than the current user, system configuration files, and
 * device and OS specific files and directories may be restricted. In these
 * situations, a <code>java.lang.SecurityException</code> is thrown from the
 * <code>Connector.open()</code> method if the file, file system, or directory
 * is not allowed to be accessed.
 * </P>
 *
 * @see FileSystemRegistry
 * @since FileConnection 1.0
 */

public interface FileConnection extends javax.microedition.io.StreamConnection {

	/**
	 * Returns an indication of whether the file connection is currently
	 * open or not.
	 * 
	 * @return	true if the file connection is open, false otherwise.
	 */
	public boolean isOpen();

    /**
     * Open and return an input stream for a connection.  The connection's 
     * target must already exist and be accessible for the input stream to be created.
     *
     * @return  An open input stream
     * @throws  IOException if an I/O error occurs, if the method is invoked on
     *          a directory, if the connection's target does not
     *          yet exist, or the connection's target is not accessible.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @throws  SecurityException   If the application is not granted read
     *          access to the connection's target.
     */
    public InputStream openInputStream() throws IOException;

    /**
     * Open and return a data input stream for a connection. The connection's 
     * target must already exist and be accessible for the input stream to be created.
     *
     * @return  An open input stream
     * @throws  IOException  If an I/O error occurs, if the method is invoked on
     *          a directory, if the connection's target does not
     *          yet exist, or the connection's target is not accessible.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @throws  SecurityException   If the application is not granted read
     *          access to the connection's target.
     */
    public DataInputStream openDataInputStream() throws IOException;

    /**
     * Open and return an output stream for a connection. The output stream
     * is positioned at the start of the file. Writing data to the output stream
     * overwrites the contents of the files (i.e. does not insert data).
     * Writing data to output streams beyond the current end of file
     * automatically extends the file size.  The connection's target must
     * already exist and be accessible for the output stream to be created.
     * {@link #openOutputStream(long)} should be used to position an output
     * stream to a different position in the file.
     * <P>
     * Changes made to a file through an output stream may not be immediately
     * made to the actual file residing on the file system because
     * platform and implementation specific use of caching and buffering of the
     * data.  Steam contents and file length extensions are not necessarily
     * visible outside of the application immediately unless
     * <code>flush()</code> is called on the stream. The returned output stream
     * is automatically and synchronously flushed when it is closed.
     * </P>
     *
     * @return  An open output stream
     * @throws  IOException  If an I/O error occurs, if the method is invoked on
     *          a directory, the file does not yet exist, or the connection's 
     *			target is not accessible.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  SecurityException    If the application is not granted write
     *          access to the connection's target.
     * @see #openOutputStream(long)
     */
    public OutputStream openOutputStream() throws IOException;

    /**
     * Open and return a data output stream for a connection. The output stream
     * is positioned at the start of the file. Writing data to the output stream
     * overwrites the contents of the files (i.e. does not insert data).
     * Writing data to output streams beyond the current end of file
     * automatically extends the file size.  The connection's target must
     * already exist and be accessible for the output stream to be created.
     * {@link #openOutputStream(long)} should be used to position an output
     * stream to a different position in the file.
     * <P>
     * Changes made to a file through an output stream may not be immediately
     * made to the actual file residing on the file system because
     * platform and implementation specific use of caching and buffering of the
     * data. Steam contents and file length extensions are not necessarily
     * visible outside of the application immediately unless
     * <code>flush()</code> is called on the stream. The returned output stream
     * is automatically and synchronously flushed when it is closed.
     * </P>
     *
     * @return  An open output stream
     * @throws  IOException  If an I/O error occurs, if the method is invoked on
     *          a directory, the file does not yet exist, or the connection's 
     *			target is not accessible.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  SecurityException    If the application is not granted write
     *          access to the connection's target.
     * @see #openOutputStream(long)
     */
    public DataOutputStream openDataOutputStream() throws IOException;

    /**
     * This method opens an output stream and positions it at the indicated 
	 * byte offset in the file.  Data written to the returned output stream at 
	 * that position overwrites any existing data until EOF is reached, and
	 * then additional data is appended. The connection's target must
     * already exist and be accessible for the output stream to be created.
     * <P>
     * Changes made to a file through an output stream may not be immediately
     * made to the actual file residing on the file system because
     * platform and implementation specific use of caching and buffering of the
     * data. Steam contents and file length extensions are not necessarily
     * visible outside of the application immediately unless
     * <code>flush()</code> is called on the stream. The returned output 
	 * stream is automatically and synchronously flushed when it is closed.
     * </P>
     *
     * @param   byteOffset number of bytes to skip over from the beginning of
     *          the file when positioning the start of the OutputStream.  If 
	 *			the provided offset is larger than or equal to the current file
	 *			size, the OutputStream is positioned at the current end of the 
	 *			file for appending.
     * @return  an open OutputStream positioned at the byte offset in the file,
     *          or the end of the file if the offset is greater than the size 
	 *			of the file.
     * @throws  IOException  If an I/O error occurs, if the method is invoked
	 *			on a directory, the file does not yet exist, or the 
	 *			connection's target is not accessible.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  SecurityException if the security if the application does not
     *          allow write access to the file.
     * @throws  IllegalArgumentException if byteOffset has a negative value.
     */
    public OutputStream openOutputStream(long byteOffset) throws IOException;

    /**
     * Determines the total size of the file system the connection's target
     * resides on.
     *
     * @return  The total size of the file system in bytes, or -1 if the
     *			file system is not accessible. 
     * @throws  SecurityException if the security of the application does not
     *          have read access to the root volume.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long totalSize();


    /**
     * Determines the free memory that is available on the file system the file
     * or directory resides on. This may only be an estimate and may vary based
     * on platform-specific file system blocking and metadata information.
     *
     * @return  The available size in bytes on a file system, or -1 if the 
     *			file system is not accessible. 
     * @throws  SecurityException if the security of the application does not
     *          have read access to the root volume.
     * @throws  IllegalModeException if the application does have read access
     *          to the directory but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
 	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long availableSize();


    /**
     * Determines the used memory of a file system the connection's target
     * resides on.  This may only be an estimate and may vary based
     * on platform-specific file system blocking and metadata information.
     *
     * @return	The used size of bytes on a file system, or -1 if the file
     *			system is not accessible. 
     * @throws  SecurityException if the security of the application does not
     *          have read access to the root volume.
     * @throws  IllegalModeException if the application does have read access
     *          to the directory but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long usedSize();


    /**
     * Determines the size in bytes on a file system of all of the files
     * that are contained in a directory.
     *
     * @param includeSubdirs If set to true, the method determines
     *                       the size of the given directory and all subdirs
     *                       recursively. If false, the method returns the size
     *                       of the files in the directory only.
     * @return  The size in bytes occupied by the files included in the
     *          directory, or -1 if the directory does not exist or is not 
     *			accessible. 
     * @throws  IOException if the method is invoked on a file.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the directory.
     * @throws  IllegalModeException if the application does have read access
     *          to the directory but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long directorySize(boolean includeSubDirs) throws IOException;


    /**
     * Determines the size of a file on the file system. The size of a file 
     * always represents the number of bytes contained in the file; there is 
     * no pre-allocated but empty space in a file.
     * <P>
     * <code>fileSize()</code> always returns size of the file on the file system, 
     * and not in any pending output stream. <code>flush()</code> should be used 
     * before calling <code>fileSize()</code> to ensure the contents 
     * of the output streams opened to the file get written to the file system.
     *</P>
     * 
     * @return  The size in bytes of the selected file, or -1 if the 
     *			file does not exist or is not accessible.
     * @throws  IOException if the method is invoked on a directory.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the file.
     * @throws  IllegalModeException if the application does have read access
     *          to the file but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long fileSize() throws IOException;


    /**
     * Checks if the file or directory is readable.  This method checks the
     * attributes associated with a file or directory by the underlying file
     * system.  Some file systems may not support associating attributes with
     * a file, in which case this method returns true.
     *
     * @return  true if the connection's target exists, is accessible, and 
     *			is readable, otherwise false.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @throws	ConnectionClosedException if the connection is closed.
     * @see     #setReadable
     */
    public boolean canRead();


    /**
     * Checks if the file or directory is writable. This method checks the
     * attributes associated with a file or directory by the underlying file
     * system.  Some file systems may not support associating attributes with
     * a file, in which case this method returns true.
     *
     * @return  true if the  connection's target exists, is accessible, and 
     *			is writable, otherwise false.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @throws	ConnectionClosedException if the connection is closed.
     * @see     #setWritable
     */
    public boolean canWrite();


    /**
     * Checks if the file is hidden.  The exact definition of hidden is
     * system-dependent. For example, on UNIX systems a file is considered to be
     * hidden if its name begins with a period character ('.'). On Win32 and
     * FAT file systems, a file is considered to be hidden if it has been marked
     * as such in the file's attributes.  If hidden files are not supported on
     * the referenced file system, this method always returns false.
     *
     * @return  true if the file exists, is accessible, and is hidden, 
     *			otherwise false.
     * @throws	ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @see     #setHidden
     */
    public boolean isHidden();


    /**
     * Sets the file or directory readable attribute to the
     * indicated value.  The readable attribute for the file on the actual
     * file system is set immediately upon invocation of this method. If the
     * file system doesn't support a settable read attribute, this method is
     * ignored and <code>canRead()</code> always returns true.
     *
     * @param   readable The new state of the readable flag of the selected file.
     * @throws	IOException of the connection's target does not exist or is not
     *			accessible.
     * @throws  ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the connection's target.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @see     #canRead
     */
    public void setReadable(boolean readable) throws IOException;


    /**
     * Sets the selected file or directory writable attribute to the
     * indicated value.  The writable attribute for the file on the actual
     * file system is set immediately upon invocation of the method. If the
     * file system doesn't support a settable write attribute, this method is
     * ignored and <code>canWrite()</code> always returns true.
     *
     * @param   writable The new state of the writable flag of the selected file.
     * @throws	IOException if the connection's target does not exist or is not
     *			accessible.
     * @throws  ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the connection's target.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @see     #canWrite
     */
    public void setWritable(boolean writable)throws IOException;


    /**
     * Sets the hidden attribute of the selected file to
     * the value provided.  The attribute is applied to the file on the actual
     * file system immediately upon invocation of this method if the file system
     * and platform support it. If the file system doesn't support a hidden
     * attribute, this method is ignored and <code>isHidden()</code> always
     * returns false.  Since the exact definition of hidden is system-dependent,
     * this method only works on file systems that support a settable file
     * attribute. For example, on Win32 and FAT file systems, a file may be
     * considered hidden if it has been marked as such in the file's
     * attributes; therefore this method is applicable.  However on UNIX
     * systems a file may be considered to be hidden if its name begins with a
     * period character ('.'). In the UNIX case, this method may be ignored and
     * the method to make a file hidden may be the <code>rename()</code> method.
     *
     * @param   hidden The new state of the hidden flag of the selected file.
     * @throws	IOException if the connection's target does not exist or is not
     *			accessible.
     * @throws  ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the connection's target.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @see     #isHidden
     */
    public void setHidden(boolean hidden) throws IOException;


    /**
     * Gets a list of all files and directories contained in a directory.
     * The directory is the connection's target as specified in
     * <code>Connector.open()</code>.
     *
     * @return	An Enumeration of strings, denoting the files and
     *			directories in the directory.  The string returned contain only
     *			the file or directory name and does not contain any path prefix
     *			(to get a complete path for each file or directory, prepend
     *			{@link #getPath}). Directories are denoted with a
     *			trailing slash "/" in their returned name.  The Enumeration has
     *			zero length if the directory is empty. Any
     *			hidden files and directories in the directory are not included
     *			in the returned list.  Any current directory indication (".")
	 *		    and any parent directory indication ("..") is not included
	 *			in the list of files and directories returned.
     * @throws  IOException if invoked on a file, the directory does not exist,
     *			the directory is not accessible, or an I/O error occurs.
     * @throws	ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the directory.
     * @throws  IllegalModeException if the application does have read access
     *          to the directory but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     */
    public Enumeration list() throws IOException;

    /**
     * Gets a filtered list of files and directories contained in a directory.
     * The directory is the connection's target as specified in
     * <code>Connector.open()</code>.
     *
     * @param   filter String against which all files and directories are
     *          matched for retrieval.  An asterisk ("*") can be used as a
     *          wildcard to represent 0 or more occurrences of any character.
     * @param   includeHidden boolean indicating whether files marked as hidden
     *          should be included or not in the list of files and directories
     *          returned.
     * @return  An Enumeration of strings, denoting the files and directories
     *          in the directory matching the filter. Directories are denoted
     *          with a trailing slash "/" in their returned name.  The
     *          Enumeration has zero length if the directory is empty or no
     *			files and/or directories are found matching the given filter. 
     *			Any current directory indication (".") and any parent directory 
	 *			indication ("..") is not included in the list of files and 
	 *			directories returned.
     * @throws  IOException if invoked on a file, the directory does not exist,
     *			the directory is not accessible, or an I/O error occurs.
     * @throws	ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the directory.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     * @throws  NullPointerException if <code>filter</code> is
     *          <code>null</code>.
     * @throws  IllegalArgumentException if filter contains any path 
     *			specification or is an invalid filename for the platform 
     *			(e.g. contains characters invalid for a filename on the platform).
     */
    public Enumeration list(String filter, boolean includeHidden) throws IOException;

    /**
     * Creates a file corresponding to the file string
     * provided in the Connector.open() method for this FileConnection.  The
     * file is created immediately on the actual file system upon invocation of
     * this method.  Files are created with zero length and data can be put into
     * the file through output streams opened on the file. This method does not
	 * create any directories specified in the file's path.
     *
     * @throws  SecurityException if the security of the application does not
     *          have write access for the file.
     * @throws  IllegalModeException if the application does have write access
     *          to the file but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  IOException if invoked on an existing file or on any directory
     *          (<code>mkdir()</code> is used to create directories), the
     *			connection's target has a trailing "/" to denote a 
     *			directory, the target file system is not accessible, or an
     *			unspecified error occurs preventing creation of the file.
     * @throws	ConnectionClosedException if the connection is closed.
     */
    public void create() throws IOException;


    /**
     * Creates a directory corresponding to the directory
     * string provided in the Connector.open() method.
     * The directory is created immediately on the actual
     * file system upon invocation of this method.  Directories in the 
	 * specified path are not recursively created and must be explicitly 
	 * created before subdirectories can be created.
     *
     * @throws  SecurityException if the security of the application does not
     *          have write access to the directory.
     * @throws  IllegalModeException if the application does have write access
     *          to the directory but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  IOException if invoked on an existing directory or on any file
     *          (<code>create()</code> is used to create files), the target
     *			file sytem is not accessible, or an unspecified error occurs 
     *			preventing creation of the directory.
     * @throws	ConnectionClosedException if the connection is closed.
     */
    public void mkdir() throws IOException;


    /**
     * Checks if the file or directory specified in the URL passed to the
     * Connector.open() method exists.
     *
     * @return  true if the connnection's target exists and is accessible, 
     *			otherwise false.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public abstract boolean exists();

    /**
     * Checks if the URL passed to the Connector.open() is a directory.
     *
     * @return  True if the connection's target exists, is accessible, and 
     *			is a directory, otherwise false. 
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
	 * @throws	ConnectionClosedException if the connection is closed.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
     */
    public boolean isDirectory();

    /**
     * Deletes the file or directory specified in the
     * Connector.open() URL.  The file or directory is deleted immediately on
     * the actual file system upon invocation of this method.  All open input
     * and output streams are automatically flushed and closed.  Attempts to
     * further use those streams result in an <code>IOException</code>. The
     * FileConnection instance object remains open and available for use.
     *
     * @throws  ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the connection's target.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  IOException If the target is a directory and it is not empty,
     *			the connection target does not exist or is unaccessible, or
     *			an unspecified error occurs preventing deletion of the target.
     */
    public void delete() throws java.io.IOException;

    /**
     * Renames the selected file or directory to a new name in the same
     * directory.  The file or directory is renamed immediately on the actual
     * file system upon invocation of this method. No file or directory by the
     * original name exists after this method call.  All previously open
     * input and output streams are automatically flushed and closed.  Attempts
     * to further use those streams result in an <code>IOException</code>.  The
     * FileConnection instance object remains open and available for use,
     * referring now to the file or directory by its new name.
     *
     * @param   newName The new name of the file or directory.  The name must
     *          not contain any path specification; the file or directory
     *          remains in its same directory as before this method call.
     * @throws  IOException if the connection's target does not exist, the 
     *			connection's target is not accessible, a file or directory 
     *			already exists by the <code>newName</code>, or 
     *			<code>newName</code> is an invalid filename for the platform 
     *			(e.g. contains characters invalid in a filename on the platform).
     * @throws	ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the connection's target.
     * @throws  IllegalModeException if the application does have write access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  NullPointerException if <code>newName</code> is
     *          <code>null</code>.
     * @throws  IllegalArgumentException if <code>newName</code> contains any
     *          path specification.
     */
    public abstract void rename(String newName) throws IOException;

    /**
     * Truncates the file, discarding all data from the given byte offset to 
	 * the current end of the file.  If the byte offset provided is greater 
	 * than or equal to the file's current byte count, the method returns 
	 * without changing the file.  Any open streams are flushed automatically
	 * before the truncation occurs.
     *
     * @param   byteOffset the offset into the file from which truncation 
     *          occurs.
     * @throws  IOException if invoked on a directory or the file does not exist
     *			or is not accessible.
     * @throws	ConnectionClosedException if the connection is closed.
     * @throws  SecurityException if the security of the application does not
     *          have write access to the file.
     * @throws  IllegalModeException if the application does have write access
     *          to the file but has opened the connection in
     *          <code>Connector.READ</code> mode.
     * @throws  IllegalArgumentException if <code>byteOffset</code> is
     *          less than zero.
     */
    public abstract void truncate(long byteOffset) throws IOException;

    /**
     * Resets this FileConnection object to another file or directory.  This
     * allows reuse of the FileConnection object for directory traversal.  The
     * current FileConnection object must refer to a directory, and the new
     * file or directory must exist within this directory, or may be the 
	 * string ".." used to indicate the parent directory for the current
	 * connection).  The FileConnection instance object remains open and 
	 * available for use, referring now to the newly specified file or 
	 * directory.
     *
     * @param   fileName name of the file or directory to which this
     *          FileConnection is reset. The fileName must be one of the
     *          values returned from the {@link #list} method, or the string
	 *			".." to indicate the parent directory of the current
	 *			connection.  The fileName must not contain any additional path 
	 *			specification; i.e. the file or directory must reside within 
	 *			the current directory.
     * @throws  NullPointerException if <code>fileName</code> is
     *          <code>null</code>.
     * @throws  SecurityException if the security of the application does not
     *          have the security access to the specified file or directory
     *          as requested in the Connector.open method
     *          invocation that originally opened this FileConnection.
     * @throws  IllegalArgumentException if <code>fileName</code> contains any
     *          path specification or does not yet exist.
     * @throws  IOException if the current FileConnection is opened on a file,
     *			the connection's target is not accessible, or
     *          <code>fileName</code> is an invalid filename for the platform
     *          (e.g. contains characters invalid in a filename on the platform).
     * @throws	ConnectionClosedException if the connection is closed.
     */
    public abstract void setFileConnection(String fileName) throws IOException;

    /**
     * Returns the name of a file or directory excluding the URL schema and
     * all paths.  Directories are denoted with a
     * trailing slash "/" in their returned name.  The String resulting
     * from this method looks as follows:
     * <pre>
     *   &lt;directory&gt;/
     * </pre>
     * or
     * <pre>
     *   &lt;filename.extension&gt;
     * </pre>
     * or if no file extension
     * <pre>
     *   &lt;filename&gt;
     * </pre>
     *
     * @return  The name of a file or directory.
     */
    public String getName();

    /**
     * Returns the path excluding the file or directory name and the "file" URL
     * schema and host from where the file or directory specified in the
     * Connector.open() method is opened. {@link #getName} can be appended to
     * this value to get a fully qualified path filename.  The String resulting
     * from this method looks as follows:
     * <pre>
     *   /&lt;root&gt;/&lt;directory&gt;/
     * </pre>
     *
     * @return  The path of a file or directory in the format specified above.
     */
    public String getPath();


    /**
     * Returns the full file URL including
     * the scheme, host, and path from where the file or directory specified
     * in the Connector.open() method is opened.  The string returned is in
	 * an escaped ASCII format as defined by RFC 2396.
     * The resulting String looks as follows:
     * <pre>
     *   file://&lt;host&gt;/&lt;root&gt;/&lt;directory&gt;/&lt;filename.extension&gt;
     * </pre>
     * or
     * <pre>
     *   file://&lt;host&gt;/&lt;root&gt;/&lt;directory&gt;/&lt;directoryname&gt;/
     * </pre>
     *
     * @return  The URL of a file or directory in the format specified above.
     */
    public String getURL();


    /**
     * Returns the time that the file denoted by the URL specified
     * in the Connector.open() method was last modified.
     *
     * @return  A long value representing the time the file was last
     *          modified, measured in milliseconds since the epoch
     *          (00:00:00 GMT, January 1, 1970), or 0L if an I/O error occurs.
     *          If modification date is not supported by the underlying platform
     *          and/or file system, then 0L is also returned.
     *			If the connection's target does not exist or is not
     *			accessible, 0L is returned.
     * @throws  SecurityException if the security of the application does not
     *          have read access for the connection's target.
     * @throws  IllegalModeException if the application does have read access
     *          to the connection's target but has opened the connection in
     *          <code>Connector.WRITE</code> mode.
	 * @throws	ConnectionClosedException if the connection is closed.
     */
    public long lastModified();
}
