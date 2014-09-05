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

package com.sun.j2me.security;

/**
 * FileConnection access permissions.
 */
public class WMAPermission extends Permission {

    static public WMAPermission SMS_SERVER = new WMAPermission(
        "javax.microedition.io.Connector.sms", "sms:open");

    static public WMAPermission getSmsSendPermission(String host, int numSeg) {
        return new WMAPermission(
            "javax.wireless.messaging.sms.send", host, Integer.toString(numSeg));
    }

    static public WMAPermission SMS_RECEIVE = new WMAPermission(
        "javax.wireless.messaging.sms.receive", "sms:receive");


    static public WMAPermission CBS_SERVER = new WMAPermission(
        "javax.microedition.io.Connector.cbs", "cbs:open");

    static public WMAPermission CBS_RECEIVE = new WMAPermission(
        "javax.wireless.messaging.cbs.receive", "cbs:receive");


    static public WMAPermission MMS_SERVER = new WMAPermission(
        "javax.microedition.io.Connector.mms", "mms:open");

    static public WMAPermission getMmsSendPermission(String addresses, String numSeg) {
        return new WMAPermission(
            "javax.wireless.messaging.mms.send", addresses, numSeg);
    }

    static public WMAPermission MMS_RECEIVE = new WMAPermission(
        "javax.wireless.messaging.mms.receive", "mms:receive");

    public WMAPermission(String name, String resource) {
        super(name, resource);
    }

    public WMAPermission(String name, String resource, String extraValue) {
        super(name, resource, extraValue);
    }
}
