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
 * Look and feel implementation of <code>Spacer</code>.
 */
class SpacerLFImpl extends ItemLFImpl implements SpacerLF {

    /**
     * Creates Look &amp; Feel object for <code>Spacer</code>.
     *
     * @param spacer the model object
     */
    SpacerLFImpl(Spacer spacer) {
        super(spacer);
	sp = spacer;
	
	// Initialize the cached requested size
	lSetRequestedSizes(sp.width, sp.height, sp.width, sp.height);
    }
    /**
     * Notifies look &amp; feel of a minimum size change in the 
     * <code>Spacer</code>.
     *
     * @param minWidth the new minimum width
     * @param minHeight the new minimum height
     */
    public void lSetMinimumSize(int minWidth, int minHeight) {
        lRequestInvalidate(true, true);
	// Set requested sizes AFTER the invalidate request above
	lSetRequestedSizes(sp.width, sp.height, sp.width, sp.height);
    }

    /**
     * Calculate minimum and preferred width and height of this item and 
     * store the result in instance variables:
     * minimumWidth, minimumHeight, preferredWidth and preferredHeight.
     *
     * Override the version in <code>ItemLFImpl</code> to do nothing.
     */
    void lGetRequestedSizes() {
	// Since the cached sizes are always kept up to date.
	// Nothing needs to be done here.

	// ASSERT (isRequestedSizesValid() == true)
    }

    /**
     * Called by event delivery to notify an <code>ItemLF</code> in current 
     * <code>FormLF</code> of a change in its peer state.
     *
     * Do nothing and returns <code>false</code> since there is no state 
     * to change.
     * 
     * @param hint not used
     *
     * @return always <code>false</code>
     */
    boolean uCallPeerStateChanged(int hint) {
	return false; // Unexpected call
    }

    /**
     * Create native resource of this <code>Item</code>.
     *
     * @param ownerId owner screen's native resource id
     */
    void createNativeResource(int ownerId) { }

    /** 
     * <code>Spacer</code> associated with this look &amp; feel.
     */
    Spacer sp;
}
