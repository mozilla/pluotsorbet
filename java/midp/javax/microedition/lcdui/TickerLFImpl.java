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
 * Implementation class for <code>TickerLF</code> interface.
 */
class TickerLFImpl implements TickerLF {

    /**
     * Constructs a new <code>TickerLFImpl</code> object for a given 
     * <code>Ticker</code>.
     *
     * @param ticker <code>Ticker</code> object for which L&amp;F has 
     *               to be created
     */
    TickerLFImpl(Ticker ticker) {
	this.ticker = ticker;
    }

    /**
     * This is needed in order to delegate the string setting to
     * <code>DisplayableLFImpl</code>.
     *
     * @param owner the last <code>Displayable</code> this ticker was set to
     */
    public void lSetOwner(DisplayableLF owner) {
	this.owner = (DisplayableLFImpl)owner;
    }

    /**
     * Change the string set to this ticker.
     *
     * @param str string to set on this ticker.
     */
    public void lSetString(String str) {

	// this method should have no effect if this ticker is not owned
	// by the current displayable. This is checked by DisplayableLFImpl
	
	if (owner != null) {
	    owner.tickerTextChanged(ticker);
	}
    }

    /** 
     * <code>Ticker</code> object that corresponds to this Look &amp; Feel 
     * object.
     */
    private Ticker ticker;

    /** The last <code>Displayable</code> this ticker was set to. */
    private DisplayableLFImpl owner = null;
}
