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

import javax.microedition.sensor.*;

/**
 * Helpers to handle conditions, limits and operators
 */
public class ConditionHelpers {

    public static boolean checkOperator(String operator) {
        return (
                operator.equals(Condition.OP_EQUALS) ||
                operator.equals(Condition.OP_GREATER_THAN) ||
                operator.equals(Condition.OP_GREATER_THAN_OR_EQUALS) ||
                operator.equals(Condition.OP_LESS_THAN) ||
                operator.equals(Condition.OP_LESS_THAN_OR_EQUALS)
                );
    }
    
    public static boolean checkLowerOperator(String operator) {
        return (
                operator.equals(Condition.OP_GREATER_THAN) ||
                operator.equals(Condition.OP_GREATER_THAN_OR_EQUALS)
                );
    }

    public static boolean checkUpperOperator(String operator) {
        return (
                operator.equals(Condition.OP_LESS_THAN) ||
                operator.equals(Condition.OP_LESS_THAN_OR_EQUALS)
                );
    }
    
    public static boolean checkValue(String operator, double limit, double value) {
        if (operator.equals(Condition.OP_EQUALS)) {
            return (Double.doubleToLongBits(limit) ==
                Double.doubleToLongBits(value));
        }
        
        if (operator.equals(Condition.OP_GREATER_THAN)) {
            return (value > limit);
        }
        
        if (operator.equals(Condition.OP_GREATER_THAN_OR_EQUALS)) {
            return checkValue(Condition.OP_EQUALS, limit, value) ||
                    checkValue(Condition.OP_GREATER_THAN, limit, value);
        }
        
        if (operator.equals(Condition.OP_LESS_THAN)) {
            return (value < limit);
        }
        
        if (operator.equals(Condition.OP_LESS_THAN_OR_EQUALS)) {
            return checkValue(Condition.OP_EQUALS, limit, value) ||
                    checkValue(Condition.OP_LESS_THAN, limit, value);
        }
        
        /* We should not be here */
        return false;        
    }
    
    public static double resolve(double value, int scale) {
        double mult = 1.0;
        for (int i = 0, limit = Math.abs(scale); i < limit; i++) {
            mult *= 10.0;
        }
        return (scale > 0)? value * mult : value / mult;
    }
}
