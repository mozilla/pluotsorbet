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

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;
import com.sun.midp.lcdui.EventConstants;

/**
 * Tests for the KeyConverter utility class.
 */
public class TestKeyConverter implements Testlet {
    TestHarness th;

    /**
     * Checks one game action.  Ensures that a keycode exists for every
     * game action defined by the specification.  Also checks that the keycode
     * maps back to the same game action.
     */
    void checkGameAction(int ga) {
        int kc = KeyConverter.getKeyCode(ga);
        th.check(kc != 0);

        int ga2 = KeyConverter.getGameAction(kc);
        th.check(ga2 != 0);
        th.check(ga2 != -1);

        th.check(ga, ga2);
    }

    /**
     * Tests existence and symmetry of every game action.  For every game
     * action, ensures that it is mapped to a keycode, and then ensures that
     * that keycode maps back to the original game action.  This test is
     * device-independent.
     */
    void testAllGameActions() {
        checkGameAction(Canvas.UP);
        checkGameAction(Canvas.DOWN);
        checkGameAction(Canvas.LEFT);
        checkGameAction(Canvas.RIGHT);
        checkGameAction(Canvas.FIRE);
        checkGameAction(Canvas.GAME_A);
        checkGameAction(Canvas.GAME_B);
        checkGameAction(Canvas.GAME_C);
        checkGameAction(Canvas.GAME_D);
    }

    /**
     * Tests getSystemKey().
     *
     * NOTE: this test includes device-dependent keycode values.
     */
    void testGetSystemKey() {
        th.check(EventConstants.SYSTEM_KEY_POWER, KeyConverter.getSystemKey(112));
        th.check(EventConstants.SYSTEM_KEY_SEND, KeyConverter.getSystemKey(116));
        th.check(EventConstants.SYSTEM_KEY_END, KeyConverter.getSystemKey(114));
        th.check(EventConstants.SYSTEM_KEY_CLEAR, KeyConverter.getSystemKey(8));
    }


    /**
     * Tests that none of the standard keys KEY_NUM0..KEY_NUM9, KEY_STAR,
     * KEY_POUND, and none of the game actions, is a system key, by
     * checking that getSystemKey returns 0 in each case.  This test is
     * probably device-independent.
     */
    void testNonSystemKey() {
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM0));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM1));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM2));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM3));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM4));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM5));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM6));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM7));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM8));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_NUM9));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_STAR));
        th.check(0, KeyConverter.getSystemKey(Canvas.KEY_POUND));

        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.UP)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.DOWN)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.LEFT)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.RIGHT)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.FIRE)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.GAME_A)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.GAME_B)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.GAME_C)));
        th.check(0, KeyConverter.getSystemKey(KeyConverter.getKeyCode(Canvas.GAME_D)));
    }

    /**
     * Tests getKeyName() for the set of numeric keys. The expected names for
     * these keys are probably device-independent.
     */
    void testGetKeyName1() {
        th.check("0", KeyConverter.getKeyName(Canvas.KEY_NUM0));
        th.check("1", KeyConverter.getKeyName(Canvas.KEY_NUM1));
        th.check("2", KeyConverter.getKeyName(Canvas.KEY_NUM2));
        th.check("3", KeyConverter.getKeyName(Canvas.KEY_NUM3));
        th.check("4", KeyConverter.getKeyName(Canvas.KEY_NUM4));
        th.check("5", KeyConverter.getKeyName(Canvas.KEY_NUM5));
        th.check("6", KeyConverter.getKeyName(Canvas.KEY_NUM6));
        th.check("7", KeyConverter.getKeyName(Canvas.KEY_NUM7));
        th.check("8", KeyConverter.getKeyName(Canvas.KEY_NUM8));
        th.check("9", KeyConverter.getKeyName(Canvas.KEY_NUM9));
        th.check("*", KeyConverter.getKeyName(Canvas.KEY_STAR));
        th.check("#", KeyConverter.getKeyName(Canvas.KEY_POUND));
    }

    /**
     * Tests getKeyName() for the set of keys mapped to game actions.
     *
     * NOTE: the expected key names are device- and language-specific.
     */
    void testGetKeyName2() {
        th.check("Up", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.UP)));
        th.check("Down", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.DOWN)));
        th.check("Left", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.LEFT)));
        th.check("Right", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.RIGHT)));
        th.check("Select", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.FIRE)));
        th.check("Calendar", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.GAME_A)));
        th.check("Addressbook", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.GAME_B)));
        th.check("Menu", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.GAME_C)));
        th.check("Mail", KeyConverter.getKeyName(KeyConverter.getKeyCode(Canvas.GAME_D)));
    }

    /**
     * Runs all tests.
     */
    public void test(TestHarness th) {
        this.th = th;
        testAllGameActions();
        testGetSystemKey();
        testNonSystemKey();
        testGetKeyName1();
        testGetKeyName2();
    }

}
