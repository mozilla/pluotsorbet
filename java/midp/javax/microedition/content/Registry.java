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

import com.sun.j2me.content.ContentHandlerImpl;
import com.sun.j2me.content.ContentHandlerServerImpl;
import com.sun.j2me.content.InvocationImpl;
import com.sun.j2me.content.RegistryImpl;
import com.sun.j2me.security.Token;
import com.sun.j2me.security.TrustedClass;
import com.sun.jsr211.security.SecurityInitializer;

/**
 * The <tt>Registry</tt> provides method to invoke,
 * register,
 * unregister, and query information about content handlers.
 * An application registers, for each content handler, 
 * zero or more content types, suffixes, and actions.
 * Access to the registry is via the {@link #getRegistry getRegistry}
 * method. The Registry class is thread safe.
 * Applications are responsible for the thread safety of 
 * Invocation objects passed as arguments to Registry methods.
 * <p>
 * Multiple content handlers can be registered for each type, suffix,
 * and action. 
 * The types, suffixes, and actions registered for a handler
 * can be used to select a handler. 
 * The content handler ID is set during registration and is
 * used to uniquely identify the content handler 
 * and to enforce access controls.
 *
 * <p>
 * A content handler is any application that is registered to
 * handle some content. It responds to requests and displays
 * or acts on the content.
 * Registration can occur dynamically or statically.
 * Static registration occurs during the installation of the
 * application package, while dynamic registration occurs via this API.
 * A content handler may be a Java or a non-Java application.
 * For example, MIDlet suites and Personal Basis Profile applications
 * using the Xlet application model can be content handlers.
 * Refer to the {@link ContentHandler ContentHandler}
 * class for information on registering Java platform content handlers.</P>
 *
 * <p>
 * When a content handler is processing an invocation, it may be
 * necessary to invoke another content handler before it is able
 * to satisfy the first request.
 * The invocation and chaining of content handlers is managed
 * to maintain the context and sequence across
 * application executions.</p>
 *
 * <P>
 * The term <em>application</em> is used more generally than the term
 * <em>content handler</em>.
 * The term application is used to refer to the
 * common handling for making requests, handling responses,
 * and querying the content handler registrations.
 * The term <em>content handler</em> is used for an application
 * that registers for types, suffixes, actions, etc. and processes
 * the requests queued to it.</p>
 *
 *
 * <H3>Content Types</H3>
 * <P>
 * A content handler can register a set of types that it can handle.
 * Content types are simple opaque strings that are NOT case sensitive. 
 * Type strings are not case sensitive, types that differ
 * only by case are treated as a single type.
 * </P>
 *
 * <h3>Suffix Processing</h3>
 * <P>
 * A content handler can register a set of suffixes that identify
 * from the syntax of a URL the content it can handle.
 * Suffixes are NOT case sensitive.
 * </P>
 *
 * <H3>Content Handler Actions</H3>
 * <P>Each content handler may register a set of actions
 * it supports.  
 * The set of actions is extensible but applications should
 * choose from the actions defined in the {@link ContentHandler} 
 * class when they are appropriate.
 * The predefined actions are:
 * {@link ContentHandler#ACTION_OPEN open},
 * {@link ContentHandler#ACTION_EDIT edit},
 * {@link ContentHandler#ACTION_NEW new},
 * {@link ContentHandler#ACTION_SEND send},
 * {@link ContentHandler#ACTION_SAVE save},
 * {@link ContentHandler#ACTION_EXECUTE execute},
 * {@link ContentHandler#ACTION_SELECT select},
 * {@link ContentHandler#ACTION_INSTALL install},
 * {@link ContentHandler#ACTION_PRINT print}, and
 * {@link ContentHandler#ACTION_STOP stop}.
 *
 *
 * <H3>Content Handler IDs</H3>
 * <P>The content handler ID is a string chosen by the
 * application vendor to identify the content handler. The
 * ID registered by a content handler MUST be unique and MUST NOT
 * match the prefix of any other registered content handler.
 * IDs are case sensitive and are treated as opaque strings. 
 * They are compared for equality or as prefixes of IDs
 * when used to locate an appropriate content handler.
 * </P>
 *
 * <P>Content handler IDs should follow the form of 
 * fully qualified Java class names or any naming
 * syntax that provides a natural way to disambiguate between
 * vendors. For example, IDs may be URLs
 * including scheme, authority (server), and path information.</P>
 * <p>
 * For example, if registered in the order below, 
 * the following content handler IDs would be valid  
 * or invalid as indicated because there is an ambiguity due
 * to prefix comparisons.
 * <ol>
 * <li><code>com.sun.applications.calc</code> - valid</li>
 * <li><code>com.sun.applications.trig</code> - valid</li>
 * <li><code>com.sun.application</code> - invalid,
 *     this is the prefix of the <code>calc</code> ID</li>
 * <li><code>com.sun.applications.calc.decimal</code> -
 *     invalid, the <code>calc</code> ID is prefix of <code>decimal</code></li>
 * </ol>
 *
 * <H3>Java Application Class</H3>
 * <P>The content handler to be launched is identified by the
 * application class. The class MUST contain entry point(s)
 * and be packaged according to the Java runtime environment.
 * For MIDP, the application class MUST extend 
 * <code>javax.microedition.midlet.MIDlet</code>.
 * The application class uniquely identifies a content handler
 * within the application package, which is usually a JAR file.
 * Each application class can only be registered to a single content
 * handler. Registering a content handler for a class will replace any
 * content handler previously registered to that class.
 *
 * <h3>Content Handler Authentication</h3>
 * Applications and content handlers using the API may,
 * for security purposes, need or want to authenticate
 * the invoked content handler or the invoking application.
 * The CHAPI implementation cooperates with the application management
 * software to authenticate the applications (if possible) and pass the
 * authentication information to the application. If the application
 * can be authenticated, then the 
 * {@link ContentHandler#getAuthority ContentHandler.getAuthority}
 * method will return the authority used in the authentication.
 * <p>
 * While processing an invocation request the content handler can use the 
 * {@link Invocation#getInvokingAuthority Invocation.getInvokingAuthority} 
 * method to verify the authenticity of the invoking application.</p>
 *
 * <h3>Content Handler Access Control</h3>
 * The visibility and accessibility of a content handler
 * can be controlled by the content handler itself either through
 * dynamic or static registration.  To restrict
 * access and visibility, the content handler MUST provide the IDs of
 * the content handlers that MUST be allowed access.
 * If any of the allowed IDs match
 * the beginning of the ID of the content handler requesting access, then the
 * application will be granted visibility and access to
 * the content handler. The comparison is performed as
 * if the <code>java.lang.string.startsWith</code> method was used.
 * The access controls are only visible to the content handler
 * itself using the method
 * {@link ContentHandlerServer#getAccessAllowed
 * ContentHandlerServer.getAccessAllowed}.
 * 
 * By default,
 * access is allowed to all applications and content handlers.
 * Access restrictions are established when the content handler is
 * registered with the {@link #register register} method.
 *
 * <A NAME="dynamic"><h3>Dynamic Registration</h3></A>
 * <p>
 * Dynamic registration is performed by
 * initializing the classname, types, suffixes,
 * actions, action names, ID, and access restrictions, and passing 
 * them to the {@link #register register} method.
 * The {@link #unregister unregister} method removes
 * a registered content handler.
 * <p>
 * The current registrations can be examined by using various methods to
 * get the {@link #getTypes types},
 * {@link #getIDs IDs},
 * {@link #getSuffixes suffixes},
 * {@link #getActions actions},
 * or to find a content handler
 * by content handler {@link #getServer classname}, or
 * to get all of the matching content handlers 
 * by {@link #forID ID}, 
 * by {@link #forType content type},
 * by {@link #forSuffix suffix}, or
 * by {@link #forAction action}.
 * Only content handlers that are accessible and visible to the
 * application will be returned.
 *
 * <h3><A name="execution"></A>Invoking a Content Handler</h3>
 * <P>
 * To invoke a content handler, an application initializes an
 * Invocation instance with the information needed to identify the
 * content and/or the content handler. Typically this could include the
 * URL, type, action, and content handler ID.
 * The application may also supply arguments and data, and set
 * whether a response is required.
 *
 * The application may request information about the content
 * and the content handler that will process it before invocation.
 *
 * Calling the {@link Registry#invoke Registry.invoke} method
 * starts the request.
 * <P>
 * When invoked, the ID, type, URL, and action are used to identify a
 * target content handler.  If multiple content handlers are registered
 * for the ID, type, suffixes or action, the implementation can decide
 * which to use to satisfy the request. If the application needs to
 * select which handler to use, the
 * {@link Registry#findHandler findHandler} method will
 * return the set of matching ContentHandlers. The application can
 * choose from the content handlers returned and use 
 * {@link Invocation#setID Invocation.setID}
 * to select a specific handler.
 * <P>
 * In a runtime environment in which only a single application can
 * run at a time, the invoking application must exit before the 
 * content handler can be started to handle the request.
 * Invocation requests are queued so that the invoking
 * application and the content handler can be run sequentially in
 * resource-constrained environments.
 * The return value of {@link Registry#invoke invoke}
 * is <code>true</code> to indicate that the application must exit.
 * This allows the invoking application to save the users context
 * and leave the user interface, if any, with some visual that the
 * user will see until the content handler is ready.</P>
 *
 * <P>
 * Invocation processing involves the invoking application,
 * the invoked
 * content handler, and the application management software (AMS).
 * The implementation of the API and the AMS MUST implement the
 * queuing of invocations to the appropriate content handler and
 * the necessary interactions with the lifecycle to start and
 * restart applications and content handlers.
 * <p>
 * The {@link Registry#invoke invoke} methods initiate the request.
 * The URL, type, action, and ID, as described above, are used to
 * dispatch the request to an appropriate content handler.
 * If the content handler is not running, it MUST be started to process
 * the request. If the content handler is already running,
 * then the new request MUST be queued to the content handler.
 * Only a single instance of each content handler application can be
 * active at a time.
 * The {@link ContentHandlerServer} class is used to dequeue and process
 * requests.
 */
