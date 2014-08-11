package com.nokia.mid.ui;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

public class DirectUtils
{
    public static DirectGraphics getDirectGraphics(Graphics g)
    {
	return new DirectGraphicsImp(g);
    }

    public static Image createImage(int width, int height, int argb) {
	Image img = Image.createImage(width, height);
	Graphics g = img.getGraphics();
	getDirectGraphics(g).setARGBColor(argb);
	g.fillRect(0, 0, width, height);
	return img;
    }

    public static native Image createImage(byte imageData[], int imageOffset, int imageLength);
}
