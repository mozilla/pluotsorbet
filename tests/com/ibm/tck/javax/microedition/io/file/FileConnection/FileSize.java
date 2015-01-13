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
public class FileSize extends TestCaseWithLog {

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
	 * Tests fileSize() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {				
				addOperationDesc("Creating file with a size of 64 bytes: " + conn.getURL());
				ensureFileExists(conn);
				ensureFileSize(conn, 64);
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==64;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests fileSize() on a file", passed);
	}
	
	/**
	 * Tests fileSize() on a file with an open OutputStream
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			OutputStream os =null;
			try {				
				addOperationDesc("Creating file with a size of 0 bytes: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening output stream on file");
				os = conn.openOutputStream();
				addOperationDesc("Writing and flushing 64 bytes to file");
				os.write(new byte[64]);
				os.flush();
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
			
				passed = fileSize==64;
			} finally {
				if (os!=null) os.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests fileSize() on a file with an open OutputStream", passed);
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
					conn.fileSize();
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
	 * IOException thrown if invoked on a directory
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory" + conn.getURL());
				ensureDirExists(conn);
			
				try {
					addOperationDesc("Attempting to call fileSize() on a directory");
					conn.fileSize();
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
		
		assertTrueWithLog("IOException thrown if invoked on a file", passed);
	}
	
	/**
	 * IllegalModeException thrown for connections opened in Connector.WRITE mode
	 */
	public void test0005() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			try {
				try {
					conn.fileSize();
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
	 * Tests fileSize() in Connector.READ mode
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {				
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				addOperationDesc("Opening connection in READ mode");
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
				
				addOperationDesc("Creating file with a size of 64 bytes: " + conn1.getURL());
				ensureFileExists(conn1);
				ensureFileSize(conn1, 64);
				
				long fileSize = conn2.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==64;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests fileSize() in Connector.READ mode", passed);
	}

	/**
	 * Tests fileSize() on a non-existent file
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {				
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				long fileSize = conn.fileSize();
				addOperationDesc("fileSize() returned " + fileSize);
				
				passed = fileSize==-1;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests fileSize() on a non-existent file", passed);
	}

	/**
	 * Tests fileSize() on a non-existent file system
	 */
	public void test0008() {
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
					long fileSize = conn.fileSize();
					addOperationDesc("fileSize() call returned " + fileSize);
					
					passed = fileSize==-1;								
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests fileSize() on a non-existent file system", passed);
	}

	/**
	 * Tests fileSize() on a non-existent host
	 */
	public void test0009() {
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
					long fileSize = conn.fileSize();
					addOperationDesc("fileSize() call returned " + fileSize);

					passed = fileSize == -1;
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests fileSize() on a non-existent host", passed);
	}

			
	protected void ensureFileSize(FileConnection conn, int fileSize) throws IOException {
		OutputStream os = conn.openOutputStream();
		try {
			os.write(new byte[fileSize]);
			os.flush();
		} finally {
			os.close();
		}
	}

}