public class Registry {

    /**
     * Inner class to request security token from SecurityInitializer.
     * SecurityInitializer should be able to check this inner class name.
     */
    static private class SecurityTrusted implements TrustedClass { };
    
    /** This class has a different security domain than the MIDlet suite */
    private static Token classSecurityToken =
        SecurityInitializer.requestToken(new SecurityTrusted());

    /** The mutex used to avoid corruption between threads. */
    private static final Object mutex = new Object();

    /** The reference to the RegistryImpl with the real implementation. */
    private RegistryImpl impl;
    
    /**
     * Gets the Registry for the application or content handler
     * that will be calling registry methods.
     * The application is identified by the classname that implements
     * the lifecycle of the Java runtime environment.
     * The classname must be the name of a registered application class
     * or a registered content handler.
     * <p>
     * For a MIDP implementation,
     * application classes must be registered with the
     * <code>MIDlet-&lt;n&gt;</code> attribute; content handlers are
     * registered with the <code>MicroEdition-Handler-&lt;n&gt</code>
     * attribute or the {@link #register register} method.
     *
     * @param classname the application class
     * @return a Registry instance providing access to content handler
     *  registrations and invocations; MUST NOT be <code>null</code>
     * @exception IllegalArgumentException is thrown if the classname
     *  is not a registered application or content handler
     * @exception NullPointerException if <code>classname</code> is
     *       <code>null</code>
     */
    public static Registry getRegistry(String classname) {
        // Find the RegistryImpl instance and get/create the Registry
        try {
            return findRegistryImpl(classname).getRegistry();
		} catch (ContentHandlerException che) {
		    throw new IllegalArgumentException(che.getMessage());
		}
    }

