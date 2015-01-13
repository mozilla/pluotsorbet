package com.ibm.tck.javax.microedition.io.file.IllegalModeException;

import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class Init extends TestCaseWithLog {

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
	}
	
	/**
	 * Tests that a message that is set can be retrieved
	 */
	public void test0001() {
		IllegalModeException e = new IllegalModeException("foo");
		assertTrueWithLog("Tests that a message that is set can be retrieved", "foo".equals(e.getMessage()));
	}

}
