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

package com.sun.amms;
import java.util.Hashtable;
import javax.microedition.media.Controllable;
import javax.microedition.media.Control;
import javax.microedition.media.MediaException;
import javax.microedition.amms.Spectator;
import com.sun.amms.directcontrol.DirectAMMSControl;


abstract class AbstractDirectControllable implements Controllable
{
    private Hashtable _hControls;
    private Control[] _aControls;

    AbstractDirectControllable()
    {
        _hControls = new Hashtable();
        _aControls = null;
    }
    protected abstract String [] getSupportedControlNames();
    
    public final Control [] getControls()
    {
        if( _aControls != null )
        {
            return _aControls;
        }
        
        String[] names = getSupportedControlNames();
        _aControls = new Control [ names.length ];
        for( int i = 0; i < names.length; i++ )
        {
            System.out.println( "AK debug: the control " + names [i] + " was declared as supported" );
            System.out.flush();
            _aControls[ i ] = ( Control )_hControls.get( names[ i ] );
            
            if( _aControls[ i ] == null )
            {
                _aControls[ i ] = createControl( names[ i ] );
            }
            
            if( _aControls[ i ] == null )
            {
                throw new RuntimeException( 
                        "Failed to find native implementation of "
                        + names[ i ] + 
                        " which was declared to be supported" );
            }
            
        }
        
        return _aControls;
        
    }
    
    public final Control getControl( String controlType )
    {
        if (controlType == null) throw new IllegalArgumentException();

        String type;

        // Prepend the package name if the type given does not
        // have the package prefix.
        if (controlType.indexOf('.') < 0) {
            type = "javax.microedition.media.control." + controlType;
        } else {
            type = controlType;
        }

        Control c;
        if( ( c = ( Control )_hControls.get( type ) ) != null )
        {
            return c;
        }
        
        if( _aControls != null )
        {
            return null;
        }
        
        return createControl( type );
    }
    
    protected abstract int getControlPeer( String controlType );
    
    private Control createControl( String type )
    {
        int control_peer = getControlPeer( type );
        if( control_peer == 0 )
        {
            return null;
        }
        
        Control c = DirectAMMSControl.createDirectAMMSControl( type, 
                control_peer );
        if( c != null )
        {
            _hControls.put( type, c );
        }
        
        return c;
    }

}

