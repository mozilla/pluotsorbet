/*
 *
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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

/*
 * (c) Copyright 2001, 2002 Motorola, Inc.  ALL RIGHTS RESERVED.
 */
package javax.obex;
import java.io.IOException;

/*
 * This interface is defined by the JSR-82 specification
 * <em>Java&trade; APIs for Bluetooth&trade; Wireless Technology,
 * Version 1.1.</em>
 */
// JAVADOC COMMENT ELIDED 
public interface HeaderSet {

    // JAVADOC COMMENT ELIDED 
    public static final int COUNT = 0xC0;

    // JAVADOC COMMENT ELIDED 
    public static final int NAME = 0x01;

    // JAVADOC COMMENT ELIDED 
    public static final int TYPE = 0x42;

    // JAVADOC COMMENT ELIDED 
    public static final int LENGTH = 0xC3;

    // JAVADOC COMMENT ELIDED 
    public static final int TIME_ISO_8601 = 0x44;

    // JAVADOC COMMENT ELIDED 
    public static final int TIME_4_BYTE = 0xC4;

    // JAVADOC COMMENT ELIDED 
    public static final int DESCRIPTION = 0x05;

    // JAVADOC COMMENT ELIDED 
    public static final int TARGET = 0x46;

    // JAVADOC COMMENT ELIDED 
    public static final int HTTP = 0x47;

    // JAVADOC COMMENT ELIDED 
    public static final int WHO = 0x4A;

    // JAVADOC COMMENT ELIDED 
    public static final int OBJECT_CLASS = 0x4F;

    // JAVADOC COMMENT ELIDED 
    public static final int APPLICATION_PARAMETER = 0x4C;

    // JAVADOC COMMENT ELIDED 
    public void setHeader(int headerID, Object headerValue);

    // JAVADOC COMMENT ELIDED 
    public Object getHeader(int headerID) throws IOException;

    // JAVADOC COMMENT ELIDED 
    public int[] getHeaderList() throws IOException;

    // JAVADOC COMMENT ELIDED 
    public void createAuthenticationChallenge(String realm, boolean userID,
        boolean access);

    // JAVADOC COMMENT ELIDED 
    public int getResponseCode() throws IOException;
}
