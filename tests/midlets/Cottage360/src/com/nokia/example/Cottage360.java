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

import javax.microedition.io.Connector;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Image;
import javax.microedition.midlet.MIDlet;
import javax.microedition.midlet.MIDletStateChangeException;
import javax.microedition.sensor.ChannelInfo;
import javax.microedition.sensor.Data;
import javax.microedition.sensor.DataListener;
import javax.microedition.sensor.SensorConnection;
import javax.microedition.sensor.SensorInfo;
import javax.microedition.sensor.SensorManager;

import com.nokia.mid.ui.DeviceControl;



public class Cottage360 extends MIDlet implements DataListener, PanoramaCanvas.ExitIf {

	static final int BUFFER_SIZE = 3;
	private static PanoramaCanvas iCanvas;
	private static SensorConnection iConnection;
	private static final String PHOTO_NAME = "/midlets/Cottage360/res/cottage360.jpg";
	public Cottage360()
	{
	}

	public void dataReceived( SensorConnection con, Data[] aData, boolean aMissed)
	{
		iCanvas.setPosition(aData);
	}

	public void exit()
	{
		try
		{
			destroyApp( true );
		}
		catch (MIDletStateChangeException e)
		{
			e.printStackTrace();
		}
		notifyDestroyed();
	}

	protected void destroyApp(boolean arg0) throws MIDletStateChangeException
	{
		try
		{
			if (iConnection!=null)
				iConnection.close();
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	protected void pauseApp()
	{
	}

	protected void startApp() throws MIDletStateChangeException
	{
		DeviceControl.setLights( 0, 100 );
		Display disp = Display.getDisplay( this );
		try
		{
			iCanvas = new PanoramaCanvas( Image.createImage(PHOTO_NAME), this );
			disp.setCurrent( iCanvas );
			iConnection = openAccelerationSensor();
			if (iConnection != null)
				iConnection.setDataListener( this, BUFFER_SIZE );
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}

	private SensorConnection openAccelerationSensor(){

		SensorInfo[] infos = SensorManager.findSensors("acceleration", null);
		if (infos.length==0) return null;

		// INT data type is preferred
		int i=0;
		for (i=0; i<infos.length && infos[i].getChannelInfos()[0].getDataType()!=ChannelInfo.TYPE_INT; i++);

		try{
			return i==infos.length ? (SensorConnection)Connector.open(infos[0].getUrl()):
				(SensorConnection)Connector.open(infos[i].getUrl());
		}catch (Exception e) {
			return null;
		}
	}
}
