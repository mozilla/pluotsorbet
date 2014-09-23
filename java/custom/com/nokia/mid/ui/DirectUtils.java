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
        // There's room for optimization here, we're creating an image
        // filling it with a blank rect (because Image::createImage(int width, int height)
        // calls ImageDataFactory::createOffScreenImageData(int width, int height), that
        // in turn calls ImageDataFactory::createMutableImageData(ImageData data, int width, int height)).
        // In the setPixels native we're ditching the canvas created in createMutableImageData and
        // we're creating a new canvas.
        Image image = Image.createImage(width, height);
        setPixels(image, argb);
        return image;
    }

    private static native void setPixels(Image image, int argb);
    private static native void makeMutable(Image image);

    public static Image createImage(byte imageData[], int imageOffset, int imageLength) {
        // There's a suggested method to create mutable images from immutable ones in
        // javax/microedition/lcdui/Image.java, but it looks pretty slower.
        Image image = Image.createImage(imageData, imageOffset, imageLength);
        makeMutable(image);
        return image;
    }

    public static Font getFont(int face, int style, int height) {
        throw new RuntimeException("DirectUtils::getFont(int,int,int) not implemented");
    }

    public static boolean setHeader(Displayable displayable, String headerText, Image headerImage, int headerTextColor, int headerBgColor, int headerDividerColor) {
        throw new RuntimeException("DirectUtils::setHeader(Displayable,String,Image,int,int,int) not implemented");
    }
}
