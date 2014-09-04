/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network;

import com.nokia.example.rlinks.SessionManager;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Vector;
import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.pki.CertificateException;

/**
 * A client for asynchronous HTTP operations.
 */
public class HttpClient implements Runnable {

    private static boolean prompted = false;
    private static boolean allowed = false;

    private static final int MAX_NETWORK_THREADS = 2;
    private static final Thread[] NETWORK_THREADS = new Thread[MAX_NETWORK_THREADS];
    private static final Vector QUEUE = new Vector();
    private static final CookieJar COOKIE_JAR = SessionManager.getInstance();

    // HTTP User-Agent string sent by this client.
    private static final String USER_AGENT_STRING = "Reddit for Series 40 Touch & Type";

    /**
     * An interface for persisting cookies received by this HTTP client.
     */
    public interface CookieJar {
        public void put(String cookieContent);
        public String getCookieHeader();
    }

    private HttpClient() {}

    /**
     * Add a network operation in the client's queue.
     *
     * @param operation HttpOperation to enqueue
     */
    public static void enqueue(final HttpOperation operation) {
        synchronized (QUEUE) {
            QUEUE.addElement(operation);
            QUEUE.notifyAll();
        }
        synchronized (NETWORK_THREADS) {
            for (int i = 0; i < NETWORK_THREADS.length; i++) {
                if (NETWORK_THREADS[i] == null) {
                    NETWORK_THREADS[i] = new Thread(new HttpClient(), "NetworkThread" + i);
                    NETWORK_THREADS[i].start();
                    return;
                }
            }
        }
    }

    /**
     * Abort a previously enqueued operation.
     *
     * @param operation HttpOperation to remove from the queue
     */
    public static void abort(final HttpOperation operation) {
        synchronized (QUEUE) {
            QUEUE.removeElement(operation);
        }
    }

    /**
     * Sends a synchronous HTTP request.
     *
     * @param operation HttpOperation with request details
     * @throws NetworkError
     */
    private byte[] sendRequest(HttpOperation operation)
        throws NetworkException {
        HttpConnection hcon = null;
        DataInputStream dis = null;
        DataOutputStream dos = null;
        ByteArrayOutputStream response = new ByteArrayOutputStream();

        try {
            // Only set mode to READ_WRITE if there's data to be written
            final int mode = operation.getRequestBody() == null ? Connector.READ : Connector.READ_WRITE;

            hcon = (HttpConnection) Connector.open(operation.getUrl(), mode);
            if (hcon == null) {
                throw new NetworkException("No network access");
            }            

            hcon.setRequestMethod(operation.getRequestMethod());
            hcon.setRequestProperty("User-Agent", USER_AGENT_STRING);

            // Set cookies in request
            if (operation.isCookiesEnabled()) {
                writeCookies(hcon);
            }

            // Send request body
            if (operation.getRequestBody() != null) {
                // Content-Type is must to pass parameters in POST Request
                hcon.setRequestProperty("Content-Type", operation.getRequestContentType());

                // Obtain DataOutputStream for sending the request string
                dos = hcon.openDataOutputStream();
                byte[] requestBody = operation.getRequestBody().getBytes();

                // Send request string to server
                for (int i = 0, len = requestBody.length; i < len; i++) {
                    dos.writeByte(requestBody[i]);
                }
            }

            // Read cookies from response
            if (operation.isCookiesEnabled()) {
                readCookies(hcon);
            }

            // Obtain DataInputStream for receiving server response
            dis = new DataInputStream(hcon.openInputStream());

            // Retrieve the response from server
            byte[] buffer = new byte[1024];
            int read = 0;
            while ((read = dis.read(buffer)) > -1 && !operation.isAborted()) {
                response.write(buffer, 0, read);
            }
        }
        catch (CertificateException ce) {
            throw new NetworkException(ce.getMessage());
        }
        catch (Exception e) {
            throw new NetworkException(e.getMessage());
        }
        finally {
            // Free up I/O streams and HTTP connection
            try {
                if (hcon != null) {
                    hcon.close();
                }
                if (dis != null) {
                    dis.close();
                }
                if (dos != null) {
                    dos.close();
                }
            }
            catch (IOException ioe) {
            }
        }
        return response.toByteArray();
    }

    /**
     * Read cookies from the HttpConnection and store them in the cookie jar.
     *
     * @param hcon HttpConnection object
     * @throws IOException
     */
    private void readCookies(HttpConnection hcon) throws IOException {
        String headerName = hcon.getHeaderField(0);

        // Loop through the headers, filtering "Set-Cookie" headers
        for (int i = 0; headerName != null; i++) {
            headerName = hcon.getHeaderFieldKey(i);

            if (headerName != null && headerName.toLowerCase().equals("set-cookie")) {
                String cookieContent = hcon.getHeaderField(i);
                COOKIE_JAR.put(cookieContent);
            }
        }
    }

    /**
     * Take cookies from the cookie jar and write them as request propreties
     * in the HttpConnection.
     *
     * @param hcon HttpConnection object
     * @throws IOException
     */
    private void writeCookies(HttpConnection hcon) throws IOException {
        String cookieHeader = COOKIE_JAR.getCookieHeader();
        if (cookieHeader == null) {
            return;
        }
        hcon.setRequestProperty("Cookie", cookieHeader);
    }

    /**
     * Thread entry point. Network operation is run in a separate thread.
     */
    public final void run() {
        HttpOperation operation = null;
        try {
            while (true) {
                // Take the next operation in queue, or wait if none available
                synchronized (QUEUE) {
                    if (QUEUE.size() > 0) {
                        operation = (HttpOperation) QUEUE.elementAt(0);
                        QUEUE.removeElementAt(0);
                    }
                    else {
                        QUEUE.wait();
                    }
                }

                if (operation == null) {
                    continue;
                }

                // Run any operation that was found in the queue
                try {
                    byte[] response = sendRequest(operation);
                    if (!operation.isAborted()) {
                        operation.responseReceived(response);
                    }
                }
                catch (NetworkException e) {
                    System.out.println("NetworkError: " + e.getMessage());
                    operation.responseReceived(null);
                }
                operation = null;
            }
        }
        catch (Throwable t) {
            System.out.println("HttpClient error: " + t.getMessage());
            operation.responseReceived(null);
        }
    }

    /**
     * Determine if network access is available.
     *
     * @return True if network access is available, false otherwise
     */
    public synchronized static boolean isAllowed() {
        if (!prompted) {
            promptNetworkAccess();
        }
        return allowed;
    }

    /**
     * Opens a network connection just to trigger the network prompt.
     */
    private static void promptNetworkAccess() {
        prompted = true;
        HttpConnection stimulus = null;
        try {
            stimulus = (HttpConnection) Connector.open("http://www.example.com/");
        }
        catch (SecurityException se) {
            allowed = false;
            return;
        }
        catch (Exception e) { /* Catch all the other exceptions */ }
        finally {
            try {
                if (stimulus != null) {
                    stimulus.close();
                }
            }
            catch (IOException ioe) {}
        }
        allowed = true;
    }
}
