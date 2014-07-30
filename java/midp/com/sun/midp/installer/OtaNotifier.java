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

package com.sun.midp.installer;

import java.io.IOException;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import com.sun.j2me.security.AccessController;

import com.sun.midp.configurator.Constants;

import com.sun.midp.io.Base64;

import com.sun.midp.io.j2me.http.Protocol;

import com.sun.midp.midlet.MIDletSuite;

import com.sun.midp.midletsuite.MIDletSuiteStorage;

import com.sun.midp.security.Permissions;
import com.sun.midp.security.SecurityToken;

/**
 * This class handles sending installation and deletion notifications as
 * specified by the OTA section of the MIDP 2.0 specification.
 * The delete notifications are only sent when an install notification is
 * sent. The installer will call this class to send the initial install
 * notification, if the notification fails and is a success notification,
 * the notification will be queued, so that the next time a MIDlet from the
 * suite is run, that suite's notification will be retried. The MIDlet
 * state handler will call this class to process install notification retries.
 */
public final class OtaNotifier {
    /** Retry delay. */
    static final int RETRY_DELAY = 3000; // 3 seconds

    /** MIDlet property for the install notify URL. */
    public static final String NOTIFY_PROP = "MIDlet-Install-Notify";

    /** Success message for the suite provider. */
    public static final String SUCCESS_MSG = "900 Success";

    /** Error message for the suite provider. */
    public static final String INSUFFICIENT_MEM_MSG =
        "901 Insufficient Memory";

    /** Error message for the suite provider. */
    public static final String USER_CANCELLED_MSG = "902 User Cancelled";

    /** Error message for the suite provider. */
    public static final String JAR_SIZE_MISMATCH_MSG = "904 JAR size mismatch";

    /** Error message for the suite provider. */
    public static final String ATTRIBUTE_MISMATCH_MSG =
        "905 Attribute Mismatch";

    /** Error message for the suite provider. */
    public static final String INVALID_JAD_MSG = "906 Invalid Descriptor";

    /** Error message for the suite provider. */
    public static final String INVALID_JAR_MSG = "907 Invalid JAR";

    /** Error message for the suite provider. */
    public static final String INCOMPATIBLE_MSG =
        "908 Incompatible Configuration or Profile";

    /** Error message for authentication failure. */
    public static final String AUTHENTICATION_FAILURE_MSG =
        "909 Application authentication failure";

    /** Error message for authorization failure. */
    public static final String AUTHORIZATION_FAILURE_MSG =
        "910 Application authorization failure";

    /** Error message for push registration failure. */
    public static final String PUSH_REG_FAILURE_MSG =
        "911 Push registration failure";

    /** Error message for push registration failure. */
    public static final String DELETE_NOTIFICATION_MSG =
        "912 Deletion Notification";

    /** Message to send when a content handler install fails. */
    public static final String CONTENT_HANDLER_CONFLICT =
        "938 Content handler conflicts with other handlers";

    /** Message to send when a content handler install fails. */
    public static final String INVALID_CONTENT_HANDLER =
        "939 Content handler install failed";

    /**
     * Posts a status message back to the provider's URL in JAD.
     * This method will also retry ALL pending delete notifications.
     * <p>
     * Method requires com.sun.midp.ams permission.
     *
     * @param message status message to post
     * @param suite MIDlet suite object
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     */
    public static void postInstallMsgBackToProvider(String message,
            MIDletSuite suite, String proxyUsername, String proxyPassword) {
        String url;

        AccessController.checkPermission(Permissions.AMS_PERMISSION_NAME);

        // Now, send out install notifications
        url = suite.getProperty(NOTIFY_PROP);
        try {
            postMsgBackToProvider(message, url, proxyUsername, proxyPassword);
        } catch (Throwable t) {
            if (message == SUCCESS_MSG) {
                // Only queue successful install notifications for retry
                addInstallNotification(suite.getID(), url);
            }
        }
    }

