/*
 *   
 *
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

package com.sun.midp.installer;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

import java.util.Vector;

import com.sun.midp.util.Properties;

/*
 * EBNF for parsing Application Descriptors, from MIDP 1.0 Spec.
 * except that the WSP before and after attrvalue in attrline is optional.
 *
 * appldesc: *attrline
 * attrline: attrname ":" *WSP attrvalue *WSP newline
 *
 * attrname: 1*<any Unicode char except CTLs or separators>
 * attrvalue: *valuechar | valuechar *(valuechar | WSP) valuechar
 * valuechar: <any Unicode char except CTLs and WSP>
 *
 * newline: CR LF | LF
 * CR: <Unicode carriage return (0x000D)>
 * LF: <Unicode linefeed (0x000A)>
 * 
 * WSP: 1*(SP | HT)
 * SP: <Unicode space (0x0020)>
 * HT: <Unicode horizontal-tab (0x0009)>
 * CTL: <Unicode characters 0x0000 - 0x001F and 0x007F>
 * separator: "(" | ")" | "<" | ">" | "@"
 *            | "," | ";" | ":" | "'" | """
 *            | "/" | "[" | "]" | "?" | "="
 *            | "{" | "}" | SP  | HT  
 */

/**
 * This class represents a set of properties loaded from a MIDP Java
 * Application Descriptor. The parsing of descriptor is more relaxed than
 * the MIDP 1.0 specification. First, the white space that is required
 * before and after the property value is optional. Second, any extra
 * carriage returns and end of file characters are ignored. Third, the key
 * of the property is only checked for whitespace. Fourth, blanklines
 * are allowed so the save code when parsing JAR manifests.
 * <p>
 * The set of properties, though not strictly ordered, will be stored
 * in the same order it was read in or created, with modifications being
 * appended to the end of the list by default.
 * <p>
 * If an alternate encoding type is not given, when saving properties 
 * to a stream or loading them from a stream, the ISO 8859-1 character 
 * encoding is used.
 */

public class JadProperties extends Properties {
    /** Horizontal Tab - Unicode character 0x09. */
    protected static final int HT = 0x09;

    /** Line Feed - Unicode character 0x0A. */
    protected static final int LF = 0x0A;

    /** Carriage Return - Unicode character 0x0D. */
    protected static final int CR = 0x0D;

    /** End Of File - Unicode character 0x1A. */
    protected static final int EOF = 0x1A;

    /** SPace - Unicode character 0x20. */
    protected static final int SP = 0x20;

    /** Buffers one line from the stream. */
    protected char[] lineBuffer = null;

    /**
     * Constructor - creates an empty property list.
     */
    public JadProperties() {
    }

    
    /**
     * Reads a JAD (key and element pairs) from the input stream.
     * The stream uses the character encoding specified by <code>enc</code>
     * <p>
     * Every property occupies one line of the input stream. Each line
     * is terminated by a line terminator which can be a LF, or (CR LF).
     * Lines from the input stream are processed until
     * end of file is reached on the input stream.
     * <p>
     * Every line describes one property to be added to the table.
     * The key consists of all the characters from beginning of the line
     * up to, but not including the first ASCII <code>:</code>.
     * All remaining characters on the line become part of the associated
     * element. The element is also trimmed of leading and trailing
     * whitespace.
     * <p>
     * As an example, each of the following line specifies the key
     * <code>"Truth"</code> and the associated element value
     * <code>"Beauty"</code>:
     * <p>
     * <pre>
     *	Truth: Beauty
     * </pre>
     * <p>
     * This method will try to continue after a format error and load as
     * many properties it can, but throw the last error encountered.
     *
     * @param      inStream   the input stream.
     * @param      enc        character encoding used on input stream,
     *                        can be null to get the default (UTF-8)
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     * @exception  InvalidJadException if the JAD is not formatted correctly.
     */
    public synchronized void load(InputStream inStream, String enc) 
	throws IOException, InvalidJadException {
            partialLoad(inStream, enc, Integer.MAX_VALUE);
    } 

