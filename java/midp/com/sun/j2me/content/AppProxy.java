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
import java.util.Hashtable;

import com.sun.midp.security.Permissions;
import com.sun.j2me.security.Token;
import com.sun.midp.security.SecurityToken;

import com.sun.midp.midlet.MIDletSuite;
import com.sun.midp.main.MIDletSuiteUtils;
import com.sun.midp.midlet.MIDletStateHandler;
import com.sun.midp.midletsuite.MIDletSuiteImpl;
import com.sun.midp.midletsuite.MIDletSuiteStorage;
import com.sun.midp.midletsuite.MIDletSuiteLockedException;
import com.sun.midp.midletsuite.MIDletSuiteCorruptedException;

import com.sun.midp.events.NativeEvent;
import com.sun.midp.events.EventTypes;
import com.sun.midp.events.EventQueue;

import com.sun.midp.io.Util;

import javax.microedition.content.ContentHandlerException;

/**
 * Each AppProxy instance provides access to the AMS information
 * and functions for a running or installed application.
 * This class must be replaced for each platform/profile combination and
 * be integrated with the appropriate Application Management Software.
 * <p>
 * The AppProxy class is *only* available within
 * this package for security purposes.
 * An instance exists for the current application and can
 * be retrieved for any installed application.
 * The following methods provide functions needed by CHAPI:
 * <ul>
 * <li>{@link #getCurrent} - the current application and the main
 *     class within it.
 * <li>{@link #forApp} - the AppProxy for a named  Jar ID and
 *     classname.  For MIDP, they are the suiteID of the storage and
 *     MIDlet within the JAR.
 * <li>{@link #getStorageID} - the storage ID for this AppProxy.
 * <li>{@link #getClassname} - the classname for this AppProxy.
 * <li>{@link #getApplicationID} - the CHAPI defined unique identifier
 *     for the application
 * <li>{@link #getApplicationName} = a user friendly name for this application.
 * <li>{@link #getAuthority} - the authority under which the application
 *     is granted permissions. For MIDP, the subject field of the signing
 *     certificate.
 * <li>{@link #getProperty} - access to the properties in the manifest or
 *     application descriptor.
 * <li>{@link #getVersion} - the version number of this application.
 * <li>{@link #getDefaultID} - the default applicationID if none is provided
 *     in the manifest or application descriptor.
 * <li>{@link #checkRegisterPermission} - method to check if the caller
 *     is allowed to register or unregister handlers.
 * <li>{@link #checkAPIPermission} - method to check if the caller has
 *     permission to use internal APIs. Caller must have an appropriate
 *     security token (depending on the platform)
 * <li>{@link #isRegistered} - used to check if the application is
 *     registered as appropriate for the platform. For MIDP, there
 *     is a MIDlet-n attribute in manifest or JAD.
 * <li>{@link #verifyApplication} - Verify that a named class is the
 *     correct type and access to be launched as an application in the
 *     profile.  For MIDP, the class must extend MIDlet.
 * <li>{@link #launch} - Request this application be launched if it is
 *     not already running.  Return a boolean indicating if the current
 *     application MUST voluntarily exit before the launched app can run.
 * <li>{@link #requestForeground} - ask the "window manager" to give the
 *     foreground to the requested application.
 * <
 * </ul>
 */
class AppProxy extends CLDCAppID {
    /** This class has a different security domain than the MIDlet suite */
    private static SecurityToken classSecurityToken;

    /** The current AppProxy. */
    private static AppProxy currentApp;

    /** The log flag to enable informational messages. */
    static final Logger LOGGER = null; // new Logger();
    
    private static final boolean isInSvmMode = isInSvmMode();

    /** The known AppProxy instances. Key is classname. */
    protected Hashtable appmap;

    /** The mutex used to avoid corruption between threads. */
    protected static final Object mutex = new Object();

    /** The MIDlet suite for this app. */
    protected final MIDletSuite msuite;

//    /** The storageId (suiteId) for this application. */
//    protected final int storageId;
//    /** The classname of the application. */
//    protected String classname;
    
    protected String version, authority;

    /** The application name. */
    private String applicationName;

    /** The ApplicationID, (same a suiteId). */
    private String applicationID;

    /** The application is registered. */
    private boolean isRegistered;

    /** MIDlet property for the suite version. */
    static final String VERSION_PROP       = "MIDlet-Version";

    /** MIDlet property for the suite vendor. */
    static final String VENDOR_PROP        = "MIDlet-Vendor";
    
