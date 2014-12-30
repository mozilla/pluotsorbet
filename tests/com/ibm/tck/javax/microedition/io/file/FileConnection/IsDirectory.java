package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import javax.microedition.io.Connector;
import javax.microedition.io.file.ConnectionClosedException;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class IsDirectory extends TestCaseWithLog {

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
		if (isTestValid("test0002")) test0002();
		if (isTestValid("test0003")) test0003();
		if (isTestValid("test0004")) test0004();
		if (isTestValid("test0005")) test0005();
		if (isTestValid("test0006")) test0006();
		if (isTestValid("test0007")) test0007();
		if (isTestValid("test0008")) test0008();		
		if (isTestValid("test0009")) test0009();
	}

	/**
	 * Tests isDirectory() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				boolean isDirectory = conn.isDirectory();
				addOperationDesc("isDirectory() returned " + isDirectory);
				
				passed = isDirectory==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isDirectory() on a file", passed);
	}
	
	/**
	 * Tests isDirectory() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				boolean isDirectory = conn.isDirectory();
				addOperationDesc("isDirectory() returned " + isDirectory);
				
				passed = isDirectory==true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isDirectory() on a file", passed);
	}
	
	/**
	 * Tests isDirectory() on a non-existent file URL
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir", Connector.READ_WRITE);
			try {
			
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean isDirectory = conn.isDirectory();
				addOperationDesc("isDirectory() returned " + isDirectory);
				
				passed = isDirectory==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isDirectory() on a non-existent file URL returns false", passed);
	}
	
	/**
	 * Tests isDirectory() on a non-existent directory URL
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);

				boolean isDirectory = conn.isDirectory();
				addOperationDesc("isDirectory() returned " + isDirectory);
				
				passed = isDirectory==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isDirectory() on a non-existent directory URL returns false", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if invoked on a closed connection
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				conn.close();

				try {
					conn.isDirectory();
					addOperationDesc("No ConnectionClosedException thrown");
					passed = false;
				} catch (ConnectionClosedException e) {
					addOperationDesc("Expected ConnectionClosedException thrown");
					passed = true;
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("ConnectionClosedException thrown if invoked on a closed connection", passed);
	}

	/**
	 * IllegalModeException thrown for connections opened in Connector.WRITE mode
	 */
	public void test0006() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			try {
				try {
					conn.isDirectory();
					addOperationDesc("No IllegalModeException thrown");
					passed = false;
				} catch (IllegalModeException e) {
					passed = true;
					addOperationDesc("Expected IllegalModeException thrown");
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IllegalModeException thrown for connections opened in Connector.WRITE mode", passed);
	}		

	/**
	 * Tests isDirectory() in Connector.READ mode
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {				
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				addOperationDesc("Opening connection in READ mode");
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
				

				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				boolean isDirectory = conn2.isDirectory();
				addOperationDesc("isDirectory() returned " + isDirectory);
				
				passed = isDirectory==true;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isDirectory() in Connector.READ mode", passed);
	}

	/**
	 * Tests isDirectory() on a non-existent file system
	 */
	public void test0008() {
		boolean passed = false;
		try {	
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="/TCKFileSystem/";
				conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"testdir/", Connector.READ_WRITE);
			} catch (IOException ioe) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} catch (SecurityException e) {
				passed = true;
				addOperationDesc("Expected SecurityException thrown");					
			}
			
			if (conn != null) {
				addOperationDesc("Opened connection on a non existent file system: " + conn.getURL());
				try {
					boolean isDirectory = conn.isDirectory();
					addOperationDesc("isDirectory() call returned " + isDirectory);
				
					passed = isDirectory==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests isDirectory() on a non-existent file system", passed);
	}
	

	/**
	 * Tests isDirectory() on a non-existent host
	 */
	public void test0009() {
		boolean passed = false;
		try {	
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="TCKBogusHost/TCKFileSystem/";
				conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"testdir/", Connector.READ_WRITE);
			} catch (IOException ioe) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} catch (SecurityException e) {
				passed = true;
				addOperationDesc("Expected SecurityException thrown");					
			}
			
			if (conn != null) {
				addOperationDesc("Opened connection on a non existent file system: " + conn.getURL());
				try {
					boolean isDirectory = conn.isDirectory();
					addOperationDesc("isDirectory() call returned " + isDirectory);
				
					passed = isDirectory==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests isDirectory() on a non-existent host", passed);
	}		

}