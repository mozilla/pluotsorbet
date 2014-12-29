package com.ibm.tck.javax.microedition.io.file.FileSystemRegistry;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class AddFileSystemListener extends TestCaseWithLog {

	private static int newState = -1;

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
	}
	
	/*
	 * Tests that a registered FileSystemListener receives notification of root changes
	 */
	public void test0001() {
		interactiveMessage("Remove media from a removable drive");
		
		FileSystemListener listener = new FileSystemListener() {
			public void rootChanged(int state, String rootName) {
				if (state == FileSystemListener.ROOT_ADDED)
					log("root added: " + rootName);
				if (state == FileSystemListener.ROOT_REMOVED)
					log("root removed: " + rootName);
				newState = state;
			}
		};
		
		boolean addResult = FileSystemRegistry.addFileSystemListener(listener);
		if (!addResult) {
			addOperationDesc("addFileSystemListener() returned false (expected true)");
			assertTrueWithLog("Tests that a registered FileSystemListener receives notification of root changes", false);
			return;
		}
		
		newState = -1;
		interactiveMessage("Insert media into a removable drive");
		while(newState==-1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (newState != FileSystemListener.ROOT_ADDED) {
			addOperationDesc("listener was not notified of media change");
			assertTrueWithLog("Tests that a registered FileSystemListener receives notification of root changes", false);
			return;
		}
		
		newState = -1;
		interactiveMessage("Remove media from a removable drive");
		while(newState==-1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (newState != FileSystemListener.ROOT_REMOVED) {
			addOperationDesc("listener was not notified of media change");
			assertTrueWithLog("Tests that a registered FileSystemListener receives notification of root changes", false);
			return;
		}
		
		newState = -1;
		interactiveMessage("Insert media into a removable drive");
		while(newState==-1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (newState != FileSystemListener.ROOT_ADDED) {
			addOperationDesc("listener was not notified of media change");
			assertTrueWithLog("Tests that a registered FileSystemListener receives notification of root changes", false);
			return;
		}
		
		assertTrueWithLog("Tests that a registered FileSystemListener receives notification of root changes", true);
	}

}
