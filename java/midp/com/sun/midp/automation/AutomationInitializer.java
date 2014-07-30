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

package com.sun.midp.automation;

import com.sun.midp.main.*;
import com.sun.midp.events.*;

/**
 * Initializes Automation API
 */
public final class AutomationInitializer {
    /**
     * Private constructor to prevent creating class instances
     */
    private AutomationInitializer() {
    }
    
    
    /**
     * Initializes Automation API.
     *
     * @param theEventQueue Automation API's isolate event queue
     * @param theMidletControllerEventProducer MIDletControllerEventProducer
     */
    public static void init(
            EventQueue theEventQueue, 
            MIDletControllerEventProducer theMidletControllerEventProducer) {
    }

    /**
     * Gets event queue.
     *
     * @return Automation API's isolate event queue
     * @throws IllegalStateException if Automation API isn't initialized
     *         or not permitted to use
     */
    static EventQueue getEventQueue() 
        throws IllegalStateException {
            
        return null;
    }
 
    /**
     * Gets MIDletControllerEventProducer.
     *
     * @return MIDletControllerEventProducer instance
     * @throws IllegalStateException if Automation API isn't initialized
     *         or not permitted to use
     */
    static MIDletControllerEventProducer getMIDletControllerEventProducer() 
        throws IllegalStateException {

        return null;
    }

    /**
     * Guarantees that Automation API is initialized and
     * permitted to use.
     * 
     * @throws IllegalStateException if Automation API isn't initialized
     *         or not permitted to use    
     */
    static void guaranteeAutomationInitialized() 
        throws IllegalStateException {
        
        throw new IllegalStateException("Automation API is not included");
    }
}
