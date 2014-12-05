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

package javax.microedition.amms;

import javax.microedition.media.Control;
import javax.microedition.media.MediaException;

import com.sun.amms.GlobalMgrImpl;

/**
 * This class is defined by the JSR-234 specification
 * <em>Advanced Multimedia Supplements API
 * for Java&trade; Platform, Micro Edition</em>
 */
// JAVADOC COMMENT ELIDED
public class GlobalManager {

    private GlobalManager() {} // Must be hidden

    private static Spectator _spectator;
    
    // JAVADOC COMMENT ELIDED
    public static Control[] getControls() {
        return GlobalMgrImpl.getInstance().getControls();
    }

    // JAVADOC COMMENT ELIDED
    public static Control getControl(String controlType) {
        return GlobalMgrImpl.getInstance().getControl( controlType );
    }
    
    // JAVADOC COMMENT ELIDED
    public static Spectator getSpectator() throws MediaException {
        if( null == _spectator ) {
            _spectator = new Spectator( GlobalMgrImpl.getInstance().getSpectatorImpl() );
        }
        return _spectator;
    }
    
    // JAVADOC COMMENT ELIDED
    public static EffectModule createEffectModule() throws MediaException {
        throw new MediaException("EffectModule is not supported");
    }

    // JAVADOC COMMENT ELIDED
    public static SoundSource3D createSoundSource3D() throws MediaException {
        return GlobalMgrImpl.getInstance().createSoundSource3D();
    }
    
    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedSoundSource3DPlayerTypes() {
        return
            GlobalMgrImpl.getInstance().getSupportedSoundSource3DPlayerTypes();
    }
     
    // JAVADOC COMMENT ELIDED
    public static MediaProcessor createMediaProcessor(String inputType)
        throws MediaException {
        return
            GlobalMgrImpl.getInstance().createMediaProcessor(inputType);
    }

    // JAVADOC COMMENT ELIDED
    public static String[] getSupportedMediaProcessorInputTypes() {
        return
            GlobalMgrImpl.getInstance().getSupportedMediaProcessorInputTypes();
    }
}
