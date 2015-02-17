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

import com.sun.javame.sensor.*;

public final class RangeCondition implements Condition {
    private double lowerLimit;
    private double upperLimit;
    private String lowerOp;
    private String upperOp;
    
    /** Creates a new instance of RangeCondition */
    public RangeCondition(double lowerLimit, String lowerOp, double upperLimit, String upperOp) {
        if (!ConditionHelpers.checkLowerOperator(lowerOp) || !ConditionHelpers.checkUpperOperator(upperOp)) {
            throw new IllegalArgumentException();            
        }
        
        if (lowerLimit > upperLimit) {
            throw new IllegalArgumentException();            
        }
        
        if (
                (lowerLimit == upperLimit) && 
                 !(
                        (lowerOp.equals(Condition.OP_GREATER_THAN_OR_EQUALS)) &&
                        (upperOp.equals(Condition.OP_LESS_THAN_OR_EQUALS))
                  )
           ) {
            throw new IllegalArgumentException();            
        }
        
        this.lowerLimit = lowerLimit;
        this.upperLimit = upperLimit;
        this.lowerOp = lowerOp;
        this.upperOp = upperOp;
    }
    
    public final double getLowerLimit() {
        return lowerLimit;
    }
    
    public final String getLowerOp() {
        return lowerOp;
    }
    
    public final double getUpperLimit() {
        return upperLimit;
    }
    
    public final String getUpperOp() {
        return upperOp;
    }
    
    public boolean isMet(double doubleValue) {
        return (ConditionHelpers.checkValue(lowerOp, lowerLimit, doubleValue) &&
                ConditionHelpers.checkValue(upperOp, upperLimit, doubleValue));
    }
    
    public boolean isMet(java.lang.Object value) {
        /* Always false */
        return false;
    }
}
