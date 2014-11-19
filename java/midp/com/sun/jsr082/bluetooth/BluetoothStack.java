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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.RemoteDevice;

/*
 * Represents native Bluetooth stack provided by the system.
 * Provides services such as device and service discovery.
 */
public abstract class BluetoothStack {

    private static class BTInstanceHolder
    {
        // Note to porting engineer: please replace the class name with
        // the one you intend to use on the target platform.
        private final static BluetoothStack INSTANCE = new JavacallBluetoothStack();
    }

    /* Instance handle of the native porting layer class. */
    private int nativeInstance = 0;

    /* Listener where discovery events are reported to. */
    private DiscoveryListener discListener = null;

    /* Contains remote name request results. */
    private Hashtable nameResults = new Hashtable();

    /* Contains authentication request results. */
    private Hashtable authenticateResults = new Hashtable();

    /* Contains set encryption request results. */
    private Hashtable encryptResults = new Hashtable();

    /* Timeout value in milliseconds for friendly name retrieval. */
    private final long ASK_FRIENDLY_NAME_TIMEOUT = 0;

    /* Timeout value in milliseconds for authentication. */
    private final long AUTHENTICATE_TIMEOUT = 0;

    /* Timeout value in milliseconds for setting encryption. */
    private final long ENCRYPT_TIMEOUT = 0;

    /* Keeps the count of pending requests. */
    int pollRequests = 0;

    /*
     * Contains results of ongoing inquiry to avoid reporting the same
     * inquiry result twice.
     */
    Vector inquiryHistory = new Vector();

    /*
     * Class constructor.
     */
    protected BluetoothStack() {
        if (!initialize()) {
            throw new RuntimeException(
                    "Failed to initialize Bluetooth");
        }
    }

    /*
     * Allocates native resources.
     */
    private native boolean initialize();

    /*
     * Releases native resources.
     */
    protected native void finalize();

    /*
     * Returns a BluetoothStack object.
     *
     * @return an instance of BluetoothStack subclass
     */
    public synchronized static BluetoothStack getInstance()
    {
        return BTInstanceHolder.INSTANCE;
    }

    /*
     * Returns a BluetoothStack object and guarantees that Bluetooth
     * radio is on.
     * @return an instance of BluetoothStack subclass
     * @throws BluetoothStateException if BluetoothStack is off and
     *        cannot be turned on.
     */
    public synchronized static BluetoothStack getEnabledInstance()
                throws BluetoothStateException {
        BluetoothStack instance = getInstance();
        if (!instance.isEnabled() && !instance.enable()) {
            throw new BluetoothStateException("Failed turning Bluetooth on");
        }
        // intent here is launching EmulationPolling and SDPServer
        // in emulation mode
//????        com.sun.jsr082.bluetooth.SDDB.getInstance();
        return instance;
    }

    /*
     * Checks if the Bluetooth radio is enabled.
     *
     * @return true if Bluetooth is enabled, false otherwise
     */
    public native boolean isEnabled();

    /*
     * Enables Bluetooth radio.
     *
     * @return true if Bluetooth is enabled, false otherwise
     */
    public native boolean enable();

    /*
     * Returns Bluetooth address of the local device.
     *
     * @return Bluetooth address of the local device, or null if
     *         the address could not be retrieved
     */
    public native String getLocalAddress();

    /*
     * Returns user-friendly name for the local device.
     *
     * @return User-friendly name for the local device, or null if
     *         the name could not be retrieved
     */
    public native String getLocalName();

    /*
     * Returns class of device including service classes.
     *
     * @return class of device value, or -1 if the information could not
     *         be retrieved
     */
    public native int getDeviceClass();

    /*
     * Sets major service class bits of the device.
     *
     * @param classes an integer whose binary representation indicates the major
     *        service class bits that should be set
     * @return true if the operation succeeded, false otherwise
     */
    public native boolean setServiceClasses(int classes);

    /*
     * Retrieves the inquiry access code that the local Bluetooth device is
     * scanning for during inquiry scans.
     *
     * @return inquiry access code, or -1 if the information could not
     *         be retrieved
     */
    public native int getAccessCode();

