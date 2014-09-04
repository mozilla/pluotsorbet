/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.util;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;

/**
 * Utility class for testing support for touch events.
 */
public class TouchChecker
    extends Canvas {

    private static TouchChecker self;
    private static boolean traitsChecked = false;
    private static boolean traitsSupported;

    public static final boolean DIRECT_TOUCH_SUPPORTED;

    protected void paint(Graphics g) {}

    private TouchChecker() {
    }

    static {
        DIRECT_TOUCH_SUPPORTED = TouchChecker.isTouchEnabled() && TouchChecker.isS40UITraitsSupported();
    }

    /**
     * Checks wheter pointer events are supported
     */
    public static boolean isTouchEnabled() {
        if (self == null) {
            self = new TouchChecker();
        }
        return self.hasPointerEvents();
    }

    /**
     * Checks whether LCDUIUtil is available and thus UI traits also
     */
    public static boolean isS40UITraitsSupported() {
        if (!traitsChecked) {
            try {
                Class.forName("com.nokia.mid.ui.LCDUIUtil");
                traitsSupported = true;
            }
            catch (NoClassDefFoundError ncdfe) {
                traitsSupported = false;
            }
            catch (Exception e) {
                traitsSupported = false;
            }
        }
        return traitsSupported;
    }
}