    static final int EXTERNAL_SUITE_ID = MIDletSuite.UNUSED_SUITE_ID;

    // native methods
    static native boolean isInSvmMode();
    
    static native void midletIsAdded( int suiteId, String className );
    static native boolean isMidletRunning( int suiteId, String className );
    static native void midletIsRemoved( int suiteId, String className );
    
    static native boolean isSuiteRunning( int suiteId );

    /**
     * Sets the security token used for privileged operations.
     * The token may only be set once.
     * @param token a Security token
     */
    static void setSecurityToken(Object token) {
        token.getClass(); // null pointer check
        if (classSecurityToken != null) {
            throw new SecurityException();
        }
        classSecurityToken = (SecurityToken)token;
    }

	static ApplicationID createAppID() {
		return new CLDCAppID();
	}
	
    /**
     * Gets the AppProxy for the currently running application.
     * @return the current application.
     */
    static AppProxy getCurrent() {
        synchronized (mutex) {
            if (currentApp == null) {
                MIDletStateHandler mh =
                    MIDletStateHandler.getMidletStateHandler();
                MIDletSuite msuite = mh.getMIDletSuite();
                try {
                    currentApp = (msuite != null) ?
                        new AppProxy(msuite, msuite.getID(),
                        		mh.getFirstRunningMidlet(), null).verify() :
                        new AppProxy(MIDletSuite.INTERNAL_SUITE_ID, "");
                } catch (ClassNotFoundException cnfe) {
                    return null;
                }
            }
        }
        return currentApp;
    }

	class VAGetter extends MIDletSuiteUser {
		void use(MIDletSuite msuite) {
			if( msuite instanceof MIDletSuiteImpl ){
		        try {
		        	authority =((MIDletSuiteImpl)msuite).getInstallInfo().getCA();
		        } catch (RuntimeException e) {}
			}
	        version = msuite.getProperty(VERSION_PROP);
		}
	};
	
    /**
     * Construct an AppProxy with the specified MIDletSuite.
     *
     * @param msuite the MIDletSuite to initialize.
     * @param classname the classname of a copackaged application.
     * @param appmap a Hashtable to keep track of instances; may be null
     * @exception ClassNotFoundException if the <code>classname</code>
     *   is not present
     * @exception IllegalArgumentException if classname is not
     *   a valid application
     */
    protected AppProxy(MIDletSuite msuite, int suiteID, String classname, Hashtable appmap)
        throws ClassNotFoundException
    {
    	super( suiteID, classname );
    	
        if (appmap == null) {
            // Allocate a Hashtable if needed
            appmap = new Hashtable();
        }

        this.msuite = msuite;
        new VAGetter().execute();
        this.appmap = appmap;
        if (LOGGER != null)
        	LOGGER.println("AppProxy created: " + this);
    }

	protected AppProxy verify() throws ClassNotFoundException {
		if(className != null){
	        verifyApplication(className);
	        initAppInfo(new MIDletSuiteUser());
	        appmap.put(className, this);
	        if (LOGGER != null)
	        	LOGGER.println("AppProxy verified: ID " + super.toString() );
		}
        return this;
	}

    /**
     * Construct an AppProxy with the specified suiteId, classname.
     * This is just a placeholder to make launch work.
     *
     * @param storageId the suiteId
     * @param classname the classname
     */
    protected AppProxy(int suiteID, String classname)
    {
    	super( suiteID, classname );
        msuite = null;

		initAppInfo( new VAGetter() ); 

        if (LOGGER != null) {
        	LOGGER.println("AppProxy created: " + classname);
        }
    }

    /**
     * Gets the AppProxy for an application class in the current bundle.
     * @param classname the name of the application class
     * @return the AppProxy for classname; <code>null</code> if not
     * a valid application (MIDlet)
     * @exception ClassNotFoundException if the <code>classname</code>
     *   is not present
     * @exception IllegalArgumentException if classname is
     *  not a valid application
     */
    AppProxy forClass(String classname) throws ClassNotFoundException
    {
        AppProxy curr = null;
        synchronized (mutex) {
            // Check if class already has a AppProxy
            curr = (AppProxy)appmap.get(classname);
            if (curr == null) {
                // Create a new instance
                // throws ClassNotFoundException and IllegalArgumentException
                curr = new AppProxy(msuite, suiteID, classname, appmap).verify();
            }
        }
        return curr;
    }

