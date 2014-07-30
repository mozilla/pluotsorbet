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

package com.sun.midp.chameleon;

/**
 * The class represents bi-directional ordered list of UI layers with
 * possibility to add, remove and iterate over list elements 
 */
class CLayerList {
    protected CLayerElement top;
    protected CLayerElement bottom;
    protected int count;

    /** Construct empty layers list */
    CLayerList() {
        top = null;
        bottom = null;
        count = 0;
    }

    /**
     * Find UI layer instance in the list
     * @param layer the instance to search in the list for
     * @return list element with searched layer if success, null otherwise
     */
    CLayerElement find(CLayer layer) {
        for(CLayerElement l = top; l != null; l = l.lower)
            if (l.layer == layer) return l;
        return null;
    }

    /**
     * Add new layer to the top of list with no check for other
     * occurrences of the layer in the list
     *
     * @param layer CLayer instance to be added
     * @return list element of the newly added layer
     */
    CLayerElement addLayer(CLayer layer) {
        CLayerElement le =
            new CLayerElement(layer, top, null);
        if (top != null)
            top.upper = le;
        top = le;
        if (bottom == null)
            bottom = le;
        count ++;
        return le;
    }

    /**
     * Remove layer from the list
     *
     * @param layer CLayer instance to be removed
     * @return true if the layer was found and removed, false otherwise
     */
    boolean removeLayer(CLayer layer) {
        CLayerElement le = find(layer);
        if (le != null) {
            removeLayerElement(le);
            le.layer = null;
            return true;
        } else {
            return false;
        }
    };

    /**
     * Remove layer element from the list with no extra checks.
     * It's caller's responsibility to apply the method on
     * list elements only.
     *
     * @param le list element to be removed
     */
    void removeLayerElement(CLayerElement le) {
        CLayerElement upper = le.upper;
        CLayerElement lower = le.lower;

        if (upper != null) {
            upper.lower = lower;
        } else if (top == le) {
            top = lower;
        }

        if (lower != null) {
            lower.upper = upper;
        } else if (bottom == le) { 
            bottom = upper;
        }

        // Clear links to neighbour layers
        le.upper = le.lower = null;
        count --;
    }

    /**
     * Get the most top list element
     * @return return the element with top most layer
     */
    CLayerElement getTop() {
        return top;
    }

    /**
     * Get the most bottom list element
     * @return return the element with top most layer
     */
    CLayerElement getBottom() {
        return bottom;
    }

    /**
     * Get number of layers in the list
     * @return number of list elements
     */
    int size() {
        return count;
    }
}
