/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation.
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners.
 * See LICENSE.TXT for license information.
 */

package com.nokia.example;


import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.sensor.Data;

import com.nokia.mid.ui.DeviceControl;


public class PanoramaCanvas extends Canvas
{

	private static final int STEP = 5;
	private static ExitIf iExit;
	private static Image iImage;
	private static int iImageHeight;
	private static int iImageWidth;
	private static int x = 0;
	private static int y = 0;
	private static int ii = 0;

	public PanoramaCanvas(  Image aImage, ExitIf aExit) throws IOException
	{
		super();
		setFullScreenMode( true );

		iExit = aExit;
		iImage = aImage;
		iImageHeight = iImage.getHeight();
		iImageWidth = iImage.getWidth();
		y=-(iImageHeight-getHeight())/2;

		repaint();
	}

	void setPosition(Data[] aData){

		if (ii++%100==0){
			DeviceControl.setLights(0, 100);   //to keep backlight on
			setFullScreenMode( true );
		}

		x = getX(x+=getX(aData));
		y = getY(y+=getY(aData));

		repaint();
	}

	/**
	 * deal with any key presses
	 */
	protected void keyPressed(int keyCode) {
		switch(keyCode){
		case -1:	//up
		case Canvas.UP:
			y = getY(y+=STEP);
			break;
		case -2: //down
		case Canvas.DOWN:
			y = getY(y+=-STEP);
			break;
		case -3:	//left
		case Canvas.LEFT:
			x = getX(x+=STEP);
			break;
		case -4: //right
		case Canvas.RIGHT:
			x = getX(x+=-STEP);
			break;
		default:
		}
		repaint();

	}


	protected void keyRepeated(int keyCode){
		keyPressed(keyCode);

	}


	protected void paint( Graphics g ){
            g.setColor(0, 0, 0);
            g.fillRect(0, 0, getWidth(), getHeight());
		g.drawImage( iImage, x , y, Graphics.TOP | Graphics.LEFT );
		if( x + iImageWidth - getWidth() < 0 )
			g.drawImage( iImage, x + iImageWidth, y, Graphics.TOP | Graphics.LEFT );

	}

	protected void pointerPressed(int x, int y) {iExit.exit();}

	protected void sizeChanged(int w, int h) {
		y=-(iImageHeight-getHeight())/ 2;
		repaint();
	}


	private int getX(Data[] aData){
		int x_axis = 0;
		boolean isPortrait = getHeight()>getWidth();
		int index= isPortrait? 0: 1;

		try{
			for (int i=0; i<3; i++){
				x_axis += aData[index].getIntValues()[0];
			}
			x_axis = (int)(x_axis/3);
		}catch (IllegalStateException e) {
			for (int i=0; i<3; i++){
				x_axis += (int)aData[index].getDoubleValues()[0];
			}
			x_axis = (int)(x_axis/3);
		}

		return isPortrait?-x_axis%iImageWidth:x_axis%iImageWidth;
	}



	private int getY(Data[] aData){
		int y_axis = 0;
		boolean isPortrait = getHeight()>getWidth();
		int index= isPortrait? 1: 0;

		try{
			for (int i=0; i<3; i++){
				y_axis += aData[index].getIntValues()[0];
			}
			y_axis = (int)(y_axis/3);
		}catch (IllegalStateException e) {
			for (int i=0; i<3; i++){
				y_axis += (int)aData[index].getDoubleValues()[0];
			}
			y_axis = (int)(y_axis/3);
		}


		return y_axis%iImageHeight;
	}

	private int getX(int x){
		x = x>0?-iImageWidth+x:x;
		return x % iImageWidth;
	}

	private int getY(int y){
		y = y>0?0:y;	// upper limit

                //This is to center the image if the screen is higher than the image.
                if (iImageHeight<getHeight())
                    return -(iImageHeight-getHeight())/2;

		return Math.abs(y)>iImageHeight-getHeight()?(getHeight()-iImageHeight):y;	//bottom limit
	}



	static interface ExitIf
	{
		public void exit();
	};

}
