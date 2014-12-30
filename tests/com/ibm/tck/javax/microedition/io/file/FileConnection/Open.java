package com.ibm.tck.javax.microedition.io.file.FileConnection;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class Open extends TestCaseWithLog {

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
		if (isTestValid("test0002")) test0002();
		if (isTestValid("test0003")) test0003();
		if (isTestValid("test0004")) test0004();
		if (isTestValid("test0005")) test0005();
	}
	
	/**
	 * Tests Connector.open() with a valid file url in Connector.READ_WRITE mode
	 */
	public void test0001() {
		boolean passed = false;
		try {	
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ_WRITE);
			passed = true;									
			conn.close();
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests Connector.open() with a valid file url in Connector.READ_WRITE mode", passed);
	}
	
	/**
	 * Tests Connector.open() with a valid file url in Connector.READ mode
	 */
	public void test0002() {
		boolean passed = false;
		try {	
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.READ);
			passed = true;									
			conn.close();
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests Connector.open() with a valid file url in Connector.READ mode", passed);
	}

	/**
	 * Tests Connector.open() with a valid file url in Connector.WRITE mode
	 */
	public void test0003() {
		boolean passed = false;
		try {	
			FileConnection conn = (FileConnection)Connector.open("file://"+getTestPath()+"test", Connector.WRITE);
			passed = true;									
			conn.close();
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests Connector.open() with a valid file url in Connector.WRITE mode", passed);
	}	


	/**
	 * Tests Connector.open() with a relative file url
	 */
	public void test0004() {
		boolean passed = false;
		try {	

			String url1 = "file:///..";
			try {
				FileConnection conn = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				passed = false;	
				addOperationDesc("No IllegalArgumentException thrown for url " + url1);								
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url1);								
			}
			
			String url2 = "file:///.";
			try {
				FileConnection conn = (FileConnection)Connector.open(url2, Connector.READ_WRITE);
				passed = false;									
				addOperationDesc("No IllegalArgumentException thrown for url " + url2);
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url2);								
			}

			String url3 = "file://"+getTestPath()+"test/..";
			try {
				FileConnection conn = (FileConnection)Connector.open(url1, Connector.READ_WRITE);
				passed = false;									
				addOperationDesc("No IllegalArgumentException thrown for url " + url3);
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url3);								
			}
			
			String url4 = "file://"+getTestPath()+"test/.";
			try {
				FileConnection conn = (FileConnection)Connector.open(url4, Connector.READ_WRITE);
				passed = false;									
				addOperationDesc("No IllegalArgumentException thrown for url " + url4);
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url4);								
			}

			String url5 = "file://"+getTestPath()+"test/../test";
			try {
				FileConnection conn = (FileConnection)Connector.open(url5, Connector.READ_WRITE);
				passed = false;									
				addOperationDesc("No IllegalArgumentException thrown for url " + url5);
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url5);								
			}
			
			String url6 = "file://"+getTestPath()+"test/./test";
			try {
				FileConnection conn = (FileConnection)Connector.open(url6, Connector.READ_WRITE);
				passed = false;									
				addOperationDesc("No IllegalArgumentException thrown for url " + url6);			
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for file url " + url6);								
			}			
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests Connector.open() with a relative file", passed);
	}

	/**
	 * Tests Connector.open() with a file url containing '\'  character
	 */
	public void test0005() {
		boolean passed = false;
		try {	
			String url = "file://"+getTestPath()+"test\\test";
			try {
				FileConnection conn = (FileConnection)Connector.open(url, Connector.READ_WRITE);
				passed = false;			
				addOperationDesc("No IllegalArgumentException thrown for url " + url);
				conn.close();
			} catch (IllegalArgumentException e) {
				passed = true;
				addOperationDesc("Expected IllegalArgumentException thrown for url " + url);								
			}
		} catch (Exception e) {
			logUnexpectedExceptionDesc(e);
			passed = false;
		}
		assertTrueWithLog("Tests Connector.open() with a file url containing '\'  character", passed);
	}

}