    /*
     * Sets the inquiry access code that the local Bluetooth device is
     * scanning for during inquiry scans.
     *
     * @param accessCode inquiry access code to be set (valid values are in the
     *        range 0x9e8b00 to 0x9e8b3f), or 0 to take the device out of
     *        discoverable mode
     * @return true if the operation succeeded, false otherwise
     */
    public native boolean setAccessCode(int accessCode);

    /*
     * Places the device into inquiry mode.
     *
     * @param accessCode the type of inquiry
     * @param listener the event listener that will receive discovery events
     * @return true if the inquiry was started, false otherwise
     */
    public boolean startInquiry(int accessCode, DiscoveryListener listener) {
        if (discListener != null || listener == null) {
            return false;
        }
        discListener = listener;
        if (startInquiry(accessCode)) {
            inquiryHistory.removeAllElements();
            startPolling();
            return true;
        }
        return false;
    }

    /*
     * Removes the device from inquiry mode.
     *
     * @param listener the listener that is receiving inquiry events
     * @return true if the inquiry was canceled, false otherwise
     */
    public boolean cancelInquiry(DiscoveryListener listener) {
        if (discListener != listener) {
            return false;
        }
        if (cancelInquiry()) {
            stopPolling();
            discListener = null;
            return true;
        }
        return false;
    }

    /*
     * Retrieves friendly name from a remote device synchronously.
     *
     * @param addr remote device address
     * @return friendly name of the remote device, or <code>null</code>
     *         if the name could not be retrieved
     */
    public String askFriendlyNameSync(String addr) {
        if (!askFriendlyName(addr)) {
            return null;
        }
        nameResults.remove(addr);
        startPolling();
        return (String)waitResult(nameResults, addr,
                ASK_FRIENDLY_NAME_TIMEOUT);
    }

