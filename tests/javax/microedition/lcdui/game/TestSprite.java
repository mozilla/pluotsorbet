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

package javax.microedition.lcdui.game;

import gnu.testlet.TestHarness;
import gnu.testlet.Testlet;

import javax.microedition.lcdui.*;
import javax.microedition.midlet.*;

public class TestSprite implements Testlet {
    TestHarness th;

    public void test(TestHarness th) {
        this.th = th;

        testInitialSize();

        testTransformNone();

        testTransformRot90();

        testTransformRot180();

        testTransformRot270();

        testTransformMirror();

        testTransformMirrorRot90();

        testTransformMirrorRot180();

        testTransformMirrorRot270();
    }

    public void testInitialSize() {
        Image image = Image.createImage(7, 4);
        Sprite sprite = new Sprite(image);

        th.check(image.getWidth(), sprite.getWidth());
        th.check(image.getHeight(), sprite.getHeight());
    }

    public void testTransformNone() {
        testTransform(0, 0, Sprite.TRANS_NONE, 11, 19, 4, 0, 2, 3);
        testTransform(1, 2, Sprite.TRANS_NONE, 10, 17, 4, 0, 2, 3);
    }

    public void testTransformRot90() {
        testTransform(0, 0, Sprite.TRANS_ROT90, 8, 19, 1, 4, 3, 2);
        testTransform(1, 2, Sprite.TRANS_ROT90, 10, 18, 1, 4, 3, 2);
    }

    public void testTransformRot180() {
        testTransform(0, 0, Sprite.TRANS_ROT180, 5, 16, 1, 1, 2, 3);
        testTransform(1, 2, Sprite.TRANS_ROT180, 6, 18, 1, 1, 2, 3);
    }

    public void testTransformRot270() {
        testTransform(0, 0, Sprite.TRANS_ROT270, 11, 13, 0, 1, 3, 2);
        testTransform(1, 2, Sprite.TRANS_ROT270, 9, 14, 0, 1, 3, 2);
    }

    public void testTransformMirror() {
        testTransform(0, 0, Sprite.TRANS_MIRROR, 5, 19, 1, 0, 2, 3);
        testTransform(1, 2, Sprite.TRANS_MIRROR, 6, 17, 1, 0, 2, 3);
    }

    public void testTransformMirrorRot90() {
        testTransform(0, 0, Sprite.TRANS_MIRROR_ROT90, 8, 13, 1, 1, 3, 2);
        testTransform(1, 2, Sprite.TRANS_MIRROR_ROT90, 10, 14, 1, 1, 3, 2);
    }

    public void testTransformMirrorRot180() {
        testTransform(0, 0, Sprite.TRANS_MIRROR_ROT180, 11, 16, 4, 1, 2, 3);
        testTransform(1, 2, Sprite.TRANS_MIRROR_ROT180, 10, 18, 4, 1, 2, 3);
    }

    public void testTransformMirrorRot270() {
        testTransform(0, 0, Sprite.TRANS_MIRROR_ROT270, 11, 19, 0, 4, 3, 2);
        testTransform(1, 2, Sprite.TRANS_MIRROR_ROT270, 9, 18, 0, 4, 3, 2);
    }

    private void testTransform(int refX, int refY, int transform,
            int expectedX, int expectedY, int expColRectX, int expColRectY,
            int expColRectWidth, int expColRectHeight) {

        Sprite sprite = new Sprite(Image.createImage(7, 4));

        sprite.defineReferencePixel(refX, refY);
        sprite.setRefPixelPosition(11, 19);
        sprite.defineCollisionRectangle(4, 0, 2, 3);
        sprite.setTransform(transform);

        th.check(4, sprite.collisionRectX);
        th.check(0, sprite.collisionRectY);
        th.check(2, sprite.collisionRectWidth);
        th.check(3, sprite.collisionRectHeight);

        th.check(expectedX, sprite.getX());
        th.check(expectedY, sprite.getY());
        th.check(expColRectX, sprite.t_collisionRectX);
        th.check(expColRectY, sprite.t_collisionRectY);
        th.check(expColRectWidth, sprite.t_collisionRectWidth);
        th.check(expColRectHeight, sprite.t_collisionRectHeight);
    }
}
