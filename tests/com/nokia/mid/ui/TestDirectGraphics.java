package com.nokia.mid.ui;

import javax.microedition.lcdui.*;
import gnu.testlet.Testlet;
import gnu.testlet.TestHarness;

public class TestDirectGraphics implements Testlet {
    public void test(TestHarness th) {
        int[] pixels = new int[1];
        Image image = DirectUtils.createImage(1, 1, 0x0000FF00);
        image.getRGB(pixels, 0, 1, 0, 0, 1, 1);
        th.todo(pixels[0], 0x0000FF00);

        short[] shortPixels = new short[1];

        image = DirectUtils.createImage(1, 1, 0xFFFF0000);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_4444_ARGB);
        th.check((short)shortPixels[0], (short)0xFF00);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_565_RGB);
        th.check((short)shortPixels[0], (short)0xF800);

        image = DirectUtils.createImage(1, 1, 0xFF00FF00);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_4444_ARGB);
        th.check((short)shortPixels[0], (short)0xF0F0);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_565_RGB);
        th.check((short)shortPixels[0], (short)0x7E0);

        image = DirectUtils.createImage(1, 1, 0xFF0000FF);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_4444_ARGB);
        th.check((short)shortPixels[0], (short)0xF00F);
        DirectUtils.getDirectGraphics(image.getGraphics()).getPixels(shortPixels, 0, 1, 0, 0, 1, 1, DirectGraphics.TYPE_USHORT_565_RGB);
        th.check((short)shortPixels[0], (short)0x1F);
    }
}

