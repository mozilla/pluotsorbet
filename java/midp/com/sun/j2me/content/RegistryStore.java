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

import com.sun.j2me.security.Token;

/**
 * Standalone Registry Storage manager.
 * All protected methods, which are all static, redirect their work
 * to alone instance allowed for given Java runtime (for MIDP
 * it is Isolate).
 * The standalone instance initializes resources in the private
 * constructor and then releases its in the native finalizer.
 */
class RegistryStore {

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
    
    static final Vector emptyVector = new Vector();
    static final ContentHandlerImpl[] emptyHandlersArray = new ContentHandlerImpl[0]; 

    /**
     * Search flags for @link getHandler() method. 
     */
    static final int SEARCH_EXACT   = 0; /** Search by exact match with ID */
    static final int SEARCH_PREFIX  = 1; /** Search by prefix of given value */

    /** This class has a different security domain than the MIDlet suite */
    private static Token classSecurityToken;
    
	static ContentHandlerImpl.Handle register(ApplicationID appID,
										ContentHandlerRegData handlerData) {
        if( !store.register0(CLDCAppID.from(appID).suiteID, CLDCAppID.from(appID).className, 
        						handlerData) )
        	return null;
        return new ContentHandlerHandle( handlerData.ID );
	}

    /**
     * Unregisters content handler specified by its ID.
     * @param handlerId ID of unregistered handler.
     * @return true if success, false - otherwise.
     */
    static boolean unregister(String handlerId) {
        return store.unregister0(handlerId);
    }

	static void enumHandlers(String callerId, int searchBy, String value,
						ContentHandlerImpl.Handle.Receiver output) {
		ContentHandlerImpl[] result = findHandler(callerId, searchBy, value);
		for( int i = 0; i < result.length; i++)
			output.push(result[i].handle);
	}

    /**
     * Tests ID value for registering handler accordingly with JSR claim:
     * <BR><CITE>Each content handler is uniquely identified by an ID. ...
     * <BR> The ID MUST NOT be equal to any other registered handler ID.
     * <BR> Every other ID MUST NOT be a prefix of this ID.
     * <BR> The ID MUST NOT be a prefix of any other registered ID. </CITE>
     * @param testID tested value
     *
     * @return conflicted handlers array.
     */
    static ContentHandlerImpl[] findConflicted(String testID) {
        ContentHandlerImpl[] result = findHandler(null, FIELD_ID, testID);
        if(AppProxy.LOGGER != null){
			AppProxy.LOGGER.println( "conflictedHandlers for '" + testID + "' [" + result.length + "]:" );
			for( int i = 0; i < result.length; i++){
				AppProxy.LOGGER.println( "app = " + result[i].applicationID + ", ID = '" + result[i].ID + "'" );
			}
        }
		return result;
    }

    /**
     * Searches content handlers by searchable fields values. As specified in
     * JSR 211 API:
     * <BR><CITE> Only content handlers that this application is allowed to
     * access will be included. </CITE> (in result).
     * @param callerId ID value to check access
     * @param searchBy indicator of searchable field. Allowed: 
     *        @link FIELD_TYPES, @link FIELD_SUFFIXES, @link FIELD_ACTIONS 
     *        values. The special case for the testId implementation: 
     *        @link FIELD_ID specified.
     * @param value Searched value
     * @return found handlers array.
     */
    static ContentHandlerImpl[] findHandler(String callerId, int searchBy, 
                                                String value) {
        /* Check value for null */
        value.length();
        HandlersCollection collection = new HandlersCollection();
        deserializeCHArray(store.findHandler0(callerId, searchBy, value), collection);
        return collection.getArray(); 
    }

    /**
     * The special finder for exploring handlers registered by the given suite.
     * @param suiteId explored suite Id
     *
     * @return found handlers array.
     */
    static ContentHandlerImpl[] forSuite(int suiteId) {
        HandlersCollection collection = new HandlersCollection();
        deserializeCHArray(store.forSuite0(suiteId), collection);
        return collection.getArray(); 
    }
    
    static ContentHandlerImpl getHandler( ApplicationID appID ){
        ContentHandlerImpl[] arr = 
        	RegistryStore.forSuite(CLDCAppID.from(appID).suiteID);
        String classname = CLDCAppID.from(appID).className;
        for (int i = 0; i < arr.length; i++) {
            if (classname.equals(CLDCAppID.from(arr[i].applicationID).className)) {
                return arr[i];
            }
        }
        return null;
    }

