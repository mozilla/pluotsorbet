package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class OpenDataOutputStream extends TestCaseWithLog {
	
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
	 * Tests openDataOutputStream()
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);

				DataOutputStream os = null;
				try {
					os = conn.openDataOutputStream();
					
					long fileSize1 = conn.fileSize();
					addOperationDesc("fileSize() is " + fileSize1);
					
					addOperationDesc("Writing int to output stream");
					os.writeInt(2);
					addOperationDesc("Closing output stream");
					os.close();
					os = null;
					
					long fileSize2 = conn.fileSize();
					addOperationDesc("fileSize() is " + fileSize2);
					
					passed = fileSize2>fileSize1;
				} finally {
					if (os != null) os.close();
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
	
		assertTrueWithLog("Tests openDataOutputStream()", passed);
	}
	
	/**
	 * IOException thrown if invoked on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("Attempting to open stream on directory");
				try {
					OutputStream os = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					os.close();
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
	 * IllegalModeException thrown for connections opened in Connector.READ mode
	 */
	public void test0003() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			
			try {
				try {
					DataOutputStream os = conn.openDataOutputStream();
					addOperationDesc("No IllegalModeException thrown");
					passed = false;
					os.close();
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
		
		assertTrueWithLog("IllegalModeException thrown for connections opened in Connector.READ mode", passed);
	}
	
	/**
	 * Tests openDataOutputStream() in Connector.WRITE mode
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			DataOutputStream os = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("Opening connection in WRITE mode");
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
				os = conn2.openDataOutputStream();
				
				long fileSize1 = conn1.fileSize();
				addOperationDesc("fileSize() is " + fileSize1);

				addOperationDesc("Writing int to output stream");
				os.writeInt(2);
				addOperationDesc("Closing output stream");
				os.close();
				os = null;
				
				long fileSize2 = conn1.fileSize();
				addOperationDesc("fileSize() is " + fileSize2);
				
				passed = fileSize2>fileSize1;
			} finally {
				if (os != null) os.close();
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
	
		assertTrueWithLog("Tests openDataOutputStream() in Connector.WRITE mode", passed);
	}
	
	/**
	 * IOException thrown if multiple output streams opened
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			OutputStream os0 = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening first input stream");
				// try for an OutputStream to make sure that they are considered the same
				os0 = conn.openOutputStream();
				try {
					DataOutputStream dos = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dos.close();
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				} 
			} finally {
				if (os0!=null) os0.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if multiple output streams opened", passed);
	}
	
	/**
	 * IOException thrown if the file the connection is opened to does not yet exist
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				try {
					DataOutputStream os = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					os.close();
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

		assertTrueWithLog("IOException thrown if the file the connection is opened to does not yet exist", passed);
	}
	
	/**
	 * Streams can be opened and closed more than once on a connection
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			DataOutputStream stream = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening stream");
				stream = conn.openDataOutputStream();
				addOperationDesc("Closing stream");
				stream.close();
				stream = null;

				addOperationDesc("Opening stream");
				stream = conn.openDataOutputStream();
				addOperationDesc("Closing stream");
				stream.close();
				stream = null;
								
				passed = true;
			} finally {
				if (stream!=null) stream.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Streams can be opened and closed more than once on a connection", passed);
	}

	/**
	 * IOException thrown if connection is closed
	 */
	public void test0008() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Closing connection");
				conn.close();

				try {
					addOperationDesc("Attempting to call openDataOutputStream() on closed connection");
					DataOutputStream os = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					os.close();
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
		
		assertTrueWithLog("IOException thrown if invoked on a closed connection", passed);
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
					DataOutputStream dos = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dos.close();
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
					DataOutputStream dos = conn.openDataOutputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dos.close();
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
	 * Tests if the DataOutputStream returned from openDataOutputStream() updates the file immediately when flush() is called.
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);

				DataOutputStream os = null;
				try {
					
					long fileSize1 = conn.fileSize();
					addOperationDesc("fileSize() is " + fileSize1);

					os = conn.openDataOutputStream();
										
					addOperationDesc("Writing 1024 bytes to output stream");
					os.write(new byte[1024]);
					os.flush();
					
					long fileSize2 = conn.fileSize();
					addOperationDesc("fileSize() is " + fileSize2);
					
					passed = fileSize2==1024;
				} finally {
					if (os != null) os.close();
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
	
		assertTrueWithLog("Tests if the DataOutputStream returned from openDataOutputStream() updates the file immediately when flush() is called", passed);
	}	
}
