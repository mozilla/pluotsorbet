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
import com.sun.midp.configurator.Constants;

public class TestStringItemNoLabelSizing implements Testlet {
    TestHarness th;

    private final int ITEM_PAD    = 4;
    private final int I_W_TEXT    = 3;
    private final int TEXT_FONT_H = 15;

    // make a string with count repetitions of ch
    private String makeString(int count, char ch) {
        char arr[] = new char[count];
        for (int i = 0; i < count; i++) {
            arr[i] = ch;
        }
        return new String(arr);
    }

    private String makeStringThatFits(char ch, int char_width, int width) {
        char arr[] = new char[width/char_width];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = ch;
        }
        return new String(arr);
    }

    private int getW(StringItem strItem) {
        return strItem.stringItemLF.lGetPreferredWidth(-1);
    }

    private int getH(StringItem strItem) {
        return strItem.stringItemLF.lGetPreferredHeight(-1);
    }

    private void checkSizes(StringItem si, int expectedW, int expectedH) {
        int w = getW(si);
        int h = getH(si);
        if (expectedW != 0) {
            th.todo(expectedW, w);
            th.todo(expectedH, h);
        } else {
            th.check(expectedW, w);
            th.check(expectedH, h);
        }
    }

    private void checkNonemptySizes(String content,
                                    int expectedConW,
                                    int expectedConH) {
        checkSizes(new StringItem(null, content),
                   ITEM_PAD + expectedConW + ITEM_PAD,
                   ITEM_PAD + expectedConH + ITEM_PAD);
    }

    public void test(TestHarness th) {
        this.th = th;

        // null content
        checkSizes(new StringItem(null, null), 0, 0);

        // empty content
        checkSizes(new StringItem(null, ""), 0, 0);

        // nonemtpy content, no newline,fits on one line
        checkNonemptySizes("i", I_W_TEXT, TEXT_FONT_H);

        // nonempty content, no newline, just fits on one line
        checkNonemptySizes(makeString(70, 'i'),
                           70*I_W_TEXT,
                           TEXT_FONT_H);

        // nonempty content, has newline => multiline
        // content consists just of "\n" (width is 0 but height is not)
        checkNonemptySizes("\n", 0, 2*TEXT_FONT_H);

        // nonempty content, has newline => multiline
        // content consists of "\ni" (first line is empty)
        checkNonemptySizes("\ni", I_W_TEXT, 2*TEXT_FONT_H);

        // nonempty content, has newline => multiline
        // content consists of "i\n"  (second line is empty)
        checkNonemptySizes("i\n", I_W_TEXT, 2*TEXT_FONT_H);

        // nonempty content, has newline => multiline
        // content consists of "i\nii" (second line is longer)
        checkNonemptySizes("i\nii", 2*I_W_TEXT, 2*TEXT_FONT_H);

        // nonempty content, has newline => multiline
        // content consists of "ii\ni" (first line is longer)
        checkNonemptySizes("ii\ni", 2*I_W_TEXT, 2*TEXT_FONT_H);


        // nonempty very long content without newline which has to wrap
        String strFitsInWidth =
            makeStringThatFits('i', I_W_TEXT,
                               Constants.NORMALWIDTH -
                               Constants.VERT_SCROLLBAR_WIDTH -
                               ITEM_PAD - ITEM_PAD);

        checkNonemptySizes(strFitsInWidth + "ii",
                           strFitsInWidth.length()*I_W_TEXT,
                           2*TEXT_FONT_H);
    }
}
