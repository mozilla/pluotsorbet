package com.ibm.tck.client;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

import java.io.*;
import java.util.*;

/**
 * The main entry point for all tests.
 * TestRunner has the following sequence.<br>
 * 	<UL>
 * 	<LI>invoked with main(), if the first arg is -noserver, simply run tests and write to stdout (useful for debugging)</LI>
 *  <LI>load 'testres' resource file (In DataOutputStream format)</LI>
 *  <LI>load client communication class name</LI>
 *  <LI>load test server URL for results</LI>
 *  <LI>load test server URL for interactive tests</LI>
 *  <LI>load number of tests</LI>
 *  <LI>load each test name and store</LI>
 *  <LI>load number of excludes</LI>
 *  <LI>load each exclude test name and store</LI>
 *  <LI>load all options set for this test run</LI>
 *  <LI>close resource file</LI>
 *  <LI>for each test name
 *  <UL>
 * 		<LI>lookup class and instantiate</LI>
 * 		<LI>call runTests()</LI>
 * 		<LI>runTests() runs each test in a test checking first to see if it excluded</LI>
 * 		<LI>results are sent back to the server (if not -noserver)</LI>
 *  </UL>
 *  <LI>done</LI>
 *  </UL>
 * One difference is that if the TestRunner is invoked by the TCKMidlet, an object
 * representing the MIDlet is set in the runner.  Any <code>TestCase</code> can
 * cast the result of <code>getMidlet()</code> to a MIDlet if they know they
 * are running on MIDP.
 *  
 */
public final class TestRunner {
	private Object midlet;
	private String serverURL;
	private String interactiveURL;
	private String clientClass;
	private Vector excludes;
	private Hashtable options = new Hashtable();
	private Vector testClasses;
	private Vector filters;
	static private boolean noserver = false;
	private ByteArrayOutputStream testResults;
	private ByteArrayOutputStream output;
	private ClientConnection clientConnection;
	private int _passed, _failed, _excluded;

	/**
	 * main() should be invoded with no arguments.  If -noserver is passed as the first argument, no attempt
	 * is made to send the results back to a server.  This is useful for testing individual
	 * tests.
	 * 
	 * @param args string array of args but only supported option is -noserver indicating whether or not to send results back to server.
	 */
	public static void main(String[] args) {
		if (args.length > 0) {
			if (args[0].toLowerCase().equals("-noserver"))
				noserver = true;
			else {
				System.out.println("Only option is [-noserver]");
			}
		}
		new TestRunner();
	}
		
	
	/**
	 * Constructor for TestRunner.
	 */
	public TestRunner() {
		excludes = new Vector(1);
		testClasses = new Vector(1);
		filters = new Vector(1);
		testResults = new ByteArrayOutputStream();
		output = new ByteArrayOutputStream();
		run();
	}
	
	/**
	 * Constructor for TestRunner.
	 * 
	 * @param midlet An object representing the MIDlet the TestRunner is running on.
	 */
	public TestRunner(Object midlet) {
		excludes = new Vector(1);
		testClasses = new Vector(1);
		filters = new Vector(1);
		testResults = new ByteArrayOutputStream();
		output = new ByteArrayOutputStream();
		this.midlet = midlet;
		run();
	}

	
	/**
	 * Answer a boolean if testName, comprised of a full class name and test description,
	 * is excluded.
	 * 
	 * @param testName the fully qualified name
	 * @return a boolean indicating whether or not a testName is excluded
	 */
	public boolean isExcluded(String testName) {
		boolean result = excludes.contains(testName);
		if (result) _excluded++;
		return result;
	}
	
	/**
	 * Answer a boolean if testName, comprised of a full class name and test description,
	 * is included based on current filter settings.
	 * 
	 * @param testName the fully qualified name
	 * @return boolean indicating whether or not a testName is filtered
	 */
	public boolean isFiltered(String testName) {
		if(filters.size() == 0)
			return true;
		for(int i = 0; i<filters.size(); i++) {
			if(testName.indexOf((String)filters.elementAt(i)) != -1)
				return true;	
		}
		return false;
	}

