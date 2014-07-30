/*
 *   
 *
 * Portions Copyright  2000-2009 Sun Microsystems, Inc. All Rights
 * Reserved.  Use is subject to license terms.
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
 *
 * Copyright 2000 Motorola, Inc. All Rights Reserved.
 * This notice does not imply publication.
 */

package javax.microedition.rms; 

/**
 * An interface defining a filter which examines a record to see if it
 * matches (based on an application-defined criteria). The
 * application implements the match() method to select records to
 * be returned by the RecordEnumeration. Returns true if the candidate
 * record is selected by the RecordFilter. This interface
 * is used in the record store for searching or subsetting records.
 * For example:
 * <pre>
 * RecordFilter f = new DateRecordFilter(); // class implements RecordFilter
 * if (f.matches(recordStore.getRecord(theRecordID)) == true)
 *   DoSomethingUseful(theRecordID);
 * </pre>
 *
 * @since MIDP 1.0
 */

public interface RecordFilter
{
    /**
     * Returns true if the candidate matches the implemented criterion.
     *
     * @param candidate the record to consider. Within this method,
     *          the application must treat this parameter as
     *          read-only.
     *
     * @return true if the candidate matches the implemented criterion
     */
    public abstract boolean matches(byte[] candidate);

}
