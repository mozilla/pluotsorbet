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

/**
 * Look &amp; feel implementation for <code>Gauge</code> using platform widget.
 */
class GaugeLFImpl extends ItemLFImpl implements GaugeLF {

    /**
     * Creates <code>GaugeLF</code> for the passed in <code>Gauge</code>.
     *
     * @param gauge the <code>Gauge</code> object associated with this 
     * look&amp;feel.
     */
    GaugeLFImpl(Gauge gauge) {
        
        super(gauge);

        this.gauge = gauge;
    }


    // *****************************************************
    //  Public methods
    // *****************************************************

    /**
     * Notifies L&amp;F of a value change in the corresponding 
     * <code>Gauge</code>.
     *
     * @param oldValue the old value set in the <code>Gauge</code>
     * @param newValue the new value set in the <code>Gauge</code>
     */
    public void lSetValue(int oldValue, int newValue) {
	// little optimization to avoid calling into native
	// in cases where unnecessary
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    // Only update native resource if it exists.
	    setValue0(nativeId, newValue, gauge.maxValue);
	    
	    lRequestInvalidate(true, true);
	}
    }

    /**
     * Notifies L&amp;F of a maximum value change in the corresponding 
     * <code>Gauge</code>.
     *
     * @param oldMaxValue the old maximum value set in the <code>Gauge</code>
     * @param newMaxValue the new maximum value set in the <code>Gauge</code>
     */
    public void lSetMaxValue(int oldMaxValue, int newMaxValue) {
	if (nativeId != DisplayableLFImpl.INVALID_NATIVE_ID) {
	    // Only update native resource if it exists.
	    setValue0(nativeId, gauge.value, newMaxValue);
	    lRequestInvalidate(true, true);
	}
    }

    /**
     * Gets the current value.
     *
     * @return the current value
     */
    public int lGetValue() {
	return gauge.value;
    }

    /**
     * Notification of a change in its peer state.
     * Update Java peer with user input.
     *  
     * @param hint the new value of an interactive gauge
     *
     * @return always <code>true</code> so to notify 
     *         <code>ItemStateListener</code>
     */
    boolean uCallPeerStateChanged(int hint) {
	synchronized (Display.LCDUILock) {
	    // Update Java peer with the new value
	    gauge.value = hint;
	}
	// Indicate to Form to notify ItemStateListener
	return true;
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

        return ((gauge.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
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

        return ((gauge.layout & Item.LAYOUT_2) != Item.LAYOUT_2);
    }

    /**
     * Create native resource for current <code>Gauge</code>.
     * Override function in <code>ItemLFImpl</code>.
     *
     * @param ownerId Owner screen's native resource id
     */
    void createNativeResource(int ownerId) {
	nativeId = createNativeResource0(ownerId,
					 gauge.label, gauge.layout,
					 gauge.interactive, 
					 gauge.maxValue, gauge.value);
    }

    /**
     * KNI function that creates native resource for current 
     * <code>Gauge</code>.
     *
     * @param ownerId Owner screen's native resource id 
     *                (<code>MidpDisplayable *</code>)
     * @param label label string of this <code>Item</code>
     * @param layout layout directive associated with this <code>Item</code> 
     * @param interactive indicates whether gauge is interactive or not
     * @param maxValue the maximum value for this gauge
     * @param initialValue the current value of the gauge
     *
     * @return native resource id (<code>MidpItem *</code>) of this 
     *         <code>Gauge</code>
     */
    private native int createNativeResource0(int ownerId,
					     String label, 
					     int layout,
					     boolean interactive,
					     int maxValue,
					     int initialValue);
    
    /**
     * KNI function that sets the current and maximum values on the native 
     * resource corresponding to the current <code>Gauge</code>.
     *
     * @param nativeId native resource id for this <code>Item</code>
     * @param newValue new value of the current <code>Gauge</code>
     * @param newMaxValue new maximum value of the current <code>Gauge</code>
     */
    private native void setValue0(int nativeId, 
				  int newValue,
				  int newMaxValue);
    
    /** 
     * <code>Gauge</code> instance associated with this view.
     */
    private Gauge gauge;     
    
} // GaugeView
