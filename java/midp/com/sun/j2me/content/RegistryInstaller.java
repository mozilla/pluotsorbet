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

import java.util.Hashtable;
import java.util.Vector;
import java.util.Enumeration;

import javax.microedition.content.ContentHandlerException;
import javax.microedition.content.ActionNameMap;

import com.sun.midp.installer.InvalidJadException;

/**
 * Support for parsing attributes and installing from the
 * manifest or application descriptors.
 */
final class RegistryInstaller {
	
	private final AppProxy appl;
	
    /** Attribute prefix for ContentHandler attributes. */
    private static final String CH_PREFIX = "MicroEdition-Handler-";

    /** Attribute suffix for ContentHandler ID attribute. */
    private static final String CH_ID_SUFFIX = "-ID";

    /** Attribute suffix for ContentHandler visibility attribute. */
    private static final String CH_ACCESS_SUFFIX = "-Access";

    /** Parsed handlers to be installed. */
    private Hashtable/*<classname, ContentHandlerRegData>*/ handlersToInstall;

    /** Old handlers to be removed. */
    private Hashtable/*<String, ContentHandlerImpl>*/ handlersToRemove;
    
    RegistryInstaller( AppProxy appl ){
    	this.appl = appl; 
    }

    /**
     * Parse the ContentHandler attributes and check for errors.
     * <ul>
     * <li> Parse attributes into set of ContentHandlers.
     * <li> If none, return
     * <li> Check for permission to install handlers
     * <li> Check each for simple invalid arguments
     * <li> Check each for MIDlet is registered
     * <li> Check each for conflicts with other application registrations
     * <li> Find any current registrations
     * <li> Remove current dynamic registrations from set to be removed
     * <li> Check and resolve any conflicts between static and current dynamic
     * </ul>
     * @param appl the AppProxy context with one or more applications
     * @return number of handlers prepared for installation.
     * @exception IllegalArgumentException if there is no classname field,
     *   or if there are more than five comma separated fields on the line.
     * @exception NullPointerException if missing components
     * @exception ContentHandlerException if handlers are ambiguous
     * @exception ClassNotFoundException if an application class cannot be found
     * @exception SecurityException if not allowed to register
     */
    int preInstall() throws ContentHandlerException, ClassNotFoundException
    {
        int sz;
        int suiteId = appl.suiteID;
        ContentHandlerImpl[] chs;
        
        if( AppProxy.LOGGER != null ) 
        	AppProxy.LOGGER.println( "RegistryInstaller.preInstall: appl = " + appl );        

        /*
         * Check for any CHAPI attributes;
         * if so, then the MIDlet suite must have permission.
         */
        handlersToRemove = new Hashtable();
        handlersToInstall = parseAttributes(appl);

        if( AppProxy.LOGGER != null ) 
        	AppProxy.LOGGER.println( "RegistryInstaller.preInstall: handlersToInstall.size = " + handlersToInstall.size() );        

        /*
         * Remove all static registrations. Verify dynamically registered.
         */
        chs = RegistryStore.forSuite(suiteId);
        sz = (chs == null? 0: chs.length);
        if( AppProxy.LOGGER != null ) 
        	AppProxy.LOGGER.println( "RegistryInstaller.preInstall: suite " + suiteId + 
        				" handlers number = " + sz );        
        for (int i = 0; i < sz; i++) {
            if( AppProxy.LOGGER != null ) 
            	AppProxy.LOGGER.println( "RegistryInstaller.preInstall: chs[" + i + "] = " + chs[i] );        
            if (chs[i] == null) continue;
            if( 0 == (chs[i].registrationMethod & 
            					ContentHandlerImpl.REGISTERED_STATIC_FLAG) ) {
                // Verify dynamic handler.
            	class ReplaceDynamicHandlerException extends Exception {};
                try {
                	String handlerClassName = 
                		CLDCAppID.from(chs[i].applicationID).className; 
                    // is the handler a valid application?
                    appl.verifyApplication(handlerClassName);
                    // is there new handler to replace this one?
                    if( handlersToInstall.containsKey(handlerClassName) )
                        throw new ReplaceDynamicHandlerException();
                    // The handler remains.
                    continue;
                } catch( ClassNotFoundException x ) {
                	// verifyApplication hasn't found handler class in the suite being installed 
                    // Pass down to remove handler
                } catch(ReplaceDynamicHandlerException t) {
                    // Pass down to remove handler
                }
            }

            if( AppProxy.LOGGER != null ) 
            	AppProxy.LOGGER.println( "RegistryInstaller.preInstall: mark " + i );        
            // Remove handler -- either [static] or [replaced] or [invalid]
            handlersToRemove.put(chs[i].ID, chs[i]);
            chs[i] = null;
        }
        if( AppProxy.LOGGER != null ) 
        	AppProxy.LOGGER.println( getClass().getName() + 
        			".preInstall: handlersToRemove " + handlersToRemove.size() );        

        /* Verify new registrations */
        Vector ids = new Vector();;
        Enumeration handlerDataEnum = handlersToInstall.elements(); 
        while (handlerDataEnum.hasMoreElements()) {
            ContentHandlerRegData handler = 
            	(ContentHandlerRegData)handlerDataEnum.nextElement();
            // Verify ID ...
            // ... look through Registry
            ContentHandlerImpl[] conf = 
            	RegistryStore.findConflicted(handler.ID);
            if (conf != null) {
                for (int j = 0; j < conf.length; j++) {
                	if (conf[j] == null) continue;
                    if (CLDCAppID.from(conf[j].applicationID).suiteID != suiteId || 
                    		!willRemove(conf[j].ID))
                        throw new ContentHandlerException(
		                            "Content Handler ID: " + handler.ID,
		                            ContentHandlerException.AMBIGUOUS);
                }
            }

            // ... look through newbies
            for( int i = 0; i< ids.size(); i++) {
                String otherID = (String)ids.elementAt(i); 
                if (handler.ID.startsWith(otherID) || otherID.startsWith(handler.ID)) {
                    throw new ContentHandlerException(
                    			"Content Handler ID: "+handler.ID,
                    			ContentHandlerException.AMBIGUOUS);
                }
            }
            
            ids.addElement(handler.ID);
        }
        
        // Check permission to install handlers
        if( handlersToInstall.size() > 0 )
        	appl.checkRegisterPermission("register");
        
        if( AppProxy.LOGGER != null ){ 
        	AppProxy.LOGGER.println( getClass().getName() + ".preInstall: handlersToInstall(" + 
        						handlersToInstall.size() + "):");
        	Enumeration keys = handlersToInstall.keys();
        	while( keys.hasMoreElements() ){
        		String classname = (String)keys.nextElement();
        		AppProxy.LOGGER.println( "\t[" + classname + "] " + 
        				handlersToInstall.get(classname).toString() );
        	}
        }
        return handlersToInstall.size();
    }

