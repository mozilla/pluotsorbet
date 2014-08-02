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

public class CLDCAppID implements ApplicationID {
	public int		suiteID;
	public String	className;
	
	public CLDCAppID( int suiteID, String classname) {
		this.suiteID = suiteID;
		this.className = classname;
	}
	
	public CLDCAppID() {
		this(AppProxy.EXTERNAL_SUITE_ID, null);
	}
	
	public boolean isNative() {
		return suiteID == AppProxy.EXTERNAL_SUITE_ID;
	}
	
	public ApplicationID duplicate() {
		return new CLDCAppID(suiteID, className);
	}

	public static CLDCAppID from(ApplicationID appID) {
		return (CLDCAppID)appID;
	}
	
	public int hashCode() {
		return suiteID + ((className != null)? className.hashCode() : 0);
	}
	
	public boolean equals(Object appID) {
		if( !(appID instanceof CLDCAppID) )
			return false;
		CLDCAppID app = ((CLDCAppID)appID);
		if( suiteID != app.suiteID )
			return false;
		if( className != null  )
			return className.equals(app.className);
		return app.className == null;
	}
	
	public String toString(){
		if( AppProxy.LOGGER != null )
			return "{" + suiteID + ", " + className + "}";
		return super.toString();
	}
}