    /**
     * Gets the RegistryImpl for the classname.
     * Create the Registry instance if it has not already been created.
     *
     * @param classname the classname
     * @return RegistryImpl
     *
     * @exception ContentHandlerException is thrown with a reason of
     *  <code>NO_REGISTERED_HANDLER</code> if the classname
     *  is not a registered application or content handler
     */
    private static RegistryImpl findRegistryImpl(String classname) 
        throws ContentHandlerException
    {
        synchronized (mutex) {
            RegistryImpl impl = 
                RegistryImpl.getRegistryImpl(classname,	classSecurityToken);
            // Make sure there is a Registry; 
            if (impl.getRegistry() == null) {
                impl.setRegistry(new Registry(impl));
            }
            return impl;
        }
    }

    /**
     * Constructor to create a new Registry with a RegistryImpl
     * and to insert it int the list of known Registry instances.
     * @param impl the RegistryImpl to delegate to
     */
    private Registry(RegistryImpl impl) {
        this.impl = impl;
    }

    /**
     * Gets the content handler server registered for the content
     * handler.
     * The <code>classname</code> MUST be registered as 
     * a content handler in the current application package using
     * either the {@link #register register} method or 
     * the static registration attributes. 
     *
     * @param classname the name of an application class or
     *  content handler registered by this application package
     * @return the content handler for the registered
     * <code>classname</code> registered by this application package;
     * MUST NOT be <code>null</code>
     *
     * @exception NullPointerException if <code>classname</code> is
     *       <code>null</code>
     * @exception ContentHandlerException is thrown with a reason of
     *  <code>NO_REGISTERED_HANDLER</code> if there is no
     *  content handler registered for the classname in the current
     *  application package
     */
    public static ContentHandlerServer getServer(String classname)
						throws ContentHandlerException
    {
		RegistryImpl registryImpl = findRegistryImpl(classname);
		// Insure only one thread promotes to ContentHandlerServer
		ContentHandlerImpl server = null;
		synchronized (mutex) {
		    server = registryImpl.getServer();
		    if (server == null) {
				throw new ContentHandlerException("No registered handler",
								ContentHandlerException.NO_REGISTERED_HANDLER);
		    }
	
		    if (!(server instanceof ContentHandlerServer)) {
				// Not already a ContentHandlerServer; replace
				server = new ContentHandlerServerImpl(server);
				registryImpl.setServer(server);
		    }
		}
		return (ContentHandlerServer)server; 
    }

