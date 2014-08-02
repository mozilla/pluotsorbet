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

package com.sun.j2me.content;

import java.io.IOException;

import javax.microedition.content.ContentHandler;
import javax.microedition.content.ContentHandlerException;
import javax.microedition.content.Invocation;
import javax.microedition.content.Registry;
import javax.microedition.io.Connection;
import javax.microedition.io.ConnectionNotFoundException;


/**
 * Implementation of Invocation class.
 * <p>
 * This class MUST NOT have any public methods that are not also
 * public in Invocation (the superclass).  The sensitive methods
 * of the class MUST be package private.
 */
public final class InvocationImpl {
	
	public static Tunnel tunnel = null;
	public static final int UNDEFINED_TID = 0;
	
    /**
     * The Invocation delegating to this instance.
     * This field is public to Invocation can set it.
     * This allows the implementation to pass a InvocationImpl to
     * back to the Invocation class and it can wrap it in an Invocation
     * before passing it to the application.
     */
    public Invocation invocation;

    /**
     * The URL of the content; may be <code>null</code>.
     * URLs of up to and including 256 characters in length MUST be
     * supported. A URL with a length of zero is treated as
     * <code>null</code> and is ignored..
     */
    String url;

    /** The content type; may be <code>null</code>. */
    String type;

    /** The content handler ID; may be <code>null</code> */
    String ID;

    /** The action to perform on the content; may be <code>null</code> */
    String action;

    /** The array of arguments; may be <code>null</code> */
    String[] arguments;

    /** The length (returned by get0) of the argument array. */
    int argsLen;

    /** The data array; may be <code>null</code>. */
    byte[] data;

    /** The length (returned by get0) needed for the data array. */
    int dataLen;

    /**
     * Set to <code>true</code> if the invoker must be notified of
     * completion.
     */
    boolean responseRequired;

    /** The username in case it is needed for authentication. */
    String username;

    /** The password in case it is needed for authentication. */
    String password;

    /** Transaction Identifier. */
    int tid = UNDEFINED_TID;

    ApplicationID	destinationApp;
//    /** The MIDlet suite that should handle this Invocation. */
//    int suiteId;
//    /** The classname of the MIDlet to deliver to. */
//    String classname;

    /**
     * The status of the request; one of
     * {@link Invocation#ACTIVE},
     * {@link Invocation#WAITING},
     * {@link Invocation#ERROR},
     * {@link Invocation#OK}, or
     * {@link Invocation#CANCELLED}.
     */
    int status;

    /** The authority that authenticated this Invocation. */
    String invokingAuthority;

    /** The ID that authenticated this Invocation. */
    String invokingID;

    ApplicationID	invokingApp;
    
//    /** The MIDlet suite of the invoking application. */
//    int invokingSuiteId;
//    /** The classname in the invoking MIDlet suite for the response. */
//    String invokingClassname;

    /** The application name of the invoking MIDlet suite. */
    String invokingAppName;

    /** The previous invocation, if any. */
    InvocationImpl previous;

    /** The tid of the previous Invocation, if any. */
    int previousTid;

    /** A zero length array of strings to re-use when needed.  */
    private static final byte[] ZERO_BYTES = new byte[0];

    /**
     * The DISPOSE status is used with {@link #setStatus setStatus}
     * to discard the native Invocation. It must not overlap with
     * Status values defined in the Invocation class and must match
     * STATUS_DISPOSE defined in invocStore.c.
     */
    static final int DISPOSE = 100;

    /**
     * Create a fresh InvocationImpl.
     */
    InvocationImpl() {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + " is created" );
        status = Invocation.INIT;
        responseRequired = true;
        arguments = ContentHandlerImpl.ZERO_STRINGS;
        data = ZERO_BYTES;
        
