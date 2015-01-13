package com.ibm.tck.javax.microedition.io.file.FileConnection;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class PackageDiscovery extends TestCaseWithLog {

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
	}
	
	/**
	 * Tests that the microedition.io.file.FileConnection.version system property is set
	 */
	public void test0001() {
		String value = System.getProperty("microedition.io.file.FileConnection.version"); 
		addOperationDesc("microedition.io.file.FileConnection.version=" + value);
		boolean passed = value != null && value.equals("1.0");
		assertTrueWithLog("Tests that the microedition.io.file.FileConnection.version system property is set to 1.0", passed);
	}

}