    private boolean willRemove(String ID) {
    	return handlersToRemove.containsKey(ID);
    }

    /**
     * Parse the ContentHandler attributes and check for errors.
     *
     * @param appl the AppProxy context with one or more applications
     *
     * @return a Vector of the ContentHandlers parsed from the attributes
     * @throws ClassNotFoundException 
     *
     * @exception IllegalArgumentException if there is no classname field,
     *   or if there are more than five comma separated fields on the line.
     * @exception NullPointerException if missing components
     * @exception ContentHandlerException if there are conflicts between
     *  content handlers
     * @exception ClassNotFoundException if an application class cannot be found
     */
    private static Hashtable/*<classname, ContentHandlersRegData>*/ 
    					parseAttributes(AppProxy appl) throws ClassNotFoundException
    {
    	Hashtable handlers = new Hashtable();
        for (int index = 1; ; index++) {
            String sindex = Integer.toString(index);
            String handler_n = CH_PREFIX.concat(sindex);
            String value = appl.getProperty(handler_n);
            if(AppProxy.LOGGER != null)
            	AppProxy.LOGGER.println( "RegistryInstaller.parseAttributes: appl.getProperty(" + handler_n + ") = '" + 
            				value + "'" );            
            if (value == null)
                break;
            String[] types = null;
            String[] suffixes = null;
            String[] actions = null;
            String[] locales = null;
            String classname;
            String[] fields = split(value, ',');

            switch (fields.length) {
            case 5: // Has locales
                locales = split(fields[4], ' ');
                // Fall through
            case 4: // Has actions
                actions = split(fields[3], ' ');
                // Fall through
            case 3: // Has suffixes
                suffixes = split(fields[2], ' ');
                // Fall through
            case 2: // Has types
                    // Parse out the types (if any)
                types = split(fields[1], ' ');
                    // Fall through
            case 1: // Has classname
                classname = fields[0];
                if (classname != null && classname.length() > 0) {
                    // Has non-empty classname
                    break;
                }
                // No classname, fall through to throw exception
            case 0: // no nothing; error
            default: // too many fields, error
                throw new IllegalArgumentException("Too many or too few fields");
            }

            // Get the application info for this new class;
            // Throws ClassNotFoundException or IllegalArgumentException
            AppProxy newAppl = appl.forClass(classname);
            if(AppProxy.LOGGER != null)
            	AppProxy.LOGGER.println( "RegistryInstaller.parseAttributes: newAppl = " + 
            				newAppl );            

            ActionNameMap[] actionnames =
                parseActionNames(actions, locales, handler_n, newAppl);

            // Parse the ID if any and the Access attribute
            String idAttr = handler_n.concat(CH_ID_SUFFIX);
            String id = newAppl.getProperty(idAttr);
            String visAttr = handler_n.concat(CH_ACCESS_SUFFIX);
            String visValue = newAppl.getProperty(visAttr);
            String[] accessRestricted = split(visValue, ' ');

            // Default the ID if not supplied
            if (id == null) {
                // Generate a unique ID based on the MIDlet suite
                id = newAppl.getApplicationID();
            }

            // Now create the handler data
            ContentHandlerRegData handlerData = 
            				new ContentHandlerRegData(
				            		ContentHandlerRegData.REGISTERED_STATIC_FLAG, 
									types, suffixes, actions, actionnames,
									id, accessRestricted);

            /* replace another handler information with the same classname */
            handlers.put(classname, handlerData);
        }
        return handlers;
    }

