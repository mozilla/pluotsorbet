package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import java.io.InputStream;
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
public class Rename extends TestCaseWithLog {

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
	}
	
	/**
	 * Tests rename() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				passed = conn2.exists();
				addOperationDesc("exists() on " + conn2.getURL() + " returned " + passed);
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests rename() on a file", passed);
	}
	
	/**
	 * Tests rename() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir1/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir2/", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting directory: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("testdir2");
				
				passed = conn2.exists();
				addOperationDesc("exists() on " + conn2.getURL() + " returned " + passed);
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests rename() on a directory", passed);
	}
	
	/**
	 * Tests that file connection target before rename() no longer exists after method invocation
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				// reopen on initial file
				conn1.close();
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				boolean conn1Exists = conn1.exists();
				
				addOperationDesc("exists() on " + conn1.getURL() + " returned " + conn1Exists);
				passed = conn1Exists==false;
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests that file connection target before rename() no longer exists after method invocation", passed);
	}
	
	/**
	 * Tests that the file connection target changes after rename() call
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				String conn1URL = conn1.getURL();
				addOperationDesc("getURL() after rename is " + conn1.getURL());
				passed = conn1URL.equals(conn2.getURL());
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests that the file connection target changes after rename() call", passed);
	}
	
	/**
	 * Input stream is closed after rename()
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			InputStream is = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("opening input stream on: " + conn1.getURL());
				is = conn1.openInputStream();
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				addOperationDesc("Attempting to read from old input stream");
				try {
					is.read();
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					passed = true;
					addOperationDesc("Expected IOException thrown");
				}
			} finally {
				if (is!=null) is.close();
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Input stream is closed after rename()", passed);
	}
	
	/**
	 * Output stream is closed after rename()
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			OutputStream os = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("opening output stream on: " + conn1.getURL());
				os = conn1.openOutputStream();
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				addOperationDesc("Attempting to write to the old output stream");
				try {
					os.write(0);
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					passed = true;
					addOperationDesc("Expected IOException thrown");
				}
			} finally {
				if (os!=null) os.close();
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Output stream is closed after rename()", passed);
	}
	
	/**
	 * Output stream was flushed after rename()
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			OutputStream os = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				long startSize = conn1.fileSize();
				addOperationDesc("fileSize() of " + conn1.getURL() + " is " + startSize);
				
				addOperationDesc("opening output stream on: " + conn1.getURL());
				os = conn1.openOutputStream();
				
				addOperationDesc("writing 1 byte");
				os.write(0);
				
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
				
				long endSize = conn2.fileSize();
				addOperationDesc("fileSize() of " + conn2.getURL() + " is " + endSize);
				
				passed = startSize==(endSize-1);
			} finally {
				if (os!=null) os.close();
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Output stream was flushed after rename()", passed);
	}
	
	/**
	 * ConnectionClosedException thrown if connection is closed
	 */
	public void test0008() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE); 
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				conn.close();

				try {
					conn.rename("test2");
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
	 * IOException thrown if connection target does not exist
	 */
	public void test0009() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does not exist already
				addOperationDesc("Deleting file: " + conn1.getURL());
				ensureNotExists(conn1);
				
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				addOperationDesc("attempting to rename " + conn1.getURL() + " to " + conn2.getURL());
				try {
					conn1.rename("test2");
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if connection target does not exist", passed);
	}
	
	/**
	 * IOException thrown if the new target already exists
	 */
	public void test0010() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
				
				// make sure the existing target does not exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				// make sure the new target does exist already
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("attempting to rename " + conn1.getURL() + " to " + conn2.getURL());
				try {
					conn1.rename("test2");
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if the new target already exists", passed);
	}
	
	/**
	 * IllegalArgumentException thrown if new target contains path specification
	 */
	public void test0011() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				
				// make sure the existing target does not exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("attempting to rename " + conn1.getURL() + " to " + "../test1");
				try {
					conn1.rename("../test1");
					addOperationDesc("No IllegalArgumentException thrown");
					passed = false;
				} catch (IllegalArgumentException e) {
					addOperationDesc("Expected IllegalArgumentException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
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
	public void test0012() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				
				// make sure the existing target does not exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("attempting to rename " + conn1.getURL() + " to " + "te:st");
				try {
					conn1.rename("te:st");
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown if new target contains invalid filename characters", passed);
	}
	
	/**
	 * NullPointerException thrown if newName is null
	 */
	public void test0013() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				
				// make sure the existing target does not exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("attempting to rename " + conn1.getURL() + " with a null newName");
				try {
					conn1.rename(null);
					addOperationDesc("No NullPointerException thrown");
					passed = false;
				} catch (NullPointerException e) {
					addOperationDesc("Expected NullPointerException thrown");
					passed = true;
				}
			} finally {
				if (conn1 != null) conn1.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("NullPointerException thrown if newName is null", passed);
	}
	
	/**
	 * rename() throws IllegalModeException for connections opened in Connector.READ mode
	 */
	public void test0014() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			try {
				try {
					conn.rename("test1");
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
		
		assertTrueWithLog("rename() throws IllegalModeException for connections opened in Connector.READ mode", passed);
	}
	
	/**
	 * Tests rename() in Connector.WRITE mode
	 */
	public void test0015() {
		boolean passed = false;
		
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test2", Connector.READ_WRITE);
					
				// make sure the existing target does  exist already
				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
					
				// make sure the new target does not exist already
				addOperationDesc("Deleting file: " + conn2.getURL());
				ensureNotExists(conn2);
				
				conn1.close();
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"test1", Connector.WRITE);
					
				addOperationDesc("renaming " + conn1.getURL() + " to " + conn2.getURL());
				conn1.rename("test2");
					
				passed = conn2.exists();
				addOperationDesc("exists() on " + conn2.getURL() + " returned " + passed);
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests rename() in Connector.WRITE mode", passed);
	}

	/**
	 * Tests rename() on a file name in escaped form.
	 */
	public void test0016() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+ getTestPath() + "test", Connector.READ_WRITE);
				
				String urlString = "file://"+getTestPath()+"a file";
				conn2 = (FileConnection)Connector.open(urlString, Connector.READ_WRITE);

				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("Deleting file: " + urlString);
				ensureNotExists(conn2);				
				
				String fileName = "a%20file";
				addOperationDesc("renaming to file: " + fileName);
				conn1.rename(fileName);
	
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);				
				
				String newPath = conn1.getPath();
				addOperationDesc("getPath() returned: " + newPath);
				
				String newName = conn1.getName();
				addOperationDesc("getName() returned: " + newName);								
				
				passed = 	conn1.exists() && conn2.exists() &&
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

		assertTrueWithLog("Test rename() on  a file name in escaped form.", passed);
	}

	/**
	 * Tests rename() on a file name in unescaped form.
	 */
	public void test0017() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+ getTestPath() + "test", Connector.READ_WRITE);
				
				String urlString = "file://"+getTestPath()+"a%20file";
				conn2 = (FileConnection)Connector.open(urlString, Connector.READ_WRITE);

				addOperationDesc("Creating file: " + conn1.getURL());
				ensureFileExists(conn1);
				
				addOperationDesc("Deleting file: " + urlString);
				ensureNotExists(conn2);				
				
				String fileName = "a file";
				addOperationDesc("renaming to file: " + fileName);
				conn1.rename(fileName);
	
				String newUrl = conn1.getURL();
				addOperationDesc("getURL() returned: " + newUrl);				
				
				String newPath = conn1.getPath();
				addOperationDesc("getPath() returned: " + newPath);
				
				String newName = conn1.getName();
				addOperationDesc("getName() returned: " + newName);								
				
				passed = 	conn1.exists() && conn2.exists() &&
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

		assertTrueWithLog("Test rename() on a file name in unescaped form.", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system
	 */
	public void test0018() {
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
					conn.rename("newfileName");
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
	public void test0019() {
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
					conn.rename("newfileName");
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
