package com.ibm.oti.connection.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

public class Util {

	public static void init() {
		System.out.println("Util.init()V commented out");
		/*try {
			System.loadLibrary("fileconn");
		} catch (UnsatisfiedLinkError err) {}*/
	}
	
	static String getSeparator (){
		return System.getProperty("file.separator");
	}

}
