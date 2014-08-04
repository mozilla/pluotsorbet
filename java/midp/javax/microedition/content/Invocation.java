/*
 *  
 *
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation. 
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt). 
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA 
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions. 
 */

package javax.microedition.content;

import java.io.IOException;

import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;

import com.sun.j2me.content.InvocationImpl;

/**
 * An <tt>Invocation</tt> contains the parameters that
 * are passed from an invoking application to a content handler and
 * the results that are returned to the application.
 * The parameters are the type, URL, action, 
 * content handler ID, and responseRequired.
 * The string and data arguments can be set using
 * {@link #setArgs setArgs} and {@link #setData setData} methods.
 * All of the parameters are provided to the content handler and
 * are returned with the handlers response, if any.
 * Invocation instances are not thread safe, the application 
 * must handle any synchronization necessary. </p>
 * <p>
 * The values of content handler ID, type, URL, and action are used to
 * identify the content handler when invoked by
 * {@link Registry#invoke Registry.invoke}.
 * If an Invocation contains an ID then it is used to identify the
 * content handler to be invoked.  The other parameters are input to
 * the content handler.
 * <p>If a type is present, it is used to find handlers that support
 * that type. The application should supply the type if it is known.
 * If the type is not set, then calling the
 * {@link #findType findType} will find the type using the URL to the
 * content. 
 * 
 * <h3>Invocation Status</h3>
 * <P>
 * The status value indicates the next processing step of
 * the invocation by the content handler.
 * The status of an Invocation can be any of the following:
 * <ul>
 * <li>{@link #INIT}&#151;indicates the Invocation is still being
 *  initialized</li>
 * <li>{@link #WAITING}&#151;indicates that this Invocation is waiting
 *   to complete </li>
 * <li>{@link #ACTIVE}&#151;indicates the Invocation is currently
 *  being processed </li>
 * <li>{@link #HOLD}&#151;indicates the Invocation is currently
 *  waiting for a chained Invocation to complete </li>
 * <li>{@link #ERROR}, {@link #OK}, {@link #CANCELLED}&#151;
 *  indicate that the Invocation is complete</li> 
 * <LI>{@link #INITIATED}&#151;
 *  indicate that the Invocation has been initiated but the content
 *  handler cannot provide a response when it is finished.
 * </ul>
 *
 * <p>All status transitions occur only during method calls that involve
 * the Invocation instance.  The transitions that occur are specified
 * in the methods that make the change visible.
 * For example, when an invoking application creates a new Invocation, the
 * status is {@link #INIT INIT}.
 * When the application calls
 * {@link Registry#invoke Registry.invoke}
 * the status changes to {@link #WAITING WAITING}.  
 * When the <code>Registry.getResponse</code> method is invoked, the
 * status will be updated to the appropriate {@link #OK OK},
 * {@link #CANCELLED CANCELLED},
 * {@link #INITIATED INITIATED}, or {@link #ERROR ERROR} status from 
 * the content handler.
 *
 * <p>
 * A content handler calls
 * {@link ContentHandlerServer#getRequest ContentHandlerServer.getRequest}
 * to get the next request.
 * The request always has the {@link #ACTIVE ACTIVE} status.
 * When the handler is finished acting on the content, the status
 * is set to either {@link #OK OK}, {@link #CANCELLED CANCELLED}, or
 * {@link #INITIATED INITIATED} by
 * the {@link ContentHandlerServer#finish ContentHandlerServer.finish} method.
 * <p>
 * If the handler is chaining, then the new Invocation follows the status
 * transitions of <code>invoke</code> as described above.  The status of the
 * previous invocation being chained from is set to {@link #HOLD HOLD}
 * by the <code>Registry.invoke</code> method. The status of the previous
 * Invocation is restored to {@link #ACTIVE ACTIVE} by the
 * {@link Registry#getResponse Registry.getResponse}
 * method that returns the status for the new Invocation.
 * <p>
 * If the content handler application causes faults because it
 * does not properly dequeue and respond to invocations as described
 * in the {@link ContentHandler} class, then the
 * status is set to {@link #ERROR ERROR} in the response queued back
 * to the invoking application.
 *
 * <H3>Access to Content</H3>
 * <P>
 * The implementation of the invocation mechanism may save or cache
 * information about the request, the URL, the content type, or content
 * during the invocation. The information may be
 * utilized when the application accesses the content with the
 * {@link #open open} method.
 * The {@link #getURL} method MUST return the original URL unmodified
 * by any implementation specific information.
 */
