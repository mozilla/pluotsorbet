package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class OpenDataInputStream extends TestCaseWithLog {

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
	}

	/**
	 * Tests openDataInputStream()
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				DataInputStream is = null;
				DataOutputStream os = null;
				try {
					addOperationDesc("Writing UTF string to output stream: foobar");
					os = conn.openDataOutputStream();
					os.writeUTF("foobar");
					os.close();
					os = null;
					
					is = conn.openDataInputStream();
					String result = is.readUTF();
					addOperationDesc("Reading UTF string from input stream: " + result);
					
					passed = result.equals("foobar");
				} finally {
					if (is != null) is.close();
					if (os != null) os.close();
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests openDataInputStream()", passed);
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
					InputStream is = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					is.close();
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
	 * IllegalModeException thrown for connections opened in Connector.WRITE mode
	 */
	public void test0003() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			
			try {
				try {
					DataInputStream is = conn.openDataInputStream();
					addOperationDesc("No IllegalModeException thrown");
					passed = false;
					is.close();
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
	 * Tests openDataInputStream() in Connector.READ mode
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			DataInputStream is = null;
			DataOutputStream os = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("Opening connection in READ mode");
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
				
				addOperationDesc("Writing UTF string to output stream: foobar");
				os = conn1.openDataOutputStream();
				os.writeUTF("foobar");

				os.close();
				os=null;
				conn1.close();
				conn1=null;

				is = conn2.openDataInputStream();
				String result = is.readUTF();
				addOperationDesc("Reading UTF string from input stream: " + result);

				passed = result.equals("foobar");
			} finally {
				if (is != null) is.close();
				if (os != null) os.close();
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests openDataInputStream() in Connector.READ mode", passed);
	}
	
	/**
	 * IOException thrown if multiple input streams opened
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			InputStream is = null;
			DataInputStream dis = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening first input stream");
				// try for an InputStream to make sure that they are considered the same
				is = conn.openInputStream();
				try {
					dis = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dis.close();
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				if (is!=null) is.close();
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if multiple input streams opened", passed);
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
					DataInputStream dis = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");
					dis.close();
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

		assertTrueWithLog("IOException thrown if the file the connection is opened to does not yet exist", passed);
	}
	
	/**
	 * Streams can be opened and closed more than once on a connection
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			InputStream stream =null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening stream");
				stream = conn.openDataInputStream();
				addOperationDesc("Closing stream");
				stream.close();
				stream = null;
				
				addOperationDesc("Opening stream");
				stream = conn.openDataInputStream();
				addOperationDesc("Closing stream");
				stream.close();
				stream =null;
				
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
					addOperationDesc("Attempting to call openDataInputStream() on closed connection");
					DataInputStream dis = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dis.close();
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
					DataInputStream dis = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					dis.close();
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
					DataInputStream dis = conn.openDataInputStream();
					addOperationDesc("No IOException thrown");					
					passed = false;
					dis.close();
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
