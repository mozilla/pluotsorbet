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

package javax.microedition.lcdui;

/**
 * Look and Feel interface used by Canvas.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface CanvasLF extends DisplayableLF {

 
    /**
     * Notifies look and feel object that repaint of a (x, y, width, height)
     * area is needed.
     *
     * @param x The x coordinate of the region to repaint
     * @param y The y coordinate of the region to repaint
     * @param width The width of the region to repaint
     * @param height The height of the region to repaint
     * @param target an optional paint target to receive the paint request
     *               when it returns via callPaint()
     */
    void lRepaint(int x, int y, int width, int height, Object target);

    /**
     * Notifies that repaint of the entire Canvas look&feel is needed.
     */
    void lRepaint();

    /**
     * Request serviceRepaints from current Display.
     * SYNC NOTE: unlike most other methods, no locking is held when
     * this function is called because Display.serviceRepaints()
     * needs to handle its own locking. 
     */
    void uServiceRepaints();

    /**
     * Display calls to get key mask of all the keys that were pressed.
     * @return keyMask  The key mask of all the keys that were pressed.
     */
    public int uGetKeyMask();
}
