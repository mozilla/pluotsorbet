/*
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

package com.sun.amms.directcontrol;
import javax.microedition.media.Control;

public class DirectAMMSControl implements 
        Control
{
    private int _peer;
    
    protected DirectAMMSControl() {}

    private static String getDirectControlClassName( String controlType )
    {
        int idx = -1;

        while( controlType.indexOf( '.', idx + 1 ) >= 0  )
        {
            idx = controlType.indexOf( '.', idx + 1 );
        }
  
        String name = "com.sun.amms.directcontrol.Direct" + 
                controlType.substring( idx + 1 );
        return name;
    }
    
    public static DirectAMMSControl createDirectAMMSControl( String controlType,
            int peer)
    {
        Class c = null;
        DirectAMMSControl ctr = null;
        
        try 
        {
            c = Class.forName( getDirectControlClassName( controlType ) );
        }
        catch( ClassNotFoundException e )
        {
            return null;
        }
        
        try
        {
            ctr = ( DirectAMMSControl )c.newInstance();
        }
        catch( InstantiationException ie )
        {
            return null;
        }
        catch( IllegalAccessException iae ) 
        {
            return null;
        }

        ctr.setNativePtr( peer );
        return ctr;
    }
    
    protected void setNativePtr( int peer )
    {
        _peer = peer;
    }
    
}
