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

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * This class implements the RecordEnumeration interface.
 */
class RecordEnumerationImpl implements RecordEnumeration, RecordListener { 

    /** The associated record store for this enumeration */
    private RecordStore recordStore;

    /** The record filter this enumeration should use, or null if none */
    private RecordFilter filter;

    /** The record comparator this enumeration should use, or null if none */
    private RecordComparator comparator;

    /** True if this should listen to <code>recordStore</code> for changes */
    private boolean keepEnumUpdated;  // false by default

    /** Current pos within the enumeration */
    private int index;  // NO_SUCH_RECORD by default

    /** Array of recordId's of records included in the enumeration */
    private int[] records;

    /**
     * A constant recordId indicating the splice point between the
     * last and first records in the enumeration. Returned by
     * <code>nextElement()</code> and <code>prevElement()</code>
     * when the next or prev element does not exist.
     */
    private static final int NO_SUCH_RECORD = -1;


    /**
     * Builds an enumeration to traverse a set of records in the
     * given record store in an optionally specified order.<p>
     *
     * The filter, if non-null, will be used to determine what
     * subset of the record store records will be used.<p>
     *
     * The comparator, if non-null, will be used to determine the
     * order in which the records are returned.<p>
     *
     * If both the filter and comparator are null, the enumeration
     * will traverse all records in the record store in an undefined
     * order. This is the most efficient way to traverse all of the
     * records in a record store.
     *
     * @param inp_recordStore the RecordStore to enumerate.
     * @param inp_filter if non-null, will be used to determine what
     *        subset of the record store records will be used.
     * @param inp_comparator if non-null, will be used to determine the
     *        order in which the records are returned.
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *        current with any changes in the records of the record store. 
     *        Use with caution as there are performance consequences.
     *
     * @see #rebuild
     */
    RecordEnumerationImpl(RecordStore inp_recordStore, 
                          RecordFilter inp_filter,
			  RecordComparator inp_comparator, 
			  boolean keepUpdated) {
	recordStore = inp_recordStore;
	filter = inp_filter;
	comparator = inp_comparator;
	keepEnumUpdated = keepUpdated;

	if (keepUpdated) {
	    inp_recordStore.addRecordListener(this);
	}

	rebuild();
    }


    /**
     * Returns the number of records available in this enumeration's
     * set. That is, the number of records that have matched the
     * filter criterion. Note that this forces the RecordEnumeration
     * to fully build the enumeration by applying the filter to all
     * records, which may take a non-trivial amount
     * of time if there are a lot of records in the record store.
     *
     * @return the number of records available in this enumeration's
     *         set. That is, the number of records that have matched 
     *         the filter criterion.
     */
    public synchronized int numRecords() {
	checkDestroyed();

	return records.length;
    }


    /**
     * Returns a copy of the <i>next</i> record in this enumeration,
     * where <i>next</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException no more records are available
     *
     * @return the next record in this enumeration.
     */
    public synchronized byte[] nextRecord() throws InvalidRecordIDException, 
	RecordStoreNotOpenException, RecordStoreException {
	checkDestroyed();
	return recordStore.getRecord(nextRecordId());
    }

    /**
     * Returns the recordId of the <i>next</i> record in this enumeration,
     * where <i>next</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. After calling
     * this method, the enumeration is advanced to the next available
     * record.
     *
     * @exception InvalidRecordIDException no more records are available.
     *
     * @return the recordId of the next record in this enumeration.
     */
    public synchronized int nextRecordId()
	throws InvalidRecordIDException {
	checkDestroyed();
	if (index == records.length - 1) {
	    throw new InvalidRecordIDException();
	}
	if (index == NO_SUCH_RECORD) {
	    index = 0;
	} else {
	    index++;
	}
	return records[index];
    }


    /**
     * Returns a copy of the <i>previous</i> record in this enumeration,
     * where <i>previous</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. The byte array
     * returned is a copy of the record. Any changes made to this array
     * will NOT be reflected in the record store. After calling
     * this method, the enumeration is advanced to the next (previous)
     * available record.
     *
     * @exception InvalidRecordIDException no more records are available.
     *
     * @return the previous record in this enumeration.
     */
    public synchronized byte[] previousRecord() throws 
        InvalidRecordIDException, RecordStoreNotOpenException, 
	RecordStoreException {

	checkDestroyed();
        return recordStore.getRecord(previousRecordId());
    }


