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

package com.sun.midp.chameleon.skins.resources;

import com.sun.midp.lcdui.DisplayAccess;
import com.sun.midp.lcdui.GraphicsAccess;
import com.sun.midp.chameleon.skins.ScreenSkin;
import com.sun.midp.chameleon.skins.TickerSkin;
import com.sun.midp.chameleon.skins.TitleSkin;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import java.io.IOException;

public class SkinLoader {
    private static SkinResources skinResources = new SkinResourcesImpl();

    /**
     * This method is called by the Display class to hand out GraphicsAccess
     * tunnel instance created in the public javax.microedition.lcdui
     * package and needed for romized images loading.
     *
     * @param access used to get images from the public LCDUI package
     */
    public static void initGraphicsAccess(GraphicsAccess access) {
        skinResources.initGraphicsAccess(access);
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
    public static void loadSkin(boolean reload)
        throws IllegalStateException, IOException {
        skinResources.loadSkin(reload);
        // After reading in the properties from storage (either ROM
        // image or filesystem, we establish all the individual values
        // in the various properties classes
        boolean loadAll = skinResources.ifLoadAllResources();
        loadResources(loadAll);
    }

    /**
     * Load resources data.
     *
     * @param loadAll if true, load all resources. Otherwise,
     * load only selected resources. The rest will be loaded
     * lazily.
     */
    private static void loadResources(boolean loadAll) {
        // load selected resources
        ScreenResources.load();
        ScrollIndResources.load();
        SoftButtonResources.load();
        TickerResources.load();
        TitleResources.load();
        AlertResources.load();


        // load the rest of resources
        if (loadAll) {
            PTIResources.load();
            InputModeResources.load();
            BusyCursorResources.load();
            ChoiceGroupResources.load();
            DateEditorResources.load();
            DateFieldResources.load();
            GaugeResources.load();
            ImageItemResources.load();
            MenuResources.load();
            ProgressBarResources.load();
            StringItemResources.load();
            TextFieldResources.load();
            UpdateBarResources.load();
            VirtualKeyboardResources.load();
        }

        checkLocale();
    }

    public static void checkLocale() {
        String locale = System.getProperty("microedition.locale");

        if (locale != null && locale.equals("he-IL")) {
            ScreenSkin.TEXT_ORIENT = Graphics.RIGHT;
            TickerSkin.DIRECTION = Graphics.RIGHT;
            TitleSkin.TEXT_ALIGN_X = Graphics.RIGHT;
            ScreenSkin.RL_DIRECTION = true;
        } else {
            ScreenSkin.TEXT_ORIENT = Graphics.LEFT;
            TickerSkin.DIRECTION = Graphics.LEFT;
            TitleSkin.TEXT_ALIGN_X = Graphics.LEFT;
            ScreenSkin.RL_DIRECTION = false;
        }

    }

    /**
     * Utility method used by skin property classes to load
     * image resources.
     *
     * @param identifier a unique identifier for the image property
     * @return the Image if one is available, null otherwise
     */
    public static Image getImage(int identifier) {
        return skinResources.getImage(identifier);
    }

    /**
     * Utility method used by skin property classes to load
     * image resources.
     *
     * @param identifier a unique identifier for the image property
     * @param index index of the image
     *
     * @return the Image if one is available, null otherwise
     */
    public static Image getImage(int identifier, int index) {
        return skinResources.getImage(identifier, index);
    }

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
    public static Image[] getCompositeImage(
            int identifier, int piecesNumber) {
        return skinResources.getCompositeImage(identifier, piecesNumber);

    }

    /**
     * Utility method used by skin property classes to load
     * Font resources.
     *
     * @param identifier a unique identifier for the Font property
     * @return the Font object or null in case of error
     */
    public static Font getFont(int identifier) {
        return skinResources.getFont(identifier);
    }

    /**
     * Utility method used by skin property classes to load
     * String resources.
     *
     * @param identifier a unique identifier for the String property
     * @return the String object or null in case of error
     */
    public static String getString(int identifier) {
        return skinResources.getString(identifier);
    }

    /**
     * Utility method used by skin property classes to load
     * integer resources.
     *
     * @param identifier a unique identifier for the integer property
     * @return an integer or -1 in case of error
     */
    public static int getInt(int identifier) {
        return skinResources.getInt(identifier);
    }

    /**
     * Returns sequence of integer numbers corresponding to
     * specified property identifer.
     *
     * @param identifier a unique identifier for the property
     * @return the int[] representing the sequence or null in case of error
     */
    public static int[] getNumbersSequence(int identifier) {
        return skinResources.getNumbersSequence(identifier);
    }
    
    /**
     * Translates constants composition from SkinResourcesConstants class
     * into corresponding constants composition from Graphics class.
     *
     * @param num constants composition
     * @return translated composition
     */
    public static int resourceConstantsToGraphics(int num) {
        if (num == SkinResourcesConstants.SOLID) {
            return Graphics.SOLID;
        } else if (num == SkinResourcesConstants.DOTTED) {
            return Graphics.DOTTED;
        }

        int rv = 0;
        if ((num & SkinResourcesConstants.TOP) != 0) {
            rv |= Graphics.TOP;
        }
        if ((num & SkinResourcesConstants.LEFT) != 0) {
            rv |= Graphics.LEFT;
        }
        if ((num & SkinResourcesConstants.BOTTOM) != 0) {
            rv |= Graphics.BOTTOM;
        }
        if ((num & SkinResourcesConstants.RIGHT) != 0) {
            rv |= Graphics.RIGHT;
        }
        if ((num & SkinResourcesConstants.VCENTER) != 0) {
            rv |= Graphics.VCENTER;
        }
        if ((num & SkinResourcesConstants.HCENTER) != 0) {
            rv |= Graphics.HCENTER;
        }

        return rv;
    }

}