    /**
     * Returns all stored in the Registry values for specified field.
     * @param callerId ID value to check access
     * @param searchBy index of searchable field. Allowed: 
     *        @link FIELD_TYPES, @link FIELD_SUFFIXES, @link FIELD_ACTIONS, 
     *        @link FIELD_ID values.
     * @return found values array.
     */
    static String[] getValues(String callerId, int searchBy) {
        String res = store.getValues0(callerId, searchBy);
        Vector v = deserializeString(res);
        String[] result = new String[ v.size() ];
        v.copyInto(result);
        return result;
    }

    /**
     * Returns array field
     * @param handlerId ID for access check
     * @param fieldId index of field. Allowed: 
     *        @link FIELD_TYPES, @link FIELD_SUFFIXES, @link FIELD_ACTIONS
     *        @link FIELD_LOCALES, @link FIELD_ACTION_MAP, @link FIELD_ACCESSES
     *        values.
     * @return array of values
     */
    static String[] getArrayField(String handlerId, int fieldId) {
        String res = store.loadFieldValues0(handlerId, fieldId);
        Vector v = deserializeString(res);
        String[] result = new String[ v.size() ];
        v.copyInto(result);
        return result;
    }

    static class HandlerData {
    	int		suiteId;
    	String 	classname;
    	int		registrationMethod;
		public String ID;
    }
    
    /**
     * Creates and loads handler's data.
     * @param handlerId ID of content handler to be loaded.
     * @param searchMode ID matching mode. Used <ul>
     *      <li> @link SEARCH_EXACT
     *      <li> @link SEARCH_PREFIX </ul>
     *
     * @return loaded ContentHandlerImpl object or
     * <code>null</code> if given handler ID is not found in Registry database.
     */
    static ContentHandlerImpl getHandler(String callerId, String id, int searchMode) {
        if (id.length() != 0) {
	        HandlerData data = deserializeCH( store.getHandler0( callerId, id, searchMode ) );
	        if( data != null )
	            return new ContentHandlerHandle( data ).get();
        }
    	return null;
    }

	static HandlerData getHandler(String handlerID) {
		return deserializeCH( store.getHandler0( null, handlerID, SEARCH_EXACT ) );
	}

    /**
     * Returns content handler suitable for URL.
     * @param callerId ID of calling application.
     * @param URL content URL.
     * @param action requested action.
     * @return found handler if any or null.
     */
    static ContentHandlerImpl getByURL(String callerId, String url, 
                                       String action) {
        return new ContentHandlerHandle( deserializeCH( store.getByURL0(callerId, url, action) ) ).get();
    }

    /**
     * Transforms serialized form to array of Strings.
     * <BR>Serialization format is the same as ContentHandlerImpl
     * used.
     * @param str String in serialized form to transform to array of Strings.
     * @return array of Strings. If input String is NULL 0-length array
     * returned. ... And we believe that input string is not misformed.
     */
    private static Vector/*<String>*/ deserializeString(String str) {
    	if( str == null )
    		return emptyVector;
    	Vector result = new Vector();
    	// all lengths in bytes
    	int pos = 0;
//        if(AppProxy.LOGGER != null) 
//        	AppProxy.LOGGER.println( "deserializeString: string length = " + str.length() );
    	while( pos < str.length() ){
    		int elem_length = (int)str.charAt(pos++) / 2;
//            if(AppProxy.LOGGER != null) 
//            	AppProxy.LOGGER.println( "deserializeString: pos = " + pos + 
//            							", elem_length = " + elem_length );
    		result.addElement(str.substring(pos, pos + elem_length));
//            if(AppProxy.LOGGER != null)
//            	AppProxy.LOGGER.println( "deserializeString: '" + str.substring(pos, pos + elem_length) + "'" );
    		pos += elem_length;
    	}
        return result;
    }

    /**
     * Restores ContentHandler main fields (ID, suite_ID, class_name and flag) 
     * from serialized form to ContentHandlerImpl object.
     * @param str ContentHandler main data in serialized form.
     * @return restored ContentHandlerImpl object or null
     */
    private static HandlerData deserializeCH(String str) {
        if(AppProxy.LOGGER != null) 
        	AppProxy.LOGGER.println( "RegistryStore.deserializeCH '" + str + "'");
        Vector components = deserializeString(str);

        if (components.size() < 1) return null;
        String id = (String)components.elementAt(0);
        if (id.length() == 0) return null; // ID is significant field

        if (components.size() < 2) return null;
        String storageId = (String)components.elementAt(1);

        if (components.size() < 3) return null;
        String class_name = (String)components.elementAt(2);

        if (components.size() < 4) return null;
        int regMethod = Integer.parseInt((String)components.elementAt(3), 16);

        HandlerData ch = new HandlerData();
        ch.ID = id;
        ch.suiteId = Integer.parseInt(storageId, 16);
        ch.classname = class_name;
        ch.registrationMethod = regMethod;
        return ch;
    }

