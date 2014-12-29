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

package javax.microedition.lcdui;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sun.midp.log.Logging;
import com.sun.midp.log.LogChannels;

/**
 * Look and feel implementation of <code>DateField</code> based on 
 * platform widget. 
 */
class DateFieldLFImpl extends ItemLFImpl implements DateFieldLF {

    /**
     * Creates <code>DateFieldLF</code> for the passed in 
     * <code>DateField</code> object.
     *
     * @param dateField the <code>DateField</code> object associated with 
                        this view
    */
    DateFieldLFImpl(DateField dateField) {
        super(dateField);

        df = dateField;
    }
        
    /**
     * Notifies L&amp;F of a date change in the corresponding 
     * <code>DateField</code>.
     *
     * @param date the new <code>Date</code> set in the 
     *             <code>DateField</code>
     */
    public void lSetDate(java.util.Date date) {
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			   "(mdSetDate: id="+nativeId+")");
	}

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    if (date != null) {	    
		setDate0(nativeId, date.getTime());
	    } else {
		setDate0(nativeId, 0l);
	    }
	}
    }

    /**
     * Setting date/time of this widget.
     *
     * @param nativeId native resource id of this <code>Item</code>
     * @param date in seconds
     */
    private native void setDate0(int nativeId, long date);

    /**
     * Notifies L&amp;F of a new input mode set in the corresponding 
     * <code>DateField</code>.
     *
     * @param mode the new input mode set in the <code>DateField</code>.
     */
    public void lSetInputMode(int mode) {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			   "(mdSetInputMode: id="+nativeId+")");
	}

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    setInputMode0(nativeId, mode);
	    lRequestInvalidate(true, true);
	}
    }

    /**
     * Set mode - time and or date.
     *
     * @param nativeId native resource id of this <code>Item</code>
     * @param mode the new input mode set in the <code>DateField</code>.
     */
    private native void setInputMode0(int nativeId, int mode);

    /**
     * Overrides <code>ItemLFImpl</code> to sync with
     * native resource before hiding it
     * Called by the system to hide this <code>Item</code>'s
     * native resource
     */
    void lHideNativeResource() {

	if (!dateSynced) {
	    // sync java peer with native data before hiding
	    df.setDateImpl(lGetDate());
	    dateSynced = true;
	}

	super.lHideNativeResource();
    }

    /**
     * Called by event delivery to notify an <code>ItemLF</code> in current 
     * <code>FormLF</code> of a change in its peer state.
     *
     * @param hint <code>1</code> if date, 
     *             <code>2</code> if hour, 
     *             <code>3</code> if minute changed
     *
     * @return always <code>true</code> to notify 
     *         <code>ItemStateListener</code>
     */
    boolean uCallPeerStateChanged(int hint) { 
	// Any hint means user has changed some aspect of the date in native
	// since native peer changed, java peer is no longer up to date.
	dateSynced = false;
	
	// if the datefield is not initialized yet, initialize now.
	if (!df.initialized) {
	    synchronized (Display.LCDUILock) {
		lGetDate();
	    }
	}	
	// Set flag so native resource will be queried for latest value
	// Tell Form that ItemStateListener should be notified
	return true;
    }

    /**
     * Gets the date currently set on the date field widget.
     * This method is called by <code>Date</code> only if <code>Date</code> 
     * is initialized.
     *
     * @return the date this widget is currently set to
     */
    public Date lGetDate() {

	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			   "(mdGetDate: id="+nativeId+")");
	}

	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {

	    long ld = getDate0(nativeId);
			
	    if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
		Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			       "(mdGetDate: returned long=="+ld+")");
	    }
	    if (ld > 0) {
		Date nd = new Date(ld);
		df.setDateImpl(nd);
		dateSynced = true;
		return nd;
	    }    
	} else if (dateSynced && df.currentDate != null) {
	    return df.currentDate.getTime();
	}

	return null;
    }

    // *****************************************************
    //  Package private methods
    // *****************************************************

    /**
     * Determine if this <code>Item</code> should have a newline after it.
     *
     * @return <code>true</code> if it should have a newline after
     */
    boolean equateNLA() {
        if (super.equateNLA()) {
            return true;
        }

        return ((df.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }
               
    /**
     * Determine if this <code>Item</code> should have a newline before it.
     *
     * @return <code>true</code> if it should have a newline before
     */
    boolean equateNLB() {
        if (super.equateNLB()) {
            return true;
        }

        return ((df.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Gets the current value set on the native datefield.
     *
     * @param nativeId native resource id of this <code>Item</code>
     *
     * @return time in millis since 1970
     */
    private native long getDate0(int nativeId);

    /**
     * Create native resource for current <code>DateField</code>.
     * Override function in <code>ItemLFImpl</code>.
     *
     * @param ownerId Owner screen's native resource id
     */
    void createNativeResource(int ownerId) {
	nativeId = createNativeResource0(ownerId,
					 df.label,
					 df.layout,
					 (df.getDate() != null ?
					  df.currentDate.getTime().getTime():
					  0),
					 df.mode,
					 df.currentDate.getTimeZone().getID());
	
	if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
	    Logging.report(Logging.INFORMATION, LogChannels.LC_HIGHUI,
			   "(createNativeResource: id="+nativeId+")");
	}
    }

    /**
     * KNI function that create native resource for current 
     * <code>DateField</code>.
     *
     * @param ownerId Owner screen's native resource id 
     *               (<code>MidpDisplayable *</code>)
     * @param label label sting of this <code>Item</code>
     * @param layout layout directive associated with 
     *               this <code>DateField</code>.
     * @param datetime date in millis since Jan 1'st 1970
     * @param displayMode date and or time mode
     * @param timeZone label of timezone
     *
     * @return native resource id (<code>MidpItem *</code>) of this 
     *         <code>Item</code>
     */
    private native int createNativeResource0(int ownerId,
					     String label,
					     int layout,
					     long datetime,
					     int displayMode,
					     String timeZone);

    /**
     * <code>DateField</code> associated with this view.
     */
    private DateField df;

    /**
     * Flag indicating if date value stored in java is up to date with native.
     * When visible, the date is stored in the native widget.
     * When hiding (and destroying) the native resource, we need to cache the 
     * value in the java peer for later use.
     * dateSynced flag is a dirty bit used to check if a sync is needed
     * when hiding.
     */
    private boolean dateSynced = true;
}