    /**
     * Gets the AppProxy for an storageID and classname.
     * @param storageId the storageId (suiteId)
     * @param classname the name of the application class
     * @return the AppProxy for suiteId, classname;
     *   <code>null</code> if not a valid application (MIDlet)
     * @exception ClassNotFoundException if the <code>classname</code>
     * is not present
     * @exception IllegalArgumentException if classname is not
     *   a valid application
     */
    AppProxy forApp(ApplicationID appID) throws ClassNotFoundException
    {
        // Check in the current suite
    	CLDCAppID id = CLDCAppID.from(appID);
        if (id.suiteID == this.suiteID) {
            return forClass(id.className);
        }

        // Create a new instance
        return new AppProxy(id.suiteID, id.className);
    }

//    /**
//     * Gets the storage ID of this application.
//     * The ID uniquely identifies the package/application bundle.
//     * @return the application ID.
//     */
//    int getStorageId() {
//        return storageId;
//    }
//
//    /**
//     * Gets the classname of this application.
//     * @return the classname
//     */
//    String getClassname() {
//        return classname;
//    }

    /**
     * Gets the user friendly application name.
     * @return the user friendly application name
     */
    String getApplicationName() {
        return applicationName;
    }

    /**
     * Gets the CHAPI application ID for this application.
     * @return the CHAPI application ID.
     */
    String getApplicationID() {
        return applicationID;
    }

    /**
     * Gets the version string for the application.
     * @return the version
     */
    String getVersion() {
        return version;
    }

    /**
     * Gets the Trusted authority that authenticated this application.
     * <p>
     * For MIDP, this is the CA of the signer.
     * If exception is thrown during getting authorization
     * this methods ignores it and returns null.
     * @return the authority.
     */
    String getAuthority() {
    	return authority;
    }

    /**
     * Gets true if the application is a registered application.
     * <p>
     * for MIDP, this means there was a MIDlet-n attribute.
     * @return true if this application is registered
     */
    boolean isRegistered() {
        return isRegistered;
    }

    /**
     * Gets a property from the manifest or application descriptor.
     * @param key the name of the property to retrieve
     * @return the value of the property or <code>null</code>
     */
    String getProperty(final String key) {
    	class user extends MIDletSuiteUser {
    		String value;
			void use(MIDletSuite msuite) {
				value = msuite.getProperty(key);
			}
    	};
        return ((user)new user().execute()).value;
    }

    /**
     * Check the permission to register or unregister.
     * @param reason the reason for the permission check
     * @exception SecurityException if not allowed
     */
    final void checkRegisterPermission(final String reason) {
    	new MIDletSuiteUser(){
			void use(MIDletSuite msuite) {
		        try {
		            msuite.checkForPermission(
		            		/*Permissions.CHAPI_REGISTER*/ 
		            		"javax.microedition.content.ContentHandler",
	                        getApplicationName(), reason);
		        } catch (InterruptedException ie) {
		            throw new SecurityException("interrupted");
		        }
        	}
    	}.execute();
    }

    /**
     * Check if the internal API use is allowed.
     * @param securityToken a generic security token
     * @exception SecurityException thrown if internal API use not allowed
     */
    final static void checkAPIPermission(Token token) {
		SecurityToken securityToken = token.getSecurityToken();
        if (securityToken != null) {
            securityToken.checkIfPermissionAllowed(Permissions.MIDP/*_PERMISSION_NAME*/);
        } else {
            MIDletSuite msuite =
                MIDletStateHandler.getMidletStateHandler().getMIDletSuite();
            if (msuite != null) {
                msuite.checkIfPermissionAllowed(Permissions.AMS_PERMISSION_NAME);
            }
        }
    }

    /**
     * Request the transition of the foreground to this application
     * from the invoking application.
     * @param fromApp the invoking application
     * @param toApp the target application
     */
    static void requestForeground(ApplicationID fromApp, ApplicationID toApp)
    {
    	if( LOGGER != null ) LOGGER.println(
    			"requestForeground: " + fromApp + " -> " + toApp);
    	
        NativeEvent event =
            new NativeEvent(EventTypes.FOREGROUND_TRANSFER_EVENT);
        event.intParam1 = CLDCAppID.from(fromApp).suiteID;
        event.stringParam1 = CLDCAppID.from(fromApp).className;
        event.intParam2 = CLDCAppID.from(toApp).suiteID;
        event.stringParam2 = CLDCAppID.from(toApp).className;

        int amsIsolateId = MIDletSuiteUtils.getAmsIsolateId();
        EventQueue eventQueue = EventQueue.getEventQueue(classSecurityToken);
        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }

