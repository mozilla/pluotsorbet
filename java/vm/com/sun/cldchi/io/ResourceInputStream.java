/*
 *   
 *
 * Copyright  1990-2007 Sun Microsystems, Inc. All Rights Reserved.
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

package com.sun.cldc.io;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

/**
 * Input stream class for accessing resource files in classpath.
 */
public class ResourceInputStream extends InputStream {
    private Object fileDecoder;
    private Object savedDecoder; // used for mark/reset functionality

    /**
     * Fixes the resource name to be conformant with the CLDC 1.0
     * specification. We are not allowed to use "../" to get outside
     * of the .jar file.
     *
     * @param name the name of the resource in classpath to access.
     * @return     the fixed string.
     * @exception  IOException if the resource name points to a
     *              classfile, as determined by the resource name's
     *              extension.
     */
    private static String fixResourceName(String name) throws IOException {
        Vector dirVector = new Vector();
        int    startIdx = 0;
        int    endIdx = 0;
        String curDir;

        while ((endIdx = name.indexOf('/', startIdx)) != -1) {
            if (endIdx == startIdx) {
                // We have a leading '/' or two consecutive '/'s
                startIdx++;
                continue;
            }

            curDir = name.substring(startIdx, endIdx);
            startIdx = endIdx + 1;

            if (curDir.equals(".")) {
                // Ignore a single '.' directory
                continue;
            }
            if (curDir.equals("..")) {
                // Go up a level
                try {
                    dirVector.removeElementAt(dirVector.size()-1);
                } catch (ArrayIndexOutOfBoundsException aioobe) {
                     // "/../resource" Not allowed!
                     throw new IOException();
                }
                continue;
            }
            dirVector.addElement(curDir);
        }

        // save directory structure
        StringBuffer dirName = new StringBuffer();

        int nelements = dirVector.size();
        for (int i = 0; i < nelements; ++i) {
          dirName.append((String)dirVector.elementAt(i));
          dirName.append("/");
        }

        // save filename
        if (startIdx < name.length()) {
            String filename = name.substring(startIdx);
            // Throw IOE if the resource ends with ".class", but, not
            //  if the entire name is ".class"
            if ((filename.endsWith(".class")) &&
                (! ".class".equals(filename))) {
                throw new IOException();
            }
            dirName.append(name.substring(startIdx));
        }
        return dirName.toString();
    }

    /**
     * Construct a resource input stream for accessing objects in the jar file.
     *
     * @param name the name of the resource in classpath to access. The
     *              name must not have a leading '/'.
     * @exception  IOException  if an I/O error occurs.
     */
    public ResourceInputStream(String name) throws IOException {
        String fixedName = fixResourceName(name);
        fileDecoder = open(fixedName);
        if (fileDecoder == null) {
            throw new IOException();
        }
     }

    /**
     * Reads the next byte of data from the input stream.
     *
     * @return     the next byte of data, or <code>-1</code> if the end
     *             of the stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read() throws IOException {
        // Fix for CR 6303054
        if (fileDecoder == null) {
            throw new IOException();
        }
        return readByte(fileDecoder);
    }

    /**
     * Gets the number of bytes remaining to be read.
     *
     * @return     the number of bytes remaining in the resource.
     * @exception  IOException  if an I/O error occurs.
     */
    public int available() throws IOException {
        if (fileDecoder == null) {
            throw new IOException();
        }
        return bytesRemain(fileDecoder);
    }

    /**
     * Reads bytes into a byte array.
     *
     * @param b the buffer to read into.
     * @param off offset to start at in the buffer.
     * @param len number of bytes to read.
     * @return     the number of bytes read, or <code>-1</code> if the end
     *             of the stream is reached.
     * @exception  IOException  if an I/O error occurs.
     */
    public int read(byte b[], int off, int len) throws IOException {
        // Fix for CR 6303054
        if (fileDecoder == null) {
            throw new IOException();
        }
        if (b == null) {
            throw new NullPointerException();
        } else if ((off < 0) || (off > b.length) || (len < 0) ||
                   ((off + len) > b.length) || ((off + len) < 0)) {
            throw new IndexOutOfBoundsException();
        }
        return readBytes(fileDecoder, b, off, len);
    }

    public void close() throws IOException {
        fileDecoder = null;
    }

    /**
     * Remembers current position in ResourceInputStream so that
     * subsequent call to <code>reset</code> will rewind the stream
     * to the saved position.
     *
     * @param readlimit affects nothing
     * @see   java.io.InputStream#reset()
     */
    public void mark(int readlimit) {
        if (fileDecoder != null) {
            savedDecoder = clone(fileDecoder);
        }
    }

    /**
     * Repositions this stream to the position at the time the
     * <code>mark</code> method was last called on this input stream.
     *
     * @exception IOException if this stream has not been marked
     * @see   java.io.InputStream#mark(int)
     */
    public void reset() throws IOException {
        if (fileDecoder == null || savedDecoder == null) {
            throw new IOException();
        }
        fileDecoder = clone(savedDecoder);
    }

    /**
     * Indicates that this ResourceInputStream supports mark/reset
     * functionality
     * 
     * @return true
     */
    public boolean markSupported() {
        return true;
    }

    // OS-specific interface to underlying file system.
    private static native Object open(String name);
    private static native int bytesRemain(Object fileDecoder);
    private static native int readByte(Object fileDecoder);
    private static native int readBytes(Object fileDecoder,
                                        byte b[], int off, int len);
    /*
     * Copies all fields from one FileDecoder object to another -
     * used remember or restore current ResourceInputStream state.
     */
    private static native Object clone(Object source);
}