    /**
     * Registers the application class using content
     * type(s), suffix(es), and action(s), action name(s),
     * access restrictions and content handler ID.
     * <p>
     * This method will replace any content handler
     * registration in the application package 
     * that has the same classname.
     * The update occurs atomically: the update to the registry
     * either occurs or it does not.
     * <p>
     * The content handler may register the following items:
     * <ul>
     *    <li>zero or more content types</li>
     *    <li>zero or more suffixes</li>
     *    <li>zero or more actions</li>
     *    <li>zero or more mappings from actions to action names</li>
     *    <li>zero or more IDs of applications allowed access </li>
     *    <li>an optional application ID</li>
     * </ul>
     * 
     * <p>
     * If no exceptions are thrown, then the type(s), suffix(s), action(s),
     * action names, access restrictions, and ID
     * will be registered for the application class.
     * <p>
     * If an exception is thrown, then the previous registration, if
     * any, will not be removed or modified.
     *
     * @param classname the name of an application class or
     *  content handler in this application package;
     *  the value MUST NOT be <code>null</code>;
     *	and the handler MUST implement the lifecycle of the Java runtime
     *  environment
     * @param types an array of types to register;
     *   if <code>null</code> it is treated the same as an empty array
     * @param suffixes an array of suffixes to register;
     *   if <code>null</code> it is treated the same as an empty array
     * @param actions an array of actions to register;
     *   if <code>null</code> it is treated the same as an empty array
     * @param actionnames an array of ActionNameMaps to register;
     *   if <code>null</code> it is treated the same as an empty array
     * @param ID the content handler ID; if <code>null</code>
     *  a default non-null value MUST be provided by the implementation
     * @param accessAllowed the IDs of applications and content
     *  handlers that are
     *  allowed visibility and access to this content handler;
     *  if <code>null</code> or an empty array then all applications and 
     *  content handlers are allowed access;
     *  otherwise ONLY applications and content handlers with matching IDs
     *  are allowed access.
     *   
     * @return the registered ContentHandler; MUST NOT be <code>null</code>
     * @exception NullPointerException if any of the following items is
     * <code>null</code>:
     * <ul>
     *    <li><code>classname</code></li>
     *    <li>any array element of <code>types</code>, <code>suffixes</code>,
     *        <code>actions</code>, <code>actionnames</code>, and
     *        <code>accessAllowed</code></li>
     * </ul>
     *
     * @exception IllegalArgumentException is thrown:
     * <ul>
     *    <li>if any of the <code>types</code>, <code>suffix</code>,
     *        <code>actions</code>, or <code>accessAllowed</code>
     *        strings have a length of zero, or </li>
     *    <li>if the <code>classname</code> does not implement the valid
     *        lifecycle for the Java runtime environment,</li>
     *    <li>if the ID has a length of zero or contains any
     *        control character or space (U+0000-U+00020),</li>
     *    <li>if the sequence of actions in any ActionNameMap 
     *        is not the same as the sequence of <code>actions</code>,
     *        or </li>
     *    <li>if the locales of the ActionNameMaps are not unique.</li>
     * </ul>
     * @exception ClassNotFoundException if the <code>classname</code>
     *   is not present
     * @exception ContentHandlerException with an error code of 
     *  {@link ContentHandlerException#AMBIGUOUS} if <code>ID</code>
     *  (or if ID is null, the default ID)
     *  is a prefix of any registered handler or if any registered
     *  handler ID is a prefix of this ID,
     *  except where the registration is replacing or updating 
     *  an existing registration with the same <code>classname</code> 
     *
     * @exception SecurityException if registration
     *   is not permitted
     */
    public ContentHandlerServer register(String classname,
					 String[] types,
					 String[] suffixes,
					 String[] actions,
					 ActionNameMap[] actionnames,
					 String ID,
					 String[] accessAllowed)
	throws SecurityException, IllegalArgumentException,
	       ClassNotFoundException, ContentHandlerException
    {
	// First register the new/replacement handler
	impl.register(classname, types, suffixes,
		      actions, actionnames, ID, accessAllowed);
	// Return the value from {#link #getServer(classname)}.
	return getServer(classname);
    }

    /**
     * Removes the content handler registration for the application
     * class and any bindings made during registration to the content ID,
     * type(s), suffix(es), and action(s), etc.
     * Only content handlers registered either statically or dynamically
     * in the current application package will be removed.
     *
     * @param classname the name of the content handler class
     * @return <code>true</code> if the content handler registered
     *   by this application was found and removed;
     *   <code>false</code> otherwise
     * @exception NullPointerException if <code>classname</code> is
     *   <code>null</code>
     */
    public boolean unregister(String classname) {
        return impl.unregister(classname);
    }

