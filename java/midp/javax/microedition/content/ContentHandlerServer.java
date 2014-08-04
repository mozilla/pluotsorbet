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


/**
 *<tt>ContentHandlerServer</tt> provides methods
 * to get new Invocation requests, to finish the processing
 * of requests and to get the access control information.
 * This server interface extends {@link ContentHandler}
 * to make available the registration information for types,
 * suffixes, actions, ID, etc. 
 * Instances are thread safe.
 *
 * <h3>Responding to an Invocation</h3>
 * <p>Content handler applications process requests using
 * either blocking calls to {@link #getRequest getRequest} or can be
 * notified of  
 * new requests with the {@link #setListener setListener} method.
 * A content handler receives an Invocation by calling
 * {@link #getRequest getRequest}.
 * The content handler should use the
 * {@link Invocation#getAction Invocation.getAction}
 * method to determine the requested action and act on the content
 * appropriately.
 * The content handler will typically call the
 * {@link Invocation#open Invocation.open} method to read the content.
 * The <code>open</code> method returns a Connection from the Generic
 * Connection framework that provides access to the content.
 * When the content handler is finished processing the Invocation,
 * it must call the
 * {@link #finish finish} method to report the status.
 * If a response was required the status and parameters are returned
 * to the invoking application.
 *
 * <h3>Required Response to the Invoking Application</h3>
 * <p>
 * The invoking application decides whether it needs a response and
 * sets the request state before calling
 * {@link Registry#invoke Registry.invoke}.
 * When an Invocation is completed either by using the
 * {@link #finish finish}
 * method or when the AMS is handling an error condition,
 * the {@link Invocation#getResponseRequired Invocation.getResponseRequired}
 * method is checked.
 * If it is <code>true</code>, then the values from the Invocation are
 * queued to the invoking application with the status set
 * by the ContentHandler or AMS.
 * When a response is queued, it will be dispatched to the invoking
 * application.
 * If a response is not required, it is not delivered to the invoking
 * application and the invoking application is not started. 
 *
 * <H3>Chaining Content Handlers</H3>
 * <p> Content handlers link Invocations that are part of
 * a user-driven task and depend on each other as part of a transaction.
 * Suppose an application <i>A</i> creates an invocation
 * <i>a</i>. When invoked, it is dispatched to content
 * handler <i>B</i> which in-turn creates an invocation <i>b</i>
 * and it is dispatched to content handler <i>C</i>.  C displays the
 * content and returns a response <i>b'</i> to B, B in turn
 * completes processing and returns a response <i>a'</i> to A.</p>
 * <p>
 * The implementation MUST have the capacity and mechanisms to support
 * the chaining of requests required for an application to invoke a
 * content handler, and the content handler invoking another content
 * handler, and for each content handler to return a response.
 * This chain length of two active invocations is the minimum
 * requirement. The implementation should not artificially
 * limit the number of invocations and responses that are supported
 * except as constrained by the resources of the device.</p>
 *
 * <p> To maintain continuity across the applications,
 * chained invocations are part of the same transaction.
 * Invoking an Invocation places it in a transaction.
 * The transaction maintains the sequence of invocations
 * across all of the applications involved.
 * The transaction maintains the invocations regardless of whether
 * a single application can run at a time or the applications
 * execute in parallel in different runtime environments. The
 * transaction is used to record and manage the steps in processing and
 * dispatching to applications. </p>
 *
 * <p> For simple non-chaining use cases that involve only two
 * applications with a single invocation and response,
 * only the methods 
 * {@link #getRequest getRequest}, {@link #finish finish},
 * {@link Registry#invoke Registry.invoke}, and
 * {@link Registry#getResponse Registry.getResponse} are needed.</p>
 * <p>
 * For chained use cases, the methods {@link Registry#invoke Registry.invoke}
 * and {@link Invocation#getPrevious Invocation.getPrevious} 
 * are used to establish
 * the sequence and to retrieve the previous Invocation.
 * The {@link Registry#invoke Registry.invoke} method places the new
 * Invocation in the same transaction as a previous Invocation.
 * The previous Invocation will be held in the transaction until
 * the new Invocation is completed.  When the response to the new
 * Invocation is returned, the previously active Invocation can be
 * retrieved with {@link Invocation#getPrevious Invocation.getPrevious}
 * so the content handler can complete its processing.</p>
 *
 * <p>
 * An Invocation can be delegated to another handler with the 
 * {@link Registry#reinvoke Registry.reinvoke} method. 
 * Responses to the reinvocation will be queued to the original invoking
 * application. </p>
 *
 * <H3>Handling Faults</H3>
 * If the content handler cannot or does not correctly handle the
 * Invocation, then the AMS MUST handle it correctly.
 * These actions prevent an incorrectly written content
 * handler from being unresponsive or being run repeatedly but never
 * processing queued invocations.
 * <ul>
 * <li>
 * If an Invocation with a status of <code>ACTIVE</code> is dequeued by
 * the content handler, but the handler does not call
 * {@link #finish finish}
 * or make a request to chain a new Invocation to the ACTIVE
 * invocation before the content handler exits, then the AMS MUST
 * complete the request with an ERROR status.
 * This ensures that the invoking application
 * will always get a response, if required, for each invocation
 * regardless of whether the content handler correctly handles it.
 * </li>
 * <li>
 * If the content handler is not running, or exits before processing
 * all queued requests or responses, then it MUST be started.
 * The content handler is expected to dequeue at least one
 * invocation that was queued before it was started.
 * If it does not dequeue any pending Invocations or can not be started,
 * then Invocations that were in the queue for the content handler
 * before it was started MUST be handled as follows:
 * <ul>
 * <li>Invocation requests with a status of <code>ACTIVE</code>
 * are completed with the <code>ERROR</code> status.</li>
 * <li>Invocation responses are discarded.</li>
 * <li>Invocations queued after the content handler was started are
 * retained and will require it to be restarted.</li>
 * </ul>
 * This serialization of queued requests and starting the content
 * handler 
 * addresses a race condition. This condition may occur when the
 * content handler is active but exits before processing Invocations that
 * were queued after it was started or it last called
 * {@link #getRequest getRequest} or
 * {@link Registry#getResponse Registry.getResponse}.
 * </li>
 * </ul>
 * <p>
 * Invocations and invocation state MUST NOT be preserved
 * across soft and hard restarts of the device software including
 * unexpected power interruptions.</p>
 *
 *
 */
