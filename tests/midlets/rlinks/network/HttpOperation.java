/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network;

import javax.microedition.io.HttpConnection;

/**
 * An abstract base class for HTTP-based network operations. Actual
 * asynchronous network operations can be implemented by extending
 * this class.
 *
 * What exactly happens when start() (or abort()) is called is left up to the
 * implementation to decide. By default, a HttpOperation enqueues itself
 * to be executed by the HttpClient.
 *
 * The response bytes received might be text or binary; how it will be
 * handled is also up to the implementation of <em>onResponseReceived</em>.
 */
    public abstract class HttpOperation {

    /**
     * Base URL for all Reddit HTTP requests.
     */
    public static final String BASE_URL = "http://www.reddit.com/";

    /**
     * Determine whether an HttpOperation needs a reload
     * (was not started, is not running, was aborted or not successfully completed).
     *
     * @param operation HttpOperation to check
     * @return True if needs a reload, false otherwise
     */
    public static boolean reloadNeeded(HttpOperation operation) {
        // Not started at all
        if (operation == null) {
            return true;
        }
        // Operation was completed successfully, without being aborted
        else if (operation.isFinished() && !operation.isAborted()) {
            return false;
        }
        // Previous operation still loading: do not start another one
        else if (!operation.isFinished()) {
            return false;
        }

        return true;
    }

    /**
     * Internal flag for whether this Operation has been aborted.
     */
    protected boolean aborted = false;

    /**
     * Internal flag for whether this Operation has been finished.
     */
    protected boolean finished = false;
    
    /**
     * Start the operation in asynchronous manner. By default enqueues the
     * operation in HttpClient. Override for custom behavior.
     */
    public void start() {
        HttpClient.enqueue(this);
    }

    /**
     * Abort the operation. The only effect this has by default is that
     * HttpClient will not start running aborted operations. The 'aborted'
     * flag must be manually taken into account in the implementing class.
     */
    public void abort() {
        HttpClient.abort(this);
        aborted = true;
    }

    /**
     * Tell whether this operation has been finished.
     *
     * @return True if finished, false otherwise
     */
    public boolean isFinished() {
        return finished;
    }

    /**
     * Tell whether this operation has been aborted.
     *
     * @return True if aborted, false otherwise
     */
    public boolean isAborted() {
        return aborted;
    }

    /**
     * Called when the response is received. Might be null or empty in case
     * of an error.
     *
     * @param response The response as a byte array
     */
    public abstract void responseReceived(byte[] response);

    /**
     * Get the target URL the operation will request.
     *
     * @return url Operation URL
     */
    public abstract String getUrl();

    /**
     * Get data to be sent in the request body. Null means no body will be sent
     * (default value).
     *
     * Override this method to have a body sent in the HTTP request.
     *
     * @return data Data to send in request body
     */
    public String getRequestBody() {
        return null;
    }

    /**
     * The HTTP request method used by this Operation, as defined in the
     * interface javax.microedition.io.HttpConnection. GET by default.
     * Can be overridden for alternative request methods.
     *
     * @return HTTP method to use in request
     */
    public String getRequestMethod() {
        return HttpConnection.GET;
    }

    public String getRequestContentType() {
        return "application/x-www-form-urlencoded";
    }

    /**
     * Override to disable cookies for the operation.
     *
     * @return Whether cookies are used for this Operation.
     */
    public boolean isCookiesEnabled() {
        return true;
    }
}