    /**
     * Loads up a given number of properties from a JAD.
     * Used when authenticating a JAD.
     *
     * @param      inStream   the input stream.
     * @param      enc        character encoding used on input stream,
     *                        null for the default encoding (UTF-8)
     * @param      propertiesToLoad maximum number of properties to load
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     * @exception  InvalidJadException if the JAD is not formatted correctly.
     */
    public void partialLoad(InputStream inStream, String enc,
            int propertiesToLoad) throws IOException,
            InvalidJadException {
        Reader in;
        String line;
        int endOfKey;
        String key = null;
        int startOfValue;
        String value = null;
        InvalidJadException jadException = null;

        if (enc == null) {
            in = new InputStreamReader(inStream, "UTF-8");
        } else {
            in = new InputStreamReader(inStream, enc);
        }

	lineBuffer = new char[512];

	for (int i = 0; i < propertiesToLoad; i++) {
            // Get next line
            line = readLine(in);
            if (line == null) {
                break;
            }

            // blank line separate groups of properties
            if (line.length() == 0) {
                continue;
            }

            endOfKey = line.indexOf(":");
            if (endOfKey == -1) {
                jadException =
                    new InvalidJadException(InvalidJadException.INVALID_KEY,
                                            line);
                continue;
            }

            key = line.substring(0, endOfKey);

            if (key == null || key.length() == 0) {
                jadException =
                    new InvalidJadException(InvalidJadException.INVALID_KEY,
                                            line);
                continue;
            }

            if (!checkKeyChars(key)) {
                jadException =
                    new InvalidJadException(InvalidJadException.INVALID_KEY,
                                            line);
                continue;
            }

            startOfValue = endOfKey + 1;
            value = line.substring(startOfValue, line.length());
            value = value.trim();
            if (value == null) {
                jadException =
                    new InvalidJadException(InvalidJadException.INVALID_VALUE,
                                            key);
                continue;
            }

            if (!checkValueChars(value)) {
                jadException = new
                    InvalidJadException(InvalidJadException.INVALID_VALUE,
                                        key);
                continue;
            }

            putProperty(key, value);
	}

        // we only need the line buffer while loading, so let it be reclaimed
	lineBuffer = null;

        if (jadException != null) {
            throw jadException;
        }
    }

    /**
     * Loads properties from the input stream using the default
     * character encoding. Currently the default encoding is UTF8.
     *
     * @see #load(InputStream inStream, String enc)
     * @param      inStream   the input stream.
     * @exception  IOException  if an error occurred when reading from the
     *               input stream.
     * @exception  InvalidJadException if the JAD is not formatted correctly.
     */
    public synchronized void load(InputStream inStream) throws IOException,
            InvalidJadException {
	load(inStream, null);
    }

    /**
     * Store key:value pair.
     *
     * @param key the key to be placed into this property list.
     * @param value the value corresponding to <tt>key</tt>.
     * @see #getProperty
     */
    protected void putProperty(String key, String value) {
	setProperty(key, value);
    }

    /**
     * Reads one using a given reader. LF or CR LF end a line.
     * The end of line and end of file characters are dropped.
     * @param in reader for a JAD
     * @return one line of the JAD or null at the end of the JAD
     * @exception IOException thrown by the reader
     */
    protected String readLine(Reader in) throws IOException {
	int room;
	int offset = 0;
	int c = 0;
        char[] temp;

	room = lineBuffer.length;

        for (;;) {
            c = in.read();
            if (c == -1 || c == LF) {
                // LF or CR LF ends a line
                break;
            }

            /*
             * throw away carriage returns and the end of file character.
             */
            if (c == CR || c == EOF) {
                continue;
            }

            if (--room < 0) {
                temp = new char[offset + 128];
                room = temp.length - offset - 1;
                System.arraycopy(lineBuffer, 0, temp, 0, offset);
                lineBuffer = temp;
            }

            lineBuffer[offset++] = (char) c;
	}

	if ((c == -1) && (offset <= 0)) {
	    return null;
	}

        return new String(lineBuffer, 0, offset);
    }

    /**
     * Check to see if all the chars in the key of a property are valid.
     *
     * @param key key to check
     *
     * @return false if a character is not valid for a key
     */
    protected boolean checkKeyChars(String key) {
        char[] temp = key.toCharArray();
        int len = temp.length;

        for (int i = 0; i < len; i++) {
            char current = temp[i];

            if (current <= 0x1F ||
                current == 0x7F ||
                current == '(' ||
                current == ')' ||
                current == '<' ||
                current == '>' ||
                current == '@' ||
                current == ',' ||
                current == ';' ||
                current == '\'' ||
                current == '"' ||
                current == '/' ||
                current == '[' ||
                current == ']' ||
                current == '?' ||
                current == '=' ||
                current == '{' ||
                current == '}' ||
                current == SP ||
                current == HT) {

                return false;
            }
        }

        return true;
    }

    /**
     * Check to see if all the chars in the value of a property are valid.
     *
     * @param value value to check
     *
     * @return false if a character is not valid for a value
     */
    protected boolean checkValueChars(String value) {
        char[] temp = value.toCharArray();
        int len = temp.length;

        // assume whitespace and newlines are trimmed
        for (int i = 0; i < len; i++) {
            char current = temp[i];
             
            // if current is a CTL character, throw exception
            if ((current <= 0x1F || current == 0x7F) && 
                (current != HT)) {
                return false;
            }
        }

        return true;
    }
}