    /* *
     * The stronger variant for request the transition of
     * the foreground to this application.
     * @param targetSuiteId the target suiteId
     * @param targetClassname the target classname
     * /
    static void requestForeground(int targetSuiteId,
                                  String targetClassname)
    {
        NativeEvent event =
            new NativeEvent(EventTypes.SET_FOREGROUND_BY_NAME_REQUEST);
        event.intParam1 = targetSuiteId;
        event.stringParam1 = targetClassname;

        int amsIsolateId = MIDletSuiteUtils.getAmsIsolateId();
        EventQueue eventQueue = EventQueue.getEventQueue(classSecurityToken);
        eventQueue.sendNativeEventToIsolate(event, amsIsolateId);
    }
    /**/
    
    /**
     * Launch this application.
     * Don't launch another application unless
     * the execute allows this application to continue after
     * the launch.
     * <p>
     * In SVM, (sequential applications) only the first
     * execute matters; later ones should not override the
     * first.  All pending Invocations are queued in InvocationStore
     * so they will not be lost.  When MIDlets exit, another
     * application will be selected from those pending.
     *
     * @param displayName name to show to the user of what to launch
     * @return <code>true</code> if the application is started.
     */
    boolean launch(String displayName) {
    	if( isMidletRunning(suiteID, className) )
        	return true;
	    if( LOGGER != null ) 
	    	LOGGER.println("AppProxy.launch(): " + (MIDletSuiteUtils.isAmsIsolate()?"":"NOT ") + "isAmsIsolate()");
    	if( isInSvmMode && !MIDletSuiteUtils.isAmsIsolate() )
    		return false;
    	if( suiteID != MIDletSuite.INTERNAL_SUITE_ID && isSuiteRunning(suiteID) )
    		return false;
    	if( LOGGER != null )
        	LOGGER.println("AppProxy.launch(): send 'launch' request {" + suiteID + ", '" + className + "'}");
    	// always launch an application in background mode
        return MIDletSuiteUtils.execute(classSecurityToken,
        							suiteID, className, displayName);
    }


    /**
     * Verify that the classname is a valid application.
     * It must extend MIDlet.
     * @param classname the application class
     *
     * @exception ClassNotFoundException is thrown if the class cannot be found
     * @exception IllegalArgumentException if the classname is null or empty
     *  or does not implement the lifecycle of a MIDlet.
     */
    protected void verifyApplication(String classname)
        throws ClassNotFoundException
    {
        /* check the classname for null and get the class */
        Class appClass = Class.forName(classname);
        Class midletClass = Class.forName("javax.microedition.midlet.MIDlet");
        if ((!midletClass.isAssignableFrom(appClass)) ||
        		appClass == midletClass) {
            throw new IllegalArgumentException("Class '" + classname + 
            							"' is not a MIDlet");
        }
    }

    /**
     * Initialize application name and application ID
     * from the attributes.
     */
    protected void initAppInfo( final MIDletSuiteUser user ) {
        // Check if it is an internal MIDlet
        if (suiteID == MIDletSuite.INTERNAL_SUITE_ID) {
            applicationName = className.substring(className.lastIndexOf('.') + 1);
            applicationID = "system";
            isRegistered = true;
            return;
        }

        new MIDletSuiteUser() {
        	void use( MIDletSuite msuite ){
		        user.use( msuite );
		        // Check if a registered MIDlet
		        String[] minfo = getMIDletInfo(msuite, className);
		
		        // if a MIDlet, set the application name and application ID
		        if (minfo != null) {
		            applicationName = minfo[0];
		            applicationID = minfo[2];
		            isRegistered = true;
		        }
		
		        // Fill in defaults for appName and applicationID
		        if (applicationName == null || applicationName.length() == 0) {
		            applicationName = msuite.getProperty(
		                                        MIDletSuiteImpl.SUITE_NAME_PROP);
		        }
		
		        if (applicationID == null || applicationID.length() == 0) {
		            applicationID = getDefaultID(msuite);
		        }
        	}
        }.execute();
    }