    /**
     * Returns the recordId of the <i>previous</i> record in this enumeration,
     * where <i>previous</i> is defined by the comparator and/or filter
     * supplied in the constructor of this enumerator. After this method
     * is called, the enumeration is advanced to the next (previous)
     * available record.
     *
     * @exception InvalidRecordIDException when no more records are available.
     *
     * @return the recordId of the previous record in this enumeration.
     */
    public synchronized int previousRecordId()
	throws InvalidRecordIDException {
	checkDestroyed();
	if (index == 0 || records.length == 0) {
	    throw new InvalidRecordIDException();
	}
	if (index == NO_SUCH_RECORD) {
	    index = records.length - 1;
	} else {
	    index--;
	}
	return records[index];
    }


    /**
     * Returns true if more elements exist in the <i>next</i> direction.
     *
     * @return true if more elements exist in the <i>next</i> direction.
     */
    public boolean hasNextElement() {
	checkDestroyed();
	if (records.length == 0 || !recordStore.isOpen()) {
	    return false;
	}
	return (index != records.length - 1);
    }


    /**
     * Returns true if more elements exist in the <i>previous</i> direction.
     *
     * @return true if more elements exist in the <i>previous</i> direction.
     */
    public boolean hasPreviousElement() {
	checkDestroyed();
	if (records.length == 0 || !recordStore.isOpen()) {
	    return false;  // no records in the enumeration
	}
	return (index != 0);
    }


    /**
     * Returns the index point of the enumeration to the beginning.
     */
    public void reset() {
	checkDestroyed();
	index = NO_SUCH_RECORD;
    }


    /**
     * Request that the enumeration be updated to reflect the current
     * record set. Useful for when an application makes a number of 
     * changes to the record store, and then wants an existing 
     * RecordEnumeration to enumerate the new changes.
     *
     * @see #keepUpdated
     */
    public void rebuild() {
	checkDestroyed();

	int[] tmp = recordStore.getRecordIDs();
	reFilterSort(tmp);
    }


    /**
     * Used to set whether the enumeration should be registered
     * as a listener of the record store, and rebuild its internal
     * index with every record addition/deletion in the record store.
     * Note that this should be used carefully due to the potential 
     * performance cost associated with maintaining the 
     * enumeration with every change.
     *
     * @param keepUpdated if true, the enumerator will keep its enumeration
     *        current with any changes in the records of the record store.
     *        Use with caution as there are possible performance consequences.
     *        If false, the enumeration will not be kept current and may 
     *        return recordIds for records that have been deleted or miss 
     *        records that are added later. It may also return records out
     *        of order that have been modified after the enumeration was 
     *        built.
     *
     * @see #rebuild
     */
    public void keepUpdated(boolean keepUpdated) {
	checkDestroyed();
	if (keepUpdated != keepEnumUpdated) {
	    keepEnumUpdated = keepUpdated;
	    if (keepUpdated) {
	        recordStore.addRecordListener(this);
		rebuild();
	    } else {
	        recordStore.removeRecordListener(this);
	    }
	}
    }


    /**
     * Returns true if the enumeration keeps its enumeration
     * current with any changes in the records.
     *
     * @return true if the enumeration keeps its enumeration
     *         current with any changes in the records
     */
    public boolean isKeptUpdated() {
	checkDestroyed();
	return keepEnumUpdated;
    }


    /**
     * From the RecordListener interface.  This method is called if
     * a record is added to <code>recordStore</code>.
     *
     * @param inp_recordStore the record store to which a record was added
     * @param recordId the record ID of the new record
     */
    public synchronized void recordAdded(RecordStore inp_recordStore, 
					 int recordId) {
	checkDestroyed();
        filterAdd(recordId);
    }

    
    /**
     * From the RecordListener interface.  This method is called if
     * a record in <code>recordStore</code> is modified.
     *
     * @param inp_recordStore the record store in which a record was modified
     * @param recordId the record ID of the modified record.
     */
    public synchronized void recordChanged(RecordStore inp_recordStore, 
					       int recordId) {
	checkDestroyed();
	
	int recIndex = findIndexOfRecord(recordId);
	if (recIndex >= 0) {
	    removeRecordAtIndex(recIndex);
	} // else record not previously in the enumeration
	
        filterAdd(recordId);
    }
    

    /**
     * From the RecordListener interface.  This method is called when a
     * record in <code>recordStore</code> is deleted.
     *
     * @param inp_recordStore the record store from which a record was deleted
     * @param recordId the record id of the deleted record
     */
    public synchronized void recordDeleted(RecordStore inp_recordStore, 
					   int recordId) {
	checkDestroyed();

	/*
	 * Remove the deleted element from the records array.
	 * No resorting is required.
	 */

	int recIndex = findIndexOfRecord(recordId);
	
	if (recIndex < 0) {
	    return;  // not in the enumeration
	}

	// remove this record from the enumeration
	removeRecordAtIndex(recIndex);    
    }