    /**
     * Scan the available properties for the locale specific
     * attribute names and parse and The actionname maps for
     * each.
     * @param actions the actions parsed for the handler
     * @param locales the list of locales to check for action names
     * @param prefix the prefix of the current handler attribute name
     * @param appl the AppProxy context with one or more applications
     * @return an array of ActionNameMap's
     * @exception IllegalArgumentException if locale is missing
     */
    private static ActionNameMap[] parseActionNames(String[] actions,
                                             String[] locales,
                                             String prefix,
                                             AppProxy appl)
    {
        if (locales == null || locales.length == 0) {
            return null;
        }
        prefix = prefix.concat("-");
        Vector maps = new Vector();
        for (int i = 0; i < locales.length; i++) {
            String localeAttr = prefix.concat(locales[i]);
            String localeValue = appl.getProperty(localeAttr);
            if (localeValue == null) {
                throw new IllegalArgumentException("missing locale");
            }
            String[] actionnames = split(localeValue, ',');
            ActionNameMap map =
                new ActionNameMap(actions, actionnames, locales[i]);
            maps.addElement(map);
        }
        if (maps.size() > 0) {
            ActionNameMap[] result = new ActionNameMap[maps.size()];
            maps.copyInto(result);
            return result;
        } else {
            return null;
        }
    }

