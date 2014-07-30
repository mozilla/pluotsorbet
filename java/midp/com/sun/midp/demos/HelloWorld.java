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
package com.sun.midp.demos;

import javax.microedition.midlet.*;

/**
 * An example MIDlet with simple "Hello World" text and an exit command.
 */
public class HelloWorld extends MIDlet {

    /**
     * Constructs the HelloWorld MIDlet.
     */
    public HelloWorld() {
    }

    /**
     * Prints "Hello World" and destroys the MIDlet.
     */
    public void startApp() {
        System.out.println("Hello World");
        notifyDestroyed();
    }

    /**
     * Pauses the MIDlet; this method does nothing because
     * there are no background activities or shared resources
     * to close.
     */
    public void pauseApp() {
    }


    /**
     * Destroys the MIDlet; this method does nothing because
     * there is nothing to cleanup that is not handled by the
     * garbage collector.
     */
    public void destroyApp(boolean unconditional) {
    }
}