    /**
     * Gets all of the unique content types for which there are registered
     * handlers. 
     * Type strings are not case sensitive, types that differ
     * only by case are treated as a single type.
     * Each type is returned only once.
     * After a successful registration, the content handler's type(s),
     * if any, will appear in this list.
     * <P>
     * Only content handlers that this application is
     * allowed to access will be included.</p>
     *
     * @return an array of types; MUST NOT be <code>null</code>
     */
    public String[] getTypes() {
        return impl.getTypes();
    }

    /**
     * Gets the IDs of the registered content handlers.
     * <P>
     * Only content handlers that this application is
     * allowed to access will be included.</p>
     * @return an array of content handler IDs;
     *  MUST NOT be <code>null</code>
     */
    public String[] getIDs() {
        return impl.getIDs();
    }

    /**
     * Gets the unique actions of the registered content handlers.
     * No duplicate strings will be returned.
     * After a successful registration the content handler's action(s),
     * if any, will appear in this list.
     * <P>
     * Only content handlers that this application is
     * allowed to access will be included.</p>
     * @return an array of content handler actions;
     *  MUST NOT be <code>null</code>
     */
    public String[] getActions() {
        return impl.getActions();
    }

    /**
     * Gets the unique suffixes of the registered content handlers.
     * Suffix strings are not case sensitive, suffixes that differ
     * only by case are treated as a single suffix.
     * Each suffix is returned only once.
     * After a successful registration the content handler's suffix(es),
     * if any, will appear in this list.
     * <P>
     * Only content handlers that this application is
     * allowed to access will be included.</p>
     * @return an array of content handler suffixes;
     *  MUST NOT be <code>null</code>
     */
    public String[] getSuffixes() {
        return impl.getSuffixes();
    }

    /**
     * Gets the registered content handlers for the content type.
     * <P>
     * Only content handlers that are visible and accessible to this
     * application are returned.
     *
     * @param type the type of the requested content handlers
     * @return an array of the <code>ContentHandler</code>s registered
     *  for the type; MUST NOT be <code>null</code>.
     *  An empty array is returned if there are no
     * <code>ContentHandler</code>s accessible to
     *  this application with the type equal to the request type.
     * @exception NullPointerException if <code>type</code> is
     *       <code>null</code>
     */
    public ContentHandler[] forType(String type) {
        return impl.forType(type);
    }

    /**
     * Gets the registered content handlers that support the action.
     * <P>
     * Only content handlers that are visible and accessible to this
     * application are returned.
     *
     * @param action content handlers for which the action is supported
     * @return an array of the <code>ContentHandler</code>s registered
     *  for the action; MUST NOT be <code>null</code>;
     *  an empty array is returned if no <code>ContentHandler</code>s
     *  are accessible to this application
     * @exception NullPointerException if <code>action</code> is
     *       <code>null</code>
     */
    public ContentHandler[] forAction(String action) {
        return impl.forAction(action);
    }

    /**
     * Gets the content handlers for the suffix.
     * <p>
     * Only content handlers that are visible and accessible to this
     * application are returned.
     *
     * @param suffix the suffix to be used to get the associated
     * content handlers
     *
     * @return an array of the <code>ContentHandler</code>s registered
     *  for the suffix; MUST NOT be <code>null</code>.
     *  An empty array is returned if there are none accessible to
     *  this application
     *
     * @exception NullPointerException if <code>suffix</code> is
     *       <code>null</code>
     */
    public ContentHandler[] forSuffix(String suffix) {
        return impl.forSuffix(suffix);
    }

    /**
     * Gets the registered content handler for the ID.
     * The query can be for an exact match or for the handler
     * matching the prefix of the requested ID.
     * <P>
     * Only a content handler which is visible to and accessible to this
     * application will be returned.
     * <P>
     * The <code>forID</code> method may be useful for applications
     * with multiple components or subsystems
     * to define a base ID for the application. 
     * A request to a particular component can be made by appending an
     * additional string to the base ID. The additional string can be
     * used by the handler itself to dispatch to
     * the component or subsystem. The <code>forID</code> method can be used to 
     * query for the registered content handler.
     *
     * @param ID the content handler application ID of the content
     *       handler requested 
     * @param exact <code>true</code> to require an exact match;
     * <code>false</code> to allow a registered content handler ID
     * 		to match a prefix of the requested ID
     *
     * @return the content handler that matches the ID,
     *       otherwise <code>null</code>
     *
     * @exception NullPointerException if <code>ID</code> is
     *       <code>null</code>
     */
    public ContentHandler forID(String ID, boolean exact) {
	return impl.forID(ID, exact);
    }

