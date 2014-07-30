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
 * Implements a &quot;ticker-tape&quot;, a piece of text that runs
 * continuously across the display. The direction and speed of scrolling are
 * determined by the implementation. While animating, the ticker string
 * scrolls continuously. That is, when the string finishes scrolling off the
 * display, the ticker starts over at the beginning of the string. 
 *
 * <p> There is no API provided for starting and stopping the ticker. The
 * application model is that the ticker is always scrolling continuously.
 * However, the implementation is allowed to pause the scrolling for power
 * consumption purposes, for example, if the user doesn't interact with the
 * device for a certain period of time. The implementation should resume
 * scrolling the ticker when the user interacts with the device again. </p>
 *
 * <p>The text of the ticker may contain
 * <A HREF="Form.html#linebreak">line breaks</A>.
 * The complete text MUST be displayed in the ticker;
 * line break characters should not be displayed but may be used 
 * as separators. </p>
 * 
 * <p> The same ticker may be shared by several <code>Displayable</code>
 * objects (&quot;screens&quot;). This can be accomplished by calling
 * {@link Displayable#setTicker setTicker()} on each of them.
 * Typical usage is for an application to place the same ticker on
 * all of its screens. When the application switches between two screens that
 * have the same ticker, a desirable effect is for the ticker to be displayed
 * at the same location on the display and to continue scrolling its contents
 * at the same position. This gives the illusion of the ticker being attached
 * to the display instead of to each screen. </p>
 *
 * <p> An alternative usage model is for the application to use different
 * tickers on different sets of screens or even a different one on each
 * screen. The ticker is an attribute of the <code>Displayable</code> class
 * so that
 * applications may implement this model without having to update the ticker
 * to be displayed as the user switches among screens. </p>
 * @since MIDP 1.0
 */

public class Ticker {

    /**
     * Constructs a new <code>Ticker</code> object, given its initial
     * contents string.
     * @param str string to be set for the <code>Ticker</code>
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     */
    public Ticker(String str) {

	if (str == null) {
	    throw new NullPointerException();
	}

        synchronized (Display.LCDUILock) {
	    message = str;
	    displayedMessage = str.trim().replace('\n', ' ');
	    tickerLF = LFFactory.getFactory().getTickerLF(this);
        }
    }

    /**
     * Sets the string to be displayed by this ticker. If this ticker is active
     * and is on the display, it immediately begins showing the new string.
     * @param str string to be set for the <code>Ticker</code>
     * @throws NullPointerException if <code>str</code> is <code>null</code>
     * @see #getString
     */
    public void setString(String str) {

	    if (str == null) {
	        throw new NullPointerException();
	    }

        if (str.equals(message)) {
             return;
        }

        synchronized (Display.LCDUILock) {
	    // Save the original unmodified message so that getString() 
	    // returns that.
	    message = str;

	    // According to the spec, linebreak characters should 
	    // not be displayed in the ticker and could be used as 
	    // separators. We will use a single white space as the
	    // separator.

	    displayedMessage = str.trim().replace('\n', ' ');
	    tickerLF.lSetString(displayedMessage);
        }
    }

    /**
     * Gets the string currently being scrolled by the ticker.
     * @return string of the ticker
     * @see #setString
     */
    public String getString() {
        // SYNC NOTE: return of atomic value, no locking necessary
        return message;
    }

    /** The message set in this Ticker */
    private String message;
    
    /** 
     * The message being displayed in this Ticker. #getString() will
     * only return what is stored in "message" and not in "displayedMessage",
     * which is only used for display purposes.
     */
    String displayedMessage;

    /** Look and Feel corresponding to this Ticker */
    TickerLF tickerLF;
}
