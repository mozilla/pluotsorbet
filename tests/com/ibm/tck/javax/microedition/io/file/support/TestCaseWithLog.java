package com.ibm.tck.javax.microedition.io.file.support;

import java.io.IOException;
import java.util.Enumeration;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import com.ibm.tck.client.TestCase;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */

public abstract class TestCaseWithLog extends TestCase {
	
	private StringBuffer operationTrace = new StringBuffer();
	
	private String testPath = null;
	
	protected String getTestPath() {
		if (testPath == null) {
			testPath = (String)getOptions().get("FilesystemTestPath");
		}
		return testPath;
	}
	
	protected void ensureFileExists(FileConnection conn) throws IOException {
		if (conn.exists()) {
			// if the file already exists, delete it to ensure zero length when created again
			recursiveDelete(conn);
		}
		
		try {
			conn.create();
		} catch (Exception e) {
			throw new IOException("could not create file <" + conn.getURL() + "> (" + e.getMessage() + ")");
		}
	}
	
	protected void ensureDirExists(FileConnection conn) throws IOException {
		if (conn.exists()) {
			// if the dir already exists, delete it to ensure zero length when created again
			recursiveDelete(conn);
		}
		
		try {
			conn.mkdir();
		} catch (Exception e) {
			throw new IOException("could not create directory <" + conn.getURL() + "> (" + e.getMessage() + ")");
		}
	}
	
	protected void ensureNotExists(FileConnection conn) throws IOException {
		if (conn.exists()) {
			recursiveDelete(conn);
		}
	}
	
	private void recursiveDelete(FileConnection conn) throws IOException {
		try {
			if (conn.isDirectory()) {
				for (Enumeration e=conn.list("*", true); e.hasMoreElements(); ) {
					String fileName = (String)e.nextElement();
					FileConnection childConn = null;
					try {
						childConn = (FileConnection)Connector.open(conn.getURL() + fileName);
						recursiveDelete(childConn);
					} finally {
						if (childConn != null) {
							childConn.close();
						}
					}
				}
			}
			conn.setWritable(true);
			conn.delete();
		} catch (IOException e) {
			throw new IOException("could not delete <" + conn.getURL() + "> (" + e.getMessage() + ")");
		}
	}

	protected void logUnexpectedExceptionDesc(Exception e) {
		logTrace();
		StringBuffer message = new StringBuffer(80);
		message.append("Unexpected exception: (");
		message.append(e.getClass().getName());
		message.append(") ");
		message.append(e.getMessage());
		log(message.toString());
	}
	public void assertTrueWithLog(String message, boolean passed) {
		logTrace();
		assertTrue(message, passed);
	}

	protected void addOperationDesc(String comment) {
		StringBuffer buffer = new StringBuffer(comment.length() + 3);
		buffer.append("  ");
		buffer.append(comment);
		buffer.append('\n');
		operationTrace.append(buffer.toString());
	}
	
	private void logTrace() {
		if (operationTrace.length() != 0) {
			log("Operation trace:");
			log(operationTrace.toString());
			// clear the trace.
			operationTrace.delete(0, operationTrace.length());
		}
	}

}
