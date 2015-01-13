package com.ibm.tck.javax.microedition.io.file.FileSystemRegistry;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class RemoveFileSystemListener extends TestCaseWithLog {

	private static int newState = -1;

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
	}
	
	/*
	 * Tests that a reregistered FileSystemListener does not receive notification of root changes
	 */
	public void test0001() {	
		FileSystemListener listener = new FileSystemListener() {
			public void rootChanged(int state, String rootName) {
				if (state == FileSystemListener.ROOT_ADDED)
					log("root added: " + rootName);
				if (state == FileSystemListener.ROOT_REMOVED)
					log("root removed: " + rootName);
				newState = state;
			}
		};
		
		boolean result = FileSystemRegistry.addFileSystemListener(listener);
		if (!result) {
			addOperationDesc("addFileSystemListener() returned false (expected true)");
			assertTrueWithLog("Tests that a reregistered FileSystemListener does not receive notification of root changes", false);
			return;
		}
		
		long startTime = System.currentTimeMillis();
		newState = -1;
		interactiveMessage("Insert or remove media from a removable drive");
		while(newState==-1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		long elapsedTime = System.currentTimeMillis() - startTime;
		
		result = FileSystemRegistry.removeFileSystemListener(listener);
		if (!result) {
			addOperationDesc("removeFileSystemListener() returned false (expected true)");
			assertTrueWithLog("Tests that a reregistered FileSystemListener does not receive notification of root changes", false);
			return;
		}
		
		newState = -1;
		interactiveMessage("Insert or remove media from a removable drive");
		startTime = System.currentTimeMillis();
		while((System.currentTimeMillis()-startTime)<2*elapsedTime) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (newState != -1) {
			addOperationDesc("deregistered listener was notified of media change");
			assertTrueWithLog("Tests that a reregistered FileSystemListener does not receive notification of root changes", false);
			return;
		}

		assertTrueWithLog("Tests that a reregistered FileSystemListener does not receive notification of root changes", true);

	}

}
