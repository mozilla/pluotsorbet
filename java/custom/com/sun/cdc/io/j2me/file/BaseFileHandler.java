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

import java.io.IOException;
import java.util.Vector;

/**
 * Base file handler.
 */
interface BaseFileHandler {

    /**
     * Connect file handler to the abstract file target. This operation should
     * not trigger any access to the native filesystem.
     *
     * @param rootName The name of the root directory.
     * @param absFile Full path to the file to be handled by this handler.
     *
     * @return native path/name.
     *
     * @throws IllegalArgumentException if filename contains characters
     *         not allowed by the file system. This check should not involve
     *         any actual access to the filesystem.
     */
    public String connect(String rootName, String absFile);

    /**
     * If necessary, creates dedicated private working directory for the MIDlet
     * suite.
     *
     * The method does nothing if specified root is not private root or
     * the directory already exists.
     *
     * @param rootName the name of file root
     *
     * @throws IOException if I/O error occures
     */
    public void ensurePrivateDirExists(String rootName) throws IOException;

    /**
     * Open the file for reading, on the underlying file system. File name is
     * passed in the link#connect() method.
     *
     * @throws IOException if file is directory, file does not exists,
     *                     if file is already open for read or other
     *                     I/O error occurs
     */
    public void openForRead() throws IOException;

    /**
     * Closes for reading the file that was open by openForRead method.
     * If the file is already closed for reading this method does nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */
    public void closeForRead() throws IOException;

    /**
     * Open the file for writing, on the underlying file system. File name is
     * passed in the link#connect() method.
     *
     * @throws IOException if file is directory, file does not exists,
     * i                   if file is already open for write or other
     *                     I/O error occurs
     */
    public void openForWrite() throws IOException;

    /**
     * Closes for writing the file that was open by openForWrite method.
     * If the file is already closed for writing this method does nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */
    public void closeForWrite() throws IOException;

    /**
     * Closes the file for both reading and writing.
     * If the file is already closed for reading and writing this method does 
     * nothing.
     *
     * @throws IOException if file is directory, file does not exists or other
     *                     I/O error occurs
     */    
    public void closeForReadWrite() throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.list() description
    public Vector list(String filter, boolean includeHidden)
                                                        throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.create() description
    public void create() throws IOException;

    /**
     * Check is file or directory corresponding to this filehandler exists.
     *
     * @return true if file exists, otherwise false
     */
    public boolean exists();

    /**
     * Check is file corresponding to this filehandler exists and is a
     * directory.
     *
     * @return true if directory exists, otherwise false
     */
    public boolean isDirectory();

    // JAVADOC COMMENT ELIDED - see FileConnection.delete() description
    public void delete() throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.rename() description
    public void rename(String newName) throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.truncate() description
    public void truncate(long byteOffset) throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.fileSize() description
    public long fileSize() throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.directorySize() description
    public long directorySize(boolean includeSubDirs) throws IOException;

    /**
     * Check if file corresponding to this filehandler exists and has a 
     * read permission.
     *
     * @return true if file has read permission, otherwise false
     */
    public boolean canRead();

    /**
     * Check is file corresponding to this filehandler exists and has a
     * write permission.
     *
     * @return true if file has write permission, otherwise false
     */
    public boolean canWrite();

    /**
     * Check is file corresponding to this filehandler exists and is
     * hidden.
     *
     * @return true if file is hidden, otherwise false
     */
    public boolean isHidden();

    // JAVADOC COMMENT ELIDED - see FileConnection.setReadable() description
    public void setReadable(boolean readable) throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.setWritable() description
    public void setWritable(boolean writable) throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.setHidden() description
    public void setHidden(boolean hidden) throws IOException;

    /**
     * Returns the time that the file denoted by this file handler
     * was last modified.
     *
     * @return time when the file was last modified.
     */
    public long lastModified();

    // JAVADOC COMMENT ELIDED - see FileConnection.mkdir() description
    public void mkdir() throws IOException;

    // JAVADOC COMMENT ELIDED
    // See javax.microedition.media.protocol.SourceStream.read() in JSR-135
    public int read(byte b[], int off, int len) throws IOException;

    // JAVADOC COMMENT ELIDED
    // See java.io.OutputStream.write() in CLDC 1.1
    public int write(byte b[], int off, int len) throws IOException;

    // JAVADOC COMMENT ELIDED
    // See java.io.OutputStream.flush() in CLDC 1.1
    public void flush() throws IOException;

    /**
     * Sets the location for the next write operation.
     * @param offset location for next write
     * @throws IOException if an error occurs
     */
    public void positionForWrite(long offset) throws IOException;

    // JAVADOC COMMENT ELIDED - see FileConnection.availableSize() description
    public long availableSize();

    // JAVADOC COMMENT ELIDED - see FileConnection.totalSize() description
    public long totalSize();

    /**
     * Returns a string that contains all characters forbidden for the use on
     * the given platform except "/" (forward slash) which is always considered
     * illegal. If there are no such characters an empty string is returned.
     * @return string of characters not allowed in file names
     */
    public String illegalFileNameChars();

    // JAVADOC COMMENT ELIDED - see FileConnection.usedSize() description
    public long usedSize();

    /**
     * Close file associated with this handler. Open file and all system
     * resources should be released by this call. Handler object can be
     * reused by subsequent call to connect().
     *
     * @throws IOException if I/O error occurs
     */
    public void close() throws IOException;
}
