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
 * Look and Feel interface used by Alert.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface AlertLF extends DisplayableLF {

    /**
     * Determines if alert associated with this view is modal.
     *
     * @return true if this AlertLF should be displayed as modal
     */    
    boolean lIsModal();


    /**
     * Gets default timeout for the alert associated with this view
     * @return the default timeout
     */
    int lGetDefaultTimeout();

    /**
     * Get the command that Alert.DISMISS_COMMAND is mapped to.
     * 
     * @return DISMISS_COMMAND's visible representation command
     */
    Command lGetDismissCommand();


    /**
     * Notifies look&feel object of a timeout change.
     * 
     * @param timeout - the new timeout set in the corresponding Alert.
     */
    void lSetTimeout(int timeout);

    /**
     * Notifies look&feel object of a Alert type change.
     * 
     * @param type - the new AlertType set in the corresponding Alert.
     */
    void lSetType(AlertType type);


    /**
     * Notifies look&feel object of a string change.
     * 
     * @param oldStr - the old string set in the corresponding Alert.
     * @param newStr - the new string set in the corresponding Alert.
     */
    void lSetString(String oldStr, String newStr);

    /**
     * Notifies look&feel object of an image change.
     * 
     * @param oldImg - the old image set in the corresponding Alert.
     * @param newImg - the new image set in the corresponding Alert.
     */
    void lSetImage(Image oldImg, Image newImg);

    /**
     * Notifies look&feel object of an indicator change.
     * 
     * @param oldIndicator - the old indicator set in the corresponding Alert
     * @param newIndicator - the new indicator set in the corresponding Alert
     */
    void lSetIndicator(Gauge oldIndicator, Gauge newIndicator);
}