    /**
     * Retry the pending install status message for this suite only.
     * This method will also retry ALL pending delete notifications,
     * if the install notification was retried.
     *
     * @param token security token of the caller
     * @param suite MIDlet suite object
     */
    public static void retryInstallNotification(SecurityToken token,
            MIDletSuite suite) {
        /*
         * Delay any processing so that startup time is not effected.
         */
        new Thread(new InstallRetryHandler(token, suite)).start();
    }

    /**
     * Retry the pending install status message for this suite only.
     * This method will also retry ALL pending delete notifications,
     * if the install notification was retried.
     *
     * @param token security token of the caller
     * @param suite MIDlet suite object
     */
    static void retryInstallNotificationInternal(SecurityToken token,
            MIDletSuite suite) {
        PendingNotification notification = new PendingNotification();

        token.checkIfPermissionAllowed(Permissions.AMS);

        // Now, send out install notifications
        if (suite.getProperty(NOTIFY_PROP) == null) {
            return;
        }

        if (!getInstallNotificationForRetry(suite.getID(), notification)) {
            return;
        }

        try {
            Protocol httpConnection = new Protocol();

            httpConnection.openPrim(token, notification.url);
            postMsgBackToProvider(SUCCESS_MSG, httpConnection, null, null);
            removeInstallNotification(notification.suiteId);
        } catch (Throwable t) {
            if (notification.retries >=
                Constants.MAX_INSTALL_DELETE_NOTIFICATION_RETRIES) {
                removeInstallNotification(notification.suiteId);
            }
        }

        try {
            // Send out delete notifications that have been queued, first
            postQueuedDeleteMsgsBackToProvider(null, null);
        } catch (Throwable t) {
            // ignore
        }
    }

    /**
     * Posts all queued delete notification messages
     *
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     */
    public static void postQueuedDeleteMsgsBackToProvider(
            String proxyUsername, String proxyPassword) {
        PendingNotification[] deleteNotifyList;

        deleteNotifyList = getDeleteNotifications();

        for (int i = 0; i < deleteNotifyList.length; i++) {
            try {
                postMsgBackToProvider(DELETE_NOTIFICATION_MSG,
                                      deleteNotifyList[i].url,
                                      proxyUsername, proxyPassword);
                removeDeleteNotification(deleteNotifyList[i].suiteId);
            } catch (Throwable t) {
                if (deleteNotifyList[i].retries >=
                        Constants.MAX_INSTALL_DELETE_NOTIFICATION_RETRIES) {
                    removeDeleteNotification(deleteNotifyList[i].suiteId);
                }
            }
        }
    }

    /**
     * Posts a status message back to the provider's URL in JAD.
     *
     * @param message status message to post
     * @param url target http url for the status message
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     *
     * @exception IOException is thrown if any error prevents the
     *            notification from being successful
     */
    private static void postMsgBackToProvider(String message, String url,
            String proxyUsername, String proxyPassword) throws IOException {
        HttpConnection transaction;

        if (url == null) {
            return;
        }

        transaction = (HttpConnection)Connector.open(url, Connector.WRITE);

        postMsgBackToProvider(message, transaction, proxyUsername,
                              proxyPassword);
    }

    /**
     * Posts a status message back to the provider's URL in JAD.
     *
     * @param message status message to post
     * @param transaction http connection to use for posting the status message
     * @param proxyUsername if not null, it will be put in the post
     * @param proxyPassword if not null, it will be put in the post
     *
     * @exception IOException is thrown if any error prevents the
     *            notification from being successful
     */
    private static void postMsgBackToProvider(String message,
            HttpConnection transaction,
            String proxyUsername, String proxyPassword) throws IOException {
        OutputStream out;

        try {
            transaction.setRequestMethod(HttpConnection.POST);

            if (proxyUsername != null && proxyPassword != null) {
                transaction.setRequestProperty("Proxy-Authorization",
                    formatAuthCredentials(proxyUsername, proxyPassword));
            }

            out = transaction.openOutputStream();

            try {
                int responseCode;

                out.write(message.getBytes());
                responseCode = transaction.getResponseCode();
                if (responseCode != HttpConnection.HTTP_OK) {
                    throw new IOException("Failed to notify " +
                        transaction.getURL() +
                        " HTTP response code: " + responseCode);
                }
            } finally {
                out.close();
            }
        } finally {
            transaction.close();
        }
    }