    /**
     * Restores ContentHandlerImpl array from serialized form.
     * @param str ContentHandlerImpl array in serialized form.
     * @return restored ContentHandlerImpl array
     */
    private static void deserializeCHArray(String str,
    						ContentHandlerImpl.Handle.Receiver output) {
    	if( str != null ){
	        Vector strs = deserializeString(str);
	        for (int i = 0; i < strs.size(); i++)
	        	output.push(new ContentHandlerHandle(deserializeCH( (String)strs.elementAt(i) )));
    	}
    }

    /**
     * Sets the security token used for privileged operations.
     * The token may only be set once.
     * @param token a Security token
     */
    static void setSecurityToken(Token token) {
		if (classSecurityToken != null) {
            throw new SecurityException();
        }
        classSecurityToken = token;
    }
    
    
    /** Singleton instance. Worker for the class static methods. */
    private static RegistryStore store = new RegistryStore();

    /**
     * Private constructor for the singleton storage class.
     * If ClassNotFoundException is thrown during ActionNameMap
     * loading the constructor throws RuntimeException
     */
    private RegistryStore() {
        try {
            Class.forName("javax.microedition.content.ActionNameMap");
        } catch (ClassNotFoundException cnfe) {
            throw new RuntimeException(cnfe.getMessage());
        }
        if (!init()) {
            throw new RuntimeException("RegistryStore initialization failed");
        }
    }

    /**
     * Native implementation of <code>findHandler</code>.
     * @param callerId ID value to check access
     * @param searchBy index of searchable field.
     * @param value searched value
     * @return found handlers array in serialized form.
     */
    private native String findHandler0(String callerId, int searchBy,
                                        String value);

    /**
     * Native implementation of <code>findBySuite</code>.
     * @param suiteId explored suite Id
     * @return handlers registered for the given suite in serialized form.
     */
    private native String forSuite0(int suiteId);

    /**
     * Native implementation of <code>getValues</code>.
     * @param callerId ID value to check access
     * @param searchBy index of searchable field.
     * @return found values in serialized form.
     */
    private native String getValues0(String callerId, int searchBy);

    /**
     * Loads content handler data.
     * @param callerId ID value to check access.
     * @param id Id of required content handler.
     * @param mode flag defined search mode applied for the operation.
     * @return serialized content handler or null.
     */
    private native String getHandler0(String callerId, String id, int mode);

    /**
     * Loads values for array fields.
     * @param handlerId ID of content handler ID.
     * @param fieldId fieldId to be loaded.
     * @return loaded field values in serialized form.
     */
    private native String loadFieldValues0(String handlerId, int fieldId);

    /**
     * Returns content handler suitable for URL.
     * @param callerId ID of calling application.
     * @param URL content URL.
     * @param action requested action.
     * @return ID of found handler if any or null.
     */
    private native String getByURL0(String callerId, String url, String action);
    
    /**
     * Initialize persistence storage.
     * @return <code>true</code> or
     * <BR><code>false</code> if initialization fails.
     */
    private native boolean init();

    /**
     * Cleanup native resources.
     */
    private native void finalize();

    /**
     * Registers given content handler.
     * @param contentHandler content handler being registered.
     * @return true if success, false - otherwise.
     */
    private native boolean register0(int storageId, String classname,
											ContentHandlerRegData handlerData);

    /**
     * Unregisters content handler specified by its ID.
     * @param handlerId ID of unregistered handler.
     * @return true if success, false - otherwise.
     */
    private native boolean unregister0(String handlerId);

}

class ContentHandlerHandle implements ContentHandlerImpl.Handle {
	private ContentHandlerImpl 	created = null;
	
	private final String	handlerID;
	
	ContentHandlerHandle( String handlerID ){
		this.handlerID = handlerID;
	}
	
	ContentHandlerHandle( RegistryStore.HandlerData data ){
		this( data.ID );
		Init( data );
	}
	
	private void Init( final RegistryStore.HandlerData data ){
		created = new ContentHandlerImpl(new CLDCAppID(data.suiteId, data.classname), this){{
			this.ID = handlerID; 
			this.registrationMethod = data.registrationMethod;
		}};
	}
	
	public ContentHandlerImpl get(){
		if( created == null )
			Init(RegistryStore.getHandler( handlerID ));
		return created;
	}
	
	public String getID() { return handlerID; }
	//public int getSuiteId() { return get().storageId; }
	
	public String[] getArrayField(int fieldId) {
		return RegistryStore.getArrayField( getID(), fieldId );
	}
}