public final class Invocation {
	static {
		Tunnel.initialize();
	}
	
    /** The InvocationImpl to delegate to. */
    private InvocationImpl invocImpl;

    /**
     * This Invocation was just constructed and is being initialized.
     */
    public static final int INIT = 1;

    /**
     * This Invocation is a new request and is being handled
     * by the content handler.
     */
    public static final int ACTIVE = 2;

    /**
     * This Invocation has been invoked and is waiting to be
     * complete.
     * @see Registry#invoke
     */
    public static final int WAITING = 3;

    /**
     * This Invocation is on hold until a chained
     * Invocation is completed.
     * @see Registry#invoke
     */
    public static final int HOLD = 4;


    /**
     * The content handler successfully completed processing
     * the Invocation.
     * Invocations queued with
     * {@link ContentHandlerServer#finish ContentHandlerServer.finish}
     * will have this status.
     */
    public static final int OK = 5;

    /**
     * The processing of the Invocation was canceled by
     * the ContentHandler.
     * Invocations queued with
     * {@link ContentHandlerServer#finish ContentHandlerServer.finish}
     * will have this status.
     */
    public static final int CANCELLED = 6;

    /**
     * The content handler failed to correctly process the Invocation
     * request.
     */
    public static final int ERROR = 7;

    /**
     * The processing of the Invocation has been initiated and will
     * continue. This status is only appropriate when the content
     * handler can not provide a response when it is finished.
     */
    public static final int INITIATED = 8;

    /**
     * Creates a new Invocation.
     * The status of the new Invocation object is <code>INIT</code>.
     * The URL, type, ID, action, arguments, and data are set to
     * empty arrays, and initialized to require a response.
     */
    public Invocation() {
    	invocImpl = new InvocationImpl(this);
    }

    /**
     * Convenient alternative constructor with URL, type, and ID.
     * The behavior is identical to
     * <code>new Invocation(url, type, ID, true, null)</code>.
     *
     * @param url the URL of the content to be dispatched;
     *	may be <code>null</code>
     * @param type the content type; may be <code>null</code>
     * @param ID the ID of the content handler; may be <code>null</code>
     */
    public Invocation(String url, String type, String ID) {
    	this(url, type, ID, true, null);
    }

    /**
     * Convenient alternative constructor with URL and type.
     * The behavior is identical to
     * <code>new Invocation(url, type, null, true, null)</code>.
     *
     * @param url the URL of the content to be dispatched;
     *	may be <code>null</code>
     * @param type the content type; may be <code>null</code>
     */
    public Invocation(String url, String type) {
    	this(url, type, null, true, null);
    }

    /**
     * Convenient alternative constructor with a URL.
     * The behavior is identical to
     * <code>new Invocation(url, null, null, true, null)</code>.
     *
     * @param url the URL of the content to be dispatched;
     *	may be <code>null</code>
     */
    public Invocation(String url) {
    	this(url, null, null, true, null);
    }

    /**
     * Creates a new instance and initializes it from the
     * specified parameters.
     * The status of the new Invocation is <code>INIT</code>.
     * None of the values are checked until the
     * {@link Registry#invoke Registry.invoke}
     * method is called.
     * String arguments or data can be set with
     * {@link #setArgs setArgs} or {@link #setData setData}.
     *
     * @param url the URL of the content to be dispatched;
     *	may be <code>null</code>
     * @param type the content type; may be <code>null</code>
     * @param ID the ID of the content handler; may be <code>null</code>
     * @param responseRequired <code>true</code> if a response is
     * required; <code>false</code> otherwise
     * @param action the action that the content handler should perform on the
     *  content; may be <code>null</code>
     */
    public Invocation(String url, String type, String ID,
		      boolean responseRequired, String action)
    {
		this();
		invocImpl.setURL(url);
		invocImpl.setType(type);
		invocImpl.setID(ID);
		invocImpl.setResponseRequired(responseRequired);
		invocImpl.setAction(action);
    }

