package com.ibm.tck.javax.microedition.io.file.FileConnection;

import java.io.IOException;
import java.util.Enumeration;
import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;
import com.ibm.tck.javax.microedition.io.file.support.URLSupport;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class GetURL extends TestCaseWithLog {

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
	 * Tests getURL() on a non-existing file url
	 */
	public void test0001() {
		boolean passed = false;
		try {
			String url = "file://" + getTestPath() + "file";	
			
			String escapedUrl		= URLSupport.getEscapedForm(url);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
						
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			ensureNotExists(conn);
			try {
				addOperationDesc("Using file url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on a non-existing file url", passed);
	}
	

	/**
	 * Tests getURL() on a non-existing directory url
	 */
	public void test0002() {
		boolean passed = false;
		try {
			String url = "file://" + getTestPath() + "dir/";
				
			String escapedUrl 		= URLSupport.getEscapedForm(url);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
			
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			ensureNotExists(conn);
			try {
				addOperationDesc("Using directory url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on a non-existing directory url", passed);
	}
	
	/**
	 * Tests getURL() on an existing file url
	 */
	public void test0003() {
		boolean passed = false;
		try {
			String url = "file://" + getTestPath() + "file";	
			
			String escapedUrl		= URLSupport.getEscapedForm(url);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
						
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			ensureFileExists(conn);
			try {
				addOperationDesc("Using file url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on an existing file url", passed);
	}
	

	/**
	 * Tests getURL() on an existing directory url
	 */
	public void test0004() {
		boolean passed = false;
		try {
			String url = "file://" + getTestPath() + "dir/";
				
			String escapedUrl 		= URLSupport.getEscapedForm(url);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
			
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			ensureDirExists(conn);
			try {
				addOperationDesc("Using directory url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on an existing directory url", passed);
	}	

	/**
	 * Tests getURL() on an existing file, and a connection url ending with '/'
	 */
	public void test0005() {
		boolean passed = false;
		try {
			String url0="file://"+getTestPath()+"file";
			FileConnection conn0 = (FileConnection)Connector.open(url0, Connector.READ_WRITE);

			addOperationDesc("Creating file: " + url0);
			ensureFileExists(conn0);
			conn0.close();
			
			String escapedUrl		= URLSupport.getEscapedForm(url0);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url0);

			String url = "file://" + getTestPath() + "file/";						
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			
			try {
				addOperationDesc("Using directory url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on an existing file, and a connection url ending with '/'", passed);
	}
	

	/**
	 * Tests getURL() on an existing directory, and a connection url not ending with '/'
	 */
	public void test0006() {
		boolean passed = false;
		try {
			String url0 = "file://" + getTestPath() + "dir/";
			FileConnection conn0 = (FileConnection)Connector.open(url0, Connector.READ_WRITE);

			addOperationDesc("Creating directory: " + url0);
			ensureDirExists(conn0);
			conn0.close();
				
			String escapedUrl 		= URLSupport.getEscapedForm(url0);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url0);

			String url = "file://" + getTestPath() + "dir";			
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			
			try {
				addOperationDesc("Using file url: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on an existing directory, and a connection url not ending with '/'", passed);
	}	

	/**
	 * Tests getURL() on an encoded file URL
	 */
	public void test0007() {
		boolean passed = false;
		try {

			String url="file://"+ getTestPath() +"foo%5e%25bar/file";

			String escapedUrl		= URLSupport.getEscapedForm(url);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
						
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			try {
				addOperationDesc("Using file: " + url);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on an encoded file URL", passed);
	}

	/**
	 * Tests if getURL() returns in escaped URL format
	 */
	public void test0008() {
		boolean passed = false;
		try {
			String unEscapedUrl		= "file://"+ getTestPath() +"a directory/a file";

			String escapedUrl 		= URLSupport.getEscapedForm(unEscapedUrl);
			String alternativeUrl	= URLSupport.getAlternativeEscapedForm(unEscapedUrl);
				
			FileConnection conn = (FileConnection)Connector.open(unEscapedUrl, Connector.READ_WRITE);
			try {
				addOperationDesc("Using file: " + unEscapedUrl);
				
				String connUrl = conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);
				
				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests if getURL() returns in escaped URL format", passed);
	}	

	/**
	 * Tests getURL() on a file url with host
	 */
	public void test0009() {
		boolean passed = false;
		try {
 			String url = "file://" + URLSupport.getPathWithHost(getTestPath())+ "file";
				
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);

			try {
				String escapedUrl 		= URLSupport.getEscapedForm(url);
				String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
			
				addOperationDesc("Using file url with host: " + url);
			
				String connUrl= conn.getURL();
				addOperationDesc("getURL() returned: " + connUrl);

				passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);			
			} finally {
				conn.close();
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on a file url with host", passed);
	}

	/**
	 * Tests getURL() on file system roots
	 */
	public void test0010() {
		// url for roots must be something like "file:///root/"  in the escaped form		
		boolean passed = false;
		try {
			Enumeration  enum = FileSystemRegistry.listRoots();

			while (enum.hasMoreElements()) {
				String root = (String) enum.nextElement();
				String url = "file:///" + root;
				
				FileConnection conn = null;
				try {
					conn = (FileConnection) Connector.open(url, Connector.READ_WRITE);
				} catch (SecurityException se) {
					// this is an unaccessible file system,
					// continue with the next file system.
					continue;
				}
				
				try {
					String escapedUrl		= URLSupport.getEscapedForm(url);
					String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);

					addOperationDesc("Using root url: " + url);

					String connUrl = conn.getURL();
					addOperationDesc("getUrl() returned: " + connUrl);					

					passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
					if (!passed) break;						
				} finally {
					conn.close();
				}			
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on file system roots", passed);
	}

	/**
	 * Tests getURL() on file system roots with 'localhost' in file url
	 */
	public void test0011() {
		// url for roots must be something like "file:///root/" in the escaped form		
		boolean passed = false;
		try {
			Enumeration  enum = FileSystemRegistry.listRoots();

			while (enum.hasMoreElements()) {
				String root = (String) enum.nextElement();
				String url = "file://localhost/" + root;
				
				FileConnection conn = null;
				try {
					conn = (FileConnection) Connector.open(url, Connector.READ_WRITE);
				} catch (SecurityException se) {
					// this is an unaccessible file system,
					// continue with the next file system.
					continue;
				}
				
				try {
					String escapedUrl		= URLSupport.getEscapedForm(url);
					String alternativeUrl	= URLSupport.getAlternativeEscapedForm(url);
				
					addOperationDesc("Using root url: " + url);

					String connUrl = conn.getURL();
					addOperationDesc("getURL() returned: " + connUrl);					

					passed = connUrl.equals(escapedUrl) || connUrl.equals(alternativeUrl);
					if (!passed) break;						
				} finally {
					conn.close();
				}			
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getURL() on file system roots with 'localhost' in file url", passed);
	}
}
