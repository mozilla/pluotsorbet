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

import java.util.Vector;

import javax.microedition.content.ActionNameMap;
import javax.microedition.content.ContentHandler;
import javax.microedition.content.Invocation;
import javax.microedition.content.RequestListener;

/**
 * The internal structure of a registered content handler.
 */
public class ContentHandlerImpl extends ContentHandlerRegData 
					implements ContentHandler {
	
	public static interface Handle {
		public static interface Receiver {
			void push( Handle handle );
		}
		
	    /** 
	     * Content Handler fields indexes.
	     * <BR>Used with functions: @link findHandler(), @link getValues() and 
	     * @link getArrayField().
	     * <BR> They should match according enums in jsr211_registry.h
	     */
	    static final int FIELD_ID         = 0;  /** Handler ID */
	    static final int FIELD_TYPES      = 1;  /** Types supported by a handler */
	    static final int FIELD_SUFFIXES   = 2;  /** Suffixes supported */
	                                            /** by a handler */
	    static final int FIELD_ACTIONS    = 3;  /** Actions supported */
	                                            /** by a handler */
	    static final int FIELD_LOCALES    = 4;  /** Locales supported */
	                                            /** by a handler */
	    static final int FIELD_ACTION_MAP = 5;  /** Handler action map */
	    static final int FIELD_ACCESSES   = 6; /** Access list */
	    static final int FIELD_COUNT      = 7; /** Total number of fields */
	    
		String				getID();
		//int 				getSuiteId();
		
	    /**
	     * Returns array field
	     * @param fieldId index of field. Allowed: 
	     *        @link FIELD_TYPES, @link FIELD_SUFFIXES, @link FIELD_ACTIONS
	     *        @link FIELD_LOCALES, @link FIELD_ACTION_MAP, @link FIELD_ACCESSES
	     *        values.
	     * @return array of values
	     */
	    String[] getArrayField(int fieldId);

		ContentHandlerImpl 	get();
	}
	
	/**
	 * handle of registered content handler.
	 */
	final protected Handle handle;
	
	final protected ApplicationID applicationID;

//    /**
//     * The MIDlet suite storagename that contains the MIDlet.
//     */
//    protected int storageId;
//
//    /**
//     * The application class name that implements this content
//     * handler.  Note: Only the application that registered the class
//     * will see the classname; for other applications the value will be
//     * <code>null</code>.
//     */
//    protected String classname;

    /** Count of requests retrieved via {@link #getRequest}. */
    protected int requestCalls;

    /**
     * The RequestListenerImpl; if a listener is set.
     */
    RequestListenerImpl listenerImpl;

    /** Property name for the current locale. */
    private final static String LOCALE_PROP = "microedition.locale";
    /**
     * The Name from parsing the Property for the MIDlet
     * with this classname.
     */
    String appname;

    /**
     * The Version parsed from MIDlet-Version attribute.
     */
    String version;

    /**
     * The authority that authenticated this ContentHandler.
     */
    String authority;

    /**
     * Initialize a new instance with the same information.
     * @param handler another ContentHandlerImpl
     * @see com.sun.j2me.content.ContentHandlerServerImpl
     */
    protected ContentHandlerImpl(ContentHandlerImpl handler) {
    	super( handler );
        handle = handler.handle;
        applicationID = handler.applicationID;
        listenerImpl = handler.listenerImpl;
        version = handler.version;
        requestCalls = handler.requestCalls;
        authority = handler.authority;
        appname = handler.appname;
    }
    
    protected ContentHandlerImpl( ApplicationID appID, Handle handle ){
    	this.applicationID = appID;
    	this.handle = handle; 
    }

    /**
     * Get the nth type supported by the content handler.
     * @param index the index into the types
     * @return the nth type
     * @exception IndexOutOfBounds if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getTypeCount getTypeCount} method.
     */
    public String getType(int index) {
        return get(index, getTypes());
    }


    /**
     * Get the number of types supported by the content handler.
     *
     * @return the number of types
     */
    public int getTypeCount() {
        return getTypes().length;
    }

    /**
     * Get types supported by the content handler.
     *
     * @return array of types supported
     */
    public String[] getTypes() {
        if (types == null) {
            types = handle.getArrayField(Handle.FIELD_TYPES);
        }
        return types;
    }

    /**
     * Determine if a type is supported by the content handler.
     *
     * @param type the type to check for
     * @return <code>true</code> if the type is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>type</code>
     * is <code>null</code>
     */
    public boolean hasType(String type) {
        return has(type, getTypes(), true);
    }

    /**
     * Get the nth suffix supported by the content handler.
     * @param index the index into the suffixes
     * @return the nth suffix
     * @exception IndexOutOfBounds if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getSuffixCount getSuffixCount} method.
     */
    public String getSuffix(int index) {
        return get(index, getSuffixes());
    }

    /**
     * Get the number of suffixes supported by the content handler.
     *
     * @return the number of suffixes
     */
    public int getSuffixCount() {
        return getSuffixes().length;
    }

    /**
     * Determine if a suffix is supported by the content handler.
     *
     * @param suffix the suffix to check for
     * @return <code>true</code> if the suffix is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>suffix</code>
     * is <code>null</code>
     */
    public boolean hasSuffix(String suffix) {
        return has(suffix, getSuffixes(), true);
    }

    /**
     * Get suffixes supported by the content handler.
     *
     * @return array of suffixes supported
     */
    public String[] getSuffixes() {
        if (suffixes == null) {
            suffixes = handle.getArrayField(Handle.FIELD_SUFFIXES);
        }
        return suffixes;
    }

    /**
     * Get the nth action supported by the content handler.
     * @param index the index into the actions
     * @return the nth action
     * @exception IndexOutOfBounds if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getActionCount getActionCount} method.
     */
    public String getAction(int index) {
        return get(index, getActions());
    }

    /**
     * Get the number of actions supported by the content handler.
     *
     * @return the number of actions
     */
    public int getActionCount() {
        return getActions().length;
    }

    /**
     * Determine if a action is supported by the content handler.
     *
     * @param action the action to check for
     * @return <code>true</code> if the action is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>action</code>
     * is <code>null</code>
     */
    public boolean hasAction(String action) {
        return has(action, getActions(), false);
    }

    /**
     * Get actions supported by the content handler.
     *
     * @return array of actions supported
     */
    public String[] getActions() {
        if (actions == null) {
            actions = handle.getArrayField(Handle.FIELD_ACTIONS);
        }
        return actions;
    }

    /**
     * Gets the value at index in the string array.
     * @param index of the value
     * @param strings array of strings to get from
     * @return string at index.
     * @exception IndexOutOfBounds if index is less than zero or
     *     greater than or equal length of the array.
     */
    static private String get(int index, String[] strings) {
        if (index < 0 || index >= strings.length) {
            throw new IndexOutOfBoundsException();
        }
        return strings[index];
    }

    /**
     * Determines if the string is in the array.
     * @param string to locate
     * @param strings array of strings to get from
     * @param ignoreCase true to ignore case in matching
     * @return <code>true</code> if the value is found
     * @exception NullPointerException if <code>string</code>
     * is <code>null</code>
     */
    static private boolean has(String string, String[] strings, boolean ignoreCase) {
        int len = string.length(); // Throw NPE if null
        for (int i = 0; i < strings.length; i++) {
            if (strings[i].length() == len &&
                string.regionMatches(ignoreCase, 0, strings[i], 0, len)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get the mapping of actions to action names for the current
     * locale supported by this content handler. The behavior is
     * the same as invoking {@link #getActionNameMap} with the current
     * locale.
     *
     * @return an ActionNameMap; if there is no map available for the
     *  current locale, then it MUST be <code>null</code>
     */
    public ActionNameMap getActionNameMap() {
        String locale = System.getProperty(LOCALE_PROP);
        return (locale == null) ? null : getActionNameMap(locale);
    }

    /**
     * Get the mapping of actions to action names for the requested
     * locale supported by this content handler.
     * The locale is matched against the available locales.
     * If a match is found it is used.  If an exact match is
     * not found, then the locale string is shortened and retried
     * if either of the "_" or "-" delimiters is present.
     * The locale is shortened by retaining only the characters up to
     * but not including the last occurrence of the delimiter
     * (either "_" or "-").
     * The shortening and matching is repeated as long as the string
     * contains one of the delimiters.
     * Effectively, this will try the full locale and then try
     * without the variant or country code, if they were present.
     *
     * @param locale for which to find an ActionNameMap;
     *   MUST NOT be <code>null</code>
     * @return an ActionNameMap; if there is no map available for the
     *  locale, then it MUST be <code>null</code>
     * @exception NullPointerException if the locale is <code>null</code>
     */
    public ActionNameMap getActionNameMap(String locale) {
        while (locale.length() > 0) {
            for (int i = 0; i < getActionNames().length; i++) {
                if (locale.equals(getActionNames()[i].getLocale())) {
                    return getActionNames()[i];
                }
            }
            int lastdash = locale.lastIndexOf('-');
            if (lastdash < 0) {
                break;
            }
            locale = locale.substring(0, lastdash);
        }
        return null;
    }

    /**
     * Gets the number of action name maps supported by the content handler.
     *
     * @return the number of action name maps
     */
    public int getActionNameMapCount() {
        return getActionNames().length;
    }

    /**
     * Gets the n<sup>th</sup> ActionNameMap supported by the
     * content handler.
     * @param index the index of the locale
     * @return the n<sup>th</sup> ActionNameMap
     *
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getActionNameMapCount getActionNameMapCount} method.
     */
    public ActionNameMap getActionNameMap(int index) {
        if (index < 0 || index >= getActionNames().length) {
            throw new IndexOutOfBoundsException();
        }
        return getActionNames()[index];
    }

    /**
     * Get actions names for the content handler.
     *
     * @return array of actions names
     */
    private ActionNameMap[] getActionNames() {
        if (actionnames == null) {
            String[] locales = handle.getArrayField(Handle.FIELD_LOCALES);
            String[] names   = handle.getArrayField(Handle.FIELD_ACTION_MAP);

            actionnames = new ActionNameMap[locales.length];
            for (int index = 0; index < locales.length; index++) {
                String[] temp = new String[getActions().length];

                System.arraycopy(names,
                                 index * getActions().length,
                                 temp,
                                 0,
                                 getActions().length);

                actionnames[index] = new ActionNameMap(getActions(),
                                                       temp,
                                                       locales[index]);
            }
        }
        return actionnames;
    }

    /**
     * Returns the name used to present this content handler to a user.
     * The value is extracted from the normal installation information
     * about the content handler application.
     *
     * @return the user-friendly name of the application;
     * it MUST NOT be <code>null</code>
     */
    public String getAppName() {
        loadAppData();
        return appname;
    }

    /**
     * Gets the version number of this content handler.
     * The value is extracted from the normal installation information
     * about the content handler application.
     * @return the version number of the application;
     * MAY be <code>null</code>
     */
    public String getVersion() {
        loadAppData();
        return version;
    }

    /**
     * Gets the name of the authority that authorized this application.
     * This value MUST be <code>null</code> unless the device has been
     * able to authenticate this application.
     * If <code>non-null</code>, it is the string identifying the
     * authority.  For example,
     * if the application was a signed MIDlet, then this is the
     * "subject" of the certificate used to sign the application.
     * <p>The format of the authority for X.509 certificates is defined
     * by the MIDP Printable Representation of X.509 Distinguished
     * Names as defined in class
     * <code>javax.microedition.pki.Certificate</code>. </p>
     *
     * @return the authority; may be <code>null</code>
     */
    public String getAuthority() {
        loadAppData();
        return authority;
    }

    /**
     * Initializes fields retrieved from AppProxy 'by-demand'.
     */
    private void loadAppData() {
        if (appname == null) {
            try {
                AppProxy app = AppProxy.getCurrent().forApp(applicationID);
                appname = app.getApplicationName();
                version = app.getVersion();
                authority = app.getAuthority();
            } catch (Throwable t) {
            }
            if (appname == null) {
                appname = "";
            }
        }
    }

    /**
     * Gets the n<sup>th</sup> ID of an application or content handler
     * allowed access to this content handler.
     * The ID returned for each index must be the equal to the ID
     * at the same index in the <tt>accessAllowed</tt> array passed to
     * {@link javax.microedition.content.Registry#register Registry.register}.
     *
     * @param index the index of the ID
     * @return the n<sup>th</sup> ID
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #accessAllowedCount accessAllowedCount} method.
     */
    public String getAccessAllowed(int index) {
        return get(index, getAccessRestricted());
    }

    /**
     * Gets the number of IDs allowed access by the content handler.
     * The number of IDs must be equal to the length of the array
     * of accessRestricted passed to
     * {@link javax.microedition.content.Registry#register Registry.register}.
     * If the number of IDs is zero then all applications and
     * content handlers are allowed access.
     *
     * @return the number of accessRestricteds
     */
    public int accessAllowedCount() {
        return getAccessRestricted().length;
    }

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
     * @exception NullPointerException if <code>accessRestricted</code>
     * is <code>null</code>
     */
    public boolean isAccessAllowed(String ID) {
        ID.length();                // check for null
        if (getAccessRestricted().length == 0) {
            return true;
        }
        for (int i = 0; i < getAccessRestricted().length; i++) {
            if (ID.startsWith(getAccessRestricted()[i])) {
                return true;
            }
        }
        return false;
    }

    /**
     * Get accesses for the content handler.
     *
     * @return array of allowed class names
     */
    public String[] getAccessRestricted() {
        if (accessRestricted == null) {
            accessRestricted = handle.getArrayField(Handle.FIELD_ACCESSES);
        }
        return accessRestricted;
    }

    /**
     * Finish this Invocation and set the status for the response.
     * The <code>finish</code> method may only be called when this
     * Invocation
     * has a status of <code>ACTIVE</code> or <code>HOLD</code>.
     * <p>
     * The content handler may modify the URL, type, action, or
     * arguments before invoking <code>finish</code>.
     * If the method {@link Invocation#getResponseRequired} returns
     * <code>true</code> then the modified
     * values MUST be returned to the invoking application.
     *
     * @param invoc the Invocation to finish
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
     * @exception IllegalStateException if the current
     *   <code>status</code> of the
     *   Invocation is not <code>ACTIVE</code> or <code>HOLD</code>
     * @exception NullPointerException if the invocation is <code>null</code>
     */
    protected boolean finish(InvocationImpl invoc, int status) {
		int currst = invoc.getStatus();
		if (currst != Invocation.ACTIVE && currst != Invocation.HOLD) {
			throw new IllegalStateException("Status already set");
		}
		// If ACTIVE or HOLD it must be an InvocationImpl
		return invoc.finish(status);
	}

    /**
     * Set the listener to be notified when a new request is
     * available for this content handler.  The request MUST
     * be retrieved using {@link #getRequest}.
     *
     * @param listener the listener to register;
     *   <code>null</code> to remove the listener.
     */
    public void setListener(RequestListener listener) {
        synchronized (this) {
            if (listener != null || listenerImpl != null) {
                // Create or update the active listener thread
                if (listenerImpl == null) {
                    listenerImpl = new RequestListenerImpl(this, listener);
                } else {
                    listenerImpl.setListener(listener);
                }

                // If the listener thread no longer needed; clear it
                if (listener == null) {
                    listenerImpl = null;
                }
            }
        }
    }

    /**
     * Notify the request listener of a pending request.
     * Overridden by subclass.
     */
    protected void requestNotify() {
    }

//    /**
//     * Compare two ContentHandlerImpl's for equality.
//     * Classname, storageID, and seqno must match.
//     * @param other another ContentHandlerImpl
//     * @return true if the other handler is for the same class,
//     * storageID, and seqno.
//     */
//    boolean equals(ContentHandlerImpl other) {
//        return storageId == other.storageId && classname.equals(other.classname);
//    }

    /**
     * Debug routine to print the values.
     * @return a string with the details
     */
    public String toString() {
        if (AppProxy.LOGGER != null) {
            StringBuffer sb = new StringBuffer(80);
            sb.append("CH:");
            sb.append(", appID: ");
            sb.append(applicationID);
            sb.append(", removed: ");
            sb.append(", flag: ");
            sb.append(registrationMethod);
            sb.append(", types: ");
            toString(sb, types);
            sb.append(", ID: ");
            sb.append(ID);
            sb.append(", suffixes: ");
            toString(sb, suffixes);
            sb.append(", actions: ");
            toString(sb, actions);
            sb.append(", access: ");
            toString(sb, accessRestricted);
            sb.append(", authority: ");
            sb.append(authority);
            sb.append(", appname: ");
            sb.append(appname);
            return sb.toString();
        } else {
            return super.toString();
        }
    }

    /**
     * Append all of the strings in the array to the string buffer.
     * @param sb a StringBuffer to append to
     * @param strings an array of strings.
     */
    private void toString(StringBuffer sb, String[] strings) {
        if (strings == null) {
            sb.append("null");
            return;
        }
        for (int i = 0; i < strings.length; i++) {
            if (i > 0) {
                sb.append(':');
            }
            sb.append(strings[i]);
        }
    }
}

//-----------------------------------------------------

class HandlerNameFinder implements ContentHandlerImpl.Handle.Receiver {

	static class FoundException extends RuntimeException {
		public ContentHandlerImpl.Handle handle;
		
		FoundException(ContentHandlerImpl.Handle handle) {
			this.handle = handle;
		}
	}

	private String handlerID;
	private boolean exact;

	HandlerNameFinder(String handlerID, boolean exact) {
		this.handlerID = handlerID;
		this.exact = exact;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( exact ){
			if( handle.getID().equals(handlerID) )
				throw new FoundException( handle );
		} else if( handle.getID().startsWith(handlerID) )
			throw new FoundException( handle );
	}
}

abstract class HandlerFilter implements ContentHandlerImpl.Handle.Receiver {
	protected ContentHandlerImpl.Handle.Receiver output;
	protected HandlerFilter( ContentHandlerImpl.Handle.Receiver output ){
		this.output = output;
	}
}

class HandlerNameFilter extends HandlerFilter {
	private String testID;
	
	protected HandlerNameFilter(String testID, ContentHandlerImpl.Handle.Receiver r) {
		super( r );
		this.testID = testID;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( handle.getID().startsWith(testID) || testID.startsWith(handle.getID()) )
			output.push(handle);
	} 
}

//class HandlerSuiteIDFilter extends HandlerFilter {
//	private int suiteId;
//
//	HandlerSuiteIDFilter( int suiteId, ContentHandlerImpl.Handle.Receiver r ){
//		super( r );
//		this.suiteId = suiteId;
//	}
//
//	public void push(ContentHandlerImpl.Handle handle) {
//		if( handle.getSuiteId() == suiteId )
//			output.push(handle);
//	}
//}

class HandlerTypeFilter extends HandlerFilter {
	private String type;
	
	protected HandlerTypeFilter(String type, ContentHandlerImpl.Handle.Receiver r) {
		super( r );
		this.type = type;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( handle.get().hasType(type) )
			output.push(handle);
	} 
}

class HandlerActionFilter extends HandlerFilter {
	private String action;
	
	protected HandlerActionFilter(String action, ContentHandlerImpl.Handle.Receiver r) {
		super( r );
		this.action = action;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( handle.get().hasAction(action) )
			output.push(handle);
	} 
}

class HandlerSuffixFilter extends HandlerFilter {
	private String suffix;
	
	protected HandlerSuffixFilter(String suffix, ContentHandlerImpl.Handle.Receiver r) {
		super( r );
		this.suffix = suffix;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( handle.get().hasSuffix(suffix) )
			output.push(handle);
	} 
}

class HandlerAccessFilter extends HandlerFilter {
	private String callerId;
	
	public HandlerAccessFilter(String callerId, ContentHandlerImpl.Handle.Receiver r) {
		super( r );
		this.callerId = callerId;
	}

	public void push(ContentHandlerImpl.Handle handle) {
		if( handle.get().isAccessAllowed(callerId) )
			output.push(handle);
	}
}

class HandlersCollection implements ContentHandlerImpl.Handle.Receiver {
	Vector/*<ContentHandlerImpl>*/ vector = new Vector(); 
	
	public void push(ContentHandlerImpl.Handle handle) {
		vector.addElement( handle.get() );
	}

	public ContentHandlerImpl[] getArray() {
		ContentHandlerImpl[] result = new ContentHandlerImpl[ vector.size() ];
		vector.copyInto(result);
		return result;
	}
}
