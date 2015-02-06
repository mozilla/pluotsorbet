/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */

package com.sun.cdc.io.j2me.file;

import com.sun.j2me.app.AppPackage;
import com.sun.j2me.security.FileConnectionPermission;
import com.sun.j2me.main.Configuration;
import com.sun.j2me.io.ConnectionBaseAdapter;
import com.sun.j2me.security.Token;

import javax.microedition.io.file.*;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.UnsupportedEncodingException;
import java.io.OutputStream;
import java.io.InputStream;
import java.util.Enumeration;
import javax.microedition.io.*;

import java.util.Vector;

/**
 * This class implements the necessary functionality
 * for a File connection.
 */
public class Protocol extends ConnectionBaseAdapter implements FileConnection {

    /** Security token for using FileConnection API from PIM */
    private Token classSecurityToken;

    /** Stores file connection mode */
    private int mode;

    /** File name string */
    private String fileName;

    /** File path string including root filesystem */
    private String filePath;

    /** Root filesystem for the file */
    private String fileRoot;

    /** File original URL */
    private String fileURL;

    /** Native path/name of the file */
    private String nativePathName;

    /** A peer to the native file */
    private BaseFileHandler fileHandler;

    /** Indicates if there is a need to try to load alternative file handler */
    private static boolean hasOtherFileHandler = true;

    /** Input stream associated with this connection */
    InputStream fis;

    /** Output stream associated with this connection */
    OutputStream fos;

    /** Separator for file path components */
    private static String sep;

    /** Static initialization of file separator */
    static {
        char[] t = new char[1];
        t[0]= DefaultFileHandler.getFileSeparator();
        sep = new String(t);
        if (sep == null) {
            throw new
                NullPointerException("Undefined \"file.separator\" property");
        }
    }

    /**
     * Constructor for file connection implementation.
     */
    public Protocol() {
        connectionOpen = false;
        fileHandler = null;
    }

    /**
     * Opens the file connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(String name, int mode, boolean timeouts)
            throws IOException {
        return openPrimImpl(name, mode, timeouts, true);
    }

    /**
     * Opens the file connection and receive security token.
     * @param token security token from PIM
     * @param name URL path fragment
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(Token token, String name)
            throws IOException {
        return openPrim(token, name, Connector.READ_WRITE);
    }

    /**
     * Opens the file connection and receive security token.
     * @param token security token from PIM
     * @param name URL path fragment
     * @param mode access mode
     * @return an opened Connection
     *  @throws IOException if some other kind of I/O error occurs.
     */
    public Connection openPrim(Token token, String name, int mode)
            throws IOException {
        classSecurityToken = token;
        return openPrim(name, mode, false);
    }

    // JAVADOC COMMENT ELIDED
    public boolean isOpen() {
        return connectionOpen;
    }

    // JAVADOC COMMENT ELIDED
    public InputStream openInputStream() throws IOException {

        inputStreamPermissionCheck();
        checkReadMode();

        try {
            ensureOpenAndConnected();
        } catch (ConnectionClosedException e) {
            throw new IOException(e.getMessage());
        }

        // IOException when target file doesn't exist
        if (!fileHandler.exists()) {
            throw new IOException("Target file doesn't exist");
        }

        if (!fileHandler.canRead()) { // no read access
            throw new SecurityException("No read access");
        }

        fileHandler.openForRead();

        fis = super.openInputStream();

        return fis;
    }

    // JAVADOC COMMENT ELIDED
    public OutputStream openOutputStream() throws IOException {
        return openOutputStream(0);
    }

