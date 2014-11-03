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

package com.sun.mmedia;

import javax.microedition.media.control.MetaDataControl;
import java.util.Vector;

public class DirectMetaData implements MetaDataControl {    
    private int hNative;    
    private Object keysLock = new Object();
    private String [] keys;
    
    private native int nGetKeyCount(int hNative);
    private native String nGetKey(int hNative, int index);
    private native String nGetKeyValue(int hNative, String key);
    
    private void updateKeys() {
        Vector vKeys = new Vector( 5 );
        int nKeys = ( hNative != 0 ) ? nGetKeyCount( hNative ) : 0;

        boolean author_key_found = false;
        boolean title_key_found  = false;

        for( int i = 0; i < nKeys; i++ ) {
            String key = nGetKey( hNative, i );
            vKeys.addElement( key );
            if( AUTHOR_KEY.equals( key ) ) author_key_found = true;
            if( TITLE_KEY.equals( key ) ) title_key_found = true;
        }

        if( !author_key_found ) vKeys.addElement( AUTHOR_KEY );
        if( !title_key_found ) vKeys.addElement( TITLE_KEY );

        nKeys = vKeys.size();

        synchronized( keysLock ) {
            if( keys == null || nKeys != keys.length ) {
                keys = new String[ nKeys ];
            }

            for( int i = 0; i < nKeys; i++ ) {
                keys[ i ] = (String)( vKeys.elementAt( i ) );
            }
        }
    }    

    DirectMetaData(int hNative) {
        this.hNative = hNative;
    }

    void playerClosed() {
        hNative = 0;
        keys = null;
    }

    public String[] getKeys() {
        updateKeys();
        return keys;
    }

    public String getKeyValue(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key is null");
        }
        updateKeys();

        synchronized( keysLock ) {
            for( int i = 0; i < keys.length; i++ ) {
                if( key.equals( keys[ i ] ) ) {
                    String s = nGetKeyValue( hNative, key );
                    if( null == s ) {
                        if( AUTHOR_KEY.equals( key ) || TITLE_KEY.equals( key ) ) {
                            s = "Unknown";
                        }
                    }
                    return s;
                }
            }
        }

        throw new IllegalArgumentException("Key is invalid");
    }
}