    /**
     * Creates a new Invocation to refer to an InvocationImpl.
     * And makes the InvocationImpl refer to the new Invocation.
     * @param impl and InvocationImpl to be associated with this Invocation
     */
    Invocation(InvocationImpl impl) {
		invocImpl = impl;
		if (impl != null) {
		    impl.invocation = this;
		}
    }


    /**
     * Sets the argument list to a new array of Strings.  The arguments
     * are used by the application to communicate to the content
     * handler and return results from the content handler.
     * The values of the arguments are not checked when they are set.
     * Instead, they are checked during
     * {@link Registry#invoke Registry.invoke} to
     * check that none of the values are <code>null</code>.
     * @param args the String array; may be <code>null</code>.
     * A <code>null</code>
     * argument is treated the same as a zero-length array
     * @see #getArgs
     */
    public void setArgs(String[] args) {
    	invocImpl.setArgs(args);
    }

    /**
     * Gets the argument list as an array of Strings. These values
     * are passed to the content handler and are returned from
     * the content handler.
     * The array is not copied; modifications to array elements 
     * will be visible.
     * @return the arguments array, which MUST NOT be <code>null</code>
     * @see #setArgs
     */
    public String[] getArgs() {
    	return invocImpl.getArgs();
    }

    /**
     * Sets the data used for the Invocation.  The data
     * is used by the application to communicate to the content
     * handler and return data from the content handler.
     * The array is not copied until the Invocation is <code>invoked</code>
     * or <code>finish</code>ed; 
     * modifications to array elements will otherwise be visible.
     *
     * @param data the byte data array; may be <code>null</code>.
     * A <code>null</code> is treated the same as a zero-length array
     * @see #getData
     */
    public void setData(byte[] data) {
    	invocImpl.setData(data);
    }

    /**
     * Gets the data for the Invocation. The data
     * is passed to the content handler.
     * The content handler may modify and return the data
     * if it returns a response.
     *
     * @return the data array, which MUST NOT be <code>null</code>
     * @see #setData
     */
    public byte[] getData() {
    	return invocImpl.getData();
    }

    /**
     * Gets the URL for the invocation.
     * The URL must be equal to the value set with {@link #setURL setURL}.
     * @return the URL or <code>null</code> if it has not been set
     * @see #setURL
     */
    public String getURL() {
    	return invocImpl.getURL();
    }

    /**
     * Sets the URL for the invocation.
     * @param url the URL to be set; may be <code>null</code>
     * @see #getURL
     */
    public void setURL(String url) {
    	invocImpl.setURL(url);
    }

    /**
     * Gets the content type for the Invocation.
     * The type for this Invocation may be set by the application using
     * {@link #setType setType}.
     * The {@link #findType findType} method can be used by an application
     * to find the type by accessing the content via the URL.
     * When found, <code>findType</code> sets the type returned
     * by <code>getType</code>.
     * 
     * @return the content type or <code>null</code> if it has not been set
     * @see #setType
     * @see #findType
     */
    public String getType() {
    	return invocImpl.getType();
    }

    /**
     * Sets the type for the Invocation.
     * @param type the type to be set for the content; may be <code>null</code>
     * @see #getType
     * @see #findType
     */
    public void setType(String type) {
    	invocImpl.setType(type);
    }

    /**
     * Gets the action to be performed on the content.
     * @return the content action or <code>null</code> if it has not been set
     * @see #setAction
     */
    public String getAction() {
    	return invocImpl.getAction();
    }

    /**
     * Sets the action to be performed on the content.
     * @param action the action to be performed on the content;
     *  may be <code>null</code>
     * @see #getAction
     */
    public void setAction(String action) {
    	invocImpl.setAction(action);
    }


