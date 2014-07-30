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
 * Look and Feel interface used by Form.
 * <p>
 * See <a href="doc-files/naming.html">Naming Conventions</a>
 * for information about method naming conventions.
 */
interface FormLF extends DisplayableLF {

    /**
     * Gets item currently in focus.
     * @return the item currently in focus in this FormLF;
     *          if there are no items in focus, null is returned
     */
    Item lGetCurrentItem();

    /**
     * Notifies look&feel object of a call to Display.setCurrentItem()
     * Note that null can be passed in clear the previously set current item.
     *
     * @param item - the item in the corresponding Form to be displayed
     */
    void uItemMakeVisible(Item item);

    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the item set
     * @param item - the item set in the corresponding Form
     *
     */
    void lSet(int itemNum, Item item);

    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the deleted item
     * @param item - the item deleted in the corresponding Form
     *
     */
    void lInsert(int itemNum, Item item);

    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     * @param itemNum - the index of the deleted item
     * @param deletedItem - the item deleted in the corresponding form
     *
     */
    void lDelete(int itemNum, Item deletedItem);


    /**
     * Notifies look&feel object of an item deleted in the corresponding
     * Form.
     * 
     */
    void lDeleteAll();

    /**
     * Called by Display to notify current FormLF of a change in its peer state.
     *
     * @param modelVersion the version of the peer's data model
     * @param subtype the sub type of peer event
     * @param itemPeerId the id of the ItemLF's peer whose state has changed
     * @param hint some value that is interpreted only between the peers
     */
    void uCallPeerStateChanged(int modelVersion, int subType, 
                                int itemPeerId, int hint);
 
}