        destinationApp = AppProxy.createAppID();
        invokingApp = AppProxy.createAppID();
    }

    /**
     * Create a fresh InvocationImpl that is being delegated to byte
     * an Invocation instance created by an application.
     * @param invocation the Invocation delegating to this implementation
     */
    public InvocationImpl(Invocation invocation) {
        this();
        this.invocation = invocation;
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
        this.arguments =
            (args == null) ? ContentHandlerImpl.ZERO_STRINGS : args;
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
        return arguments;
    }

    /**
     * Sets the data used for the Invocation.  The data
     * is used by the application to communicate to the content
     * handler and return data from the content handler.
     * @param data the byte data array; may be <code>null</code>.
     * A <code>null</code> is treated the same as a zero-length array
     * @see #getData
     */
    public void setData(byte[] data) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setData " + data );
        this.data = (data == null) ? ZERO_BYTES : data;
    }

    /**
     * Gets the data for the Invocation. The data
     * is passed to the content handler.
     * The content handler may modify and return the data
     * if it returns a response.
     * The array is not copied; modifications to array elements
     * will be visible.
     * @return the data array, which MUST NOT be <code>null</code>
     * @see #setData
     */
    public byte[] getData() {
        return data;
    }

    /**
     * Gets the URL for the invocation.
     * The URL must be equal to the value set with {@link #setURL setURL}.
     * @return the URL or <code>null</code> if it has not been set
     * @see #setURL
     */
    public String getURL() {
        return url;
    }

    /**
     * Sets the URL for the invocation.
     * @param url the URL to be set; may be <code>null</code>
     * @see #getURL
     */
    public void setURL(String url) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setURL " + url );
        this.url = url;
    }


    /**
     * Gets the content type for the Invocation.
     * @return the content type or <code>null</code> if it has not been set
     * @see #setType
     * @see #findType
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the type for the Invocation.
     * @param type the type to be set for the content; may be <code>null</code>
     * @see #getType
     */
    public void setType(String type) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setType " + type );
        this.type = type;
    }


    /**
     * Gets the action to be performed on the content.
     * @return the content action or <code>null</code> if it has not been set
     * @see #setAction
     */
    public String getAction() {
        return action;
    }

    /**
     * Sets the action to be performed on the content.
     * @param action the action to be performed on the content;
     *  may be <code>null</code>
     * @see #getAction
     */
    public void setAction(String action) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setAction " + action );
        this.action = action;
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
        return responseRequired;
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
     *        <code>INIT</code>
     * @see #getResponseRequired
     */
    public void setResponseRequired(boolean responseRequired) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setResponseRequired " + responseRequired );
        if (getStatus() != Invocation.INIT) {
            throw new IllegalStateException();
        }
        this.responseRequired = responseRequired;
    }

    /**
     * Gets the content handler ID for this Invocation.
     * @see Registry#forID
     * @return the ID of the ContentHandler; may be
     * <code>null</code>
     * @see #setID
     */
    public String getID() {
        return ID;
    }

    /**
     * Sets the ID of the content handler for this Invocation.
     * @param ID of the content handler; may be <code>null</code>
     * @see #getID
     */
    public void setID(String ID) {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".setID " + ID );
        this.ID = ID;
    }

    /**
     * Checks this Invocation and uses the ID, type, URL, and action
     * find a matching ContentHandler and queue this request to it.
     * The actual launching of the application is done in the Registry.
     *
     * If the <code>previous</code> Invocation is <code>null</code> then
     * a new transaction is created; otherwise, this
     * Invocation will use the same transaction as the
     * <code>previous</code> Invocation.
     * <p>
     * The status of this Invocation must be <code>INIT</code>.
     * If there is a previous Invocation, that Invocation must
     * have a status of <code>ACTIVE</code>.
     * <p>
     * Candidate content handlers are found as described in
     * {@link Registry#findHandler Registry.findHandler}.
     * If any handlers are
     * found, one is arbitrarily selected for this Invocation.
     * <p>
     * The status of this Invocation is set to <code>WAITING</code>.
     * If there is a non-null previous Invocation,
     * its status is set to <code>HOLD</code>.
     * A copy of the Invocation is made, the status is set to
     * <code>ACTIVE</code> and then queued to the
     * target content handler.
     * If the invoked content handler is not running, it must be started
     * as described in <a href="#execution">Invocation Processing</a>.
     *
     * <p>
     * The calling thread blocks while the content handler is being determined.
     * If a network access is needed there may be an associated delay.
     *
     * @param previous a previous Invocation for this Invocation;
     *  may be <code>null</code>
     * @param handler the ContentHandlerImpl that is the target
     *
     * @return <code>true</code> if the application MUST first
     *  voluntarily exit before the content handler can be started;
     *  <code>false</code> otherwise
     *
     * @exception IllegalArgumentException is thrown if:
     *  <ul>
     *     <li> the <code>classname</code> does not implement the
     *          lifecycle required by the Java runtime, or </li>
     *     <li> the ID, type, and URL are all <code>null</code>, or </li>
     *     <li> the argument array contains any <code>null</code>
     *          references</li>
     *  </ul>
     * @exception IOException is thrown if the URL to be accessed is
     *   not available
     * @exception ContentHandlerException is thrown with a reason of:
     *  <ul>
     *      <li><code>TYPE_UNKNOWN</code> if the type
     *          is not set and cannot be determined from the URL, or</li>
     *      <li><code>NO_REGISTERED_HANDLER</code> if
     *          there is no registered content handler for the type or
     *          ID</li>
     * </ul>
     * @exception IllegalStateException is thrown if the status of this
     *     Invocation is not <code>INIT</code> or if the status of the previous
     *     Invocation, if any, is not <code>ACTIVE</code>
     * @exception SecurityException if an invoke operation is not permitted
     */
    boolean invoke(InvocationImpl previous, ContentHandlerImpl handler)
        				throws IllegalArgumentException, IOException
    {
    	if( AppProxy.LOGGER != null )
    		AppProxy.LOGGER.println( getClass().getName() + ".invoke prev = " + 
    				previous + ", handler = '" + handler + "'" );
        /*
         * Check all of the arguments for validity.
         */
        for (int i = 0; i < arguments.length; i++) {
            if (arguments[i] == null) {
                throw new IllegalArgumentException("argument[" + i + "] is null");
            }
        }

        if (previous != null) {
            this.previous = previous;
            this.previousTid = previous.tid;
        }

        // Fill information about the target content handler.
        setStatus(Invocation.INIT);
        setID(handler.ID);
        destinationApp = handler.applicationID.duplicate();

        // Queue this Invocation
        InvocationStore.put(this);
        setStatus(Invocation.WAITING);
        
        // Set the status of the previous invocation
        if (previous != null) {
            previous.setStatus(Invocation.HOLD);
        }
        
        return InvocationStoreProxy.launchInvocationTarget( this ) == 
        					InvocationStoreProxy.LIT_MIDLET_START_FAILED;
    }

	/**
     * Finish this Invocation and set the status for the response.
     *
     * @param status the new status of the Invocation. This MUST be either
     *         <code>OK</code> or <code>CANCELLED</code>.
     *
     * @return <code>true</code> if the MIDlet suite MUST
     *   voluntarily exit before the response can be returned to the
     *   invoking application
     *
     * @exception IllegalArgumentException if the new
     *   <code>status</code> of the Invocation
     *    is not <code>OK</code> or <code>CANCELLED</code>
     */
    boolean finish(int status) {
    	if( AppProxy.LOGGER != null ){
    		AppProxy.LOGGER.println( "finish( " + status + "), Invocation " + this);
    	}
        switch( status ){
        	case Invocation.OK:
        	case Invocation.CANCELLED:
        	case Invocation.INITIATED: 
        		break;
        	default:
        		throw new IllegalArgumentException();
        }

        setStatus(status);

        if (getResponseRequired()) {
            if (destinationApp.isNative()) {
            	// 'native to java' invocation is finished
                return AppProxy.platformFinish(tid);
            }
            return InvocationStoreProxy.launchInvocationTarget( this ) == 
						InvocationStoreProxy.LIT_MIDLET_START_FAILED;
        }
        return false;
    }

    /**
     * Creates and opens a Connection to the content accessible by
     * using the URL. This method is
     * equivalent to
     * {@link javax.microedition.io.Connector#open Connector.open}
     * with the URL provided.
     * The application should use this method to access the
     * content of the URL
     * so that any type or content information cached by the
     * implementation can be fully utilized. The content is opened
     * in read only mode.
     *
     * @param timeouts         a flag to indicate that the caller
     *                         wants timeout exceptions
     * @return                 a Connection object
     *
     * @exception ConnectionNotFoundException is thrown if:
     *   <ul>
     *      <li>there is no URL, or</li>
     *      <li>the target URL cannot be found, or</li>
     *      <li>the requested protocol type is not supported</li>
     *   </ul>
     * @exception IOException  if some other kind of I/O error occurs
     * @exception SecurityException  may be thrown if access to the
     *   protocol handler is prohibited
     */
    public Connection open(boolean timeouts) throws IOException
    {
        url.length(); // null check
        ContentReader reader = new ContentReader(url, username, password);
        return reader.open(timeouts);
    }

    /**
     * Provide the credentials needed to access the content.
     * @param username the username; may be <code>null</code>
     * @param password the password for the username;
     *   may be <code>null</code>
     */
    public void setCredentials(String username, char[] password) {
        this.username = username;
        this.password = (password == null) ? null : new String(password);
    }

    /**
     * Returns the status of this Invocation, which can be
     * <code>INIT</code>, <code>WAITING</code>, <code>HOLD</code>,
     * <code>ACTIVE</code>, <code>OK</code>,
     * <code>CANCELLED</code>, or <code>ERROR</code>.
     * The application uses the status to determine how
     * to process an Invocation returned from
     * <code>getInvocation</code>.
     *
     * @see javax.microedition.content.Registry#invoke
     *
     * @return the current status of this Invocation
     */
    public int getStatus() {
        return status;
    }

    /**
     * Set the status of this InvocationImpl.
     * If the invocation is still active in the native code
     * set the status in native also.
     * @param status the new status
     */
    void setStatus(int status) {
        this.status = status;
        
        switch( this.status ){
        	case Invocation.OK: 
        	case Invocation.CANCELLED: 
        	case Invocation.ERROR: 
        	case Invocation.INITIATED:
                /* 
                 * If a response is required, switch the target
                 * application; if not then discard the Invocation.
                 */
                if (!responseRequired){
                	if( tid != UNDEFINED_TID ){
	                	InvocationStore.dispose(tid);
	                	tid = UNDEFINED_TID;
                	}
                	return;
                }
                	
                /* Swap the source and target applications */
                ApplicationID tmpApp = invokingApp;
                invokingApp = destinationApp;
                destinationApp = tmpApp;

                InvocationStore.update(this);
                /* Unmark the response it is "new" to the target */
                InvocationStore.resetFlags(tid);
        		break;
        	default:
                InvocationStore.update(this);
        		break;
        }
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
     */
    public String findType() throws IOException, ContentHandlerException, SecurityException
    {
        if (type != null) {
            return type;
        }

        if (url != null) {
            ContentReader reader = new ContentReader(url, username, password);
            String type = reader.findType();
            if (type != null) {
                this.type = type;
                return type;
            }
        } else if (data.length > 0) {
            // TODO: try to determine type by data signature
        }

        throw new ContentHandlerException(
                "Can not determine the content type",
                                ContentHandlerException.TYPE_UNKNOWN);
    }

    /**
     * Returns the previous Invocation linked to this
     * Invocation by this application's previous call to
     * {@link Registry#invoke(Invocation invoc, Invocation previous)}.
     *
     * @return the previous Invocation, if any, set when this
     *        Invocation was invoked;
     *        <code>null</code> is returned if the Invocation was not
     *  invoked with a previous Invocation.
     */
    public InvocationImpl getPrevious() {
        return previous;
    }

    /**
     * Gets the authority, if any, used to authenticate the
     * application that invoked this request.
     * This value MUST be <code>null</code> unless the device has been
     * able to authenticate this application.
     * If <code>non-null</code>, it is the string identifying the
     * authority.  For example,
     * if the application was a signed MIDlet, then this is the
     * "subject" of the certificate used to sign the application.
     *
     * <p>The format of the authority for X.509 certificates is defined
     * by the MIDP Printable Representation of X.509 Distinguished
     * Names as defined in class
     *
     * <code>javax.microedition.pki.Certificate</code>. </p>
     * @return the authority used to authenticate this application
     * or <code>null</code> otherwise
     *
     * @exception IllegalStateException if the current status is not
     * <code>ACTIVE</code> or <code>HOLD</code>
     *
     * @see ContentHandler#getAuthority
     */
    public String getInvokingAuthority() {
        if (status != Invocation.ACTIVE && status != Invocation.HOLD) {
            return null;
        }
        return invokingAuthority;
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
        if (status != Invocation.ACTIVE && status != Invocation.HOLD) {
            return null;
        }
        return invokingAppName;
    }

    /**
     * Gets the ID of the application that invoked the content
     * handler. This information is available only if the status is
     * <code>ACTIVE</code> or <code>HOLD</code>.
     *
     * This information has been authenticated only if
     * <code>getInvokingAuthority</code> is non-null.
     *
     * @return the application's ID if status is <code>ACTIVE</code>
     * or <code>HOLD</code>; <code>null</code> otherwise
     *
     * @exception IllegalStateException if the current status is not
     * <code>ACTIVE</code> or <code>HOLD</code>
     *
     * @see ContentHandler#getID
     */
    public String getInvokingID() {
        if (status != Invocation.ACTIVE && status != Invocation.HOLD) {
            return null;
        }
        return invokingID;
    }

    /**
     * Return a printable form of InvocationImpl.
     * Disabled if not logging
     * @return a String containing a printable form
     */
    public String toString() {
        if (AppProxy.LOGGER != null) {
            StringBuffer sb = new StringBuffer(200);
            
            sb.append("tid: "); sb.append(tid);
        	sb.append( " status = ");
        	String s = "{" + status + "}";
        	switch( status ){
    	    	case Invocation.ACTIVE: s = "ACTIVE"; break;
    	    	case Invocation.WAITING: s = "WAITING"; break;
    	    	case Invocation.ERROR: s = "ERROR"; break;
    	    	case Invocation.OK: s = "OK"; break;
    	    	case Invocation.CANCELLED: s = "CANCELLED"; break;
    	    	case Invocation.HOLD: s = "HOLD"; break;
    	    	case Invocation.INIT: s = "INIT"; break;
    	    	case Invocation.INITIATED: s = "INITIATED"; break;
        	}
        	sb.append(s + ", handlerID = '" + ID + "'" );

            sb.append("\n type: ");      sb.append(getType());
            sb.append(", url: ");       sb.append(getURL());
            sb.append(", respReq: ");   sb.append(getResponseRequired());
            sb.append("\n   invokee: "); sb.append(destinationApp);
            sb.append(", invoker: "); sb.append(invokingApp);
            return sb.toString();
        }
        return super.toString();
    }

	public Invocation wrap() {
		if( invocation == null )
			invocation = tunnel.newInvocation(this);
	    return invocation;
	}
}