    /**
     * Gets the <code>responseRequired</code> mode for
     * this Invocation.
     * If <code>true</code>, then the invoking application requires a
     * response to the Invocation.
     * @return the current value of the <code>responseRequired</code>
     * mode. If
     * <code>true</code>, then a response must be returned to the
     * invoking application.
     * @see #setResponseRequired
     */
    public boolean getResponseRequired() {
    	return invocImpl.getResponseRequired();
    }

    /**
     * Sets the <code>responseRequired</code> mode for
     * this Invocation.
     * If <code>true</code>, then the invoking application requires a
     * response to the Invocation.
     * The value in the request can be changed only if the status is
     * <code>INIT</code>.
     * @param responseRequired
     * <code>true</code> to require a response,
     * <code>false</code> otherwise
     * @exception IllegalStateException is thrown if the status is not
     *	<code>INIT</code>
     * @see #getResponseRequired
     */
    public void setResponseRequired(boolean responseRequired) {
    	invocImpl.setResponseRequired(responseRequired);
    }

    /**
     * Gets the status of this Invocation, which can be
     * <code>INIT</code>, <code>WAITING</code>, <code>HOLD</code>,
     * <code>ACTIVE</code>, <code>OK</code>,
     * <code>CANCELLED</code>,
     * <code>INITIATED</code>, or <code>ERROR</code>.
     *
     * @return the status of this Invocation
     *
     * @see Registry#invoke
     * @see Registry#getResponse
     * @see ContentHandlerServer#getRequest
     * @see ContentHandlerServer#finish
     */
    public int getStatus() {
    	// Delegated to the implementation class
    	return invocImpl.getStatus();
    }

    /**
     * Gets the content handler ID for this Invocation.
     *
     * @return the ID of the ContentHandler; may be
     * <code>null</code>
     * @see Registry#forID
     * @see #setID
     */
    public String getID() {
    	// Delegated to the implementation class
    	return invocImpl.getID();
    }

    /**
     * Sets the ID of the content handler for this Invocation.
     * @param ID of the content handler; may be <code>null</code>
     * @see #getID
     */
    public void setID(String ID) {
    	// Delegated to the implementation class
    	invocImpl.setID(ID);
    }

    /**
     * Gets the previous Invocation saved in this
     * Invocation by 
     * {@link Registry#invoke Registry.invoke} or
     * {@link Registry#getResponse Registry.getResponse}.
     * Invocations returned by 
     * {@link ContentHandlerServer#getRequest ContentHandlerServer.getRequest}
     * MUST return <code>null</code>.
     *
     * @return the previous Invocation, if any, saved when this
     *	Invocation was invoked or returned as a response;
     *  may be <code>null</code> 
     *
     * @see Registry#invoke
     * @see Registry#getResponse
     */
    public Invocation getPrevious() {
		InvocationImpl prev = invocImpl.getPrevious();
		if (prev != null) {
		    if (prev.invocation == null) {
				/*
				 * An InvocationImpl created by the implementation needs
				 * a Invocation to return to the application.
				 */
				prev.invocation = new Invocation(prev);
		    }
		    return prev.invocation;
		}
	    return null;
    }

    /**
     * Gets the authority, if any, used to authenticate the
     * application that invoked this request.
     * This value MUST be <code>null</code> unless the device has been
     * able to authenticate this application.
     * If <code>non-null</code>, it is the string identifying the
     * authority.
     *
     * @return the authority used to authenticate this application
     * and if the status is either <code>ACTIVE</code> or <code>HOLD</code>;
     * otherwise it is <code>null</code>,
     * the application has not been authenticated
     *
     * @see ContentHandler#getAuthority
     */
    public String getInvokingAuthority() {
		// Delegated to the implementation class
		return invocImpl.getInvokingAuthority();
    }

    /**
     * Gets the ID of the application that invoked the content
     * handler. This information is available only if the status is
     * <code>ACTIVE</code> or <code>HOLD</code>.
     *
     * This information has been authenticated only if
     * <code>getInvokingAuthority</code> is non-null.
     *
     * @return the invoking application's ID if the Invocation status
     * is <code>ACTIVE</code>
     * or <code>HOLD</code>; <code>null</code> otherwise
     *
     * @see Registry#getID
     */
    public String getInvokingID() {
		// Delegated to the implementation class
		return invocImpl.getInvokingID();
    }

