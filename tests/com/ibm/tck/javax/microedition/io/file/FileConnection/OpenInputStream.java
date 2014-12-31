package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class OpenInputStream extends TestCaseWithLog {

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
	 * Tests openInputStream()
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				InputStream is = null;
				OutputStream os = null;
				try {
					addOperationDesc("Writing byte to output stream: 69");
					os = conn.openOutputStream();
					os.write(69);
					os.flush();
					
					is = conn.openInputStream();
					int result = is.read();
					addOperationDesc("Reading byte from input stream: " + result);
					
					passed = result==69;
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

		assertTrueWithLog("Tests openInputStream()", passed);
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
					InputStream is = conn.openInputStream();
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
					InputStream is = conn.openInputStream();
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
	 * Tests openInputStream() in Connector.READ mode
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			InputStream is = null;
			OutputStream os = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("Opening connection in READ mode");
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
				
				addOperationDesc("Writing byte to output stream: 69");
				os = conn1.openOutputStream();
				os.write(69);
				
				os.close();
				os=null;
				conn1.close();				
				conn1=null;
								
				is = conn2.openInputStream();
				int result = is.read();
				addOperationDesc("Reading byte from input stream: " + result);
				
				passed = result==69;
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

		assertTrueWithLog("Tests openInputStream() in Connector.READ mode", passed);
	}
	
	/**
	 * IOException thrown if multiple input streams opened
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			InputStream is0 = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening first input stream");
				is0 = conn.openInputStream();
				try {
					InputStream is = conn.openInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					is.close();
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				if (is0!=null) is0.close();
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
					InputStream is = conn.openInputStream();
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

		assertTrueWithLog("IOException thrown if the file the connection is opened to does not yet exist", passed);
	}
	
	/**
	 * Streams can be opened and closed more than once on a connection
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			InputStream stream = null;
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening stream");
				stream = conn.openInputStream();
				addOperationDesc("Closing stream");
				stream.close();
				stream = null;
				
				addOperationDesc("Opening stream");
				stream = conn.openInputStream();
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

	/*
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
					addOperationDesc("Attempting to call openInputStream() on closed connection");
					InputStream is = conn.openInputStream();
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
					InputStream is = conn.openInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					is.close();
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
					InputStream is = conn.openInputStream();
					addOperationDesc("No IOException thrown");
					passed = false;
					is.close();
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
