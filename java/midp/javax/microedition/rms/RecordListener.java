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
* A listener interface for receiving Record Changed/Added/Deleted
* events from a record store.
* @see RecordStore#addRecordListener
*
* @since MIDP 1.0
*/

public interface RecordListener
{
    /**
    * Called when a record has been added to a record store.
    *
    * @param recordStore the RecordStore in which the record is stored
    * @param recordId the recordId of the record that has been added
    */
    public abstract void recordAdded(RecordStore recordStore, int recordId);

    /**
    * Called after a record in a record store has been changed. If the
    * implementation of this method retrieves the record, it will
    * receive the changed version.
    *
    * @param recordStore the RecordStore in which the record is stored
    * @param recordId the recordId of the record that has been changed
    */
    public abstract void recordChanged(RecordStore recordStore, int recordId);

    /**
    * Called after a record has been deleted from a record store. If the
    * implementation of this method tries to retrieve the record
    * from the record store, an InvalidRecordIDException will be thrown.
    *
    * @param recordStore the RecordStore in which the record was stored
    * @param recordId the recordId of the record that has been deleted
    */
    public abstract void recordDeleted(RecordStore recordStore, int recordId);

}
