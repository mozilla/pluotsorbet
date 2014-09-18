package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Displayable;

public class DirectUtils {
    public static DirectGraphics getDirectGraphics(Graphics g) {
        return new DirectGraphicsImp(g);
    }

    public static Image createImage(int width, int height, int argb) {
        int[] pixels = new int[width * height];

        int i = 0;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                pixels[i++] = argb;
            }
        }

        return makeMutable(Image.createRGBImage(pixels, width, height, true));
    }

    private static native Image makeMutable(Image image);

    public static Image createImage(byte imageData[], int imageOffset, int imageLength) {
        // There's a suggested method to create mutable images from immutable ones in
        // javax/microedition/lcdui/Image.java, but it looks pretty slower.
        return makeMutable(Image.createImage(imageData, imageOffset, imageLength));
    }

    public static Font getFont(int face, int style, int height) {
        throw new RuntimeException("DirectUtils::getFont(int,int,int) not implemented");
    }

    public static boolean setHeader(Displayable displayable, String headerText, Image headerImage, int headerTextColor, int headerBgColor, int headerDividerColor) {
        throw new RuntimeException("DirectUtils::setHeader(Displayable,String,Image,int,int,int) not implemented");
    }
}
