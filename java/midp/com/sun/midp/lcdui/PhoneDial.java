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

package com.sun.midp.lcdui;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * PhoneDial allows for UI java code to access the phones native 
 * features. 
 */
public class PhoneDial {

    /**
     * The call methods gets a phone call made.
     * @param phoneNumber the phoneNumber to be called in string format
     * @return true if the service is available
     */
    public static boolean call(String phoneNumber) {
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_CORE,
			   "phoneNumber is call "+phoneNumber);
	}
	// native function would need to handle the actual call
	return true;
    }
    
}
