package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DirectUtils
{
    public static DirectGraphics getDirectGraphics(Graphics g)
    {
	return new DirectGraphicsImp(g);
    }

    public static native Image createImage(byte imageData[], int imageOffset, int imageLength);
    public static native Image createImage(int width, int height, int argb);
}