    /**
     * Gets the registered content handlers that could be used for
     * this Invocation.  Only handlers accessible to the application
     * are considered. The values for ID, type, URL, and
     * action are used in the following order:
     * <ul>
     *    <li>If the ID is non-null, then a candidate
     *        handler is determined by the {@link #forID forID}
     *        method with the  parameter <tt>exact</tt> set to false.
     *        The type and URL are ignored. If there is no handler that matches
     *        the requested ID then a <tt>ContentHandlerException</tt>
     *        is thrown.</li>
     *
     *    <li>If the ID and type are <code>null</code> and
     *        the URL is <code>non-null</code> and
     *        if the protocol supports typing of content, then
     *        the type is determined
     *        as described in {@link Invocation#findType}.
     *        If the type cannot be determined from the content,
     *        the type is set to <code>null</code>.</li>
     *
     *    <li>If the ID is null and type is non-null,
     *        then the set of candidate handlers is determined from the
     *        {@link #forType forType} method.
     *        If there are no handlers that match the requested type
     *        then a <tt>ContentHandlerException</tt> is thrown. </li>
     *
     *    <li>If both the ID and type are <code>null</code> and
     *        the URL is <code>non-null</code> and 
     *        if the protocol does not support typing of content
     *        or the type was not available from the content,
     *        then the set of candidate handlers
     *        includes any handler with a suffix that matches the
     *        end of the path component of the URL.
     *        If there are no handlers that match a registered
     *        suffix then a <tt>ContentHandlerException</tt> is thrown.</li>
     *
     *    <li>If the ID, type, and URL are all null, the set of candidate
     *        handlers includes all of the accessible handlers.</li>
     *
     *    <li>If the action is non-null, the set of candidate handlers
     *        is reduced to contain only handlers that support the
     *        action.</li>
     *
     *    <li>If the set of candidate handlers is empty
     *        then a <tt>ContentHandlerException</tt> is thrown.</li>
     * </ul>
     * <p>
     * The calling thread blocks while the ID and type are being determined.
     * If a network access is needed there may be an associated delay.
     *
     * @param invocation the ID, type, action, and URL that
     *  are needed to identify one or more content handlers;
     *  must not be <code>null</code>
     * @return an array of the <code>ContentHandler</code>(s)
     *  that could be used for this Invocation; MUST NOT be <code>null</code>;
     *
     * @exception IOException is thrown if access to the content fails
     * @exception ContentHandlerException is thrown with a reason of
     *      <code>NO_REGISTERED_HANDLER</code> if
     *          there is no registered content handler that
     *          matches the requested ID, type, URL, and action
     *
     * @exception IllegalArgumentException is thrown if ID, type, URL,
     *  and action are all <code>null</code> or
     *  if the content is accessed via the URL and the URL is invalid
     * @exception NullPointerException is thrown if the
     *  <code>invocation</code> is <code>null</code>
     * @exception SecurityException is thrown if access to the content
     *  is not permitted
     */
    public ContentHandler[] findHandler(Invocation invocation)
	throws IOException, ContentHandlerException, SecurityException
    { 
        return impl.findHandler(invocation.getInvocImpl());
    }

