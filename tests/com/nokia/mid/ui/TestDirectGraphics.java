package com.nokia.mid.ui;

import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestDirectGraphics implements Testlet {
    public void test(TestHarness th) {
        int[] pixels = new int[1];
        DirectUtils.createImage(1, 1, 0x0000FF00).getRGB(pixels, 0, 1, 0, 0, 1, 1);
        th.todo(pixels[0], 0x0000FF00);
    }
}

