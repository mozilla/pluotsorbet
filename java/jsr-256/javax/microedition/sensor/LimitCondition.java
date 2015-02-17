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

public final class LimitCondition implements Condition {
    private double limit;
    private String operator;
    
    /** Creates a new instance of LimitCondition */
    public LimitCondition(double limit, java.lang.String operator) {
        if (operator == null) {
            throw new NullPointerException();
        }
        
        if (!ConditionHelpers.checkOperator(operator)) {
            throw new IllegalArgumentException();        
        }
        
        this.limit = limit;
        this.operator = operator;
    }
    
    public final double getLimit() {
        return limit;
    }
    
    public final String getOperator() {
        return operator;
    }
    
    public boolean isMet(double value) {
        return ConditionHelpers.checkValue(operator, limit, value);
    }
    
    public boolean isMet(java.lang.Object value) {
        /* Always false */
        return false;
    }
}
