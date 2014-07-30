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

import com.sun.midp.events.EventQueue;

import com.sun.midp.lcdui.DisplayContainer;
import com.sun.midp.lcdui.DisplayDeviceContainer;
import com.sun.midp.lcdui.DisplayAccess;
import com.sun.midp.lcdui.DisplayEventHandler;
import com.sun.midp.lcdui.DisplayEventProducer;
import com.sun.midp.lcdui.ForegroundController;
import com.sun.midp.lcdui.RepaintEventProducer;
import com.sun.midp.lcdui.ItemEventConsumer;

/**
 * This class has dual functiopnality:
 *
 * First, it implements DisplayEventHandler I/F and thus provides access
 * to display objects (creation, preemption, set/get IDs and other properties).
 *
 * Second, it implements ItemEventConsumer I/F and thus processes
 * LCDUI events that due to different reasons can't be associated with
 * Display instance specific DisplayEventConsumer objects,
 * but need to be processed by isolate-wide handler.
 * TBD: These are subjects for futher investigation to move them
 * to DisplayEventConsumer.
 *
 * In addition, it implements a number of package private methods that work
 * with Display and are called locally by display/DisplayAccessor.
 * TBD: These are subjects for further investination to move them closer
 * to end users: Display & displayAccessor classes.
 *
 */
class DisplayEventHandlerImpl implements DisplayEventHandler,
        ItemEventConsumer {

    /** Cached reference to Active Displays Container. */
    private DisplayContainer displayContainer;

    /** Cached reference to the ForegroundController. */
    private ForegroundController foregroundController;

    /** The preempting display. */
    private DisplayAccess preemptingDisplay;

    /** If request to end preemption was called */
    private boolean preemptionDoneCalled = false;

    /** Package private constructor restrict creation to LCDUI package. */
    DisplayEventHandlerImpl() {
    }

    /**
     * Initialize Display Event Handler.
     * DisplayEventHandler I/F method.
     *
     * @param theDisplayEventProducer producer for display events
     * @param theForegroundController controls which display has the foreground
     * @param theRepaintEventProducer producer for repaint events events
     * @param theDisplayContainer container for display objects
     * @param theDisplayDeviceContainer container for display device objects
     */
    public void initDisplayEventHandler(
        DisplayEventProducer theDisplayEventProducer,
        ForegroundController theForegroundController,
        RepaintEventProducer theRepaintEventProducer,
        DisplayContainer theDisplayContainer,
	DisplayDeviceContainer theDisplayDeviceContainer) {

        foregroundController = theForegroundController;

        displayContainer = theDisplayContainer;

        /*
         * TBD: not a good idea to call static initializer
         * from non-static method ...
         * Maybe to create a separate method:
         * DisplayEventHandler.initDisplayClass(token,...)
         * for these purposes and call it from Suite Loader's main() ?
         * displayEventHandlerImpl I/F miplementor will call
         * Display.initClass() from itsinitDisplayClass() method ?
         */
        Display.initClass(
            theForegroundController,
            theDisplayEventProducer,
            theRepaintEventProducer,
            theDisplayContainer,
	    theDisplayDeviceContainer);
    }

    /**
     * Sets the trusted state of the display event handler.
     * DisplayEventHandler I/F method.
     *
     * @param drawTrustedIcon true, to draw the trusted icon in the upper
     *                status bar for every display of this suite
     */
    public void setTrustedState(boolean drawTrustedIcon) {
        Display.setTrustedState(drawTrustedIcon);
    }

    /**
     * Preempt the current displayable with
     * the given displayable until donePreempting is called.
     * To avoid dead locking the event thread his method
     * MUST NOT be called in the event thread.
     * DisplayEventHandler I/F method.
     *
     * @param d displayable to show the user
     * @param waitForDisplay if true this method will wait if the
     *        screen is being preempted by another thread, however
     *        if this is called in the event dispatch thread this
     *        method will return null regardless of the value
     *        of <code>waitForDisplay</code>
     *
     * @return an preempt token object to pass to donePreempting done if
     * prempt will happen, else null
     *
     * @exception InterruptedException if another thread interrupts the
     *   calling thread while this method is waiting to preempt the
     *   display.
     */
    public Object preemptDisplay(Displayable d, boolean waitForDisplay)
            throws InterruptedException {
        Display tempDisplay;
        String title;

        if (d == null) {
            throw new NullPointerException(
                "The displayable can't be null");
        }

        title = d.getTitle();
        if (title == null) {
            throw new NullPointerException(
                "The title of the displayable can't be null");
        }

        if (EventQueue.isDispatchThread()) {
            // Developer programming error
            throw new RuntimeException(
                "Blocking call performed in the event thread");
        }

        /**
         * This sync protects preempt related local fields:
         * preemptingDisplay and destroyPreemptingDisplay.
         */
        synchronized (this) {
            if (preemptingDisplay != null) {

                if (!waitForDisplay) {
                    return null;
                }

                this.wait();
            }

            // This class will own the display.
            tempDisplay =
                new Display("com.sun.midp.lcdui.DisplayEventHandlerImpl");

            foregroundController.startPreempting(tempDisplay.displayId);

            tempDisplay.setCurrent(d);

            preemptingDisplay = tempDisplay.accessor;

            return preemptingDisplay;
        }
    }

    /**
     * Display the displayable that was being displayed before
     * preemptDisplay was called.
     * DisplayEventHandler I/F method.
     *
     * @param preemptToken the token returned from preemptDisplay
     */
    public void donePreempting(Object preemptToken) {
        /**
         * This sync protects preempt related local fields:
         * preemptingDisplay and destroyPreemptingDisplay.
         */
        synchronized (this) {
            if (preemptingDisplay != null &&
                (preemptToken == preemptingDisplay || preemptToken == null)) {

                preemptionDoneCalled = true;

                foregroundController.stopPreempting(
                    preemptingDisplay.getDisplayId());

            }
        }
    }

    /**
     * Called by Display to notify DisplayEventHandler that
     * Display has been sent to the background to finish
     * preempt process if any.
     *
     * @param displayId id of Display
     */
    public void onDisplayBackgroundProcessed(int displayId) {

        synchronized (this) {
            if (preemptionDoneCalled && preemptingDisplay != null &&
                preemptingDisplay.getDisplayId() == displayId) {

                displayContainer.removeDisplaysByOwner(
                    preemptingDisplay.getOwner());
                preemptingDisplay = null;

                preemptionDoneCalled = false;
    
                // A midlet may be waiting to preempt
                this.notify();
            }
        }
    }

    /**
     * Called by event delivery to process an Item state change.
     * ItemEventConsumer I/F method.
     *
     * @param item the Item which has changed state
     */
    public void handleItemStateChangeEvent(Item item) {
        if (item.owner != null) {
            item.owner.uCallItemStateChanged(item);
        }
    }

    /**
     * Called by event delivery to refresh a CustomItem's size information.
     * ItemEventConsumer I/F method.
     *
     * @param ci the custom item whose size information has to be changed
     */
    public void handleItemSizeRefreshEvent(CustomItem ci) {
        ci.customItemLF.uCallSizeRefresh();
    }


/*
 * private methods
 */
    static {
        // Instantiate link with MMAPI video player for repaint hooks
        new MMHelperImpl();
    }
}