    /*
     * Performs remote device authentication synchronously.
     *
     * @param addr remote device address
     * @return <code>true</code> if authentication was successful,
     *         <code>false</code> otherwise
     */
    public boolean authenticateSync(String addr) {
        if (!authenticate(addr)) {
            return false;
        }
        int handle = getHandle(addr);
        authenticateResults.remove(new Integer(handle));
        startPolling();
        Boolean result = (Boolean)waitResult(authenticateResults,
                new Integer(handle), AUTHENTICATE_TIMEOUT);
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * Sets encryption mode synchronously.
     *
     * @param addr remote device address
     * @param enable <code>true</code> if the encryption needs to be enabled,
     *               <code>false</code> otherwise
     * @return <code>true</code> if authentication was successful,
     *         <code>false</code> otherwise
     */
    public boolean encryptSync(String addr, boolean enable) {
        if (!encrypt(addr, enable)) {
            return false;
        }
        int handle = getHandle(addr);
        encryptResults.remove(new Integer(handle));
        startPolling();
        Boolean result = (Boolean)waitResult(encryptResults,
                new Integer(handle), ENCRYPT_TIMEOUT);
        if (result == null) {
            return false;
        }
        return result.booleanValue();
    }

    /*
     * Starts a supplementary polling thread.
     */
    public synchronized void startPolling() {
        pollRequests++;
        PollingThread.resume();
    }

    /*
     * Cancels event polling for one request. Polling thread will continue to
     * run unless there are no other pending requests.
     */
    public synchronized void stopPolling() {
        pollRequests--;
        if (pollRequests > 0) {
            return;
        }
        PollingThread.suspend();
    }

    /*
     * Checks for Bluetooth events and processes them.
     */
    public void pollEvents() {
        while (checkEvents(null)) {
            BluetoothEvent event = retrieveEvent(null);
            if (event != null) {
                event.dispatch();
            }
        }
    }

    /*
     * Retrieves Bluetooth event.
     *
     * @param handle event handle data
     * @return a Bluetooth event object
     */
    protected abstract BluetoothEvent retrieveEvent(Object handle);

    /*
     * Called when an inquiry request is completed.
     *
     * @param success indicates whether inquiry completed successfully
     */
    void onInquiryComplete(boolean success) {
        if (discListener == null) {
            return;
        }
        stopPolling();
        discListener = null;
        inquiryHistory.removeAllElements();
        int type = success ? DiscoveryListener.INQUIRY_COMPLETED :
                DiscoveryListener.INQUIRY_ERROR;
        DiscoveryAgentImpl.getInstance().inquiryCompleted(type);
    }

    /*
     * Called when an inquiry result is obtained.
     *
     * @param result inquiry result object
     */
    void onInquiryResult(InquiryResult result) {
        if (discListener == null) {
            return;
        }
        String addr = result.getAddress();
        Enumeration e = inquiryHistory.elements();
        while (e.hasMoreElements()) {
            InquiryResult oldResult = (InquiryResult)e.nextElement();
            if (oldResult.getAddress().equals(addr)) {
                // inquiry result is already in our possession
                return;
            }
        }
        inquiryHistory.addElement(result);
        RemoteDevice dev
            = DiscoveryAgentImpl.getInstance().getRemoteDevice(addr);
        DiscoveryAgentImpl.getInstance().addCachedDevice(addr);
        discListener.deviceDiscovered(dev, result.getDeviceClass());
    }

    /*
     * Called when a name retrieval request is completed.
     *
     * @param addr Bluetooth address of a remote device
     * @param name friendly name of the device
     */
    void onNameRetrieve(String addr, String name) {
        stopPolling();
        putResult(nameResults, addr, name);
    }

    /*
     * Called when an authentication request is completed.
     *
     * @param handle connection handle for an ACL connection
     * @param result indicates whether the operation was successful
     */
    void onAuthenticationComplete(int handle, boolean result) {
        stopPolling();
        putResult(authenticateResults, new Integer(handle),
            new Boolean(result));
    }

    /*
     * Called when a set encryption request is completed.
     *
     * @param handle connection handle for an ACL connection
     * @param result indicates whether the operation was successful
     */
    void onEncryptionChange(int handle, boolean result) {
        stopPolling();
        putResult(encryptResults, new Integer(handle), new Boolean(result));
    }

    /*
     * Puts result value into hastable and notifies threads waiting for the
     * result to appear.
     *
     * @param hashtable <code>Hashtable</code> object where the result will be
     *                 stored
     * @param key key identifying the result
     * @param value value of the result
     */
    private void putResult(Hashtable hashtable, Object key, Object value) {
        synchronized (hashtable) {
            hashtable.put(key, value);
            hashtable.notify();
        }
    }

    /*
     * Waits for the specified key to appear in the given hastable. If the key
     * does not appear within the timeout specified, <code>null</code> value is
     * returned.
     *
     * @param hashtable <code>Hashtable</code> object where the key is expected
     * @param key the key expected to appear in the hastable
     * @param timeout timeout value in milliseconds
     * @return <code>Object</code> corresponding to the given key
     */
    private Object waitResult(Hashtable hashtable, Object key, long timeout) {
        synchronized (hashtable) {
            if (timeout == 0) {
                // infinite timeout
                while (true) {
                    if (hashtable.containsKey(key)) {
                        return hashtable.remove(key);
                    }
                    try {
                        // wait for a new key-value pair to appear in hashtable
                        hashtable.wait();
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            }
            // endTime indicates time up to which the method is allowed to run
            long endTime = System.currentTimeMillis() + timeout;
            while (true) {
                if (hashtable.containsKey(key)) {
                    return hashtable.remove(key);
                }
                // update timeout value
                timeout = endTime - System.currentTimeMillis();
                if (timeout <= 0) {
                    return null;
                }
                try {
                    // wait for a new key-value pair to appear in hashtable
                    hashtable.wait(timeout);
                } catch (InterruptedException e) {
                    return null;
                }
            }
        }
    }

    /*
     * Retrieves default ACL connection handle for the specified remote device.
     *
     * @param addr the Bluetooth address of the remote device
     * @return ACL connection handle value
     */
    private native int getHandle(String addr);

    /*
     * Passes device discovery request to the native porting layer.
     *
     * @param accessCode the type of inquiry
     * @return <code>true</code> if the operation was accepted,
     *         <code>false</code> otherwise
     */
    private native boolean startInquiry(int accessCode);

    /*
     * Passes cancellation of device discovery request to the native porting
     * layer.
     *
     * @return <code>true</code> if the operation was accepted,
     *         <code>false</code> otherwise
     */
    private native boolean cancelInquiry();

    /*
     * Passes remote device's friendly name acquisition request to the native
     * porting layer.
     *
     * @param addr Bluetooth address of the remote device
     * @return <code>true</code> if the operation was accepted,
     *         <code>false</code> otherwise
     */
    private native boolean askFriendlyName(String addr);

    /*
     * Passes remote device authentication request to the native porting layer.
     *
     * @param addr Bluetooth address of the remote device
     * @return <code>true</code> if the operation was accepted,
     *         <code>false</code> otherwise
     */
    private native boolean authenticate(String addr);

    /*
     * Passes connection encryption change request to the native porting layer.
     *
     * @param addr Bluetooth address of the remote device
     * @param enable <code>true</code> if the encryption needs to be enabled,
     *               <code>false</code> otherwise
     * @return <code>true</code> if the operation was accepted,
     *         <code>false</code> otherwise
     */
    private native boolean encrypt(String addr, boolean enable);

    /*
     * Checks if Bluetooth events are available on the native porting layer.
     *
     * @param handle interger two words length array for storing event data in;
     * the first word indicates event's minor id.
     * @return <code>true</code> if there are pending events,
     *         <code>false</code> otherwise
     */
    protected native boolean checkEvents(Object handle);

    /*
     * Reads binary event data from the native porting layer. This data can
     * be interpreted differently by different subclasses of BluetoothStack.
     *
     * @param eventData event object to be filled with data
     * @return number of bytes read
     */
    protected native boolean readData(Object eventData);
}

/*
 * Supplementary thread which periodically polls Bluetooth stack for events.
 */
class PollingThread extends Thread {

    /* Instance of this class. */
    private static PollingThread instance = new PollingThread();

    /* Flag indicating if this thread should be suspended. */
    private static boolean suspended = true;

    /* Polling interval in milliseconds. */
    private final int POLL_INTERVAL = 1000;

    /*
     * Class constructor.
     */
    public PollingThread() {
    }

    /*
     * Suspends this thread.
     */
    public static void suspend() {
        synchronized (instance) {
            suspended = true;
        }
    }

    /*
     * Resumes this thread.
     */
    public static void resume() {
        try {
            instance.start();
        } catch (IllegalThreadStateException e) {
        }
        synchronized (instance) {
            suspended = false;
            instance.notify();
        }
    }

    /*
     * Execution body.
     */
    public void run() {
        BluetoothStack stack = BluetoothStack.getInstance();
        while (true) {
            try {
                synchronized (this) {
                    if (suspended) {
                        wait();
                    }
                }
                stack.pollEvents();
                synchronized (this) {
                    wait(POLL_INTERVAL);
                }
            } catch (InterruptedException e) {
                break;
            }
        }
    }

}

/*
 * Supplementary thread which dispatches Bluetooth events to user notifiers.
 */
class Dispatcher extends Thread {

    /* Instance of this class. */
    private static Dispatcher instance = new Dispatcher();

    /* Vector containing Bluetooth events awaiting to be dispatched. */
    private static Vector events = new Vector();

    /*
     * Class constructor.
     */
    public Dispatcher() {
    }

    /*
     * Puts the event in the event queue.
     * It also starts event dispatcher thread if it's not running yet.
     *
     * @param event the event to be enqueued
     */
    public static void enqueue(BluetoothEvent event) {
        try {
            instance.start();
        } catch (IllegalThreadStateException e) {
        }
        synchronized (events) {
            events.addElement(event);
            events.notify();
        }
    }

    /*
     * Execution body.
     */
    public void run() {
		while (true) {
        	BluetoothEvent event = null;
			synchronized (events) {
				if (!events.isEmpty()) {
                  event = (BluetoothEvent)events.firstElement();
                  events.removeElementAt(0);
				} else {
					try {
						events.wait();
					} catch (InterruptedException e) {
						break;
					}
				}
			}
			if (event != null) {
				event.process();
			}
		}
    }

}
