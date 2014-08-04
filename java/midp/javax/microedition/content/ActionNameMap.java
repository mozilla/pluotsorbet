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

import com.sun.j2me.content.ContentHandlerRegData;

/**
 * An <code>ActionNameMap</code> provides a mapping between
 * actions and corresponding action names.
 * The action name SHOULD be used by an application when the action
 * is presented to a user.
 * The action names in each map apply to a single {@link #getLocale locale}.
 * The application should get the appropriate
 * <code>ActionNameMap</code> based on the desired locale
 * from the method
 * {@link ContentHandler#getActionNameMap(String locale)
 *  ContentHandler.getActionNameMap}. 
 * The actions and corresponding action names are set when the
 * <code>ActionNameMap</code> is created and are immutable thereafter.
 * The indices of the actions and action names are in the range
 * 0 to size-1.
 */
public final class ActionNameMap {
    /** The locale for this ActionNameMap. */
    private final String locale;

    /** The array of actions. */
    private final String[] actions;

    /** The array of action names. */
    private final String[] actionnames;

    /**
     * Create a new map of actions to action names for a locale.
     * The actions and names are parallel sequences of equal length.
     * Each action maps to the corresponding action name.
     *
     * @param actions an array of actions; MUST NOT be <code>null</code>
     * @param actionnames an array of action names;
     *        MUST NOT be <code>null</code>
     * @param locale of the action names; MUST NOT be <code>null</code>;
     *        should be formatted according to the locale syntax
     *        conventions in {@link ContentHandler}.
     * 
     * @exception IllegalArgumentException:
     * <UL>
     * <LI>if any of the <code>actions</code> strings or 
     *    <code>actionname</code> strings have
     *    a length of zero,</LI>
     * <LI>if the length of the <code>actions</code> and
     *    <code>actionnames</code> arrays
     *    are unequal, or equal to zero, or </LI>
     * <LI>if the <code>actions</code> array includes any duplicate
     *     actions.</LI>
     * </UL>
     * @exception NullPointerException if <code>actions</code>, 
     *  <code>actionnames</code>, <code>locale</code>, or
     *  any array element is <code>null</code>.
     */
    public ActionNameMap(String[] actions,
			 String[] actionnames,
			 String locale)
    {
	if (locale.length() == 0) {	// trigger NullPointerException
	    throw new IllegalArgumentException("empty string");
	}
	if (actions.length != actionnames.length ||
	    actions.length == 0) {
	    throw new IllegalArgumentException("lengths incorrect");
	}

	this.locale = locale;
	this.actions = ContentHandlerRegData.copy(actions,false,false);
	this.actionnames = ContentHandlerRegData.copy(actionnames,false,false);
	if (findDuplicate(this.actions) >= 0) {
	    throw new IllegalArgumentException("duplicate string");
	}        
    }

    /**
     * Gets the action name for an action.
     *
     * @param action the action for which to get the associated action name;
     *   MUST NOT be <code>null</code>
     * @return the action name; <code>null</code> is returned
     *   if the action is not found in the sequence of actions
     * @exception NullPointerException if action is <code>null</code>
     */
    public String getActionName(String action) {
	int index = find(actions, action);
	return (index >= 0) ? actionnames[index] : null;
    }

    /**
     * Gets the action for the action name.
     * If the action name appears more than once in the sequence,
     * then any one of the corresponding actions may be returned.
     *
     * @param actionname the action name for which to get the
     *   associated action; MUST NOT be <code>null</code>
     * @return the action;  <code>null</code> is returned
     *   if the <code>actionname</code> is not found in the sequence
     *   of action names  
     * @exception NullPointerException if actionname is <code>null</code>
     */
    public String getAction(String actionname) {
	int index = find(actionnames, actionname);
	return (index >= 0) ? actions[index] : null;
    }

    /**
     * Gets the locale for this set of action names.
     * @return the locale string; must not be <code>null</code>
     */
    public String getLocale() {
	return locale;
    }

    /**
     * Gets the number of pairs of actions and action names.
     * @return the number of actions and corresponding action names
     */ 
    public int size() {
	return actions.length;
    }

    /**
     * Gets the action at the specified index.
     *
     * @param index the index of the action
     * @return the action at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the {@link #size size} method.
     */
    public String getAction(int index) {
	return actions[index];
    }


    /**
     * Gets the action name at the specified index.
     *
     * @param index the index of the action name
     * @return the action name at the specified index
     * @exception IndexOutOfBoundsException if index is less than zero or
     *     greater than or equal to the value of the {@link #size size} method.
     */
    public String getActionName(int index) {
	return actionnames[index];
    }
    /**
     * Search a String for a string.
     * @param strings the array of strings
     * @param string the string to find
     * @return the index of the string or -1 if not found
     * @exception NullPointerException if string is <code>null</code>
     */
    private int find(String[] strings, String string) {
	for (int i = 0; i < strings.length; i++) {
	    if (string.equals(strings[i])) {
		return i;
	    }
	}
	return -1;
    }

    /**
     * Check the strings in an array are unique; no duplicates.
     * @param strings the string array to check.
     * @return return the index of a string that is duplicated;
     * return -1 if none
     * 
     */
    private int findDuplicate(String[] strings) {
	for (int i = 0; i < strings.length; i++) {
	    for (int j = i + 1; j < strings.length; j++) {
		if (strings[i].equals(strings[j])) {
		    return j;
		}
	    }
	}
	return -1;
    }
}