    /**
     * Implements RecordEnumeration.destroy() interface.  Called
     * to signal that this enumeration will no longer be used, and that
     * its resources may be collected.
     */ 
    public synchronized void destroy() {
	checkDestroyed();
	if (keepEnumUpdated) {
	    recordStore.removeRecordListener(this);
	}

	filter = null;
	comparator = null;
	records = null;
	recordStore = null; // a signal that this is destroyed!
    }


    /**
     * Helper method that checks if this enumeration can be used.
     * If this enumeration has been destroyed, an exception is thrown.
     *
     * @exception IllegalStateException if RecordEnumeration has been 
     *            destroyed.
     */
    private void checkDestroyed() {
	if (recordStore == null) {
	    throw new IllegalStateException();
	}
    }

    /**
     * Used to add a record to an already filtered and sorted
     * <code>records</code> array.  More efficient than 
     * <code>reFilterSort</code> because it relies on 
     * <code>records</code> being in sorted order.
     *
     * First ensures that record <code>recordId</code> 
     * meets this enumeration's filter criteria.
     * If it does it is added to records as array element
     * 0.  If a comparator is defined for this enumeration,
     * the helper method <code>sortAdd</code> is called to 
     * properly position <code>recordId</code> within the ordered
     * <code>records</code> array.
     *
     * Should be called from within a 
     * synchronized (recordStore.rsLock) block.
     *
     * @param recordId the record to add to this enumeration
     */
    private void filterAdd(int recordId) {
	int insertPoint = -1;
	if (filter != null) {
	    try {
		if (!filter.matches(recordStore.getRecord(recordId))) {
		    if (Logging.REPORT_LEVEL <= Logging.WARNING) {
			Logging.report(Logging.WARNING, LogChannels.LC_RMS,
				       "Unexpected case in filterAdd: " + 
				       "recordId filtered out");
		    }
		    return;  // recordId filtered out
		}
	    } catch (RecordStoreException rse) {
		return;  // recordId does not exist
	    }
	}

	// the new record has been accepted by the filter
	int[] newrecs = new int[records.length + 1];
	newrecs[0] = recordId;  // insert new record at front of list
	System.arraycopy(records, 0, newrecs, 1, records.length);
	records = newrecs;
	if (comparator != null) {  // move the new record into place
	    try {
		insertPoint = sortInsert();
	    } catch (RecordStoreException rse) {
		// NOTE: - should never be here
		// throw a RSE?  destroy record enumeration?
		if (Logging.TRACE_ENABLED) {
		    Logging.trace(rse, "Unexpected case in filterAdd: " + 
				  "caught RSE");
		}
	    }
	}
	// keep index up to date as well
	if (index != NO_SUCH_RECORD && insertPoint <= index) {
	    index++;
	}
    }


    /**
     * Helper method called by <code>filterAdd</code>.
     * Moves the possibly unsorted element zero in the
     * <code>records</code> array to its sorted position
     * within the array.
     *
     * @return index of inserted element.
     * @exception RecordStoreException if an error occurs
     *            in the comparator function.
     */
    private int sortInsert() throws RecordStoreException {
	// bubble sort the first record in records into place
	int tmp;
	int i;
	int j;
	for (i = 0, j = 1; i < records.length - 1; i++, j++) {
	    if (comparator.compare(recordStore.getRecord(records[i]),
				   recordStore.getRecord(records[j])) ==
		RecordComparator.FOLLOWS) {
		// if i follows j swap them in records
		tmp = records[i];
		records[i] = records[j];
		records[j] = tmp;
	    } else {
		break;  // done sorting if compare returns EQUALS or PRECEDES 
	    }
	}
	return i; // final index of new record in records
    }
    
    
    /**
     * Find the index in records of record <code>recordId</code> 
     * and return it.  
     *
     * @param recordId the record index to find
     * @return the index of the record, or -1.
     */
    private int findIndexOfRecord(int recordId) {
	int idx;
	int recIndex = -1;
	for (idx = records.length - 1; idx >= 0; idx--) {
	    if (records[idx] == recordId) {
		recIndex = idx;
		break;
	    }
	} 
	return recIndex;
    }


    /**
     * Internal helper method which 
     * removes the array element at index <code>recIndex</code>
     * from the internal <code>records</code> array.  
     *
     * <code>recIndex</code> should be non negative.
     *
     * @param recIndex the array element to remove.
     */
    private void removeRecordAtIndex(int recIndex) {
	int[] tmp = new int[records.length - 1];
	System.arraycopy(records, 0, tmp, 0, recIndex);
	System.arraycopy(records, recIndex + 1, tmp, 
			 recIndex, (records.length - recIndex) - 1);
	records = tmp;
	
	/* 
	 * If a record prior to current index was deleted
	 * update index so nothing is skipped
	 */
	if (index != NO_SUCH_RECORD && recIndex <= index) {
	    index --;
	} else if (index == records.length) {
	    // last element in records removed
	    index --;
	}
    }


