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

package javax.microedition.sensor;

public interface SensorInfo {
    public static final int CONN_EMBEDDED               = 1;
    public static final int CONN_REMOTE                 = 2;
    public static final int CONN_SHORT_RANGE_WIRELESS   = 4;
    public static final int CONN_WIRED                  = 8;
    
    public static final java.lang.String CONTEXT_TYPE_AMBIENT   = "ambient";
    public static final java.lang.String CONTEXT_TYPE_DEVICE    = "device";
    public static final java.lang.String CONTEXT_TYPE_USER      = "user";
    static final java.lang.String CONTEXT_TYPE_VEHICLE          = "vehicle";
    
    /* Optional sensor information */
    public static final java.lang.String PROP_LATITUDE          = "latitude";
    public static final java.lang.String PROP_LOCATION          = "location";
    public static final java.lang.String PROP_LONGITUDE         = "longitude";
    public static final java.lang.String PROP_MAX_RATE          = "maxSamplingRate";
    public static final java.lang.String PROP_VENDOR            = "vendor";
    public static final java.lang.String PROP_VERSION           = "version";
    static final java.lang.String PROP_IS_CONTROLLABLE          = "controllable";
    static final java.lang.String PROP_IS_REPORTING_ERRORS      = "errorsReported";
    
    public ChannelInfo[] getChannelInfos();
    public int getConnectionType();
    public java.lang.String getContextType();
    public java.lang.String getDescription();
    public int getMaxBufferSize();
    public java.lang.String getModel();
    public java.lang.Object getProperty(java.lang.String name);
    public java.lang.String[] getPropertyNames();
    public java.lang.String getQuantity();
    public java.lang.String getUrl();
    public boolean isAvailabilityPushSupported();
    public boolean isAvailable();
    public boolean isConditionPushSupported();
}