    /**
     * Formats the username and password for HTTP basic authentication
     * according RFC 2617.
     *
     * @param username for HTTP authentication
     * @param password for HTTP authentication
     *
     * @return properly formated basic authentication credential
     */
    private static String formatAuthCredentials(String username,
                                                String password) {
        byte[] data = new byte[username.length() + password.length() + 1];
        int j = 0;

        for (int i = 0; i < username.length(); i++, j++) {
            data[j] = (byte)username.charAt(i);
        }

        data[j] = (byte)':';
        j++;

        for (int i = 0; i < password.length(); i++, j++) {
            data[j] = (byte)password.charAt(i);
        }

        return "Basic " + Base64.encode(data, 0, data.length);
    }

    /**
     * Retrieves the queued delete notification list. Each element
     * in the array is a URL to send a delete notification to.
     *
     * @return the delete notification list
     */
    private static synchronized PendingNotification[]
            getDeleteNotifications() {
        PendingNotification[] array =
            new PendingNotification[getNumberOfDeleteNotifications()];

        if (array.length > 0) {
            for (int i = 0; i < array.length; i++) {
                array[i] = new PendingNotification();
            }

            fillDeleteNotificationListForRetry(array);
        }

        return array;
    }

    /**
     * Retrieves the number of URLs queued delete in the notification list.
     *
     * @return the number of URLs in the delete notification list
     */
    private static native int getNumberOfDeleteNotifications();

    /**
     * Retrieves the queued delete notification list from storage and
     * increments the retry count of every member of the list.
     *
     * @param list empty delete notification list to fill
     */
    private static native void fillDeleteNotificationListForRetry(
        PendingNotification[] list);

    /**
     * Removes the element from the delete notification list.
     *
     * @param suiteId suite ID of the notification
     */
    private static native void removeDeleteNotification(int suiteId);

    /**
     * Adds an element to the install notification list.
     *
     * @param suiteId suite the notification belongs to
     * @param url url to send the notification to
     */
    private static native void addInstallNotification(int suiteId,
                                                      String url);

    /**
     * Retrieves the URL of suite's install notification from storage and
     * increments the retry count of element.
     *
     * @param suiteId suite ID of the notification
     * @param dest where to put the notification
     *
     * @return true if the notification is found
     */
    private static native boolean getInstallNotificationForRetry(
        int suiteId, PendingNotification dest);

    /**
     * Removes the element from the install notification list.
     *
     * @param suiteId suite ID of the notification
     */
    private static native void removeInstallNotification(int suiteId);
}

/** Executes install reties in the background. */
final class InstallRetryHandler implements Runnable {
    /** Security token of the caller. */
    private SecurityToken token;

    /** MIDlet suite to retry. */
    private MIDletSuite suite;

    /**
     * Construct a InstallRetryHandler.
     *
     * @param theToken security token of the caller
     * @param theSuite MIDlet suite object
     */
    InstallRetryHandler(SecurityToken theToken, MIDletSuite theSuite) {
        token = theToken;
        suite = theSuite;
    }

    /** Retries after a short delay. */
    public void run() {
        try {
            Thread.sleep(OtaNotifier.RETRY_DELAY);
        } catch (InterruptedException ie) {
            // ignore
        }

        OtaNotifier.retryInstallNotificationInternal(token, suite);
    }
}
/** Pending install or delete notification */
final class PendingNotification {
    /** Number of times the record has been retried. */
    int retries;

    /** Suite this notification is for. */
    int suiteId;

    /** URL to post the notification to. */
    String url;

    /**
     * Returns a debug string.
     *
     * @return value of each field in a string
     */
    public String toString() {
        return "PendingNotification(suite ID = " + suiteId + ", retries = " +
            retries + ", URL = " + url + ")";
    }
}
