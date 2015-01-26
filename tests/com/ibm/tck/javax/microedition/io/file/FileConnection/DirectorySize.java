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
public class DirectorySize extends TestCaseWithLog {

	protected FileConnection testFile2;
	protected FileConnection testFile3;
	protected FileConnection testSubSubDir;

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
	 * Tests directorySize() on empty directory
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				long directorySize = conn.directorySize(true);
				addOperationDesc("directorySize(true) returned " + directorySize);
				
				passed = directorySize==0;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests directorySize() on empty directory", passed);
	}
	
	/**
	 * Tests directorySize() on directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file1", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file2", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file with a size of 64 bytes: " + conn2.getURL());
				ensureFileExists(conn2);
				ensureFileSize(conn2, 64);
				
				addOperationDesc("Creating file with a size of 32 bytes: " + conn3.getURL());
				ensureFileExists(conn3);
				ensureFileSize(conn3, 32);
				
				long directorySize = conn1.directorySize(true);
				addOperationDesc("directorySize(true) returned " + directorySize);
				
				passed = directorySize==96;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog(" Tests directorySize() on directory", passed);
	}
	
	/**
	 * Tests directorySize() on directory with subdirectories (with includeSubDirs as false)
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file1", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/file2", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file with a size of 64 bytes: " + conn2.getURL());
				ensureFileExists(conn2);
				ensureFileSize(conn2, 64);
				
				addOperationDesc("Creating directory: " + conn3.getURL());
				ensureDirExists(conn3);
				
				addOperationDesc("Creating file with a size of 32 bytes: " + conn4.getURL());
				ensureFileExists(conn4);
				ensureFileSize(conn4, 32);
				
				long directorySize = conn1.directorySize(false);
				addOperationDesc("directorySize(false) returned " + directorySize);
				
				passed = directorySize==64;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
				if (conn4 != null) conn4.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests directorySize() on directory with subdirectories (with includeSubDirs as false)", passed);
	}
	
	/**
	 * Tests directorySize() on directory with subdirectories (with includeSubDirs as true)
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file1", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/file2", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file with a size of 64 bytes: " + conn2.getURL());
				ensureFileExists(conn2);
				ensureFileSize(conn2, 64);
				
				addOperationDesc("Creating directory: " + conn3.getURL());
				ensureDirExists(conn3);
				
				addOperationDesc("Creating file with a size of 32 bytes: " + conn4.getURL());
				ensureFileExists(conn4);
				ensureFileSize(conn4, 32);
				
				long directorySize = conn1.directorySize(true);
				addOperationDesc("directorySize(true) returned " + directorySize);
				
				passed = directorySize==96;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
				if (conn4 != null) conn4.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests directorySize() on directory with subdirectories (with includeSubDirs as true)", passed);
	}

	/**
	 * ConnectionClosedException thrown if invoked on a closed connection
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				conn.close();

				try {
					conn.directorySize(true);
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
	 * IOException thrown if invoked on a file
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating file" + conn.getURL());
				ensureFileExists(conn);
				
				try {
					addOperationDesc("Attempting to call directorySize() on a file");
					conn.directorySize(true);
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
	public void test0007() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.WRITE);
			try {
				try {
					conn.directorySize(true);
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
	 * Tests directorySize() in Connector.READ mode
	 */
	public void test0008() {
		boolean passed = false;
		try {	
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
			try {
				long directorySize = conn.directorySize(false);
				addOperationDesc("directorySize() call returned " + directorySize);
				
				passed = true;								
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests directorySize() in Connector.READ mode", passed);
	}


	/**
	 * Tests directorySize() on a non-existent directory
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				long directorySize = conn.directorySize(true);
				addOperationDesc("directorySize(true) returned " + directorySize);
				
				passed = directorySize==-1;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests directorySize() on a non-existent directory", passed);
	}

	/**
	 * Tests directorySize() on a non-existent file system
	 */
	public void test0010() {
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
					long directorySize = conn.directorySize(true);
					addOperationDesc("directorySize() call returned " + directorySize);
					
					passed = directorySize==-1;								
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests directorySize() on a non-existent file system", passed);
	}

	/**
	 * Tests directorySize() on a non-existent host
	 */
	public void test0011() {
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
				addOperationDesc("Opened connection on a non existent host: " + conn.getURL());
				try {
					long directorySize = conn.directorySize(true);
					addOperationDesc("directorySize() call returned " + directorySize);

					passed = directorySize == -1;
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests directorySize() on a non-existent host", passed);
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
