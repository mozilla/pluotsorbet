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
 * A <tt>ContentHandler</tt> provides the details of a
 * content handler registration.
 *
 * Each ContentHandler contains the ID, content types, suffixes,
 * actions, and action names. It provides the ID,
 * authority, and application name and version of the content handler.
 * The values are set when the content handler is
 * {@link Registry#register register}ed. 
 * ContentHandler instances are immutable and thread safe. 
 * Content handlers can only be changed by re-registering which
 * returns a new ContentHandler instance.
 * The registered content handlers can be queried using the query methods
 * of {@link Registry}. 
 *
 * <H3>Content Types</H3>
 * <P>
 * For the purposes of this API, content types are simple opaque
 * strings that are NOT case-sensitive. All comparisons are performed
 * using case-insensitive string comparisons.
 * By convention, the {@link #UNIVERSAL_TYPE UNIVERSAL_TYPE}
 * is used to indicate any type. A content
 * handler that can support any type of content should include it as
 * one of types when it is registered. Any application can get the list
 * of universal handlers with a query for the <code>UNIVERSAL_TYPE</code>.
 * Handlers with this type are only returned
 * by <code>Registry.findHandler</code> or
 * <code>Registry.forType</code> if the type requested
 * is equal to <code>UNIVERSAL_TYPE</code>. </p>
 * <p>
 * The most common content types are MIME types.
 * <A HREF="http://www.ietf.org/rfc/rfc2046.txt">RFC-2046</A>
 * defines the
 * Multipurpose Internet Mail Extensions (MIME) Part Two: Media Types.
 * It defines the general structure of the MIME media typing system and
 * defines an initial set of media types.
 * <A HREF="http://www.ietf.org/rfc/rfc2046.txt">RFC-2048</A>
 * describes the specific IANA registration procedures for
 * MIME-related facilities.
 * Other strings may be used as content types, but only if the type
 * system semantics are well defined. An example of where the type system
 * semantics are well defined is in the XML
 * messaging schema.</P>
 *
 * <h3>Suffixes</h3>
 * <P>
 * A content handler can declare a set of suffixes that identify
 * content it can handle based on the syntax of a URL.
 * The suffix is a case-insensitive string that
 * includes punctuation, for example ".png".
 * For some URLs and content storage mechanisms, such as
 * file systems, the content type is not readily available.
 * To accommodate this,
 * a mapping can be used to associate URL suffixes with content
 * handlers.  The common practice in file systems is to map filename
 * extensions to content types.  For example, a file ending in
 * <code>.png</code>
 * can be identified as content type <code>image/png</code>.  This
 * mapping is used if the content access mechanism does not support
 * content typing or if the content type is not available from
 * the content.
 * For the <code>http</code>
 * protocol, that supports a mechanism to identify the content type, 
 * the suffix matching MUST be used to identify content handlers if
 * the type is not defined for a particular URL.
 * <a href="http://www.ietf.org/rfc/rfc2396.txt">RFC 2396 -
 * Uniform Resource Identifiers (URI): Generic Syntax</a>
 * describes the syntax of URI's and the path component.
 * Suffix matching consists of comparing each of the registered
 * suffixes with the last n characters of the path component of the URL,
 * where n is the length of the suffix.
 * The comparison is case-insensitive  and is done using the equivalent of
 * <code>java.lang.String.regionMatches</code>.
 * If multiple suffixes would match, the longest suffix that
 * matches is used.
 * </P>
 *
 * <h3>Actions</h3>
 * <P>Each content handler registers a set of actions 
 * it supports.  
 * Actions are Java strings representing semantic functions the
 * content handler can perform on every content type and suffix
 * registered. 
 * Actions are case-sensitive strings.
 * The set of actions is extensible but applications should 
 * choose from the following actions when appropriate:
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
 * <P>The content handler application should provide localized action
 * names for each action. 
 * The action names are used by applications that need to present the
 * possible actions to users in locale appropriate terminology.
 * A mapping for each action to action name should be created
 * for each locale using the
 * {@link ActionNameMap#ActionNameMap ActionNameMap.ActionNameMap} method.
 * The action name maps for all the locales supported by the
 * content handler MUST be included when the content handler is
 * registered.
 * The attribute <code>Microedition-Handler-&lt;n&gt;-&lt;locale&gt;</code>
 * is used to declare action names in the application packaging.
 *
 * <h3>Locale Strings</h3>
 * <p>A locale string MUST include a language code,
 *  and may include a country code and a variant.
 *  The values are separated by a delimiter defined by the Java
 *  runtime environment.
 *  For MIDP, locale strings follow the definition of the 
 *  system property <code>microedition.locale</code> and
 *  the delimiter MUST be a hyphen ("-" = U+002D).
 *  The values for the language, country code and variant are not
 *  validated. 
 * <p> Application developers should refer to 
 *  ISO-639-1 for language codes and  to ISO-3166-1 for country codes.
 */
public interface ContentHandler {
    /**
     * Gets the content handler application ID.
     * The ID uniquely identifies the content handler with
     * the value provided to the {@link Registry#register register}
     * method, if one was supplied, otherwise a unique ID.
     * 
     * This information has been authenticated only if
     * <code>getAuthority</code> is non-null.
     *
     * @return the ID; MUST NOT be <code>null</code>
     */
    public String getID();

    /**
     * Gets the type supported by the content handler at the specified
     * index.
     * The type returned for each index must be the equal to the type
     * at the same index in the <tt>types</tt> array passed to
     * {@link Registry#register Registry.register}.  
     *
     * @param index the index of the type
     * @return the type at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getTypeCount getTypeCount} method.
     */
    public String getType(int index);

    /**
     * Gets the number of types supported by the content handler.
     * The number of types must be equal to the length of the array
     * of types passed to {@link Registry#register Registry.register}.
     *
     * @return the number of types
     */
    public int getTypeCount();

    /**
     * Determines if a type is supported by the content handler.
     *
     * @param type the type to check for
     * @return <code>true</code> if the type is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>type</code>
     * is <code>null</code>
     */
    public boolean hasType(String type);

    /**
     * Gets the suffix supported by the content handler at the
     * specified index.
     * The suffix returned for each index must be the equal to the suffix
     * at the same index in the <tt>suffixes</tt> array passed to
     * {@link Registry#register Registry.register}.  
     *
     * @param index the index of the suffix
     * @return the suffix at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getSuffixCount} method.
     */
    public String getSuffix(int index);

    /**
     * Gets the number of suffixes supported by the content handler.
     * The number of suffixes must be equal to the length of the array
     * of suffixes passed to {@link Registry#register Registry.register}.
     *
     * @return the number of suffixes
     */
    public int getSuffixCount();

    /**
     * Determines if a suffix is supported by the content handler.
     *
     * @param suffix the suffix to check for
     * @return <code>true</code> if the suffix is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>suffix</code>
     * is <code>null</code>
     */
    public boolean hasSuffix(String suffix);

    /**
     * Gets the action supported by the content handler at the
     * specified index.
     * The action returned for each index must be the equal to the action
     * at the same index in the <tt>actions</tt> array passed to
     * {@link Registry#register Registry.register}.  
     *
     * @param index the index of the action
     * @return the action at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getActionCount getActionCount} method.
     */
    public String getAction(int index);

    /**
     * Gets the number of actions supported by the content handler.
     * The number of actions must be equal to the length of the array
     * of actions passed to {@link Registry#register Registry.register}.
     *
     * @return the number of actions
     */
    public int getActionCount();

    /**
     * Determines if an action is supported by the content handler.
     *
     * @param action the action to check for
     * @return <code>true</code> if the action is supported;
     *  <code>false</code> otherwise
     * @exception NullPointerException if <code>action</code>
     * is <code>null</code>
     */
    public boolean hasAction(String action);

    /**
     * Gets the mapping of actions to action names for the current
     * locale supported by this content handler. The behavior is
     * the same as invoking
     * {@link #getActionNameMap getActionNameMap(String)}
     * with the current locale. 
     *
     * @return an ActionNameMap; if there is no map available for the
     *  current locale or if the locale is null or empty,
     *  then it MUST be <code>null</code> 
     */
    public ActionNameMap getActionNameMap();

    /**
     * Gets the mapping of actions to action names for the requested
     * locale supported by this content handler.
     * The locale is matched against the available ActionNameMaps.
     * If a match is found it is used.  If an exact match is
     * not found, then the locale string is shortened and retried
     * if a delimiter is present.
     * For MIDP, the delimiter is ("-" = U+002D).
     * The locale is shortened by retaining only the characters up to
     * but not including the last occurrence of the delimiter.
     * 
     * The shortening and matching is repeated as long as the string
     * contains a delimiter.
     * Effectively, this will try the full locale and then try
     * without the variant and then without the country code,
     * if present.
     *
     * @param locale for which to find an <tt>ActionNameMap</tt>;
     *   MUST NOT be <code>null</code>
     * @return an <tt>ActionNameMap</tt>; if there is no map available for the
     *  locale, then it MUST be <code>null</code> 
     * @exception NullPointerException if the <code>locale</code>
     *  is <code>null</code> 
     */
    public ActionNameMap getActionNameMap(String locale);

    /**
     * Gets the number of action name maps supported by the content handler.
     * The number of action name maps must be equal to the length of
     * the array of action name maps passed to
     * {@link Registry#register Registry.register}.
     *
     * @return the number of action name maps
     */
    public int getActionNameMapCount();

    /**
     * Gets the ActionNameMap supported by the
     * content handler at the specified index.
     * The ActionNameMap returned for each index must be the equal
     * to the ActionNameMap at the same index in the <tt>actionnames</tt>
     * array passed to {@link Registry#register Registry.register}.  
     *
     * @param index the index of the locale
     * @return the ActionNameMap at the specified index
     *
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the
     *     {@link #getActionNameMapCount getActionNameMapCount} method.
     */
    public ActionNameMap getActionNameMap(int index);

    /**
     * Gets the user-friendly application name of this content handler. 
     * The value is extracted from the normal installation information
     * about the content handler application.
     *
     * @return the user-friendly name of the application;
     * it MUST NOT be <code>null</code>
     */
    public String getAppName();

    /**
     * Gets the version of this content handler.
     * The value is extracted from the normal installation information
     * about the content handler application.
     * @return the version of the application;
     * MAY be <code>null</code>
     */
    public String getVersion();

    /**
     * Gets the authority that authenticated this application.
     * This value MUST be <code>null</code> unless the device has been
     * able to authenticate this application.
     * If <code>non-null</code>, it is the string identifying the
     * authority.
     * The authority is determined by the security mechanisms
     * and policy of the Java runtime environment.
     * For signed MIDP applications, it is the subject of the signing
     * certificate.
     *
     * @return the authority; may be <code>null</code>
     */
    public String getAuthority();

    /** Action to <code>open</code> content. */
    public static final String ACTION_OPEN = "open";
    /** Action to <code>edit</code> the content. */
    public static final String ACTION_EDIT = "edit";
    /** Action to <code>send</code> the content via email or messaging. */
    public static final String ACTION_SEND = "send";
    /** Action to <code>save</code> the content. */
    public static final String ACTION_SAVE = "save";
    /** Action to <code>execute</code> the content. */
    public static final String ACTION_EXECUTE = "execute";
    /** 
     * Action to <code>select</code> a value from the content,
     * usually with user input, and return its value.
     * For example, if the content were a URL of an address book,
     * then the user would be asked to choose an entry or entries to
     * return.
     * The format of the return value depends on the content handler
     * and the content, but if appropriate it should take the form 
     * of a URL.
     */
    public static final String ACTION_SELECT = "select";
    /** Action to <code>install</code> the content on the device. */
    public static final String ACTION_INSTALL = "install";
    /** Action to <code>print</code> the content. */
    public static final String ACTION_PRINT = "print";
    /** Action to create <code>new</code> content. */
    public static final String ACTION_NEW = "new";

    /**
     * Action to request a content handler to stop processing
     * the content identified by the URL, ID, and
     * arguments.  If stopping a previous request, these
     * values should match the corresponding values in that request.
     */
    public final String ACTION_STOP = "stop";
     
    /**
     * The universal type; a handler supporting this type can handle
     * any type of content.
     */
    public static final String UNIVERSAL_TYPE = "*";
}
