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

import java.util.*;
import javax.microedition.sensor.*;

public class ChannelUrl {
    private static final String CHANNEL = "channel=";
    private static final char CH_SEP = '&';
    
    
    /** Creates a new instance of ChannelUrl */
    public ChannelUrl() {
    }
    
    public static String createUrl(ChannelImpl ch) {
        StringBuffer b = new StringBuffer(CHANNEL);
        b.append(ch.getName());   // Need revisit
        
        /* If there are conditions then add them to the URL */
        Condition[] allConds = ch.getAllConditions();
        if (allConds != null && allConds.length > 0) {
            /* Find all unique conditions 
             ******************************/
            Vector ucv = new Vector(allConds.length);

            for (int i = 0; i < allConds.length; i++) {
                if (allConds[i] instanceof ObjectCondition) {
                    continue;
                }

                boolean found = false;
                int count = ucv.size();
                for (int j = 0; j < count; j++) {
                    if (allConds[i].equals(ucv.elementAt(j))) {
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    ucv.addElement(allConds[i]);
                }
            }
            
            int scale = ch.getScale();
            
            int count = ucv.size();
            for (int i = 0; i < count; i++) {
                b.append(CH_SEP);
                
                Condition cond = (Condition)ucv.elementAt(i);
                
                if (cond instanceof LimitCondition) {
                    LimitCondition lc = (LimitCondition)cond;
                    b.append("limit=");
                    b.append(Double.toString(ConditionHelpers.resolve(lc.getLimit(), scale)));
                    b.append(CH_SEP);
                    b.append("op=");
                    b.append(lc.getOperator());
                } else {
                    RangeCondition rc = (RangeCondition)cond;
                    b.append("lowerLimit=");
                    b.append(Double.toString(ConditionHelpers.resolve(rc.getLowerLimit(), scale)));
                    b.append("&lowerOp=");
                    b.append(rc.getLowerOp());
                    b.append("&upperLimit=");
                    b.append(Double.toString(ConditionHelpers.resolve(rc.getUpperLimit(), scale)));
                    b.append("&upperOp=");
                    b.append(rc.getUpperOp());
                }
            }
        }
        
        return b.toString();
    }    
}
