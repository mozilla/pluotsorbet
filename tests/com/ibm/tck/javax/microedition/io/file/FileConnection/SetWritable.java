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
public class SetWritable extends TestCaseWithLog {

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
		if (isTestValid("test0012")) test0012();
		if (isTestValid("test0013")) test0013();		
	}

	/**
	 * Tests setWritable() on a file
	 */
	public void test0001() {
		boolean passed = false;
		FileConnection conn = null;
		try {
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
					
				boolean canWrite = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite);
				
				addOperationDesc("setting writable attribute to " + !canWrite);
				conn.setWritable(!canWrite);
				
				boolean canWrite2 = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite2);
				
				passed = canWrite2 != canWrite; 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests setWritable() on a file", passed);
	}

	/**
	 * Tests setWritable() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		FileConnection conn = null;
		try {
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
					
				boolean canWrite = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite);
				
				addOperationDesc("setting writable attribute to " + !canWrite);
				conn.setWritable(!canWrite);
				
				boolean canWrite2 = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite2);
				
				passed = canWrite2 != canWrite; 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests setWritable() on a directory", passed);
	}
	
	/**
	 * Tests setWritable() on a file (writable attribute is NOT supported by filesystem)
	 */
	public void test0003() {
		boolean passed = false;
		FileConnection conn = null;
		try {
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
					
				boolean canWrite = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite);
				
				addOperationDesc("setting writable attribute to false");
				conn.setWritable(false);
				
				boolean canWrite2 = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite2);
				
				passed = canWrite == true && canWrite2 == true; 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests setWritable() on a file (writable attribute is NOT supported by filesystem)", passed);
	}
	
	/**
	 * Tests setWritable() on a directory (writable attribute is NOT supported by filesystem)
	 */
	public void test0004() {
		boolean passed = false;
		FileConnection conn = null;
		try {
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
					
				boolean canWrite = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite);
				
				addOperationDesc("setting writable attribute to false");
				conn.setWritable(false);
				
				boolean canWrite2 = conn.canWrite();
				addOperationDesc("canWrite() returned " + canWrite2);
				
				passed = canWrite == true && canWrite2 == true; 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests setWritable() on a directory (writable attribute is NOT supported by filesystem)", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if connection is closed
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("closing connection");
				conn.close();

				try {
					conn.setWritable(true);
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
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
			
			try {
				try {
					conn.setWritable(true);
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
	 * Tests setWritable() in Connector.WRITE mode
	 */
	public void test0007() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = null;
			FileConnection conn2 = null;
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
						
				boolean canWrite = conn2.canWrite();
				addOperationDesc("canWrite() returned " + canWrite);
					
				addOperationDesc("setting writable attribute to " + !canWrite);
				conn.setWritable(!canWrite);
					
				boolean canWrite2 = conn2.canWrite();
				addOperationDesc("canWrite() returned " + canWrite2);
					
				passed = true;
			} finally {
				if (conn != null) conn.close();
				if (conn2 != null) conn2.close();
			} 
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests setWritable() in Connector.WRITE mode", passed);
	}

	/**
	 * IOException thrown for a non-existent file
	 */
	public void test0008() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				try {
					conn.setWritable(true);
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
		
		assertTrueWithLog("setWritable throws IOException for a non-existent file", passed);
	}

	/**
	 * IOException thrown for a non-existent file (writable attribute is NOT supported by filesystem)
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				try {
					conn.setWritable(true);
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
		
		assertTrueWithLog("setWritable throws IOException for a non-existent file (writable attribute is NOT supported by filesystem)", passed);
	}
		
	/**
	 * IOException thrown for a non-existent directory
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				try {
					conn.setWritable(true);
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
		
		assertTrueWithLog("setWritable throws IOException for a non-existent directory", passed);
	}

	/**
	 * IOException thrown for a non-existent directory (writable attribute is NOT supported by filesystem)
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				try {
					conn.setWritable(true);
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
		
		assertTrueWithLog("setWritable throws IOException for a non-existent directory (writable attribute is NOT supported by filesystem)", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system
	 */
	public void test0012() {
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
					conn.setWritable(true);
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
	public void test0013() {
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
					conn.setWritable(false);
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
