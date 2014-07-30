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

import com.sun.midp.configurator.Constants;

/**
 * Implementation class for TickerLF interface.
 */
class TickerLFImpl implements TickerLF {

    /**
     * Constructs a new <code>Ticker</code> object, given its initial
     * contents string.
     * @param ticker Ticker object for which L&F has to be created
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    TickerLFImpl(Ticker ticker) {
	    this.ticker = ticker;
    }

    /**
     * needed for implementations where the ticker is operated through the
     * Displayable directly
     * @param owner the last Displayable this ticker was set to
     */
    public void lSetOwner(DisplayableLF owner) {
        this.owner = owner;
    }
    
    /**
     * change the string set to this ticker
     * @param str string to set on this ticker.
     */
    public void lSetString(String str) {
        if (owner != null) {
            Display d = owner.lGetCurrentDisplay();
            if (d != null) {
                d.lSetTicker(owner, ticker);
            }
        }
    }

    /** DisplayableLF this ticker is associated with */
    private DisplayableLF owner;
    
    /** Ticker object that corresponds to this Look & Feel object */
    private Ticker ticker;
}