    /**
     * Get the user-friendly name of the application that invoked
     * the content handler. This information is available only if the status is
     * <code>ACTIVE</code> or <code>HOLD</code>.
     *
     * This information has been authenticated only if
     * <code>getInvokingAuthority</code> is non-null.
     *
     * @return the application's name if status is <code>ACTIVE</code>
     * or <code>HOLD</code>; <code>null</code> otherwise
     *
     * @see ContentHandler#getID
     */
    public String getInvokingAppName() {
		// Delegated to the implementation class
		return invocImpl.getInvokingAppName();
    }

    /**
     * Finds the type of the content in this Invocation.
     * If the <tt>getType</tt> method return value is
     * <code>non-null</code>, then the type is returned. 
     * <p>
     * If the type is <code>null</code> and the URL is non-<code>null</code>,
     * then the content type will be found by accessing the content
     * through the URL.
     * When found, the type is set as if the <code>setType</code> method 
     * was called;  subsequent calls to
     * {@link #getType getType} and {@link #findType findType}
     * will return the type.  
     * If an exception is thrown, the <code>getType</code> method will
     * return <code>null</code>.
     * <p>
     * The calling thread blocks while the type is being determined.
     * If a network access is needed there may be an associated delay.
     *
     * @return the <code>non-null</code> content type
     * @exception IOException if access to the content fails
     *
     * @exception ContentHandlerException is thrown with a reason of
     * {@link ContentHandlerException#TYPE_UNKNOWN}
     *  if the type is <code>null</code> and cannot be found from the
     *  content either because the URL is <code>null</code> or the type is
     *  not available from the content
     * @exception IllegalArgumentException if the content is accessed via
     *  the URL and the URL is invalid
     * @exception SecurityException is thrown if access to the content
     *  is required and is not permitted
     *
     * @see Invocation#setType
     * @see Invocation#getType
     */
    public String findType()
			throws IOException, ContentHandlerException, SecurityException
    {
		// Delegated to the implementation class
		return invocImpl.findType();
    }

    /**
     * Creates and opens a Connection to the content addressed by
     * the URL in {@link #getURL getURL}. This method is
     * similar to <code>Connector.open(getURL(), READ, timeouts)</code>
     * but may deliver the content from a cache.
     * The application should use this method to access the
     * content of the URL
     * so that any type or content information cached by the
     * implementation can be utilized. The content is opened
     * in read only mode.
     * Regardless of whether or not the content is cached, the
     * application or content handler must have permission to access
     * the content via the URL.
     *
     * @param timeouts         a flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 a Connection object
     *
     * @exception ConnectionNotFoundException is thrown if:
     *   <ul>
     *      <li>the target URL can not be found, or</li>
     *      <li>the requested protocol type is not supported</li>
     *   </ul>
     * @exception NullPointerException if the URL is null
     * @exception IllegalArgumentException if a parameter is invalid.
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException is thrown if access to the
     *   protocol handler is prohibited
     */
    public Connection open(boolean timeouts)
        		throws IOException, SecurityException
    {
        return invocImpl.open(timeouts);
    }    

    /**
     * Provide the credentials needed to access the content.
     * Use of the credential is protocol specific.
     * @param username the username; may be <code>null</code>
     * @param password the password for the username;
     *   may be <code>null</code>
     */
    public void setCredentials(String username, char[] password) {
    	invocImpl.setCredentials(username, password);
    }

    /**
     * Gets the InvocationImpl for this Invocation.
     * @return the InvocationImpl delegate.
     */
    InvocationImpl getInvocImpl() {
    	return invocImpl;
    }

    /**
     * Sets the InvocationImpl for this Invocation.
     * @param invocImpl the InvocationImpl delegate.
     */
    void setInvocImpl(InvocationImpl invocImpl) {
    	this.invocImpl = invocImpl;
    }
}
