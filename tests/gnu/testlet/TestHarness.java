/* vim: set filetype=java shiftwidth=4 tabstop=8 autoindent cindent expandtab : */

// Copyright (c) 1998, 1999  Cygnus Solutions
// Written by Tom Tromey <tromey@cygnus.com>

// This file is part of Mauve.

// Mauve is free software; you can redistribute it and/or modify
// it under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 2, or (at your option)
// any later version.

// Mauve is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// GNU General Public License for more details.

// You should have received a copy of the GNU General Public License
// along with Mauve; see the file COPYING.  If not, write to
// the Free Software Foundation, 59 Temple Place - Suite 330,
// Boston, MA 02111-1307, USA.

package gnu.testlet;

import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;

public abstract class TestHarness {
    public abstract void check(boolean ok);
    public abstract void todo(boolean ok);
    public abstract void debug(String msg);
    public abstract void setNote(String note);

    public void checkPoint(String note) {
	setNote(note);
    }

    public void check(boolean result, boolean expected) {
	boolean ok = (result == expected);
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected (" + expected + ")");
    }

    public void check(int result, int expected) {
        boolean ok = (result == expected);
        check(ok);
        if (!ok)
            debug("got (" + result + "), expected (" + expected + ")");
    }

    public void check(long result, long expected) {
	boolean ok = (result == expected);
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected (" + expected + ")");
    }

    public void check(Object result, Object expected) {
	boolean ok = (result == null ? expected == null : (result.toString().equals(expected.toString())));
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected (" + expected + ")");
    }

    public void check(float result, float expected) {
        // This triple check overcomes the fact that == does not
        // compare NaNs, and cannot tell between 0.0 and -0.0;
        // and all without relying on java.lang.Double (which may
        // itself be buggy - else why would we be testing it? ;)
        // For 0, we switch to infinities, and for NaN, we rely
        // on the identity in JLS 15.21.1 that NaN != NaN is true.
        boolean ok = (result == expected ? (result != 0)
                      || (1 / result == 1 / expected)
                      : (result != result)
                      && (expected != expected));
        check(ok);
        if (!ok) {
            debug("got (" + result + "), expected (" + expected + ")");
        }
    }

    public void check(double result, double expected) {
        // This triple check overcomes the fact that == does not
        // compare NaNs, and cannot tell between 0.0 and -0.0;
        // and all without relying on java.lang.Double (which may
        // itself be buggy - else why would we be testing it? ;)
        // For 0, we switch to infinities, and for NaN, we rely
        // on the identity in JLS 15.21.1 that NaN != NaN is true.
        boolean ok = (result == expected ? (result != 0)
                      || (1 / result == 1 / expected)
                      : (result != result)
                      && (expected != expected));
	check(ok);
	if (!ok)
	    debug("got (" + result + "), expected (" + expected + ")");
    }

    public void check(boolean ok, String note) {
	setNote(note);
	check(ok);
    }

    public void check(boolean result, boolean expected, String note) {
	setNote(note);
	check(result, expected);
    }

    public void check(long result, long expected, String note) {
	setNote(note);
	check(result, expected);
    }
    
    public void pass() {
	check(true);
    }

    public void pass(String note) {
	check(true, note);
    }

    public void fail() {
	check(false);
    }

    public void fail(Object note) {
	check(false, "" + note);
    }

    public void todo(long result, long expected) {
        boolean ok = (result == expected);
        todo(ok);
        if (ok)
            debug("got (" + result + ")");
    }

    public void todo(Object result, Object expected) {
        boolean ok = (result.toString().equals(expected.toString()));
        todo(ok);
        if (ok)
            debug("got (" + result + ")");
    }

    public void todo(long result, long expected, String note) {
        setNote(note);
        todo(result, expected);
    }

    public void todo(boolean result, String note) {
        setNote(note);
        todo(result);
    }

    public Display getDisplay() {
        return display;
    }

    public TestHarness(Display d) {
        display = d;
    }

    public void setScreenAndWait(Displayable s) {
        display.setCurrent(s);
        while (!(s.isShown() && (display.getCurrent() == s))) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                fail("INTERRUPTED");
                break;
            }
        }
    }

    // NOTE: this only works for screens that have no native elements.
    // To compare a screen with native elements, create a tests/gfx/ test.
    public void compareScreenToReferenceImage(String referenceImagePath, int maxDifferingPixels, String message) {
        int numDifferent = getNumDifferingPixels(referenceImagePath);
        check(numDifferent <= maxDifferingPixels, message + ". " + numDifferent + " > " + maxDifferingPixels);
    }

    public native int getNumDifferingPixels(String referenceImagePath);

    private Display display;
}