    /**
     * Checks the Invocation and uses the ID, type, URL, and action,
     * if present, to find a matching ContentHandler and queues this
     * request to it.
     * <p>
     * If the <code>previous</code> Invocation is <code>null</code>, then
     * a new transaction is created; otherwise, this
     * Invocation will use the same transaction as the
     * <code>previous</code> Invocation.
     * <p>
     * The status of this Invocation MUST be <code>INIT</code>.
     * If there is a previous Invocation, that Invocation MUST
     * have a status of <code>ACTIVE</code> and this Invocation MUST
     * require a response.
     * <p>
     * Candidate content handlers are found as described in
     * {@link #findHandler findHandler}. If any handlers are
     * found, one is selected for this Invocation.
     * The choice of content handler is implementation dependent.
     * <p>
     * A copy of the Invocation is made, the status is set to
     * <code>ACTIVE</code> and then queued to the
     * target content handler.
     * If the invoked content handler is not running, it MUST be started
     * as described in <a href="#execution">Invoking a Content Handler</a>.
     * <p>
     * The status of this Invocation is set to <code>WAITING</code>.
     * If there is a non-null <code>previous</code> Invocation,
     * its status is set to <code>HOLD</code>.
     * The <code>previous</code> Invocation is saved in the waiting
     * Invocation.  
     * It can be retrieved by the <code>getPrevious</code> method.
     * <p>
     * The calling thread blocks while the content handler is being determined.
     * If a network access is needed, there may be an associated delay.
     *
     * @param invocation the Invocation containing the target ID, type, URL,
     *  actions, arguments, and responseRequired parameters;
     *  MUST NOT be <code>null</code>
     * @param previous a previous Invocation for this Invocation;
     *  may be <code>null</code>
     *
     * @return <code>true</code> if the application MUST
     *  voluntarily exit to allow the target content handler to be started;
     *  <code>false</code> otherwise
     *
     * @exception IllegalArgumentException is thrown if:
     *  <ul>
     *     <li> the ID, type, URL, and action are all
     *          <code>null</code>,</li>
     *     <li> the argument array contains any <code>null</code>
     *          references, or <li>
     *     <li> the content is accessed via the URL and the URL is
     *          invalid, or
     *     <li> the <code>invocation.getResponseRequired</code>
     *          method returns <code>false</code> and 
     *          <code>previous</code> is non-null</li>
     *  </ul>
     * @exception IOException is thrown if access to the content fails
     * @exception ContentHandlerException is thrown with a reason of
     *      <code>NO_REGISTERED_HANDLER</code> if
     *          there is no registered content handler that
     *          matches the requested ID, type, URL, and action
     *
     * @exception IllegalStateException is thrown if the status of this
     *	Invocation is not <code>INIT</code> or if the status of the previous
     *	Invocation, if any, is not <code>ACTIVE</code>
     * @exception NullPointerException is thrown if the
     *  <code>invocation</code> is <code>null</code>
     * @exception SecurityException if access to the content is not permitted
     */
    public boolean invoke(Invocation invocation, Invocation previous)
	throws IllegalArgumentException, IOException,
	       ContentHandlerException, SecurityException
    {
	if (invocation.getStatus() != Invocation.INIT) {
	    throw new IllegalStateException();
	}

	if (previous != null &&
            previous.getStatus() != Invocation.ACTIVE) {
	    throw new IllegalStateException();
	}
        
	InvocationImpl invocImpl = invocation.getInvocImpl();
	
	InvocationImpl prevImpl = null;
	if (previous != null) {
	    prevImpl = previous.getInvocImpl();
	}

	return impl.invoke(invocImpl, prevImpl);
    }


    /**
     * Checks the Invocation and uses the ID, type, URL, and action,
     * if present, to find a matching ContentHandler and queues this
     * request to it.
     * The behavior is identical to
     * <code>invoke(invocation, null)</code>.
     *
     * @param invocation the Invocation containing the target ID, type,
     *  URL, action, arguments, and responseRequired parameters;
     *  MUST NOT be <code>null</code>
     *
     * @return <code>true</code> if the application MUST
     *  voluntarily exit to allow the target content handler to be started;
     *  <code>false</code> otherwise
     *
     * @exception IllegalArgumentException is thrown if:
     *  <ul>
     *     <li> the ID, type, URL, and action are all
     *          <code>null</code>, or </li>
     *     <li> the content is accessed via the URL and the URL is
     *          invalid, or
     *     <li> the argument array contains any <code>null</code>
     *          references</li>
     *  </ul>
     * @exception IOException is thrown if access to the content fails
     * @exception ContentHandlerException is thrown with a reason of
     *      <code>NO_REGISTERED_HANDLER</code> if
     *          there is no registered content handler that
     *          matches the requested ID, type, URL, and action
     *
     * @exception IllegalStateException is thrown if the status of this
     *	Invocation is not <code>INIT</code>
     * @exception NullPointerException is thrown if the
     *  <code>invocation</code> is <code>null</code>
     * @exception SecurityException if access to the content is not permitted
     */
    public boolean invoke(Invocation invocation)
	throws IllegalArgumentException, IOException,
	       ContentHandlerException, SecurityException
    {
	return invoke(invocation, null);
    }

