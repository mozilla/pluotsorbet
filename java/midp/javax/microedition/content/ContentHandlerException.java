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

/**
 * A <tt>ContentHandlerException</tt> is thrown to report errors
 * specific to registration and invocation of content handlers. 
 * Instances are immutable and thread safe.
 */
public class ContentHandlerException extends IOException {
    /** The error code. */
    private int errcode;

    /**
     * The reason is <CODE>NO_REGISTERED_HANDLER</CODE> in a
     * ContentHandlerException when there is no content handler
     * registered of the requested combination of
     * ID, type, suffix, and action.
     */
    public final static int NO_REGISTERED_HANDLER = 1;

    /**
     * The reason is <code>TYPE_UNKNOWN</code> in a
     * ContentHandlerException when the type is not available.
     */
    public final static int TYPE_UNKNOWN = 2;

    /**
     * The reason is <CODE>AMBIGUOUS</CODE> in a
     * ContentHandlerException when an ID does not
     * uniquely identify a single content handler application.
     */
    public final static int AMBIGUOUS = 3;

    /**
     * Constructs a <code>ContentHandlerException</code> with a reason
     * and error code.
     * The error message string <code>reason</code> can later be
     * retrieved by the
     * {@link java.lang.Throwable#getMessage java.lang.Throwable.getMessage}
     * method.
     * @param reason the reason for the exception
     * @param errcode the error code; one of 
     *  {@link #NO_REGISTERED_HANDLER}, {@link #AMBIGUOUS},
     *  or {@link #TYPE_UNKNOWN}
     * @exception IllegalArgumentException if <code>errcode</code> is not
     *  one of 
     *  {@link #NO_REGISTERED_HANDLER}, {@link #AMBIGUOUS},
     *  or {@link #TYPE_UNKNOWN}
     */
    public ContentHandlerException(String reason, int errcode) {
	super(reason);
	if (errcode < NO_REGISTERED_HANDLER ||
	    errcode > AMBIGUOUS) {
	    throw new IllegalArgumentException();
	}
	this.errcode = errcode;
    }

    /**
     * Returns the error code for the exception.
     * @return the error code;  one of 
     *  {@link #NO_REGISTERED_HANDLER}, {@link #AMBIGUOUS},
     *  or {@link #TYPE_UNKNOWN}
     */
    public int getErrorCode() {
	return errcode;
    }
}