    // JAVADOC COMMENT ELIDED
    public OutputStream openOutputStream(long byteOffset) throws IOException {
        if (byteOffset < 0) {
            throw new IllegalArgumentException("Offset has a negative value");
        }

        outputStreamPermissionCheck();
        checkWriteMode();

        try {
            ensureOpenAndConnected();
        } catch (ConnectionClosedException e) {
            throw new IOException(e.getMessage());
        }

        // IOException when target file doesn't exist
        if (!fileHandler.exists()) {
            throw new IOException("Target file doesn't exist");
        }

        if (!fileHandler.canWrite()) {
            // no write access
            throw new SecurityException("No write access");
        }

        fileHandler.openForWrite();
        fileHandler.positionForWrite(byteOffset);

        fos = super.openOutputStream();

        return fos;
    }

    // JAVADOC COMMENT ELIDED
    public long totalSize() {
        long size = -1;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            size = fileHandler.totalSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long availableSize() {
        long size = -1;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            size = fileHandler.availableSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long usedSize() {
        long size = -1;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            size = fileHandler.usedSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long directorySize(boolean includeSubDirs) throws IOException {
        long size = 0;

        // Permissions and ensureOpenAndConnected called by exists()
        if (exists()) {
            if (!isDirectory()) {
                throw new
                    IOException("directorySize is not invoked on directory");
            }
        } else {
            return -1L;
        }

        try {
            size = fileHandler.directorySize(includeSubDirs);
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public long fileSize() throws IOException {
        long size = -1;

        checkReadMode();

        if (isDirectory()) {
            throw new IOException("fileSize invoked on a directory");
        }

        try {
            ensureOpenAndConnected();

            size = fileHandler.fileSize();
        } catch (IOException e) {
            size = -1;
        }

        return size;
    }

    // JAVADOC COMMENT ELIDED
    public boolean canRead() {
        boolean res = false;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res = fileHandler.canRead();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean canWrite() {
        boolean res = false;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res = fileHandler.canWrite();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isHidden() {
        boolean res = false;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res = fileHandler.isHidden();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public void setReadable(boolean readable) throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        fileHandler.setReadable(readable);
    }

    // JAVADOC COMMENT ELIDED
    public void setWritable(boolean writable) throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        fileHandler.setWritable(writable);
    }

    // JAVADOC COMMENT ELIDED
    public void setHidden(boolean hidden) throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        fileHandler.setHidden(hidden);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration list() throws IOException {
        return listInternal(null, false);
    }

    // JAVADOC COMMENT ELIDED
    public Enumeration list(String filter, boolean includeHidden)
        throws IOException {

        if (filter == null) {
            throw new NullPointerException("List filter is null");
        }

        return listInternal(EscapedUtil.getUnescapedString(filter),
            includeHidden);
    }

    // JAVADOC COMMENT ELIDED
    public void create() throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        if (fileName.charAt(fileName.length() - 1) == '/') {
            throw new IOException("Can not create directory");
        }

        fileHandler.create();
    }

    // JAVADOC COMMENT ELIDED
    public void mkdir() throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        fileHandler.mkdir();
    }

    // JAVADOC COMMENT ELIDED
    public boolean exists() {
        boolean res = false;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res = fileHandler.exists();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public boolean isDirectory() {
        boolean res = false;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res = fileHandler.isDirectory();
        } catch (IOException e) {
            res = false;
        }

        return res;
    }

    // JAVADOC COMMENT ELIDED
    public void delete() throws java.io.IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        try {
            if (fis != null) {
                fis.close();
                fis = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            fileHandler.closeForReadWrite();
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.delete();
    }


    // JAVADOC COMMENT ELIDED
    public void rename(String newName) throws IOException {
        checkWriteMode();

        newName = EscapedUtil.getUnescapedString(newName);
        // Following line will throw NullPointerException if newName is null
        int dirindex = newName.indexOf('/');
        if (dirindex != -1 && dirindex != (newName.length() - 1)) {
            throw new
              IllegalArgumentException("New name contains path specification");
        }

        if (!"/".equals(sep) && newName.indexOf(sep) != -1) {
            throw new
              IllegalArgumentException("New name contains path specification");
        }

        ensureOpenAndConnected();
        checkIllegalChars(newName);

        try {
            if (fis != null) {
                fis.close();
                fis = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            if (fos != null) {
                fos.close();
                fos = null;
            }
        } catch (IOException e) {
            // Ignore silently
        }

        try {
            fileHandler.closeForReadWrite();
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.rename(filePath + newName);

        fileName = newName;
        fileURL = "file://" + filePath + fileName;
    }

    // JAVADOC COMMENT ELIDED
    public void truncate(long byteOffset) throws IOException {
        checkWriteMode();

        ensureOpenAndConnected();

        if (byteOffset < 0) {
            throw new IllegalArgumentException("offset is negative");
        }

        try {
            if (fos != null) {
                fos.flush();
            }
        } catch (IOException e) {
            // Ignore silently
        }

        fileHandler.truncate(byteOffset);
    }

    // JAVADOC COMMENT ELIDED
    public void setFileConnection(String fileName) throws IOException {
        ensureOpenAndConnected();

        // Note: permissions are checked by openPrim method

        // Following line will throw NullPointerException if fileName is null
        int dirindex = fileName.indexOf('/');
        if (dirindex != -1 && dirindex != (fileName.length() - 1)) {
            throw new IllegalArgumentException(
                "Contains any path specification");
        }

        if (fileName.equals("..") && this.fileName.length() == 0) {
            throw new IOException(
                "Cannot set FileConnection to '..' from a file system root");
        }

        if (!"/".equals(sep) && fileName.indexOf(sep) != -1) {
            throw new
            IllegalArgumentException("Contains any path specification");
        }

        checkIllegalChars(fileName);

        // According to the spec, the current FileConnection object must refer
        // to a directory.
        // Check this right here in order to avoid IllegalModeException instead
        // of IOException.
        if (!fileHandler.isDirectory()) {
            throw new IOException("Not a directory");
        }

        String origPath = filePath, origName = this.fileName;

        String tmp_sep;
        // Note: security checks are performed before any object state changes
        if (fileName.equals("..")) {
            // go one directory up
            openPrim("//" + filePath, mode, false);
        } else {
            int fileNameLen = this.fileName.length();
            if (fileNameLen == 0 || this.fileName.charAt(fileNameLen - 1) == '/') {
                tmp_sep = "";
            } else {
                tmp_sep = "/";
            }
            // go deeper in directory structure
            openPrimImpl("//" + filePath 
                     + this.fileName + tmp_sep + fileName,
                     mode, false, false);
        }

        // Old file connection must be a directory. It can not have open
        // streams so no need to close it. Just reset it to null
        fileHandler = null;

        // Reconnect to the new target
        ensureOpenAndConnected();

        // At this point we are already refer to the new file
        if (!fileHandler.exists()) {
            // Revert to an old file
            openPrim("//" + origPath + origName, mode, false);
            fileHandler = null;

            throw new IllegalArgumentException("New target does not exists");
        }
    }

    /**
     * Spec is not consistent: sometimes it requires IOException
     * and sometimes IllegalArgumentException in case of illegal chars
     * in the filename
     * @param name URL path fragment
     * @throws IOException if name contains unsupported characters
     */
    private void checkIllegalChars(String name) throws IOException {

        String illegalChars = fileHandler.illegalFileNameChars();
        for (int i = 0; i < illegalChars.length(); i++) {
            if (name.indexOf(illegalChars.charAt(i)) != -1) {
                throw new
                    IOException("Contains characters invalid for a filename");
            }
        }
    }

    // JAVADOC COMMENT ELIDED
    public String getName() {
        String name = fileName;

        try {
            if (exists()) {
                int lastPos = name.length() - 1;
                if (isDirectory()) {
                    if (!name.equals("") && name.charAt(lastPos) != '/') 
                        name += '/';
                } else {
                    if (name.charAt(lastPos) == '/')
                        name = name.substring(0, lastPos);
                }
            }
        } catch (SecurityException e) {
            // According to spec should silently ignore any exceptions
        } catch (IllegalModeException e) {
            // According to spec should silently ignore any exceptions
        } catch (ConnectionClosedException e) {
            // According to spec should silently ignore any exceptions
        }

        return name;
    }

    // JAVADOC COMMENT ELIDED
    public String getPath() {
        return filePath;
    }

    // JAVADOC COMMENT ELIDED
    public String getURL() {
        String url = EscapedUtil.getEscapedString(fileURL);

        try {
            if (exists()) {
                int lastPos = url.length() - 1;
                if (isDirectory()) {
                    if (url.charAt(lastPos) != '/') {
                        url += '/';
                    }
                } else {
                    if (url.charAt(lastPos) == '/') {
                        url = url.substring(0, lastPos);
                    }
                }
            }
        } catch (SecurityException e) {
            // According to spec should silently ignore any exceptions
        } catch (IllegalModeException e) {
            // According to spec should silently ignore any exceptions
        } catch (ConnectionClosedException e) {
            // According to spec should silently ignore any exceptions
        }

        return url;
    }

    // JAVADOC COMMENT ELIDED
    public long lastModified() {
        long res = 0;

        try {
            checkReadMode();

            ensureOpenAndConnected();

            res =  fileHandler.lastModified();
        } catch (IOException e) {
            res = 0;
        }

        return res;
    }

    /**
     * Throws <code>SecurityException</code> if permission check fails.
     *
     * @param name     permission name.
     * @param fileURL  URL of the file that is going to be accessed.
     */
    protected void checkPermission(String name, String fileURL)
        throws InterruptedIOException {
        try {
            AppPackage.getInstance().
                checkForPermission(new FileConnectionPermission(name, fileURL));
        } catch (InterruptedException ie) {
            throw new InterruptedIOException(
                "Interrupted while trying to ask the user permission");
        }
    }

    /**
     * Throws <code>SecurityException</code> if <code>InputStream</code>
     * opening permission check fails.
     */
    protected void inputStreamPermissionCheck() throws InterruptedIOException {
        checkReadPermission();
    }

    /**
     * Throws <code>SecurityException</code> if <code>OutputStream</code>
     * opening permission check fails.
     */
    protected void outputStreamPermissionCheck() throws InterruptedIOException {
        checkWritePermission();
    }

    // JAVADOC COMMENT ELIDED
    // See javax.microedition.media.protocol.SourceStream.read() in JSR-135
    protected int readBytes(byte b[], int off, int len)
        throws IOException {

        checkReadMode();

        ensureConnected();

        int readBytes = fileHandler.read(b, off, len);
        // return '-1' instead of '0' as stream specification requires
        // in case the end of the stream has been reached
        return (readBytes > 0) ? readBytes : -1;
    }

    /**
     * Writes <code>len</code> bytes from the specified byte array
     * starting at offset <code>off</code> to this output stream.
     * <p>
     * Polling the native code is done here to allow for simple
     * asynchronous native code to be written. Not all implementations
     * work this way (they block in the native code) but the same
     * Java code works for both.
     *
     * @param      b     the data.
     * @param      off   the start offset in the data.
     * @param      len   the number of bytes to write.
     * @return     number of bytes written
     * @exception  IOException  if an I/O error occurs. In particular,
     *             an <code>IOException</code> is thrown if the output
     *             stream is closed.
     */
    protected int writeBytes(byte b[], int off, int len)
        throws IOException {
        checkWriteMode();

        ensureConnected();

        return fileHandler.write(b, off, len);
    }

    // JAVADOC COMMENT ELIDED
    // See java.io.OutputStream.flush() in CLDC 1.1
    protected void flush() throws IOException {
        checkWriteMode();

        ensureConnected();

        fileHandler.flush();
    }

    /**
     * Called once by each child input stream.
     * If the input stream is marked open, it will be marked closed and
     * the if the connection and output stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeInputStream() throws IOException {
        maxIStreams++;
        fileHandler.closeForRead();
        super.closeInputStream();
    }

    /**
     * Called once by each child output stream.
     * If the output stream is marked open, it will be marked closed and
     * the if the connection and input stream are closed the disconnect
     * method will be called.
     *
     * @exception IOException if the subclass throws one
     */
    protected void closeOutputStream() throws IOException {
        maxOStreams++;
        flush();
        fileHandler.closeForWrite();
        super.closeOutputStream();
    }

    /**
     * Free up the connection resources.
     *
     * @exception  IOException  if an I/O error occurs.
     */
    protected void disconnect() throws IOException {
        try {
            if (fileHandler != null) {
                fileHandler.close();
            }
        } finally {
            fileHandler = null;
        }
    }

    // In order to compile against MIDP's ConnectionBaseAdapter
    /**
     * Establishes the connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @throws IOException if an error occurs
     */
    protected void connect(String name, int mode, boolean timeouts)
        throws IOException {}

    /**
     * Checks that the connection is already open.
     * @throws IOException if the connection is closed
     */
    protected void ensureConnected() throws IOException {
        if (!isRoot(fileRoot)) {
            throw new IOException("Root is not accessible");
        }

        connect(fileRoot, filePath + fileName);
    }

    /**
     * Connects to the native file handler and sets <code>nativePathName</code>
     * to the real name in the file system.
     *
     * @param root virtual root of the file.
     * @param pathName path and name of the file at the given root.
     */
    private void connect(String root, String pathName) throws IOException {
        if (fileHandler == null) {
            fileHandler = getFileHandler();

            nativePathName = fileHandler.connect(root, pathName);

            fileHandler.ensurePrivateDirExists(root);
        }
    }

    /**
     * Opens the file connection.
     * @param name URL path fragment
     * @param mode access mode
     * @param timeouts flag to indicate that timeouts allowed
     * @param unescape flag to indicate whether URL must be unescaped
     * @return an opened Connection
     * @throws IOException if some other kind of I/O error occurs.
     */
    private Connection openPrimImpl(String name, int mode, boolean timeouts, boolean unescape)
            throws IOException {

        // Accepting URLs without double slash after scheme (violates RFC 1738).
        // See CR 6588553.
        int rootStart = name.startsWith("//") ?
            name.indexOf('/', 2) : 0;

        if (rootStart == -1 || name.charAt(0) != '/' ) {
            throw new IllegalArgumentException("Malformed File URL");
        }

        /* The string must be a valid URL path separated by "/" */
		
        if (name.indexOf("/../", rootStart) != -1 ||
            name.indexOf("/./", rootStart) != -1 ||
            name.endsWith("/..") ||
            name.endsWith("/.") ||
            !"/".equals(sep) && name.indexOf(sep, rootStart) != -1 ||
            name.indexOf('\\') != -1) {
                throw new
                    IllegalArgumentException("/. or /.. is not supported "
                    + "or other illegal characters found");
        }

        if (unescape) {
            name = EscapedUtil.getUnescapedString(name);
        }
        String fileURL = "file:" + name;

        String fileName;
        String fileRoot;
        String filePath;
        int nameLength = name.length();
        int pathStart = name.indexOf('/', rootStart + 1);

        if (pathStart == -1) {
            throw new IllegalArgumentException("Root is not specified");
        }

        if (pathStart == (nameLength - 1)) {
            fileName = "";
            fileRoot = name.substring(rootStart + 1);
            filePath = name.substring(rootStart);
        } else {
            fileRoot = name.substring(rootStart + 1, pathStart + 1);

            int fileStart = name.lastIndexOf('/', nameLength - 2);

            if (fileStart <= pathStart) {
                fileName = name.substring(pathStart + 1);
                filePath = name.substring(rootStart, pathStart + 1);
            } else {
                filePath = name.substring(rootStart, fileStart + 1);
                fileName = name.substring(fileStart + 1);
            }
        }

        connect(fileRoot, filePath + fileName);

        checkIllegalChars(fileName);

        // Perform security checks before any object state changes since
        // this method is used not only by Connector.open() but
        // by FileConnection.setFileConnection() too.
        switch (mode) {
        case Connector.READ:
            checkReadPermission(nativePathName, mode);
            checkReadMode();
            maxOStreams = 0;
            break;
        case Connector.WRITE:
            checkWritePermission(nativePathName, mode);
            checkWriteMode();
            maxIStreams = 0;
            break;
        case Connector.READ_WRITE:
            checkReadPermission(nativePathName, mode);
            checkReadMode();
            checkWritePermission(nativePathName, mode);
            checkWriteMode();
            break;
        default:
            throw new IllegalArgumentException("Invalid mode");
        }

        this.fileURL = fileURL;
        this.mode = mode;
        this.fileRoot = fileRoot;
        this.filePath = filePath;
        this.fileName = fileName;

        connectionOpen = true;
        return this;
    }

    /**
     * Checks if path is a root path.
     * @param root path to be checked
     * @return <code>true</code> if path is a root,
     *                <code>false</code> otherwise.
     */
    private boolean isRoot(String root) {
        Vector r = listRoots(); // retrieve up-to-date list of mounted roots
        for (int i = 0; i < r.size(); i++) {
            String name = (String)r.elementAt(i);
            if (name.equals(root)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks that the connection is already open and connected.
     * @throws ConnectionClosedException if the connection is closed
     * @throws IOException if any error occurs while connecting
     */
    protected void ensureOpenAndConnected() throws IOException {
        if (!isOpen()) {
            throw new ConnectionClosedException("Connection is closed");
        }

        ensureConnected();
    }

    /**
     * Checks that the connection mode allows reading.
     *
     * @throws IllegalModeException if connection is write only
     */
    protected final void checkReadMode() {
        if (mode == Connector.WRITE) {
            throw new IllegalModeException("Connection is write only");
        }
    }

    /**
     * Checks that the connection mode allows writing.
     *
     * @throws IllegalModeException if connection is read only
     */
    protected final void checkWriteMode() {
        if (mode == Connector.READ) {
            throw new IllegalModeException("Connection is read only");
        }
    }

    /**
     * Checks that the application has permission to read.
     * @param fileURL complete file URL
     * @param mode access mode
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if read is not allowed
     * @throws IllegalModeException if connection is write only
     */
    private final void checkReadPermission(String filePath, int mode)
            throws InterruptedIOException {

        if (classSecurityToken == null) { // FC permission
            checkPermission(FileConnectionPermission.READ.getName(),
                filePath);
        } else { // call from PIM
            classSecurityToken.checkIfPermissionAllowed(
                FileConnectionPermission.READ);
        }
    }

    /**
     * Checks that the application has permission to read.
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if read is not allowed
     * @throws IllegalModeException if connection is write only
     */
    protected final void checkReadPermission() throws InterruptedIOException {
        checkReadPermission(nativePathName, mode);
    }

    /**
     * Checks that the application has permission to write.
     * @param fileURL complete file URL
     * @param mode access mode
     * @throws InterruptedIOException if the permission dialog is
     * terminated before completed
     * @throws SecurityException if write is not allowed
     * @throws IllegalModeException if connection is read only
     */
    private final void checkWritePermission(String filePath, int mode)
            throws InterruptedIOException {

        if (classSecurityToken == null) { // FC permission
            checkPermission(FileConnectionPermission.WRITE.getName(),
                filePath);
        } else { // call from PIM
            classSecurityToken.checkIfPermissionAllowed(
                FileConnectionPermission.WRITE);
        }
    }

    /**
     * Checks that the application has permission to write.
     * @throws InterruptedIOException if the permission dialog is
     *                                terminated before completed
     * @throws SecurityException if write is not allowed
     * @throws IllegalModeException if connection is read only
     */
    protected final void checkWritePermission() throws InterruptedIOException {
        checkWritePermission(nativePathName, mode);
    }


    /**
     * Gets an array of file system roots.
     * @return up-to-date array of file system roots;
     *         empty array is returned if there are no roots available.
     */
    public static Vector listRoots() {
        BaseFileHandler fh = getFileHandler();
        return fh.listRoots();
    }

    // JAVADOC COMMENT ELIDED - see FileConnection.list() description
    private Enumeration listInternal(String filter, boolean includeHidden)
        throws IOException {
        checkReadMode();

        ensureOpenAndConnected();

        if (filter != null) {
            if (filter.indexOf('/') != -1) {
                throw new IllegalArgumentException(
                    "Filter contains any path specification");
            }

            String illegalChars = fileHandler.illegalFileNameChars();
            for (int i = 0; i < illegalChars.length(); i++) {
                if (filter.indexOf(illegalChars.charAt(i)) != -1) {
                    throw new
                        IllegalArgumentException("Filter contains characters "
                            + "invalid for a filename");
                }
            }
        }

        return fileHandler.list(filter, includeHidden).elements();
    }

    /**
     * Gets the file handler.
     * @return handle to current file connection
     */
    private static BaseFileHandler getFileHandler() {
        String def = "com.sun.cdc.io.j2me.file.DefaultFileHandler";
        String n = null;
        if (hasOtherFileHandler) {
            n = Configuration.getProperty(
                               "com.sun.io.j2me.fileHandlerImpl");
            if (n == null) {
                hasOtherFileHandler = false;
            }
        }
        if (hasOtherFileHandler) {
            try {
                return (BaseFileHandler) (Class.forName(n)).newInstance();
            } catch (ClassNotFoundException e) {
                hasOtherFileHandler = false;
            } catch (Error e) {
                hasOtherFileHandler = false;
            } catch (IllegalAccessException e) {
                hasOtherFileHandler = false;
            } catch (InstantiationException e) {
                hasOtherFileHandler = false;
            }
        }
        try {
            return (BaseFileHandler) (Class.forName(def)).newInstance();
        } catch (ClassNotFoundException e) {
            throw new Error("Unable to create FileConnection Handler: " + e);
        } catch (Error e) {
            throw new Error("Unable to create FileConnection Handler: " + e);
        } catch (IllegalAccessException e) {
            throw new Error("Unable to create FileConnection Handler: " + e);
        } catch (InstantiationException e) {
            throw new Error("Unable to create FileConnection Handler: " + e);
        }
    }

    native public int available() throws IOException;
}
/**
 * Utility for escaped character handling.
 */
class EscapedUtil {
    /**
     * Gets the escaped string.
     * @param name string to be processed
     * @return escaped string
     * @throws IllegalArgumentException if encoding not supported
     */
    public static String getEscapedString(String name) {
        try {
            if (name == null) {
                return null;
            }
            byte newName[] = new byte[name.length()*12];
            int nextPlace = 0;
            for (int i = 0; i < name.length(); i++) {
                char c = name.charAt(i);
                if (containsReserved(c)) {
                    char data[] = {c};
                    byte[] reservedBytes = new String(data).getBytes("utf-8");
                    for (int j = 0; j < reservedBytes.length; j++) {
                        newName[nextPlace++] = '%';
                        byte upper = (byte) ((reservedBytes[j] >> 4) & 0xF);
                        if (upper <= 9) {
                            newName[nextPlace++] = (byte) ('0' + upper);
                        } else {
                            newName[nextPlace++] = (byte) ('A' + (upper - 10));
                        }
                        byte lower = (byte) (reservedBytes[j] & 0xF);
                        if (lower <= 9) {
                            newName[nextPlace++] = (byte) ('0' + lower);
                        } else {
                            newName[nextPlace++] = (byte) ('A' + (lower - 10));
                        }
                    }
                } else {
                    newName[nextPlace++] = (byte)c;
                }
            }
            return new String(newName, 0, nextPlace);
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee.getMessage());
        }
    }


    /**
     * Gets the unescaped string.
     * <pre>
     *   escaped   = "%" hex hex
     *   hex       = digit | "A" | "B" | "C" | "D" | "E" | "F" |
     *                       "a" | "b" | "c" | "d" | "e" | "f"
     * </pre>
     * @param name string to be processed
     * @return escaped string
     * @throws IllegalArgumentException if encoding not supported
     *
     */
    public static String getUnescapedString(String name) {
        try {
            if (name == null) {
                return null;
            }
            if (name.indexOf("%") == -1) {
                return name;
            } else {
                byte newName[] = new byte[name.length()];
                int nextPlace = 0;
                for (int i = 0; i < name.length(); i++) {
                    char c = name.charAt(i);
                    if (c == '%') {
                        String hexNum = name.substring(i+1, i+3).toUpperCase();
                        if (isHexCharsLegal(hexNum)) {
                            c = hexToChar(hexNum);
                            i = i + 2;
                        } else {
                            throw new IllegalArgumentException("Bad format");
                        }
                    } else if (containsReserved(c)) {
                        throw
                            new IllegalArgumentException("Bad escaped format");
                    }
                    newName[nextPlace++] = (byte)c;
                }
                return new String(newName, 0, nextPlace,  "UTF-8");
            }
        } catch (UnsupportedEncodingException uee) {
            throw new IllegalArgumentException(uee.getMessage());
        }
    }

    /**
     * Checks if the hexadecimal character is valid.
     * @param hexValue string to be checked
     * @return <code>true</code> if all characters are valid
     */
    private static boolean isHexCharsLegal(String hexValue) {
        if ((isDigit(hexValue.charAt(0)) || isABCDEF(hexValue.charAt(0))) &&
            (isDigit(hexValue.charAt(1)) || isABCDEF(hexValue.charAt(1)))) {
            return true;
        } else {
            return false;
        }

    }

    /**
     * Converts one hexadecimal char.
     * @param hexValue string to be processed
     * @return normalized hex value
     */
    private static char hexToChar(String hexValue) {
        char c = 0;
        if (isDigit(hexValue.charAt(0))) {
            c += (hexValue.charAt(0) - '0')*16;
        } else {
            c += (hexValue.charAt(0) - 'A' + 10)*16;
        }

        if (isDigit(hexValue.charAt(1))) {
            c += (hexValue.charAt(01) - '0');
        } else {
            c += (hexValue.charAt(1) - 'A' + 10);
        }
        return c;
    }

    /**
     * Checks if character is decimal digit.
     * @param c character to check
     * @return <code>true</code> if in the range 0..9
     */
    private static boolean isDigit(char c) {
        return (c >= '0' && c <= '9');
    }

    /**
     * Checks if character is hexadecimal digit.
     * @param c character to check
     * @return  <code>true</code> if in the range A..F
     */
    private static boolean isABCDEF(char c) {
        return (c >= 'A' && c <= 'F');
    }

    /**
     * Checks if character is from the reserved character set.
     * @param c character to check
     * @return  <code>true</code> if not in the range A..Z,
     * a..z,..9, or punctuation (forward slash, colon, hyphen,
     * under score, period, exclamation, tilde, asterisk, single quote,
     * left paren or right paren).
     */
    private static boolean containsReserved(char c) {
        return !((c >= 'A' && c <= 'Z') || (c >= 'a' && c <= 'z') ||
                 (c >= '0' && c <= '9') || ("/:-_.!~*'()".indexOf(c) != -1));
    }

} // End  of EscapeUtil