	protected void postResults(String testName, boolean passed, String description, String testLog, long elapsedTime) {
		System.out.print(testName);
		if (passed)
			_passed++;
		else
			_failed++;
		System.out.print(passed ? " passed (" : " failed (");
		System.out.print(description);
		System.out.println(")");
		if (testLog.length() > 0) {
			System.out.print("TESTLOG: ");
			System.out.println(testLog);
		}
		if (!noserver) {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dataOutput = new DataOutputStream(baos);
				dataOutput.writeUTF(testName);
				dataOutput.writeBoolean(passed);
				dataOutput.writeUTF(description);
				dataOutput.writeUTF(testLog);
				dataOutput.writeLong(elapsedTime);
				dataOutput.close();
				clientConnection.postData(serverURL, baos.toByteArray());
			} catch (Exception e) {
				System.out.println("Error in assertTrue()");
				System.out.println("Exception " + e);
			}
		}
	}
	
	protected Hashtable getOptions() {
		return options;
	}
	
	protected Object getMidlet() {
		return midlet;
	}

	protected void postMessage(String testName, String description) {
		System.out.print(testName);
		System.out.println(" Interactive request:");
		System.out.println(description);
		if (noserver) {
			System.out.println("Error, there is no server specified");
			return;
		} else {
			try {
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				DataOutputStream dataOutput = new DataOutputStream(baos);
				dataOutput.writeUTF(testName);
				dataOutput.writeUTF(description);
				dataOutput.close();
				clientConnection.postData(interactiveURL, baos.toByteArray());
			} catch (Exception e) {
				System.out.println("Error in assertTrue()");
				System.out.println("Exception " + e);
			}
		}
	}

	private void run() {
		// if (!loadTestResources()) return;
		loadHardcodedTestResources();
		if (!noserver) {
			try {
				Class clazz = Class.forName(clientClass);
				clientConnection = (ClientConnection) clazz.newInstance();
			} catch (Throwable e) {
				System.out.println("Connection class " + clientClass + " cannot be instantiated");
				return;
			}
		}
		
		Enumeration e = testClasses.elements();
		while (e.hasMoreElements()) {
			String className = (String) e.nextElement();
			Class testClass=null;
			try {
				testClass = Class.forName(className);
				TestCase tests = (TestCase) testClass.newInstance();
				tests.testRunner(this);
				tests.runTests();
			} catch (Exception testException) {
				System.out.println("Exception running test " + className);
				System.out.println(testException);
				return;
			}
		}
		StringBuffer results = new StringBuffer();
		results.append(_passed + _failed + _excluded);
		results.append(" tests, ");
		results.append(_passed);
		results.append(" passed, ");
		results.append(_excluded);
		results.append(" excluded, ");
		results.append(_failed);
		results.append(" failed\r\n");
		if (_failed == 0)
			results.append("All Tests Passed");
		else
			results.append("There were test failures");
		System.out.println(results);
	}
	
	private boolean loadTestResources() {
		InputStream res = this.getClass().getResourceAsStream("/testres");
		if (res == null) {
			System.out.println("Could not find TestRunner resource file, exiting...");
			return false;
		}
		// Any exceptions result in test failure
		try {
			DataInputStream dis = new DataInputStream(res);
			// Get the Client Class to use as a connection
			clientClass = dis.readUTF();
			// Get the server URL to write results to
			serverURL = dis.readUTF();
			// Get the server URL for interactive tests
			interactiveURL = dis.readUTF();
			// Get the number of tests classes
			int testClassCount = dis.readInt();
			// Load all the testClass names
			while (testClassCount-- > 0) {
				testClasses.addElement(dis.readUTF());
			}
			int excludeCount = dis.readInt();
			// Load all the testClass names
			while (excludeCount-- > 0) {
				excludes.addElement(dis.readUTF());
			}
			// Load the number of options
			int optionCount = dis.readInt();
			while (optionCount-- > 0) {
				String key = dis.readUTF();
				String value = dis.readUTF();
				options.put(key, value);
			}
			//Load the test filters
			int filterCount = dis.readInt();
			while(filterCount-- > 0) {
				filters.addElement(dis.readUTF());	
			}
			res.close();
		} catch (IOException e) {
			System.out.println("Resource file contains errors, exiting...");
			return false;
		}
		return true;
	}

	private void loadHardcodedTestResources() {
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.ConnectionClosedException.Init");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.AvailableSize");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanRead");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanWrite");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Create");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Delete");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.DirectorySize");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Exists");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.FileSize");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.GetName");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.GetPath");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.GetURL");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsDirectory");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsHidden");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.LastModified");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.List");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Mkdir");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Open");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.OpenDataInputStream");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.OpenDataOutputStream");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.OpenInputStream");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.OpenOutputStream");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.OpenOutputStream_Long");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.PackageDiscovery");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Rename");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetFileConnection");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetReadable");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetWritable");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.TotalSize");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.Truncate");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.UsedSize");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemListener.RootChanged");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.AddFileSystemListener");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.ListRoots");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.RemoveFileSystemListener");
		testClasses.addElement("com.ibm.tck.javax.microedition.io.file.IllegalModeException.Init");

		// Exclude these methods because they rely on functionality
		// that we haven't yet implemented.  We should implement it!
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.AvailableSize#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.AvailableSize#test0006");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.AvailableSize#test0007");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanRead#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanRead#test0004");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanWrite#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.CanWrite#test0004");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.DirectorySize#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.DirectorySize#test0003");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.DirectorySize#test0004");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsHidden#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsHidden#test0003");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsHidden#test0007");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.IsHidden#test0008");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.LastModified#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.LastModified#test0004");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.List#test0003");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.List#test0003");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.List#test0006");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.List#test0006");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0005");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0006");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0008");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0009");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0010");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0011");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0012");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetHidden#test0013");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetReadable#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetReadable#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetWritable#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.SetWritable#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.TotalSize#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.TotalSize#test0003");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileConnection.UsedSize#test0001");

		// Exclude these methods because they rely on the server-based
		// interactive mode of the test runner, which we don't support.
		// We should figure out how to make them non-interactive!
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.AddFileSystemListener#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.ListRoots#test0001");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.ListRoots#test0002");
		excludes.addElement("com.ibm.tck.javax.microedition.io.file.FileSystemRegistry.RemoveFileSystemListener#test0001");

		options.put("FilesystemTestPath", "//tcktestdir/");
	}

}
