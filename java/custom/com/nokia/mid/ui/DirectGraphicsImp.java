package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.game.Sprite;

public class DirectGraphicsImp implements DirectGraphics {
    private Graphics graphics;

    public DirectGraphicsImp(Graphics g) {
        graphics = g;
    }

    public native void drawElementBorder(int x, int y, int w, int h, int borderType, boolean withFocus);
    public native void drawImage(Image image, int x, int y, int anchor, int manipulation);
    public native void drawPixels(byte pixels[], byte transparencyMask[], int offset, int scanlength, int x, int y, int width,
				  int height, int manipulation, int format);
    public native void drawPixels(int pixels[], boolean transparency, int offset, int scanlength, int x, int y, int width,
				  int height, int manipulation, int format);
    public native void drawPixels(short pixels[], boolean transparency, int offset, int scanlength, int x, int y, int width,
				  int height, int manipulation, int format);
    public native void drawPolygon(int xPoints[], int xOffset, int yPoints[], int yOffset, int nPoints, int argbColor);
    public native void drawTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);
    public native void fillPolygon(int xPoints[], int xOffset, int yPoints[], int yOffset, int nPoints, int argbColor);
    public native void fillTriangle(int x1, int y1, int x2, int y2, int x3, int y3, int argbColor);
    public native int getAlphaComponent();
    public native int getNativePixelFormat();
    public native void getPixels(byte pixels[], byte transparencyMask[], int offset, int scanlength, int x, int y, int width,
				 int height, int format);
    public native void getPixels(int pixels[], int offset, int scanlength, int x, int y, int width,
				 int height, int format);
    public native void getPixels(short pixels[], int offset, int scanlength, int x, int y, int width,
				 int height, int format);
    public native void setARGBColor(int i);
}
