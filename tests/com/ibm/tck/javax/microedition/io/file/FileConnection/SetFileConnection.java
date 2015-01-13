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
public class SetFileConnection extends TestCaseWithLog {

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
		if (isTestValid("test0003")) test0003();
		if (isTestValid("test0004")) test0004();
		if (isTestValid("test0005")) test0005();
		if (isTestValid("test0006")) test0006();
		if (isTestValid("test0007")) test0007();
		if (isTestValid("test0008")) test0008();
		if (isTestValid("test0009")) test0009();
		if (isTestValid("test0010")) test0010();
		if (isTestValid("test0011")) test0011();
		if (isTestValid("test0013")) test0013();
		if (isTestValid("test0014")) test0014();
		if (isTestValid("test0015")) test0015();		
		if (isTestValid("test0016")) test0016();
	}
	
	/**
	 * Traverse to parent directory
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath(), Connector.READ_WRITE);

				addOperationDesc("Opened the file connection on url: " + conn1.getURL());
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Setting the file connection to parent directory");
				conn1.setFileConnection("..");
				
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);
				passed = newUrl.equals(conn2.getURL());
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();				
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Traverse to parent directory", passed);
	}
	
	/**
	 * Traverse to a subdirectory
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath(), Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				
				addOperationDesc("Opened the file connection on url: " + conn1.getURL());
								
				addOperationDesc("Creating subdirectory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Setting the file connection to subdirectory");
				conn1.setFileConnection("testdir/");
				
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);				
				passed = newUrl.equals(conn2.getURL());
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Traverse to subdirectory", passed);
	}
	
	/**
	 * Traverse to a file
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath(), Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);

				addOperationDesc("Opened the file connection on url: " + conn1.getURL());
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("setting the file connection to file");
				conn1.setFileConnection("test");
	
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);				
				passed = newUrl.equals(conn2.getURL());
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Traverse to a file", passed);
	}
	
	/**
	 * IOException thrown if connection's target is a file
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			addOperationDesc("Opened the file connection on a file: " + conn.getURL());
			
			addOperationDesc("Creating file: " + conn.getURL());
			ensureFileExists(conn);
			try {
				addOperationDesc("Attempting to set the file connection to the parent directory");		
				conn.setFileConnection("..");

				addOperationDesc("No IOException thrown");
				passed = false;
			} catch (IOException e) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IOException thrown if connection's target is a file", passed);
	}
	
	/**
	 * IllegalArgumentException thrown if new target does not exist
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			addOperationDesc("Opened the file connection on url: " + conn.getURL());		
			
			addOperationDesc("Creating directory: " + conn.getURL());
			ensureDirExists(conn);
			try {
				addOperationDesc("Attempting to set the file connection to a non-existent file");		
				conn.setFileConnection("noexist");
				addOperationDesc("No IllegalArgumentException thrown");
				passed = false;
			} catch (IllegalArgumentException e) {
				addOperationDesc("Expected IllegalArgumentException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IllegalArgumentException thrown if new target does not exist", passed);
	}
	
	/**
	 * IllegalArgumentException thrown if new target contains path specification
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			addOperationDesc("Creating directory: " + conn.getURL());
			ensureDirExists(conn);
			try {
				conn.setFileConnection("../testdir/");
				addOperationDesc("No IllegalArgumentException thrown");
				passed = false;
			} catch (IllegalArgumentException e) {
				addOperationDesc("Expected IllegalArgumentException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IllegalArgumentException thrown if new target contains path specification", passed);
	}
	
	/**
	 * IOException thrown if new target contains invalid filename characters
	 */
	public void test0008() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			addOperationDesc("Creating directory: " + conn.getURL());
			ensureDirExists(conn);
			try {
				conn.setFileConnection("te:st");
				addOperationDesc("No IOException thrown");
				passed = false;
			} catch (IOException e) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IOException thrown if new target contains invalid filename characters", passed);
	}
	
	/**
	 * NullPointerException thrown if newFile is null
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			addOperationDesc("Creating directory: " + conn.getURL());
			ensureDirExists(conn);
			try {
				conn.setFileConnection(null);
				addOperationDesc("No NullPointerException thrown");
				passed = false;
			} catch (NullPointerException e) {
				addOperationDesc("Expected NullPointerException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("NullPointerException thrown if newFile is null", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if connection is closed
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				conn.close();

				try {
					conn.setFileConnection("..");
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
	 * Security mode is preserved for new target
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("opening connection in READ_WRITE mode");
				// already done
				
				addOperationDesc("traversing to parent directory");
				conn1.setFileConnection("..");
				
				addOperationDesc("attempting read operation");
				conn1.canRead();
				addOperationDesc("attempting write operation");
				conn1.setReadable(true);
				conn1.close();
				
				addOperationDesc("opening connection in READ mode");
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
				
				addOperationDesc("traversing to parent directory");
				conn1.setFileConnection("..");
				
				addOperationDesc("attempting read operation");
				conn1.canRead();
				addOperationDesc("attempting write operation");
				try {
					conn1.setReadable(true);
					addOperationDesc("No IllegalModeException thrown");
					passed = false;
					return;
				} catch (IllegalModeException e) {
					addOperationDesc("Expected IllegalModeException thrown");
				}
				conn1.close();
				
				addOperationDesc("opening connection in WRITE mode");
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.WRITE);
				
				addOperationDesc("traversing to parent directory");
				conn1.setFileConnection("..");
				
				addOperationDesc("attempting read operation");
				try {
					conn1.canRead();
					addOperationDesc("No IllegalModeException thrown");
					passed = false;
					return;
				} catch (IllegalModeException e) {
					addOperationDesc("Expected IllegalModeException thrown");
				}
				addOperationDesc("attempting write operation");
				conn1.setReadable(true);
				conn1.close();
				
				passed = true;
			} finally {
				conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Traverse to parent directory", passed);
	}
	
	private boolean isSameFile(FileConnection fc1, FileConnection fc2) {
		return fc1.getPath().equals(fc2.getPath()) && fc1.getName().equals(fc2.getName());
	}

	/**
	 * Traverse to a file name that is in unescaped form.
	 */
	public void test0013() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				String urlString = "file://"+getTestPath()+"a%20file";
				conn1 = (FileConnection)Connector.open("file://"+getTestPath(), Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(urlString, Connector.READ_WRITE);

				addOperationDesc("Opened the file connection on url: " + conn1.getURL());
				
				addOperationDesc("Creating file: " + urlString);
				ensureFileExists(conn2);
				
				String fileName ="a file";
				addOperationDesc("setting the file connection to file:" + fileName );
				conn1.setFileConnection(fileName);
	
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);				
				
				String newPath = conn1.getPath();
				addOperationDesc("getPath() returned: " + newPath);
				
				String newName = conn1.getName();
				addOperationDesc("getName() returned: " + newName);								
				
				passed = 	conn1.exists() &&
							newUrl.equals(conn2.getURL()) &&
							newPath.equals(conn2.getPath()) &&
							newName.equals(conn2.getName());
							
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Traverse to a file name that is in unescaped form.", passed);
	}
	
	/**
	 * IOException thrown if connection is opened on a non-existent directory
	 */
	public void test0014() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			addOperationDesc("Opened the file connection on url: " + conn.getURL());		
			
			addOperationDesc("Deleting directory: " + conn.getURL());
			ensureNotExists(conn);
			try {
				addOperationDesc("Attempting to set the file connection to a new file");		
				conn.setFileConnection("newFileName");
				addOperationDesc("No IOException thrown");
				passed = false;
			} catch (IOException e) {
				addOperationDesc("Expected IOException thrown");
				passed = true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("IOException thrown if connection is opened on a non-existent directory", passed);
	}
	
	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system
	 */
	public void test0015() {
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
					conn.setFileConnection("newFileName");
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
	public void test0016() {
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
					conn.setFileConnection("newFileName");
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
}