    /**
     * Reinvokes the Invocation and uses the ID, type, URL, and action
     * to find a matching ContentHandler and re-queues this request to
     * it. Reinvocation is used to delegate the handling of an active
     * Invocation to another content handler.
     * The processing of the Invocation instance is complete and the
     * status is set to <code>OK</code>. Responses to the
     * reinvocation will be queued to the original invoking
     * application, if a response is required.
     *
     * <p>
     * The status of this Invocation MUST be <code>ACTIVE</code>.
     * <p>
     * Candidate content handlers are found as described in
     * {@link #findHandler findHandler}. If any handlers are
     * found, one is selected for this Invocation.
     * The choice of content handler is implementation dependent.
     * <p>
     * The status of this Invocation is set to <code>OK</code>.
     * A copy of the Invocation is made, the status is set to
     * <code>ACTIVE</code>, and then queued to the
     * target content handler.
     * If the invoked content handler application is not running,
     * it MUST be started
     * as described in <a href="#execution">Invocation Processing</a>.
     *
     * <p>
     * The calling thread blocks while the content handler is being determined.
     * If a network access is needed there may be an associated delay.
     *
     * @param invocation an Invocation containing the target ID, type,
     *  action, arguments, and responseRequired parameters;
     *  MUST NOT be <code>null</code>
     *
     * @return <code>true</code> if the application MUST
     *  voluntarily exit to allow the target content handler to be started;
     *  <code>false</code> otherwise
     *
     * @exception IllegalArgumentException is thrown if:
     *  <ul>
     *     <li> the ID, type, and URL are all <code>null</code>, or </li>
     *     <li> the content is accessed via the URL and the URL is
     *          invalid, or
     *     <li> the argument array contains any <code>null</code>
     *          references</li>
     *  </ul>
     * @exception IOException is thrown if access to the content fails
     * @exception ContentHandlerException is thrown with a reason of:
     *      <code>NO_REGISTERED_HANDLER</code> if
     *          there is no registered content handler that
     *          matches the requested ID, type, URL, and action
     *
     * @exception IllegalStateException is thrown if the status of this
     *	Invocation is not <code>ACTIVE</code>
     * @exception NullPointerException is thrown if the
     *  <code>invocation</code> is <code>null</code>
     * @exception SecurityException if access to the content is not permitted
     */
    public boolean reinvoke(Invocation invocation)
	throws IllegalArgumentException, IOException,
	       ContentHandlerException, SecurityException
    {
	if (invocation.getStatus() != Invocation.ACTIVE) {
	    throw new IllegalStateException();
	}

	return impl.reinvoke(invocation.getInvocImpl());
    }

    /**
     * Gets the next Invocation response pending for this application.
     * If requested, the method waits until an Invocation response
     * is available.
     * The method can be unblocked with a call to
     * {@link #cancelGetResponse cancelGetResponse}.
     * The application can process the Invocation based on
     * its status. The status is one of
     * <code>OK</code>, <code>CANCELLED</code>, <code>ERROR</code>,
     * or <code>INITIATED</code>.
     * <p>
     * If the Invocation was invoked with
     * {@link #invoke(Invocation invocation, Invocation previous)},
     * the <code>getPrevious</code> method MUST return the
     * previous Invocation.
     * If the status of the previous Invocation is <code>HOLD</code>
     * then its status is restored to <code>ACTIVE</code>.
     *
     * <p>
     * If the original Invocation instance is reachable, then it
     * MUST be updated with the values from the response
     * and be returned to the application. If it is not
     * reachable, then a new instance is returned from 
     * <code>getResponse</code> with the response values.
     *
     * @param wait <code>true</code> if the method
     *  MUST wait for an Invocation if one is not available;
     *  otherwise <code>false</code> if the method MUST NOT wait
     *
     * @return the next pending response Invocation or <code>null</code>
     *  if the <code>wait</code> is false and no Invocation is available or
     *  if canceled with {@link #cancelGetResponse}
     * @see #invoke
     * @see #cancelGetResponse
     */
    public Invocation getResponse(boolean wait) {
        return impl.getResponse(wait);
    }

    /**
     * Cancels a pending <code>getResponse</code>.
     * This method will force a thread blocked in a call to the
     * <code>getResponse</code> method for this Registry instance
     * to return early.
     * If no thread is blocked; this call has no effect.
     */
    public void cancelGetResponse() {
        impl.cancelGetResponse();
    }

    /**
     * Sets the listener to be notified when a new response is
     * available for the application context.  The request must
     * be retrieved using {@link #getResponse getResponse}.
     * If the listener is <code>non-null</code> and a response is
     * available, the listener MUST be notified.
     * <br>
     * Note that if <tt>getResponse</tt> is being called concurrently
     * with the listener then the listener may not be called because
     * the response has already been returned to the application.
     * The <tt>invocationResponseNotify</tt> is only used as a hint that
     * a response may be available.
     *
     * @param listener the listener to register;
     *   <code>null</code> to remove the listener
     */
    public void setListener(ResponseListener listener) {
        impl.setListener(listener);
    }

    /**
     * Gets the content handler ID for the current application.
     * The ID uniquely identifies the content handler.
     * If the application is a content handler as returned from
     * {@link #getServer getServer} then the ID MUST be 
     * the content handler ID returned from 
     * {@link ContentHandlerServer#getID ContentHandlerServer.getID}.
     * Otherwise, the ID will be generated for the profile.
     * The package documentation
     * for "Content Handlers and the Mobile Information Device Profile"
     * defines the value for MIDP.
     * @return the ID; MUST NOT be <code>null</code>
     */
    public String getID() {
        return impl==null? null : impl.getID();
    }
}
