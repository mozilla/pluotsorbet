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
public class Truncate extends TestCaseWithLog {

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
		if (isTestValid("test0010")) test0010();		
		if (isTestValid("test0011")) test0011();	
	}

	/**
	 * Tests truncate()
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			OutputStream os = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Writing 8 bytes");
				os = conn.openOutputStream();
				os.write(new byte[8]);
				os.close();
				os = null;
				
				addOperationDesc("Truncating at byte 1");
				conn.truncate(1);
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==1;
			} finally {
				if (os != null) os.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests truncate()", passed);
	}

	/**
	 * Tests truncate() where byteOffset is greater than the current end of file
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			OutputStream os = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Writing 8 bytes");
				os = conn.openOutputStream();
				os.write(new byte[8]);
				os.close();
				os = null;
				
				addOperationDesc("Truncating at byte 10");
				conn.truncate(10);
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==8;
			} finally {
				if (os != null) os.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests truncate() where byteOffset is greater than the current end of file", passed);
	}
	
	/**
	 * IOException thrown if invoked on a directory
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				try {
					conn.truncate(0);
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if invoked on a directory", passed);
	}
	
	/**
	 * IOException thrown if the file does not exist
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);

				try {
					conn.truncate(0);
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests truncate()", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if connection is closed
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				conn.close();
				try {
					conn.truncate(0);
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
		
		assertTrueWithLog("ConnectionClosedException thrown if connection is closed", passed);
	}
	
	/**
	 * IllegalModeException thrown for connections opened in Connector.READ mode
	 */
	public void test0006() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			
			try {
				try {
					conn.truncate(0);
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
	 * IllegalArgumentException thrown if byteOffset is negative
	 */
	public void test0007() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			
			addOperationDesc("Creating file: " + conn.getURL());
			ensureFileExists(conn);
			
			try {
				try {
					conn.truncate(-1);
					addOperationDesc("No IllegalArgumentException thrown");
					passed = false;
				} catch (IllegalArgumentException e) {
					passed = true;
					addOperationDesc("Expected IllegalArgumentException thrown");
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
	 * Tests truncate() in Connector.WRITE mode
	 */
	public void test0008() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = null;
			FileConnection conn2 = null;
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("Creating file");
				conn.create();
				
				boolean exists = conn2.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==true;
			} finally {
				if (conn != null) conn.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests truncate() in Connector.WRITE mode", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="/TCKFileSystem/";
				try {
					conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"test", Connector.READ_WRITE);
					addOperationDesc("Opened connection on a non existent file system: " + conn.getURL());
				} catch (SecurityException e) {
					passed = true;
					addOperationDesc("Expected SecurityException thrown");					
				}				

				if (conn!=null) {
					conn.truncate(0);
					addOperationDesc("No IOException thrown");
					passed = false;
				}
			} catch (IOException e) {
				passed = true;
				addOperationDesc("Expected IOException thrown");
			} finally {
				if (conn!=null) conn.close();
			}
			
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent file system", passed);
	}	

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent host
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn = null;
			try {
				String possibleNonExistentFilesystem ="TCKBogusHost/TCKFileSystem/";
				try {
					conn = (FileConnection)Connector.open("file://"+possibleNonExistentFilesystem+"test", Connector.READ_WRITE);
					addOperationDesc("Opened connection on a non existent host: " + conn.getURL());
				} catch (SecurityException e) {
					passed = true;
					addOperationDesc("Expected SecurityException thrown");					
				}

				if (conn!=null) {
					conn.truncate(0);
					addOperationDesc("No IOException thrown");
					passed = false;
				}
			} catch (IOException e) {
				passed = true;
				addOperationDesc("Expected IOException thrown");
			} finally {
				if (conn!=null) conn.close();
			}
			
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent host", passed);
	}

	/**
	 * Tests if a file is truncated properly even if an open outputstream is not explicitly flushed by the user 
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			OutputStream os = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Writing 8 bytes");
				os = conn.openOutputStream();
				os.write(new byte[8]);				
									
				addOperationDesc("Truncating at byte 7");
				conn.truncate(7);
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==7;
			} finally {
				if (os != null) os.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests if a file is truncated properly even if an open outputstream is not explicitly flushed by the user", passed);
	}
}
