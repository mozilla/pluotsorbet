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
 * Look and Feel interface used by Gauge.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface GaugeLF extends ItemLF {
    
    /**
     * Notifies L&F of a value change in the corresponding Gauge.
     * @param oldValue - the old value set in the Gauge
     * @param value - the new value set in the Gauge
     */
    void lSetValue(int oldValue, int value);

    /**
     * Notifies L&F of a maximum value change in the corresponding Gauge.
     * @param oldMaxValue - the old maximum value set in the Gauge
     * @param maxValue - the new maximum value set in the Gauge
     */
    void lSetMaxValue(int oldMaxValue, int maxValue);

    /**
     * Returns the current value in the gauge.
     * @return the current value in the Gauge
     */
    int lGetValue();
}