    /**
     * Gets the content handler ID for the current application.
     * The ID uniquely identifies the application which contains the
     * content handler.
     * The application ID is assigned when the application is installed.
     * <p>
     * All installed applications have vendor and name;
     *
     * @return the ID; MUST NOT be <code>null</code>
     */
    String getDefaultID() {
    	class user extends MIDletSuiteUser {
    		String defID;
			void use(MIDletSuite msuite) { defID = getDefaultID(msuite); }
    	};
    	return ((user)new user().execute()).defID;
    }

    private String getDefaultID( MIDletSuite suite ) {
        StringBuffer sb = new StringBuffer(80);
        String s = suite.getProperty(VENDOR_PROP);
        sb.append((s != null) ? s : "internal");
        sb.append('-');
        s = suite.getProperty(MIDletSuiteImpl.SUITE_NAME_PROP);
        sb.append((s != null) ? s : "system");
        sb.append('-');
        sb.append(className);
        return sb.toString().replace(' ', '_');
    }

    /**
     * Get the MIDletInfo for the named MIDlet.
     * @param suite the MIDlet suite to look in for the midlet
     * @param classname the class name to look for
     * @return an array of Strings, name, icon, ID
     *  null if there is no matching MIDlet-n.
     */
    private static String[] getMIDletInfo(MIDletSuite suite, String classname)
    {
        for (int i = 1; ; i++) {
            String midletn = "MIDlet-".concat(Integer.toString(i));
            String attr = suite.getProperty(midletn);
            if (attr == null) {
                break; // break out of loop, not found
            }

            Vector args = Util.getCommaSeparatedValues(attr);
            if (args.size() < 3) {
                // Not enough args to be legit
                continue;
            }

            if (!classname.equals(args.elementAt(2))) {
                continue;
            }
            String[] values = new String[args.size()];
            args.copyInto(values);

            String ID = suite.getProperty(midletn.concat("-ID"));
            values[2] = ID;

            return values;
        }
        return null;
    }

    /**
     * Starts native content handler.
     * @param handler Content handler to be executed.
     * @return true if invoking app should exit.
     * @exception ContentHandlerException if no such handler ID in the Registry
     * or native handlers execution is not supported.
     */
    static boolean launchNativeHandler(String handlerID) 
    										throws ContentHandlerException {
        int result = launchNativeHandler0(handlerID);
        if (result < 0) {
            throw new ContentHandlerException(
                        "Unable to launch platform handler",
                        ContentHandlerException.NO_REGISTERED_HANDLER);
        }
        return (result > 0);
    }

    /**
     * Informs platform about finishing of processing platform's request
     * @param invoc finished invocation
     * @return should_exit flag for the invocation handler
     */
    static boolean platformFinish(int tid) {
        return platformFinish0(tid);
    }

    /**
     * Starts native content handler.
     * @param handlerId ID of the handler to be executed
     * @return result status:
     * <ul>
     * <li> 0 - LAUNCH_OK 
     * <li> > 0 - LAUNCH_OK_SHOULD_EXIT
     * <li> &lt; 0 - error
     * </ul>
     */
    private static native int launchNativeHandler0(String handlerId);

    private static native boolean platformFinish0(int tid);

    /**
     * Create a printable representation of this AppProxy.
     * @return a printable string
     */
    public String toString() {
        if (LOGGER != null) {
            return super.toString() +
                ", registered: " + isRegistered +
                ", name: " + applicationName +
                ", ID: " + applicationID;
        } else {
            return super.toString();
        }
    }
    
    protected class MIDletSuiteUser {
    	void use( MIDletSuite msuite ){};
    	MIDletSuiteUser execute() {
    		if( msuite != null ){
                if (LOGGER != null) LOGGER.println("msuite isn't null: " + msuite);
    			use( msuite );
    		} else if( suiteID != MIDletSuite.INTERNAL_SUITE_ID ){
	            try {
	                MIDletSuite suite = MIDletSuiteStorage.
	                        getMIDletSuiteStorage(classSecurityToken).
	                        getMIDletSuite(suiteID, false);
	                if (LOGGER != null) LOGGER.println("use msuite " + suite);
	                if( suite != null ){
	                	try {
	                		use(suite);
	                	} finally {
	                		suite.close();
	                	}
	                }
	            } catch (MIDletSuiteLockedException msle) {
	                if (LOGGER != null) {
	                	LOGGER.log("AppProxy initialization failed", msle);
	                }
	            } catch (MIDletSuiteCorruptedException msce) {
	                if (LOGGER != null) {
	                	LOGGER.log("AppProxy initialization failed", msce);
	                }
	            }
    		}
			return this;
    	}
    }
}
