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
package com.sun.midp.appmanager;

import javax.microedition.lcdui.*;
import com.sun.midp.i18n.Resource;
import com.sun.midp.i18n.ResourceConstants;

import com.sun.midp.midletsuite.MIDletSuiteLockedException;
import com.sun.midp.midletsuite.MIDletSuiteCorruptedException;

/**
 * Displays error messages using the Display instance object passed
 * into the Constructor.
 */
class DisplayError {

    /** The display instance to be used to display error alerts */
    Display display;


    /**
     * Creates a DisplayError instance given a display object.
     * @param display the Display instance where Alerts will the errors
     *        will be shown
     */
    DisplayError(Display display) {
        this.display = display;
    }

    /**
     * Display the Alert with the error message.
     *
     * @param appName - name of the application in which the error happen
     * @param t - throwable that was thrown while performing one
     *             of the operations on the application
     * @param alertTitle - if non-null it will be the Alert title, otherwise
     *        a default value will be used
     *       (Resource.getString(ResourceConstants.AMS_CANNOT_START))
     * @param alertMessage - if non-null it will be the Alert message,
     *        otherwise a default message will be generated using
     *       (Resource.getString(ResourceConstants.ERROR))
     */
    void showErrorAlert(String appName, Throwable t,
                        String alertTitle, String alertMessage) {
        showErrorAlert(appName, t, alertTitle, alertMessage, null);    
    }

    /**
     * Display the Alert with the error message.
     *
     * @param appName - name of the application in which the error happen
     * @param t - throwable that was thrown while performing one
     *             of the operations on the application
     * @param alertTitle - if non-null it will be the Alert title, otherwise 
     *        a default value will be used 
     *       (Resource.getString(ResourceConstants.AMS_CANNOT_START))
     * @param alertMessage - if non-null it will be the Alert message, 
     *        otherwise a default message will be generated using
     *       (Resource.getString(ResourceConstants.ERROR))
     * @param nextDisplayable the Displayable to be shown after
     *        the error alert is dismissed; can be null
     */
    void showErrorAlert(String appName, Throwable t, 
                        String alertTitle, String alertMessage,
                        Displayable nextDisplayable) {

        if (alertMessage == null) {

            if (t instanceof MIDletSuiteLockedException) {
                String[] values = new String[1];
                values[0] = appName;
                alertMessage = Resource.getString(
                                   ResourceConstants.AMS_MGR_UPDATE_IS_RUNNING,
                                   values);
            } else if (t instanceof MIDletSuiteCorruptedException) {
                String[] values = new String[1];
                values[0] = appName;
                alertMessage = Resource.getString(
                            ResourceConstants.AMS_MIDLETSUITE_ID_CORRUPT_MSG,
                            values);
            } else {

                t.printStackTrace();

                StringBuffer sb = new StringBuffer();
                
                sb.append(appName);
                sb.append("\n");
                sb.append(Resource.getString(ResourceConstants.ERROR));
                sb.append(": ");
                sb.append(t.toString());
                alertMessage = sb.toString();
            }
        
        }

        if (alertTitle == null) {
            alertTitle = 
                Resource.getString(ResourceConstants.AMS_CANNOT_START);
        }
        Alert a = new Alert(alertTitle, alertMessage, null, AlertType.ERROR);
        a.setTimeout(Alert.FOREVER);

        if (nextDisplayable == null) {
            display.setCurrent(a);
        } else {
            display.setCurrent(a, nextDisplayable);
        }
    }

    /**
     * Display an alert screen when midlet suite is corrupted
     * @param msg Message to display on alert
     */
    void showCorruptedSuiteAlert(String msg) {
        Alert a = new Alert(Resource.getString
                            (ResourceConstants.AMS_CANT_ACCESS),
                            msg, null, AlertType.ERROR);
        a.setTimeout(2000);
        display.setCurrent(a, display.getCurrent());
    }
}
