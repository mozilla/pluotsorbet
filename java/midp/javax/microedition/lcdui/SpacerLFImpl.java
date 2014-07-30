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
* This is the look & feel implementation for Spacer.
*/
class SpacerLFImpl extends ItemLFImpl implements SpacerLF {


    /**
     * Creates Look & Feel object for Spacer.
     * @param spacer <placeholder>
     */
    SpacerLFImpl(Spacer spacer) {
        super(spacer);
	this.spacer = spacer;
    }
    /**
     * Notifies Look & Feel of a minimum size change in the Spacer.
     * @param minWidth - the new minimum width
     * @param minHeight - the new minimum height
     */
    public void lSetMinimumSize(int minWidth, int minHeight) {
        lRequestInvalidate(true, true);
    }

    /**
     * Get the minimum width of this Item
     *
     * @return the minimum width
     */
    public int lGetMinimumWidth() {
        return spacer.width;
    }

    /**
     * Get the preferred width of this Item
     *
     * @param h the tentative content height in pixels, or -1 if a
     * tentative height has not been computed
     * @return the preferred width
     */
    public int lGetPreferredWidth(int h) {
        return spacer.width;
    }

    /**
     * Get the minimum height of this Item
     *
     * @return the minimum height
     */
    public int lGetMinimumHeight() {
        return spacer.height;
    }

    /**
     * Get the preferred height of this Item
     *
     * @param w the tentative content width in pixels, or -1 if a
     * tentative width has not been computed
     * @return the preferred height
     */
    public int lGetPreferredHeight(int w) {
        return spacer.height;
    }

    // ***********************************************
    // Package private methods
    // ***********************************************

    /**
     * Used by the Form Layout to set the size of Items.
     * Spacer should add no padding around itself
     *
     * @param height the tentative content height in pixels
     * @return the preferred width 
     */
    int lGetAdornedPreferredWidth(int height) {
	return lGetPreferredWidth(height);
    }
    
    /**
     * Used by the Form Layout to set the size of Items.
     * Spacer should add no padding around itself
     *
     * @param width the tentative content width in pixels
     * @return the preferred height
     */
    int lGetAdornedPreferredHeight(int width) {
	return lGetPreferredHeight(width);
    }

    /**
     * Used by the Form Layout to set the size of this Item
     * @return the minimum width that includes cell spacing
     */
    int lGetAdornedMinimumWidth() {
	return lGetMinimumWidth();
    }

    /**
     * Used by the Form Layout to set the size of this Item
     * @return the minimum height that includes cell spacing
     */
    int lGetAdornedMinimumHeight() {
	return lGetMinimumHeight();
    }

    /**
     * Overrides ItemLFImpl's getInnerBounds so that extra padding is not
     * added
     * @param dimension <placeholder>
     * @return the inner bounds.
     */
    protected int getInnerBounds(int dimension) {
	return bounds[dimension];
    }


    /**
     * Returns the locked width of the Spacer, or -1 if it's not locked.
     * Overrides Item's getLockedWidth which returns lockedWidth plus
     * padding
     */
    protected int getLockedWidth() {
	return spacer.lockedWidth; 
    }
    
    /**
     * Returns the locked height of the Spacer, or -1 if it's not locked.
     * Overrides Item's getLockedHeight which returns lockedHeight plus
     * padding
     */
    protected int getLockedHeight() {
	return spacer.lockedHeight;
    }

    /**
     * Paint the content of this Item
     *
     * @param g the Graphics object to be used for rendering the item
     * @param w current width of the item in pixels
     * @param h current height of the item in pixels
     */
    void lCallPaint(Graphics g, int w, int h) {
        /*
         * There's no reason to erase anything because Form will erase
         * any dirty region for us
         */
    }

    /** Spacer associated with this Look & Feel */
    Spacer spacer;
}
