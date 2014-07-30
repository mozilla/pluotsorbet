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
 * An interface defining a comparator which compares two records (in an
 * implementation-defined manner) to see if they match or what their
 * relative sort order is. The application implements this interface
 * to compare two candidate records. The return value must indicate
 * the ordering of the two records. The compare method is called by
 * RecordEnumeration to sort and return records in an application
 * specified order. For example:
 * <pre>
 * RecordComparator c = new AddressRecordComparator();
 * if (c.compare(recordStore.getRecord(rec1), recordStore.getRecord(rec2))
 *	 == RecordComparator.PRECEDES)
 * return rec1;
 * </pre>
 *
 * @since MIDP 1.0
 */

public interface RecordComparator
{
    /**
     * EQUIVALENT means that in terms of search or sort order, the
     * two records are the same. This does not necessarily mean that
     * the two records are identical.
     * <P>The value of EQUIVALENT is 0.</P>
     */
    public static final int EQUIVALENT = 0;

    /**
     * FOLLOWS means that the left (first parameter) record <em>follows</em>
     * the right (second parameter) record in terms of search or
     * sort order.
     * <P>The value of FOLLOWS is 1.</P>
     */
    public static final int FOLLOWS = 1;

    /**
     * PRECEDES means that the left (first parameter) record <em>precedes</em>
     * the right (second parameter) record in terms of search or
     * sort order.
     * <P>The value of PRECEDES is -1.</P>
     */
    public static final int PRECEDES = -1;

    /**
     * Returns <code>RecordComparator.PRECEDES</code> if rec1
     * precedes rec2 in sort order, or <code>RecordComparator.FOLLOWS</code>
     * if rec1 follows rec2 in sort order, or
     * <code>RecordComparator.EQUIVALENT</code> if rec1 and rec2
     * are equivalent in terms of sort order.
     *
     * @param rec1 the first record to use for comparison. Within this
     *          method, the application must treat this parameter as
     *          read-only.
     * @param rec2 the second record to use for comparison. Within
     *          this method, the application must treat this parameter
     *          as read-only.
     * @return <code>RecordComparator.PRECEDES</code> if rec1 precedes
     *          rec2 in sort order, or
     *          <code>RecordComparator.FOLLOWS</code> if rec1 follows
     *          rec2 in sort order, or
     *          <code>RecordComparator.EQUIVALENT</code> if rec1 and
     *          rec2 are equivalent in terms of sort order
     */
    public abstract int compare(byte[] rec1, byte[] rec2);

}
