package com.ibm.tck.client;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.*;
import java.util.Hashtable;
import java.util.Vector;

/**
 * TestCase is the root of functionality for a Unit test. A Unit test may
 * in fact have several tests.  There is a possibility of some/all of the tests
 * being excluded therefore naming of the tests is important.
 */
public abstract class TestCase {
	private TestRunner runner;
	private String currentTestName;
	private StringBuffer testLog;
	private long startTime;
	private final static String CRLF = "\r\n";

	protected final void testRunner(TestRunner runner) {
		this.runner = runner;
	}
	
	/**
	 * The implementation of the runTests() method should invoke a
	 * series of tests. The tests should be executed like the following.
	 * <pre>
	 * <code>
	 * 		if (isTestValid("test0001"))
	 * 			test0001();
	 * 		if (isTestValid("test0002"))
	 * 			test0002();
	 * </code>
	 * </pre>
	 * Each test then makes use of ONE (1) assertTrue(String, boolean) and
	 * the reports are sent back to the server using DataOutputStreams:
	 * <pre>
	 * <code>
	 * 		String  -> com.company.subpackage.test.TestClass#test0001
	 * 		boolean -> pass or fail
	 * 		String  -> testDescription
	 * 		String  -> testLog
	 * 		long	-> elapsedTime
	 * </code>
	 * </pre>
	 * 
	 * TestCase can make use of log(String) which appends to the testLog.
	 * 
	 * TestCase can make use of interactiveMessage(String) which will force the TCK Harness
	 * to display a message to the user. When the user presses 'Continue' this method will
	 * return in the testcase and you can proceed with the test.
	 * 
	 * TestCase can make use of options() to find out what options the TCK Harness is running with.
	 */
	public abstract void runTests();
	
	/**
	 * Determines whether or not a specific test has been excluded or not. The
	 * TestCase class will take care of fully qualifying the name so you need
	 * only pass the test name.
	 * 
	 * @param testName the name of the test to check validity for
	 * @return a boolean indicating whether or not this test is valid
	 */
	public final boolean isTestValid(String testName) {
		StringBuffer fullName = new StringBuffer();
		fullName.append(this.getClass().getName());
		fullName.append("#");
		fullName.append(testName);
		currentTestName = fullName.toString();
		testLog = new StringBuffer();
		boolean result = !runner.isExcluded(currentTestName) && 
						  runner.isFiltered(currentTestName);
		startTime = System.currentTimeMillis();
		return result;
	}
	
	/**
	 * Add a message to the log during 1 test run.  A CR/LF combination is added
	 * after each logMessage.  The log is reset after each <code>isTestValid()</code>
	 * call.
	 * 
	 * @param logMessage A String of information to show during the test run
	 */
	public final void log(String logMessage) {
		if (logMessage==null) return;
		testLog.append(logMessage);
		testLog.append(CRLF);
	}
	
	/**
	 * Assertion point for deciding whether a test passes or fails.  The description
	 * should match the XML entry for the test.  The full test name, results, description
	 * and log are sent to the server.  Care should be taken to call this method once
	 * per test otherwise the server may get out of sync.
	 * 
	 * @param description A string describing the test. Should be the same as the description in the XML file.
	 * @param mustBeTrue A boolean stating whether the test passed (<code>true</code>) or failed (<code>false</code>).
	 */
	public final void assertTrue(String description, boolean mustBeTrue) {
		long elapsedTime = System.currentTimeMillis() - startTime;
		runner.postResults(currentTestName, mustBeTrue, description, testLog.toString(), elapsedTime);
	}

	/**
	 * Request for the TCK Server to display an interactive message to the user. Upon returning from
	 * this call the testcase can continue.
	 * 
	 * @param description A String to display interactively to the user operating the TCK Harness.
	 */
	public final void interactiveMessage(String message) {
		runner.postMessage(currentTestName, message);
	}
	
	/**
	 * Answer a Hashtable of options that were set at the time this testcase was generated.
	 * 
	 * @return a Hashtable of options, possibly empty but never <code>null</code>.
	 */
	public final Hashtable getOptions() {
		return runner.getOptions();
	}

	/**
	 * Answer an Object that is the MIDlet running the testcase.  If the <code>TestCase</code> makes any modification
	 * to the MIDlet they should be undone before the <code>TestCase</code> finishes. 
	 *
	 * @return an Object (or null) representing the MIDlet the TestCase is running in.
	 */
	public final Object getMidlet() {
		return runner.getMidlet();
	}

}