public interface ContentHandlerServer extends ContentHandler {

    /**
     * Gets the next Invocation request pending for this
     * ContentHandlerServer. 
     * The method can be unblocked with a call to
     * {@link #cancelGetRequest cancelGetRequest}.
     * The application should process the Invocation as
     * a request to perform the <code>action</code> on the content. 
     *
     * @param wait <code>true</code> if the method must wait
     * for an Invocation if one is not available;
     * <code>false</code> if the method MUST NOT wait.
     *
     * @return the next pending Invocation or <code>null</code>
     *  if no Invocation is available; <code>null</code>
     *  if canceled with {@link #cancelGetRequest cancelGetRequest}
     * @see Registry#invoke
     * @see #finish
     */
    public Invocation getRequest(boolean wait);

    /**
     * Cancels a pending <code>getRequest</code>. 
     * This method will force all threads blocked in a call to the
     * <code>getRequest</code> method for this ContentHandlerServer
     * to return.
     * If no threads are blocked; this call has no effect.
     */
    public void cancelGetRequest();

    /**
     * Finishes the Invocation and sets the status for the response.
     * The <code>finish</code> method can only be called when the
     * Invocation
     * has a status of <code>ACTIVE</code> or <code>HOLD</code>.
     * <p>
     * The content handler may modify the URL, type, action, or
     * arguments before invoking <code>finish</code>.
     * If the method
     * {@link Invocation#getResponseRequired Invocation.getResponseRequired}
     * returns <code>true</code>, then the modified
     * values MUST be returned to the invoking application.
     *
     * @param invocation the Invocation to finish
     * @param status the new status of the Invocation;
     *   MUST be either <code>OK</code>, <code>CANCELLED</code>
     *   or <code>INITIATED</code>
     *
     * @return <code>true</code> if the application MUST
     *   voluntarily exit to allow pending responses or requests
     *   to be handled;
     *   <code>false</code> otherwise
     *
     * @exception IllegalArgumentException if the new
     *   <code>status</code> of the Invocation
     *    is not <code>OK</code>, <code>CANCELLED</code>,
     *    or <code>INITIATED</code>
     * @exception IllegalStateException if the current
     *   <code>status</code> of the
     *   Invocation is not <code>ACTIVE</code> or <code>HOLD</code>
     * @exception NullPointerException if the invocation is <code>null</code>
     */
    public boolean finish(Invocation invocation, int status);

    /**
     * Sets the listener to be notified when a new request is
     * available for this content handler.  The request is
     * retrieved using {@link #getRequest getRequest}.
     * If the listener is <code>non-null</code> and a request is
     * available, the listener MUST be notified.
     *
     * @param listener the listener to register;
     *   <code>null</code> to remove the listener.
     */
    public void setListener(RequestListener listener);

    /**
     * Gets the ID at the specified index of an application or content
     * handler allowed access to this content handler.
     * The ID returned for each index must be the equal to the ID
     * at the same index in the <tt>accessAllowed</tt> array passed to
     * {@link Registry#register Registry.register}.  
     *
     * @param index the index of the ID
     * @return the ID at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #accessAllowedCount accessAllowedCount} method.
     */
    public String getAccessAllowed(int index);

    /**
     * Gets the number of IDs allowed access by the content handler.
     * The number of IDs MUST be equal to the length of the array
     * of <code>accessAllowed</code> passed to 
     * {@link Registry#register Registry.register}.
     * If the number of IDs is zero then all applications and
     * content handlers are allowed access.
     *
     * @return the number of IDs allowed access 
     */
    public int accessAllowedCount();

    /**
     * Determines if an ID MUST be allowed access by the content handler.
     * Access MUST be allowed if the ID has a prefix that exactly matches
     * any of the IDs returned by {@link #getAccessAllowed}.
     * The prefix comparison is equivalent to
     * <code>java.lang.String.startsWith</code>.
     *
     * @param ID the ID for which to check access
     * @return <code>true</code> if access MUST be allowed by the
     *  content handler;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>ID</code>
     * is <code>null</code>
     */
    public boolean isAccessAllowed(String ID);
}
