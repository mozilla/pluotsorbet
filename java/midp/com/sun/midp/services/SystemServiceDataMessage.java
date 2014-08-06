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

package com.sun.midp.services;

import java.io.DataInput;
import java.io.DataOutput;

/**
 * Message is what is passed between service and client. At low 
 * level message body is just array of bytes. It is also possible 
 * to write/extract a sequence of primitive types from the message. 
 * In other words, message body can be seen as data stream from which
 * you can read/write primitive types.
 */
public abstract class SystemServiceDataMessage extends SystemServiceMessage {
    /**
     * Gets message body as DataInput.
     *
     * @return DataInput interface for reading data from message
     */
    abstract public DataInput getDataInput();

    /**
     * Gets message body as DataOutput.
     *
     * @return DataOutput interface for writing data to message
     */
    abstract public DataOutput getDataOutput();
}