    /**
     * Split the values in a field by delimiter and return a string array.
     * @param string the string containing the values
     * @param delim the delimiter that separates the values
     * @return a String array of the values; must be null
     */
    static String[] split(String string, char delim) {
        String[] ret = ContentHandlerImpl.ZERO_STRINGS;
        if (string != null) {
            Vector values = getDelimSeparatedValues(string, delim);
            ret = new String[values.size()];
            values.copyInto(ret);
        }
        return ret;
    }

    /**
     * Create a vector of values from a string containing delimiter separated
     * values. The values cannot contain the delimiter. The output values will
     * be trimmed of whitespace. The vector may contain zero length strings
     * where there are 2 delimiters in a row or a comma at the end of the input
     * string.
     *
     * @param input input string of delimiter separated values
     * @param delim the delimiter separating values
     * @return vector of string values.
     */
    private static Vector getDelimSeparatedValues(String input, char delim) {
        Vector output = new Vector(5, 5);
        int len;
        int start;
        int end;

        input = input.trim();
        len = input.length();
        if (len == 0) {
            return output;
        }

        for (start = end = 0; end < len; ) {
            // Skip leading spaces and control chars
            while (start < len && (input.charAt(start) <= ' ')) {
                start += 1;
            }

            // Scan for end delimiter (tab also if delim is space)
            for (end = start; end < len; end++) {
                char c = input.charAt(end);
                if (c == delim || (c == '\t' && delim == ' ')) {
                    output.addElement(input.substring(start, end).trim());
                    start = end + 1;
                    break;
                }
            }
        }

        end = len;
        output.addElement(input.substring(start, end).trim());

        return output;
    }

    /**
     * Performs static installation (registration) the application
     * to handle the specified type and to provide a set of actions.
     *
     * @exception InvalidJadException if there is a content handlers
     * IDs conflict
     */
    void install() {
        // Remove static and conflicted handlers.
        if( AppProxy.LOGGER != null && handlersToRemove != null ){ 
        	AppProxy.LOGGER.println( getClass().getName() + 
        				".install: handlersToRemove(" + handlersToRemove.size() + "):");
        	Enumeration htr = handlersToRemove.keys();
        	while( htr.hasMoreElements() ) AppProxy.LOGGER.println( "\t" + htr.nextElement() );
        }
    	Enumeration htr = handlersToRemove.keys();
    	while( htr.hasMoreElements() ) {
            RegistryStore.unregister( (String)htr.nextElement() );
        }

        // Install new handlers.
        if( handlersToInstall != null ){
        	Enumeration keys = handlersToInstall.keys(); 
            while( keys.hasMoreElements() ) {
            	String classname = (String)keys.nextElement();
                ContentHandlerRegData handlerData =
                	(ContentHandlerRegData)handlersToInstall.get(classname);
                try {
					RegistryStore.register(appl.forClass(classname), handlerData);
	                if (AppProxy.LOGGER != null) {
	                    AppProxy.LOGGER.println("Register: " + classname + ", id: " + handlerData.getID());
	                }
				} catch (ClassNotFoundException e) {
					// assert( false );
					// it's impossible because appl.forClass(classname) already checked
					// this class
				}
            }
        }
    }

    /**
     * Performs static uninstallation (unregistration) of the application.
     *
     * @param suiteId suite ID to be unregistered
     * @param update flag indicated whether the given suite is about remove or
     * update
     */
    static void uninstallAll(int suiteId, boolean update) {
        ContentHandlerImpl[] chs = RegistryStore.forSuite(suiteId);
        for (int i = 0; i < chs.length; i++) {
            if (!update || (chs[i].registrationMethod & 
            						ContentHandlerImpl.REGISTERED_STATIC_FLAG) != 0) {
                RegistryStore.unregister(chs[i].getID());
            }
        }
    }

}
