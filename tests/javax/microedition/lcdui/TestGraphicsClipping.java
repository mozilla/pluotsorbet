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

public class TestGraphicsClipping implements Testlet {

    static final int BACKGROUND_COLOR = 0xffffffff;
    int width = 10;
    int height = 10;
    int backgroundColor = 0;

    void draw(Graphics g) {
        g.setColor(0);
        g.fillRect(0, 0, width, height);
    }

    boolean verifyClip(int[] unclippedData, int[] clippedData, int cx, int cy, int cw, int ch) {
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int index = y*width + x;
                if (y >= cy && y < cy+ch && x >= cx && x < cx + cw) {
                    /* should match unclipped version when alpha is ignored */
                    if ((clippedData[index]) != (unclippedData[index])) {
                        System.out.println("FAILURE: inside clip (" + x + "," + y + ")" +
                              " clipped=" +
                              Integer.toHexString(clippedData[index]) +
                              " unclipped=" +
                              Integer.toHexString(unclippedData[index]));
                        return false;
                    }
                } else {
                    /* should be background color when alpha is ignored */
                    if ((clippedData[index]&0x00ffffff) != (backgroundColor&0xffffff)) {
                        System.out.println("FAILURE: outside clip (" + x + "," + y + ")" +
                              " clipped=" +
                              Integer.toHexString(clippedData[index]&0xffffff) +
                              " backgroundColor=" +
                              Integer.toHexString(backgroundColor&0xffffff));
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public void test(TestHarness th) {
        int[] unclippedData = new int[width*height];
        int[] clippedData = new int[width*height];

        Image image = Image.createImage(width, height);
        Graphics g = image.getGraphics();

        backgroundColor = g.getDisplayColor(BACKGROUND_COLOR);

        for (int cx = 1; cx < width; cx++) {
            for (int cy = 1; cy < height; cy++) {
                for (int cw = 1; cw < width-cx; cw++) {
                    for (int ch = 1; ch < height-cy; ch++) {
                        g.setClip(0, 0, width, height);
                        g.setColor(backgroundColor);
                        g.fillRect(0, 0, width, height);
                        draw(g);
                        image.getRGB(unclippedData, 0, width, 0, 0, width, height);

                        g.setColor(backgroundColor);
                        g.fillRect(0, 0, width, height);
                        g.setClip(cx, cy, cw, ch);
                        draw(g);
                        image.getRGB(clippedData, 0, width, 0, 0, width, height);

                        th.check(verifyClip(unclippedData, clippedData, cx, cy, cw, ch));
                    }
                }
            }
        }
    }
}
