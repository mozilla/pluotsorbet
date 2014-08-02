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

package com.sun.j2me.content;

class Logger {
	
	static final private java.io.PrintStream out = System.out;

    /**
     * Log an information message to the system logger for this AppProxy.
     * @param msg a message to write to the log.
     */
    void println(String msg) {
        out.println(">> " + threadID() + ": " + msg);
    }

    /**
     * Log an information message to the system logger for this AppProxy.
     * @param msg a message to write to the log.
     * @param t Throwable to be logged
     */
    void log(String msg, Throwable t) {
        out.println("** " + threadID() + ": " + msg);
        t.printStackTrace();
    }


    /**
     * Map a thread to an printable string.
     * @return a short string for the thread
     */
    private String threadID() {
        Thread thread = Thread.currentThread();
        int i = thread.hashCode() & 0xff;
        return "T" + i;
    }
}
