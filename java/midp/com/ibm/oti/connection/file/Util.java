package com.ibm.oti.connection.file;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

public class Util {

	public static void init() {
		// This is commented out because System::loadLibrary is just used to load
		// external libraries implementing natives and it isn't in the J2ME System
		// class.  And we don't warn about it because we're never going to need
		// to implement it.

		/*try {
			System.loadLibrary("fileconn");
		} catch (UnsatisfiedLinkError err) {}*/
	}
	
	static String getSeparator (){
		return System.getProperty("file.separator");
	}

}
