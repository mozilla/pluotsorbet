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
 * Look and Feel interface used by CustomItem.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface CustomItemLF extends ItemLF {

    /**
     * Notifies L&F that repaint of the entire custom item is needed
     */
    void lRepaint();

    /**
     * Notifies L&F that repaint of the specified region is needed.
     *
     * @param x the x coordinate of the origin of the dirty region
     * @param y the y coordinate of the origin of the dirty region
     * @param w the width of the dirty region
     * @param h the height of the dirty region
     */
    void lRepaint(int x, int y, int w, int h);

    /**
     * Notifies L&F that Custom Item was invalidated.
     */
    void lInvalidate();

    // JAVADOC COMMENT ELIDED
    int lGetInteractionModes();

    /**
     * Refresh the cached preferred and minimum sizes of this CustomItem.
     */
    void uCallSizeRefresh();
}
