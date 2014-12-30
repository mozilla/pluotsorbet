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
public class IsHidden extends TestCaseWithLog {

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
		if (isTestValid("test0014")) test0014();
		if (isTestValid("test0015")) test0015();		
	}
	
	/**
	 * Tests isHidden() on a hidden file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Setting file as hidden");
				conn.setHidden(true);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a hidden file", passed);
	}

	/**
	 * Tests isHidden() on a non-hidden file
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Setting file as non-hidden");
				conn.setHidden(false);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a non-hidden file", passed);
	}
	
	/**
	 * Tests isHidden() on a hidden directory
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("Setting directory as hidden");
				conn.setHidden(true);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==true;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a hidden directory", passed);
	}
	
	/**
	 * Tests isHidden() on a non-hidden directory
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("Setting directory as non-hidden");
				conn.setHidden(false);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a non-hidden directory", passed);
	}
	
	/**
	 * Tests isHidden() on a file (hidden attribute is NOT supported by filesystem)
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Setting file as hidden");
				conn.setHidden(true);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden + " (should have no effect)");
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a file (hidden attribute is NOT supported by filesystem)", passed);
	}
	
	/**
	 * Tests isHidden() on a directory (hidden attribute is NOT supported by filesystem)
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("Setting directory as hidden");
				conn.setHidden(true);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden + " (should have no effect)");
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests isHidden() on a file (hidden attribute is NOT supported by filesystem)", passed);
	}
	
	/**
	 * isHidden() throws ConnectionClosedException if connection is closed
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Closing connection");
				conn.close();
				
				try {
					addOperationDesc("Attempting to call isHidden() on a closed connection");
					boolean isHidden = conn.isHidden();
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

		assertTrueWithLog("isHidden() throws ConnectionClosedException if connection is closed", passed);
	}
	
	/**
	 * IllegalModeException thrown for connections opened in Connector.WRITE mode
	 */
	public void test0008() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			try {
				try {
					conn.isHidden();
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
	 * Tests isHidden() in Connector.READ mode
	 */
	public void test0009() {
		boolean passed = false;
		try {	
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			try {
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() call returned " + isHidden);
				
				passed = true;								
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests isHidden() in Connector.READ mode", passed);
	}

	/**
	 * Tests isHidden() on a non-existent file
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("isHidden() returns false for a non-existent file", passed);
	}

	/**
	 * Tests isHidden() on a non-existent file (hidden attribute is NOT supported by filesystem)
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("isHidden() returns false for a non-existent file (hidden attribute is NOT supported by filesystem)", passed);
	}

	/**
	 * Tests isHidden() on a non-existent directory
	 */
	public void test0012() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("isHidden() returns false for a non-existent directory", passed);
	}

	/**
	 * Tests isHidden() on a non-existent directory (hidden attribute is NOT supported by filesystem)
	 */
	public void test0013() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);
				
				boolean isHidden = conn.isHidden();
				addOperationDesc("isHidden() returned " + isHidden);
				
				passed = isHidden==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("isHidden() returns false for a non-existent directory (hidden attribute is NOT supported by filesystem)", passed);
	}

	/**
	 * Tests isHidden() on a non-existent file system
	 */
	public void test0014() {
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
					boolean isHidden = conn.isHidden();
					addOperationDesc("isHidden() call returned " + isHidden);
				
					passed = isHidden==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests isHidden() on a non-existent file system", passed);
	}
	

	/**
	 * Tests isHidden() on a non-existent host
	 */
	public void test0015() {
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
				addOperationDesc("Opened connection on a non existent file system: " + conn.getURL());
				try {
					boolean isHidden = conn.isHidden();
					addOperationDesc("isHidden() call returned " + isHidden);
				
					passed = isHidden==false;	
				} finally {
					conn.close();
				}
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("Tests isHidden() on a non-existent host", passed);
	}				
}