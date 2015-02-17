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
package com.sun.javame.sensor;

import javax.microedition.sensor.MeasurementRange;


public class ChannelModel {
    public int scale;
    public String name;
    public String unit;
    public int dataType; // 1 == Double type, 2 == Integer type, 4 == Object type
    public int accuracy;
    public int mrangeCount;
    public long[] mrageArray;
    
    public MeasurementRange[] getMeasurementRanges(){
        MeasurementRange[] res = new MeasurementRange[mrangeCount];
        for (int i=0;i<mrangeCount;i+=3){
            res[i] = new MeasurementRange(
                    Double.longBitsToDouble(mrageArray[i*3]),
                    Double.longBitsToDouble(mrageArray[i*3+1]),
                    Double.longBitsToDouble(mrageArray[i*3+2]));
        }
        return res;
    }
    
}