    /**
     * Internal helper method for filtering and sorting records if
     * necessary. Called from rebuild().
     *
     * Should be called from within a synchronized(recordStore.rsLock) block
     *
     * @param filtered array of record stores to filter and sort.
     */ 
    private void reFilterSort(int[] filtered) {
	int filteredIndex = 0;
	if (filter == null) {
	    /*
	     * If this enumeration doesn't have any filters, the
	     * recordId's returned by getRecordIDs should be 
	     * used as they are.
	     */
	    records = filtered;
	} else {
	    /*
	     * If a filter has been specified, filter the recordStore
	     * records to determine the subset to be used for this
	     * enumeration.
	     */
	    for (int i = 0; i < filtered.length; i++) {
		// if this record matches the filter keep it
		try {
		    if (filter.matches(recordStore.getRecord(filtered[i]))) {
			// need revisit : if element overlap is allowed
			if (filteredIndex != i) {
			    filtered[filteredIndex++] = filtered[i];
			} else {
			    filteredIndex++;
			}
		    }
		} catch (RecordStoreException rse) {
		    // if a record can't be found it doesn't match
		}
	    }
	    
	    records = new int[filteredIndex];
	    System.arraycopy(filtered, 0, records, 0, filteredIndex);
	}
	/*
	 * If a comparator has been specified, sort the remaining
	 * records by comparing records against each other using
	 * the comparator the application provides.
	 */
	if (comparator != null) {
	    try {
		QuickSort(records, 0, records.length - 1, comparator);
	    }
	    catch (RecordStoreException rse) {
		// NOTE: - should never be here
		// throw a RSE?  destroy record enumeration?

		if (Logging.TRACE_ENABLED) {
		    Logging.trace(rse, "Unexpected case in reFilterSort:" + 
				  " caught RSE");
		}
	    }
	}
	reset(); // reset the current index of this enumeration
    }
    
    
    /**
     * Quicksort helper function for sorting the records.
     *
     * @param a the array of recordId's to sort using comparator.
     * @param lowIndex the low bound of the range to sort.
     * @param highIndex the hight bound of the range to sort.
     * @param inp_comparator the RecordComparator to use to compare records.
     */
    private void QuickSort(int a[], int lowIndex, int highIndex, 
			   RecordComparator inp_comparator)
	throws RecordStoreException {

	/*
	 * A different sorting algorithm may be preferred, because a
	 * large recursive quicksort can consume lots of
	 * stack. Quicksort is very fast for most random sequences
	 * however...
	 */
	int left = lowIndex;	// the "left" index
	int right = highIndex;	// the "right" index

	/*
	 * First partition the data into two regions, where every
	 * element on the left side of the partition is less than
	 * every element on the right side of the element.
	 */
	if (highIndex > lowIndex) {
	    /* 
	     * Arbitrarily choose the initial pivot point to be the
	     * middle of the array.
	     */
	    int ind = (lowIndex + highIndex) / 2;
	    int pivotIndex = a[ind];
	    byte[] pivotData = recordStore.getRecord(pivotIndex);
	    
	    // loop through the array until the indices cross
	    while (left <= right) {
		/* 
		 * Starting on the left, scan right until the
		 * first element greater than or equal to the
		 * pivot element is found.
		 */
		while ((left < highIndex) && 
		       (inp_comparator.compare(recordStore.getRecord(a[left]),
					   pivotData) ==
			RecordComparator.PRECEDES)) {
		    left++;
		}
		/*
		 * Starting on the right, scan left until the
		 * first element that is less than or equal to the
		 * pivot element is found.
		 */
		while ((right > lowIndex) && 
		      (inp_comparator.compare(recordStore.getRecord(a[right]),
					  pivotData) ==
		       RecordComparator.FOLLOWS)) {
		    right--;
		}

		// if the indexes haven't crossed, swap the elements
		if (left <= right) {
		    int tmp = a[left];
		    a[left] = a[right];
		    a[right] = tmp;
		    left++;
		    right--;
		}
	    }
	    
	    // Sort the left side of the partition
	    if (lowIndex < right) {
		QuickSort(a, lowIndex, right, inp_comparator);
	    }
	    // Sort the right side of the partition
	    if (left < highIndex) {
		QuickSort(a, left, highIndex, inp_comparator);
	    }
	}
    }
}
