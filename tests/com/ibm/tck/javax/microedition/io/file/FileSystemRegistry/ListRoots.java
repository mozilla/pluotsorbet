package com.ibm.tck.javax.microedition.io.file.FileSystemRegistry;

import java.util.Enumeration;
import java.util.NoSuchElementException;

import javax.microedition.io.file.FileSystemListener;
import javax.microedition.io.file.FileSystemRegistry;

import com.ibm.tck.javax.microedition.io.file.support.TestCaseWithLog;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
public class ListRoots extends TestCaseWithLog {

	private static int newState = -1;
	private static String changedRoot = null;

	/**
	 * @see com.ibm.tck.client.TestCase#runTests()
	 */
	public void runTests() {
		if (isTestValid("test0001")) test0001();
		if (isTestValid("test0002")) test0002();
	}
	
	/*
	 * Tests listRoots()
	 */
	public void test0001() {
		Enumeration e = FileSystemRegistry.listRoots();
		if (e==null) {
			addOperationDesc("Enumeration was null");
			assertTrueWithLog("Tests listRoots()", false);
			return;
		} else {
			addOperationDesc("listRoots() returned:");
			int i= 0;
			while(e.hasMoreElements()) {
				i++;
				String root = (String)e.nextElement();
				addOperationDesc(i + ". " + root);
				if (root==null) {
					addOperationDesc("Root was null");
					assertTrueWithLog("Tests listRoots()", false);
					return;
				}
				if (!root.endsWith("/")) {
					addOperationDesc("Root does not end with slash: " + root);
					assertTrueWithLog("Tests listRoots()", false);
					return;
				}
			}
		}
		
		addOperationDesc("");
		
		boolean passed = false;
		if (!e.hasMoreElements()) {
			try {
				e.nextElement();
				addOperationDesc("NoSuchElementException expected");
				passed = false;
			} catch (NoSuchElementException ex) {
				addOperationDesc("NoSuchElementException thrown as expected");
				passed = true;
			}
		}
		
		assertTrueWithLog("Tests listRoots()", passed);
	}
	
	/*
	 * Tests that listRoots() changes when roots are mounted or unmounted
	 */
	public void test0002() {
		interactiveMessage("Remove media from a removable drive");
		
		FileSystemListener listener = new FileSystemListener() {
			public void rootChanged(int state, String rootName) {
				if (state == FileSystemListener.ROOT_ADDED)
					log("root added: " + rootName);
				if (state == FileSystemListener.ROOT_REMOVED)
					log("root removed: " + rootName);
				newState = state;
				changedRoot = rootName;
			}
		};
		
		FileSystemRegistry.addFileSystemListener(listener);
		
		newState = -1;
		interactiveMessage("Insert media into a removable drive");
		while(newState==-1) {
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {}
		}
		if (newState != FileSystemListener.ROOT_ADDED) {
			addOperationDesc("listener was not notified of media change");
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
			return;
		}
		if (!containsRoot(FileSystemRegistry.listRoots(), changedRoot)) {
			addOperationDesc("root was not added to listRoots()");
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
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
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
			return;
		}
		if (containsRoot(FileSystemRegistry.listRoots(), changedRoot)) {
			addOperationDesc("root was not removed to listRoots()");
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
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
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
			return;
		}
		if (!containsRoot(FileSystemRegistry.listRoots(), changedRoot)) {
			addOperationDesc("root was not added to listRoots()");
			assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", false);
			return;
		}
		
		assertTrueWithLog("Tests that listRoots() changes when roots are mounted or unmounted", true);
	}
	
	private static boolean containsRoot(Enumeration e, String rootName) {
		while(e.hasMoreElements()) {
			String r = (String)e.nextElement();
			if (r.equals(rootName)) {
				return true;
			}
		}
		return false;
	}

}
