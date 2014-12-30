package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.ConnectionClosedException;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class UsedSize extends TestCaseWithLog {

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
	}
	
	/**
	 * Tests usedSize()
	 */
	public void test0001() {
		boolean passed = false;
		try {	
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			OutputStream os = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				long usedSize = conn.usedSize();
				addOperationDesc("first usedSize() call returned " + usedSize);

				int incrementSize = 4096;
				addOperationDesc("writing " + incrementSize + " bytes to disk");
				os = conn.openOutputStream();
				os.write(new byte[incrementSize]);
				os.close();
				os = null;
	
				long usedSize2 = conn.usedSize();
				addOperationDesc("second usedSize() call returned " + usedSize2 + " (should be more than first call)");
				
				passed = usedSize2>usedSize;									
			} finally {
				if (os != null) os.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests usedSize()", passed);
	}
	
	/**
	 * Tests usedSize() on two different connections on the same root volume
	 */
	public void test0002() {
		boolean passed = false;
		try {	
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				
				long totalSize1 = conn1.totalSize();
				long totalSize2 = conn2.totalSize();
				
				addOperationDesc("usedSize() returned " + totalSize1 + " for " + conn1.getURL());
				addOperationDesc("usedSize() returned " + totalSize2 + " for " + conn2.getURL());
				
				passed = totalSize1 == totalSize2;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests usedSize() on two different connections on the same root volume", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if invoked on a closed connection
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				conn.close();

				try {
					conn.usedSize();
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
		
		assertTrueWithLog("Tests usedSize() on a closed connection", passed);
	}
	
	/**
	 * IllegalModeException thrown for connections opened in Connector.WRITE mode
	 */
	public void test0004() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			try {
				try {
					conn.usedSize();
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
	 * Tests usedSize() in Connector.READ mode
	 */
	public void test0005() {
		boolean passed = false;
		try {	
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			try {
				long totalSize = conn.usedSize();
				addOperationDesc("usedSize() call returned " + totalSize);
				
				passed = true;								
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests usedSize() in Connector.READ mode", passed);
	}

	/**
	 * Tests usedSize() on a non-existent file system
	 */
	public void test0006() {
		boolean passed = false;
		try {	
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="/TCKFileSystem/";
				conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"test", Connector.READ_WRITE);
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
					long usedSize = conn.usedSize();
					addOperationDesc("usedSize() call returned " + usedSize);
					
					passed = usedSize==-1;								
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests usedSize() on a non-existent file system", passed);
	}

	/**
	 * Tests usedSize() on a non-existent host
	 */
	public void test0007() {
		boolean passed = false;
		try {	
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="TCKBogusHost/TCKFileSystem/";
				conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"test", Connector.READ_WRITE);
			} catch (IOException ioe) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} catch (SecurityException e) {
				passed = true;
				addOperationDesc("Expected SecurityException thrown");					
			}
			
			if (conn != null) {
				addOperationDesc("Opened connection on a non existent host: " + conn.getURL());
				try {
					long usedSize = conn.usedSize();
					addOperationDesc("usedSize() call returned " + usedSize);

					passed = usedSize == -1;
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests usedSize() on a non-existent host", passed);
	}
}
