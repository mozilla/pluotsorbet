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
public class Exists extends TestCaseWithLog {

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
	 * Tests exists() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests exists() on a file", passed);
	}
	
	/**
	 * Tests exists() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests exists() on a directory", passed);
	}
	
	/**
	 * Tests exists() on a non-existent file
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests exists() on a non-existent file", passed);
	}
	
	/**
	 * Tests exists() on a non-existent directory
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests exists() on a non-existent directory", passed);
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
					conn.exists();
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
					conn.exists();
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
	 * Tests exists() in Connector.READ mode
	 */
	public void test0007() {
		boolean passed = false;
		try {	
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			FileConnection conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			try {
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				boolean exists = conn2.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==true;						
			} finally {
				conn1.close();
				conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests exists() in Connector.READ mode", passed);
	}

	/**
	 * Tests exists() on a non-existent file system
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
					boolean exists = conn.exists();
					addOperationDesc("exists() call returned " + exists);
				
					passed = exists==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests exists() on a non-existent file system", passed);
	}
	
	/**
	 * Tests exists() on a non-existent host
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
					boolean exists = conn.exists();
					addOperationDesc("exists() call returned " + exists);
				
					passed = exists==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests exists() on a non-existent host", passed);
	}		
}
