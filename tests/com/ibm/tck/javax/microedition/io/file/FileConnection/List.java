package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.ConnectionClosedException;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.IllegalModeException;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class List extends TestCaseWithLog {
	
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
		if (isTestValid("test0016")) test0016();
		if (isTestValid("test0017")) test0017();
		if (isTestValid("test0018")) test0018();
		if (isTestValid("test0019")) test0019();
		if (isTestValid("test0020")) test0020();
		if (isTestValid("test0021")) test0021();
		if (isTestValid("test0022")) test0022();
		if (isTestValid("test0023")) test0023();		
		if (isTestValid("test0024")) test0024();
		if (isTestValid("test0025")) test0025();
		if (isTestValid("test0026")) test0026();
		if (isTestValid("test0027")) test0027();
		if (isTestValid("test0028")) test0028();
		if (isTestValid("test0029")) test0029();				
		if (isTestValid("test0030")) test0030();
		if (isTestValid("test0031")) test0031();
		if (isTestValid("test0032")) test0032();
		if (isTestValid("test0033")) test0033();

	}
	
	/**
	 * Tests list()
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"subdir/","file"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list()", passed);
	}

	/**
	 * Tests list() on an empty directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);

				Enumeration e = conn1.list();
				passed = testList(e, new String[]{});
			} finally {
				if (conn1 != null) conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list() on an empty directory", passed);
	}
	
	/**
	 * Tests list() on a directory with a hidden file
	 */
	public void test0003() {
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
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("Setting file as hidden: " + conn3.getURL());
				conn3.setHidden(true);
				
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"file1"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list() on a directory with a hidden file", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean)
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("calling list(\"*\", false)");
				Enumeration e = conn1.list("*", false);
				passed = testList(e, new String[]{"subdir/","file"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean)", passed);
	}

	/**
	 * Tests list(java.lang.String, boolean) on an empty directory
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("calling list(\"*\", false)");
				Enumeration e = conn1.list("*", false);
				passed = testList(e, new String[]{});
			} finally {
				if (conn1 != null) conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean) on an empty directory", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) on a directory with a hidden file
	 */
	public void test0006() {
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
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("Setting file as hidden: " + conn3.getURL());
				conn3.setHidden(true);
				
				addOperationDesc("calling list(\"*\", false)");
				Enumeration e = conn1.list("*", false);
				passed = testList(e, new String[]{"file1"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean) on a directory with a hidden file", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) on a directory with a hidden file
	 */
	public void test0007() {
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
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("Setting file as hidden: " + conn3.getURL());
				conn3.setHidden(true);
				
				addOperationDesc("calling list(\"*\", true)");
				Enumeration e = conn1.list("*", true);
				passed = testList(e, new String[]{"file1", "file2"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean) on a directory with a hidden file", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) with a single wildcard
	 */
	public void test0008() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/filedir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file2", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("calling list(\"file*\", false)");
				Enumeration e = conn1.list("file*", false);
				passed = testList(e, new String[]{"filedir/", "file2"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean) with a single wildcard", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) with a single wildcard
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/ff", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/filef", Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/filefa", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("Creating file: " + conn4.getURL());
				ensureFileExists(conn4);
				
				addOperationDesc("calling list(\"f*f\", false)");
				Enumeration e = conn1.list("f*f", false);
				passed = testList(e, new String[]{"ff", "filef"});
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

		assertTrueWithLog("Tests list(java.lang.String, boolean) with a single wildcard", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) with two wildcards
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/filef", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/filefoo", Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/afilefoo", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				addOperationDesc("Creating file: " + conn4.getURL());
				ensureFileExists(conn4);
				
				addOperationDesc("calling list(\"f*f*\", false)");
				Enumeration e = conn1.list("f*f*", false);
				passed = testList(e, new String[]{"filef", "filefoo"});
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

		assertTrueWithLog("Tests list(java.lang.String, boolean) with two wildcards", passed);
	}
	
	/**
	 * list() throws ConnectionClosedException if connection is closed
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				conn.close();

				try {
					conn.list();
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
		
		assertTrueWithLog("list() throws ConnectionClosedException if connection is closed", passed);
	}
	
	/**
	 * list(java.lang.String, boolean) throws ConnectionClosedException if connection is closed
	 */
	public void test0012() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				conn.close();

				try {
					conn.list("*", false);
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
		
		assertTrueWithLog("list(java.lang.String, boolean) throws ConnectionClosedException if connection is closed", passed);
	}
	
	/**
	 * list() throws IOException if invoked on a file
	 */
	public void test0013() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);

				try {
					conn.list();
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
		
		assertTrueWithLog("list() throws IOException if invoked on a file", passed);
	}
	
	/**
	 * list(java.lang.String, boolean) throws IOException if invoked on a file
	 */
	public void test0014() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				try {
					conn.list("*", false);
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
		
		assertTrueWithLog("list(java.lang.String, boolean) throws IOException if invoked on a file", passed);
	}
	
	/**
	 * list() throws IllegalModeException for connections opened in Connector.WRITE mode
	 */
	public void test0015() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.WRITE);
			try {
				try {
					conn.list();
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
		
		assertTrueWithLog("list() throws IllegalModeException for connections opened in Connector.WRITE mode", passed);
	}
	
	/**
	 * list(java.lang.String, boolean) throws IllegalModeException for connections opened in Connector.WRITE mode
	 */
	public void test0016() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.WRITE);
			try {
				try {
					conn.list("*", false);
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
		
		assertTrueWithLog("list(java.lang.String, boolean) throws IllegalModeException for connections opened in Connector.WRITE mode", passed);
	}
	
	/**
	 * NullPointerException thrown if filter is null
	 */
	public void test0017() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);

				try {
					conn.list(null, false);
					addOperationDesc("No NullPointerException thrown");
					passed = false;
				} catch (NullPointerException e) {
					addOperationDesc("Expected NullPointerException thrown");
					passed = true;
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		
		assertTrueWithLog("NullPointerException thrown if filter is null", passed);
	}
	
	/**
	 * Tests list() in Connector.READ mode
	 */
	public void test0018() {
		boolean passed = false;
		
		FileConnection conn1 = null;
		FileConnection conn2 = null;
		FileConnection conn3 = null;
		try {
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
					
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
					
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
					
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
			
			addOperationDesc("Opening connection in READ mode");
			conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
			try {
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"subdir/","file"});						
			} finally {
				conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list() in Connector.READ mode", passed);
	}
	
	/**
	 * Tests list(java.lang.String, boolean) in Connector.READ mode
	 */
	public void test0019() {
		boolean passed = false;
		
		FileConnection conn1 = null;
		FileConnection conn2 = null;
		FileConnection conn3 = null;
		try {
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
					
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
					
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
					
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
			
			addOperationDesc("Opening connection in READ mode");
			conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ);
			try {
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"subdir/","file"});						
			} finally {
				conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests list(java.lang.String, boolean) in Connector.READ mode", passed);
	}
	
	/**
	 * IllegalArgumentException thrown if filter contains any path specification
	 */
	public void test0020() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				try {
					conn1.list("subdir/", false);
					addOperationDesc("No IllegalArgumentException thrown");
					passed = false;
				} catch (IllegalArgumentException e) {
					addOperationDesc("Expected IllegalArgumentException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IllegalArgumentException thrown if filter contains invalid path specification", passed);
	}
	
	/**
	 * IllegalArgumentException thrown if filter contains invalid filename characters
	 */
	public void test0021() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/subdir/", Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + conn2.getURL());
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + conn3.getURL());
				ensureFileExists(conn3);
				
				try {
					conn1.list("fi:le", false);
					addOperationDesc("No IllegalArgumentException thrown");
					passed = false;
				} catch (IllegalArgumentException e) {
					addOperationDesc("Expected IllegalArgumentException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IllegalArgumentException thrown if filter contains invalid path specification", passed);
	}

	/**
	 * Tests list() returns unescaped file and directory names
	 */
	public void test0022() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a dir/";
				String url3 = "file://"+getTestPath()+"testdir/a file";
				
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);
	
				addOperationDesc("calling list()");			
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"a dir/","a file"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Test list() returns unescaped file and directory names", passed);
	}

	/**
	 * Tests list() returns unescaped file and directory names
	 */
	public void test0023() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a%20dir/";
				String url3 = "file://"+getTestPath()+"testdir/a%20file";
				
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);

				addOperationDesc("calling list()");				
				Enumeration e = conn1.list();
				passed = testList(e, new String[]{"a dir/","a file"});
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
				if (conn3 != null) conn3.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Test list() returns unescaped file and directory names", passed);
	}

	/**
	 * Tests (java.lang.String, boolean) returns unescaped file and directory names
	 */
	public void test0024() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;			
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a dir/";
				String url3 = "file://"+getTestPath()+"testdir/a file";
				String url4 = "file://"+getTestPath()+"testdir/other file";
								
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open(url4, Connector.READ_WRITE);
								
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);

				addOperationDesc("Creating file: " + url4);
				ensureFileExists(conn4);

				addOperationDesc("calling list(\"a*\", true)");								
				Enumeration e = conn1.list("a*", true);
				passed = testList(e, new String[]{"a dir/","a file"});
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

		assertTrueWithLog("Test list(java.lang.String, boolean) returns unescaped file and directory names", passed);
	}

	/**
	 * Tests list(java.lang.String, boolean) returns unescaped file and directory names
	 */
	public void test0025() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;			
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a%20dir/";
				String url3 = "file://"+getTestPath()+"testdir/a%20file";
				String url4 = "file://"+getTestPath()+"testdir/other%20file";
				
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open(url4, Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);

				addOperationDesc("Creating file: " + url4);
				ensureFileExists(conn4);

				addOperationDesc("calling list(\"a*\", true)");					
				Enumeration e = conn1.list("a*", true);
				passed = testList(e, new String[]{"a dir/","a file"});
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

		assertTrueWithLog("Test list(java.lang.String, boolean) returns unescaped file and directory names", passed);
	}


	/**
	 * Tests (java.lang.String, boolean) accepts escaped filter parameter
	 */
	public void test0026() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;			
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a dir/";
				String url3 = "file://"+getTestPath()+"testdir/a file";
				String url4 = "file://"+getTestPath()+"testdir/other file";
								
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open(url4, Connector.READ_WRITE);
								
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);

				addOperationDesc("Creating file: " + url4);
				ensureFileExists(conn4);

				addOperationDesc("calling list(\"a%20*\", true)");									
				Enumeration e = conn1.list("a%20*", true);
				passed = testList(e, new String[]{"a dir/","a file"});
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

		assertTrueWithLog("Test list(java.lang.String, boolean) accepts escaped filter parameter", passed);
	}

	/**
	 * Tests (java.lang.String, boolean) accepts unescaped filter parameter
	 */
	public void test0027() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			FileConnection conn3 = null;
			FileConnection conn4 = null;			
			try {
				
				String url1 = "file://"+getTestPath()+"testdir/";
				String url2 = "file://"+getTestPath()+"testdir/a%20dir/";
				String url3 = "file://"+getTestPath()+"testdir/a%20file";
				String url4 = "file://"+getTestPath()+"testdir/other%20file";
				
				conn1 = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				conn3 = (FileConnection)Connector.open(url3, Connector.READ_WRITE);
				conn4 = (FileConnection)Connector.open(url4, Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + url1);
				ensureDirExists(conn1);
				
				addOperationDesc("Creating directory: " + url2);
				ensureDirExists(conn2);
				
				addOperationDesc("Creating file: " + url3);
				ensureFileExists(conn3);

				addOperationDesc("Creating file: " + url4);
				ensureFileExists(conn4);

				addOperationDesc("calling list(\"a *\", true)");					
				Enumeration e = conn1.list("a *", true);
				passed = testList(e, new String[]{"a dir/","a file"});
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

		assertTrueWithLog("Test list(java.lang.String, boolean) accepts unescaped filter parameter", passed);
	}

	/**
	 * list() throws IOException if invoked on a non-existent directory
	 */
	public void test0028() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting  directory: " + conn.getURL());
				ensureNotExists(conn);

				try {
					conn.list();
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
		
		assertTrueWithLog("list() throws IOException if invoked on a non-existent directory", passed);
	}

	/**
	 * list(java.lang.String, boolean) throws IOException if invoked on a non-existent directory
	 */
	public void test0029() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE); 
			try {
				addOperationDesc("Deleting  directory: " + conn.getURL());
				ensureNotExists(conn);

				try {
					conn.list("*", false);
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
		
		assertTrueWithLog("list(java.lang.String, boolean) throws IOException if invoked on a non-existent directory", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system, and list() is invoked
	 */
	public void test0030() {
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
					conn.list();
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
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent file system, and list() is invoked", passed);
	}	

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent host, and list() is invoked
	 */
	public void test0031() {
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
					conn.list();
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
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent host, and list() is invoked", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system, and list(java.lang.String, boolean) is invoked
	 */
	public void test0032() {
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
					conn.list("*", false);
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
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent file system, and list(java.lang.String, boolean) is invoked", passed);
	}	

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent host, and list(java.lang.String, boolean) is invoked
	 */
	public void test0033() {
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
					conn.list("*", false);
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
		
		assertTrueWithLog("IOException or SecurityException thrown if connection is opened on a non-existent host, and list(java.lang.String, boolean) is invoked", passed);
	}

	private boolean testList(Enumeration e, String[] expected) {
		Vector found = new Vector(expected.length);
		
		while(e.hasMoreElements()) {
			String next = (String)e.nextElement();
			if (found.contains(next)) {
				assertTrueWithLog("list() returned duplicate strings: " + next, false);
				return false;
			}
			found.addElement(next);
		}
			
		for(int i=0; i<expected.length; i++) {
			int idx = found.indexOf(expected[i]);
			if (idx==-1) {
				assertTrueWithLog("list() did not return " + expected[i], false);
				return false;
			} else {
				found.removeElementAt(idx);
			}
		}
			
		if(found.size()>0) {
			String unexpected = (String)found.elementAt(0);
			assertTrueWithLog("list() returned unexpected strings " + unexpected, false);
			return false;
		}
		
		return true;
	}	
}
