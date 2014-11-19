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

package com.sun.jsr082.bluetooth;

/*
 * Inquiry result event.
 */
public class InquiryResultEvent extends BluetoothEvent {

    /* An array of inquiry results. */
    private InquiryResult[] results;

    /*
     * Creates event using a single inquiry result record.
     *
     * @param result inquiry result record
     */
    public InquiryResultEvent(InquiryResult result) {
    	super.eventName = "InquiryResultEvent";
        results = new InquiryResult[1];
        results[0] = result;
    }

    /*
     * Creates event using an array of results.
     *
     * @param resultsArray an array of result records
     */
    public InquiryResultEvent(InquiryResult[] resultsArray) {
        results = resultsArray;
    }

    /*
     * Processes this event.
     */
    public void process() {
        BluetoothStack stack = BluetoothStack.getInstance();
        for (int i = 0; i < results.length; i++) {
            stack.onInquiryResult(results[i]);
        }
    }

}
