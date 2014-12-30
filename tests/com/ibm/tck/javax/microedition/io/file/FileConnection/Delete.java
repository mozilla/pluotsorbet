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
public class Delete extends TestCaseWithLog {

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
	}
	
	/**
	 * Tests delete() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Deleting file");
				conn.delete();
				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);

				passed = exists == false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests delete() on a file", passed);
	}
	
	/**
	 * Tests delete() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating directory: " + conn.getURL());
				ensureDirExists(conn);
				
				addOperationDesc("Deleting directory");
				conn.delete();

				boolean exists = conn.exists();
				addOperationDesc("exists() returned " + exists);

				passed = exists ==false;
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests delete() on a file", passed);
	}
	
	/**
	 * Tests delete() on a non-empty directory throws IOException
	 */
	public void test0003() {
		boolean passed = false;
		try {
			FileConnection conn1 = null;
			FileConnection conn2 = null;
			try {
				conn1 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/file", Connector.READ_WRITE);
				
				addOperationDesc("Creating directory: " + conn1.getURL());
				ensureDirExists(conn1);
				
				addOperationDesc("Creating file: " + conn2.getURL());
				ensureFileExists(conn2);
				
				try {
					addOperationDesc("Attempting to delete a non-empty directory");
					conn1.delete();					
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");									
					boolean exists = conn1.exists();
					addOperationDesc("exists() returned " + exists);

					passed = exists == true;
				}
			} finally {
				if (conn1 != null) conn1.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown when trying to delete() on a non-empty directory.", passed);
	}
	
	/**
	 * Tests delete() on a non-existent file throws IOException
	 */
	public void test0004() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting file: " + conn.getURL());
				ensureNotExists(conn);

				try {
					addOperationDesc("Attempting to delete the file again");
					conn.delete();
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					boolean exists = conn.exists();
					addOperationDesc("exists() returned " + exists);
	
					passed = exists == false;
				} 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown when trying to delete() a non-existent file", passed);
	}
	
	/**
	 * Tests delete() on a non-existent directory throws IOException
	 */
	public void test0005() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"testdir/", Connector.READ_WRITE);
			try {
				addOperationDesc("Deleting directory: " + conn.getURL());
				ensureNotExists(conn);

				try {
					addOperationDesc("Attempting to delete the directory again");
					conn.delete();
					addOperationDesc("No IOException thrown");
					passed = false;
				} catch (IOException e) {
					addOperationDesc("Expected IOException thrown");
					boolean exists = conn.exists();
					addOperationDesc("exists() returned " + exists);
	
					passed = exists == false;
				} 
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("IOException thrown when trying to delete() a non-existent directory", passed);
	}

	/**
	 * Input stream is closed after delete()
	 */
	public void test0006() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening input stream on file");
				InputStream is = conn.openInputStream();
				try {
					
					addOperationDesc("Deleting file");
					conn.delete();
					
					try {
						is.read();
						addOperationDesc("No IOException thrown from InputStream.read()");
						passed = false;
					} catch (IOException e) {
						passed = true;
						addOperationDesc("Expected IOException thrown from InputStream.read()");
					}
				} finally {
					is.close();
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Input stream is closed after delete()", passed);
	}
	
	/**
	 * Output stream is closed after delete()
	 */
	public void test0007() {
		boolean passed = false;
		try {
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			try {
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn);
				
				addOperationDesc("Opening output stream on file");
				OutputStream os = conn.openOutputStream();
				try {
					
					addOperationDesc("Deleting file");
					conn.delete();
					
					try {
						os.write(0);
						addOperationDesc("No IOException thrown from OutputStream.write()");
						passed = false;
					} catch (IOException e) {
						passed = true;
						addOperationDesc("Expected IOException thrown from OutputStream.write()");
					}
				} finally {
					os.close();
				}
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Output stream is closed after delete()", passed);
	}

	/**
	 * ConnectionClosedException thrown if connection is closed
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
					addOperationDesc("Attempting to call delete() on closed connection");
					conn.delete();
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
	 * IllegalModeException thrown for connections opened in Connector.READ mode
	 */
	public void test0009() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in READ mode");
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			
			try {
				try {
					conn.delete();
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
	 * Tests delete() in Connector.WRITE mode
	 */
	public void test0010() {
		boolean passed = false;
		try {
			addOperationDesc("Opening connection in WRITE mode");
			FileConnection conn = null;
			FileConnection conn2 = null;
			try {
				conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
				conn2 = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
				
				addOperationDesc("Creating file: " + conn.getURL());
				ensureFileExists(conn2);
				
				addOperationDesc("Deleting file");
				conn.delete();
				
				boolean exists = conn2.exists();
				addOperationDesc("exists() returned " + exists);
				
				passed = exists==false;
			} finally {
				if (conn != null) conn.close();
				if (conn2 != null) conn2.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests delete() in Connector.WRITE mode", passed);
	}

	/**
	 * IOException or SecurityException thrown if connection is opened on a non-existent file system
	 */
	public void test0011() {
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
					conn.delete();
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
	public void test0012() {
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
					conn.delete();
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
