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
public class GetPath extends TestCaseWithLog {

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
		
	}

	/**
	 * Tests getPath() on a file
	 */
	public void test0001() {
		boolean passed = false;
		try {
			String testpath= getTestPath();			
			
			String url="file://" + testpath + "file";	
			
			passed = testConnectionPath(testpath, url);			
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on a file", passed);
	}

	/**
	 * Tests getPath() on a directory
	 */
	public void test0002() {
		boolean passed = false;
		try {
			String testpath= getTestPath();			
			
			String url="file://" + testpath + "dir/";	
			
			passed = testConnectionPath(testpath, url);						
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on a file", passed);
	}
	
	
	/**
	 * Tests getPath() on a file with an encoded URL
	 */
	public void test0003() {
		boolean passed = false;
		try {
			String testpath = getTestPath() + "foo%5e%25bar/";
						
			String url="file://" + testpath + "file"; 
			
			passed = testConnectionPath(testpath, url);			
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on a file with an encoded URL", passed);
	}

	/**
	 * Tests getPath() on a file with an encoded URL, alternative
	 */
	public void test0004() {
		boolean passed = false;
		try {
			String testpath = getTestPath() + "foo%5E%25bar/";
			
			String url="file://" + testpath + "file"; 
			
			passed = testConnectionPath(testpath, url);
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on a file with an encoded URL, alternative", passed);
	}

	/**
	 * Tests if getPath() returns in unescaped URL format
	 */
	public void test0005() {
		boolean passed = false;
		try {

			String testpath= getTestPath() + "a directory/";
						
			String url="file://" + testpath + "a file";	
		
			passed = testConnectionPath(testpath, url);	
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests if getPath() returns in unescaped URL format", passed);
	}	

	private boolean testConnectionPath(String testpath, String url) throws IOException {		
		FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
		
		try {
			addOperationDesc("Using file url: " + url);
			
			String path = conn.getPath();
			addOperationDesc("getPath() returned: " + path);
			
			String correctPath = URLSupport.getPathWithoutHost(testpath);
			return path.equals(URLSupport.getUnescapedForm(correctPath));	
		} finally {
			conn.close();
		}
	}	

	/**
	 * Tests getPath() on a file url with host
	 */
	public void test0006() {
		boolean passed = false;
		try {
			String url = "file://" + URLSupport.getPathWithHost(getTestPath()) + "file";
				
			FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
			
			try {
				addOperationDesc("Using file url with host: " + url);
			
				String path = conn.getPath();
				addOperationDesc("getPath() returned: " + path);
		
				String correctPath = URLSupport.getPathWithoutHost(getTestPath());
				passed = path.equals(URLSupport.getUnescapedForm(correctPath));	
			} finally {
				conn.close();
			}

		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on a file url with host", passed);
	}
	

	/**
	 * Tests getPath() on file system roots
	 */
	public void test0007() {
		// path for roots must be "/" + root
		boolean passed = false;
		try {
			Enumeration  rootsEnum = FileSystemRegistry.listRoots();

			while (rootsEnum.hasMoreElements()) {
				String root = (String) rootsEnum.nextElement();
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
					addOperationDesc("Using root url: " + url);

					String path = conn.getPath();
					addOperationDesc("getPath() returned: " + path);
					
					passed = path.equals('/' + root);
					if (!passed) break;						
				} finally {
					conn.close();
				}
			
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on file system roots", passed);
	}
	
	/**
	 * Tests getPath() on roots with 'localhost' in file url
	 */
	public void test0008() {
		// path for roots must be "/" + root
		boolean passed = false;
		try {
			Enumeration  rootsEnum = FileSystemRegistry.listRoots();

			while (rootsEnum.hasMoreElements()) {
				String root = (String) rootsEnum.nextElement();
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
					addOperationDesc("Using root url: " + url);

					String path = conn.getPath();
					addOperationDesc("getPath() returned: " + path);
					
					passed = path.equals('/' + root);
					if (!passed) break;						
				} finally {
					conn.close();
				}
			
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}

		assertTrueWithLog("Tests getPath() on file system roots with 'localhost' in file url", passed);
	}
	
}
