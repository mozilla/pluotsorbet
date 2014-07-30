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
 * An interface representing a bidirectional record store Record
 * enumerator. The RecordEnumeration logically maintains a sequence of
 * the recordId's of the records in a record store. The enumerator
 * will iterate over all (or a subset, if an optional record filter
 * has been supplied) of the records in an order determined by an
 * optional record comparator.
 *
 * <p>By using an optional <code>RecordFilter</code>, a subset of the
 * records can be chosen that match the supplied filter. This can be
 * used for providing search capabilities.</p>
 *
 * <p>By using an optional <code>RecordComparator</code>, the
 * enumerator can index through the records in an order determined by
 * the comparator. This can be used for providing sorting
 * capabilities.</p>
 *
 * <p>If, while indexing through the enumeration, some records are
 * deleted from the record store, the recordId's returned by the
 * enumeration may no longer represent valid records. To avoid this
 * problem, the RecordEnumeration can optionally become a listener of
 * the RecordStore and react to record additions and deletions by
 * recreating its internal index. Use special care when using this
 * option however, in that every record addition, change and deletion
 * will cause the index to be rebuilt, which may have serious
 * performance impacts.</p>
 *
 * <p>If the RecordStore used by this RecordEnumeration is closed,
 * this RecordEnumeration becomes invalid and all subsequent
 * operations performed on it may give invalid results or throw a
 * RecordStoreNotOpenException, even if the same RecordStore is later
 * opened again. In addition, calls to <code>hasNextElement()</code>
 * and <code>hasPreviousElement()</code> will return false.</p>
 *
 * <p>The first call to <code>nextRecord()</code> returns the record
 * data from the first record in the sequence. Subsequent calls to
 * <code>nextRecord()</code> return the next consecutive record's
 * data. To return the record data from the previous consecutive from
 * any given point in the enumeration, call
 * <code>previousRecord()</code>. On the other hand, if after
 * creation, the first call is to <code>previousRecord()</code>, the
 * record data of the last element of the enumeration will be
 * returned. Each subsequent call to <code>previousRecord()</code>
 * will step backwards through the sequence until the beginning is
 * reached.</p>
 *
 * <p>Final note, to do record store searches, create a
 * RecordEnumeration with no RecordComparator, and an appropriate
 * RecordFilter with the desired search criterion.</p>
 *
 * @since MIDP 1.0
 */

public interface RecordEnumeration
{
    /**
     * Returns the number of records available in this enumeration's
     * set. That is, the number of records that have matched the
     * filter criterion. Note that this forces the RecordEnumeration
     * to fully build the enumeration by applying the filter to all
     * records, which may take a non-trivial amount
     * of time if there are a lot of records in the record store.
     *
     * @return the number of records available in this enumeration's
     *          set. That is, the number of records that have matched
     *          the filter criterion.
     */
    public int numRecords();

    /**
     * Returns a copy of the <em>next</em> record in this enumeration,
     * where <em>next</em> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException when no more records are
     *          available. Subsequent calls to this method will
     *          continue to throw this exception until
     *          <code>reset()</code> has been called to reset the
     *          enumeration.
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a general record store
     *          exception occurs
     *
     * @return the next record in this enumeration
     */
    public byte[] nextRecord()
	throws InvalidRecordIDException, RecordStoreNotOpenException,
	RecordStoreException;

    /**
     * Returns the recordId of the <em>next</em> record in this enumeration,
     * where <em>next</em> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException when no more records are
     *          available. Subsequent calls to this method will
     *          continue to throw this exception until
     *          <code>reset()</code> has been called to reset the
     *          enumeration.
     *
     * @return the recordId of the next record in this enumeration
     */
    public int nextRecordId()
	throws InvalidRecordIDException;

    /**
     * Returns a copy of the <em>previous</em> record in this enumeration,
     * where <em>previous</em> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next (previous)
     * available record.
     *
     * @exception InvalidRecordIDException when no more records are
     *          available. Subsequent calls to this method will
     *          continue to throw this exception until
     *          <code>reset()</code> has been called to reset the
     *          enumeration.
     * @exception RecordStoreNotOpenException if the record store is
     *          not open
     * @exception RecordStoreException if a general record store
     *          exception occurs.
     *
     * @return the previous record in this enumeration
     */
    public byte[] previousRecord()
	throws InvalidRecordIDException, RecordStoreNotOpenException,
	RecordStoreException;

    /**
     * Returns the recordId of the <em>previous</em> record in this
     * enumeration, where <em>previous</em> is defined by the
     * comparator and/or filter supplied in the constructor of this
     * enumerator. After calling this method, the enumeration is
     * advanced to the next (previous) available record.
     *
     * @exception InvalidRecordIDException when no more records are
     *          available. Subsequent calls to this method will
     *          continue to throw this exception until
     *          <code>reset()</code> has been called to reset the
     *          enumeration.
     *
     * @return the recordId of the previous record in this enumeration
     */
    public int previousRecordId()
	throws InvalidRecordIDException;

    /**
     * Returns true if more elements exist in the <em>next</em> direction.
     *
     * @return true if more elements exist in the <em>next</em>
     *         direction
     */
    public boolean hasNextElement();

    /**
     * Returns true if more elements exist in the <em>previous</em> direction.
     *
     * @return true if more elements exist in the <em>previous</em>
     *         direction
     */
    public boolean hasPreviousElement();

    /**
     * Returns the enumeration index to the same state as right
     * after the enumeration was created.
     */
    public void reset();

    /**
     * Request that the enumeration be updated to reflect the current
     * record set. Useful for when a MIDlet
     * makes a number of changes to the record store, and then wants an
     * existing RecordEnumeration to enumerate the new changes.
     *
     * @see #keepUpdated
     */
    public void rebuild();

    /**
     * Used to set whether the enumeration will be keep its internal
     * index up to date with the record store record
     * additions/deletions/changes. Note that this should
     * be used carefully due to the potential performance problems
     * associated with maintaining the enumeration with every change.
     *
     * @param keepUpdated if true, the enumerator will keep its
     *          enumeration current with any changes in the records of
     *          the record store. Use with caution as there are
     *          possible performance consequences. Calling
     *          <code>keepUpdated(true)</code> has the same effect as
     *          calling <code>RecordEnumeration.rebuild</code>: the
     *          enumeration will be updated to reflect the current
     *          record set.  If false the enumeration will not be kept
     *          current and may return recordIds for records that have
     *          been deleted or miss records that are added later. It
     *          may also return records out of order that have been
     *          modified after the enumeration was built. Note that
     *          any changes to records in the record store are
     *          accurately reflected when the record is later
     *          retrieved, either directly or through the
     *          enumeration. The thing that is risked by setting this
     *          parameter false is the filtering and sorting order of
     *          the enumeration when records are modified, added, or
     *          deleted.
     *
     * @see #rebuild
     */
    public void keepUpdated(boolean keepUpdated);

    /**
     * Returns true if the enumeration keeps its enumeration
     * current with any changes in the records.
     *
     * @return true if the enumeration keeps its enumeration
     *          current with any changes in the records
     */
    public boolean isKeptUpdated();

    /**
     * Frees internal resources used by this RecordEnumeration.
     * MIDlets should call this method when they are done using a
     * RecordEnumeration. If a MIDlet tries to use a RecordEnumeration
     * after this method has been called, it will throw a
     * <code>IllegalStateException</code>. Note that this method is
     * used for manually aiding in the minimization of immediate
     * resource requirements when this enumeration is no longer
     * needed.
     */
    public void destroy();

}
