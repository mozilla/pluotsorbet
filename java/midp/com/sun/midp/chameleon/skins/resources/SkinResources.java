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

package com.sun.midp.chameleon.skins.resources;

import com.sun.midp.lcdui.GraphicsAccess;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import java.io.IOException;

/** Interface for accessing skin resources. */
public abstract class SkinResources {

    /**
     * A special internal reference to a tunnel which will provide
     * package access to Image inside of javax.microedition.lcdui.
     */
    protected GraphicsAccess graphicsAccess;

    /**
     * This class needs no real constructor, but its here as 'public'
     * so the SecurityIntializer can do a newInstance() on it and call
     * the initSecurityToken() method.
     */
    public SkinResources() {
    }

    /**
     * This method is called by the Display class to hand out GraphicsAccess
     * tunnel instance created in the public javax.microedition.lcdui
     * package and needed for romized images loading. 
     *
     * @param access used to get images from the public LCDUI package
     */
    public void initGraphicsAccess(GraphicsAccess access) {
        if (graphicsAccess == null) {
            graphicsAccess = access;
        }
    }

    /**
     * Load the skin, including all its properties and images. Some parts
     * of the skin may be lazily initialized, but this method starts the
     * process. If the flag to 'reload' is true, the method will ignore
     * all previously loaded resources and go through the process again.
     *
     * @param reload if true, ignore previously loaded resources and reload
     * @throws java.io.IOException if there was error reading skin data file
     * @throws IllegalStateException if skin data file is invalid
     */
    abstract public void loadSkin(boolean reload)
        throws IllegalStateException, IOException;
    
    /**
     * Utility method used by skin property classes to load
     * image resources.
     * 
     * @param identifier a unique identifier for the image property
     * @return the Image if one is available, null otherwise
     */
    abstract public Image getImage(int identifier);
    
    /**
     * Utility method used by skin property classes to load
     * image resources.
     * 
     * @param identifier a unique identifier for the image property
     * @param index index of the image
     *
     * @return the Image if one is available, null otherwise
     */
    abstract public Image getImage(int identifier, int index) ;

    /**
     * Utility method used by skin property classes to load
     * composite image resources consisting of a few images.
     *
     * @param identifier a unique identifier for the composite image property
     * @param piecesNumber number of pieces consisting the composite image 
     *
     * @return the Image[] with loaded image pieces,
     * or null if some of the pieces is not available
     */
    public Image[] getCompositeImage(
            int identifier, int piecesNumber) {
        Image[] result = new Image[piecesNumber];
        for (int i = 0; i < piecesNumber; i++) {
            result[i] = getImage(identifier, i);
            if (result[i] == null) {
                result = null;
                break;
            }
        }
        return result;
    }

    /**
     * Utility method used by skin property classes to load
     * Font resources.
     *
     * @param identifier a unique identifier for the Font property
     * @return the Font object or null in case of error
     */     
    abstract public Font getFont(int identifier);
    
    /**
     * Utility method used by skin property classes to load
     * String resources.
     *
     * @param identifier a unique identifier for the String property
     * @return the String object or null in case of error
     */     
    abstract public String getString(int identifier);
    
    /**
     * Utility method used by skin property classes to load
     * integer resources.
     *
     * @param identifier a unique identifier for the integer property
     * @return an integer or -1 in case of error
     */     
    abstract public int getInt(int identifier);

    /**
     * Returns sequence of integer numbers corresponding to 
     * specified property identifer.
     *
     * @param identifier a unique identifier for the property
     * @return the int[] representing the sequence or null in case of error
     */
    abstract public int[] getNumbersSequence(int identifier);


    /**
     * Determine how skin resources should be loaded: at once, 
     * during skin loading, or lazily, on first use.
     * @return true if all resources should be loaded at once.
     */
    abstract public boolean ifLoadAllResources();

}
