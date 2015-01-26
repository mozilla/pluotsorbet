package com.ibm.tck.client;

/*
 * Licensed Materials - Property of IBM,
 * (c) Copyright IBM Corp. 2003  All Rights Reserved
 */
 
/**
 * ClientConnection must be implemented and included in any test suite
 * suites. Implementations of this interface must be able to open the
 * <code>url</code> passed and send the entire byte array <code>data</code>
 * via the POST method to the url.  The server will be able to handle chunked
 * and non-chunked data.
 */
public interface ClientConnection {
	
	/**
	 * This method is used to post data to the server via a URL.
	 * The protocol MUST be via HTTP 1.1 using the POST method.
	 * 
	 * @param url The full URL for providing the test results
	 * @param data A byte array containing data.
	 */
	public abstract void postData(String url, byte[] data);

}
