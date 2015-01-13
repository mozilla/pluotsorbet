package com.ibm.tck.client;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
 
/**
 * MIDP includes http so just use that as a transport layer.
 */
public class MIDPClientConnection implements ClientConnection {

	/**
	 * @see com.ibm.tck.client.ClientConnection#postData(String, byte[])
	 */
	public void postData(String url, byte[] data) {
		HttpConnection connection=null;
		try {
			connection = (HttpConnection) Connector.open(url);
			connection.setRequestMethod("POST");
		} catch (IOException e) {
			System.out.print("Exception opening connection ");
			System.out.println(e);
			return;
		} catch (SecurityException e) {
			System.out.print("SecurityException opening connection ");
			System.out.println(e);
		}
			
		OutputStream out=null;
		try {
			out = connection.openOutputStream();
			out.write(data);
		} catch (IOException e) {
			System.out.print("Exception in opening and writing data ");
			System.out.println(e);
			try {
				connection.close();
			} catch (IOException innerEx) {}
		}

		InputStream in = null; 
		try {
			in = connection.openInputStream();
		} catch (Exception e1) {
		}	
		if(in != null) { 
			byte buf[] = new byte[256];
			try {
				in.read(buf);
			} catch (Exception e1) {
				e1.printStackTrace();
			}	
			try {
				in.close();
			} catch (Exception e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			out.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		try {
			connection.close();
		} catch (Exception e1) {
			e1.printStackTrace();
		}
		
	}

}
