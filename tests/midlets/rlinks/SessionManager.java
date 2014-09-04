/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks;

import com.nokia.example.rlinks.network.HttpClient.CookieJar;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.EOFException;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Hashtable;
import javax.microedition.rms.InvalidRecordIDException;
import javax.microedition.rms.RecordEnumeration;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

/**
 * Store and retrieve user session data in RMS storage.
 * 
 * For simplicity, this class also implements the CookieJar interface
 * so that a single datastore can be used also by HttpClient to store
 * cookies sent by the API.
 */
public final class SessionManager implements CookieJar {

    private static SessionManager instance = null;
    private static final String RECORDSTORE_SESSION = "session";

    private String category = null;
    private String username = null;
    private String modhash = null;
    private Hashtable cookieTable = new Hashtable();

    /**
     * Initialize the SessionManager with data from last session (if any)
     * when created for the first time.
     */
    private SessionManager() {
        load();
    }

    /**
     * Get a shared SessionManager instance.
     *
     * @return SessionManager
     */
    public static SessionManager getInstance() {
        if (instance == null) {
            instance = new SessionManager();
        }
        return instance;
    }

    /**
     * Determine whether the user is logged in.
     *
     * @return True if logged in, false otherwise
     */
    public boolean isLoggedIn() {
        return modhash != null;
    }

    /**
     * Return the currently selected category (subreddit).
     *
     * @return Name of the currently selected category, or null if front page
     */
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
        save();
    }

    /**
     * Return the modhash for the logged-in user.
     *
     * @return Modhash for the currently logged in user, or null if none
     */
    public String getModhash() {
        return modhash;
    }

    /**
     * Return the username for the logged-in user.
     *
     * @return Username for the currently logged in user, or null if none
     */
    public String getUsername() {
        return username;
    }

    /**
     * Set a user as logged-in.
     *
     * @param username Username logged in as
     * @param modhash Modhash received upon login
     */
    public void setLoggedIn(String username, String modhash) {
        this.username = username;
        this.modhash = modhash;
        save();
    }

    /**
     * Set a user as logged-out. Effectively clears modhash and any cookies.
     */
    public void setLoggedOut() {
        modhash = null;
        cookieTable.clear();
        save();
    }

    /**
     * Load session data from RMS.
     */
    private void load() {
        RecordStore rs = null;
        RecordEnumeration re = null;
        ByteArrayInputStream bais = null;
        DataInputStream dis = null;
        try {
            rs = RecordStore.openRecordStore(RECORDSTORE_SESSION, true);
            re = rs.enumerateRecords(null, null, true);

            // If no session data is found, skip retrieval and go by the defaults
            if (!re.hasNextElement()) {
                return;
            }

            int id = re.nextRecordId();
            bais = new ByteArrayInputStream(rs.getRecord(id));
            dis = new DataInputStream(bais);

            try {
                category = dis.readUTF();
                category = category.equals("") ? null : category;
                username = dis.readUTF();
                username = username.equals("") ? null : username;
                modhash = dis.readUTF();
                modhash = modhash.equals("") ? null : modhash;
                
                // Retrieve the cookieTable: first read the number of Strings
                // to expect, then read the Strings and further parse and insert
                // them as key/value values in the cookieTable
                int numCookies = dis.readInt();
                for (int i = 0; i < numCookies; i++) {
                    String tmp = dis.readUTF();
                    int index = tmp.indexOf('|');
                    String key = tmp.substring(0, index);
                    String value = tmp.substring(index + 1);
                    cookieTable.put(key, value);
                }
            }
            catch (EOFException eofe) {
                eofe.printStackTrace();
            }            
        }
        catch (InvalidRecordIDException ire) {
            ire.printStackTrace();
        }
        catch (RecordStoreException rse) {
            rse.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (dis != null) {
                try {
                    dis.close();
                }
                catch (IOException ex) {}
            }
            if (bais != null) {
                try {
                    bais.close();
                }
                catch (IOException ex) {}
            }
            if (re != null) {
                re.destroy();
            }
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                }
                catch (Exception ex) {}
            }
        }
    }

    /**
     * Save session data into RMS.
     */
    private void save() {
        try {
            RecordStore.deleteRecordStore(RECORDSTORE_SESSION); // Clear data
        }
        catch (Exception e) { /* Nothing to delete */ }

        RecordStore rs = null;
        ByteArrayOutputStream baos = null;
        DataOutputStream dos = null;
        try {
            rs = RecordStore.openRecordStore(RECORDSTORE_SESSION, true);
            baos = new ByteArrayOutputStream();
            dos = new DataOutputStream(baos);

            dos.writeUTF(category == null ? "" : category);
            dos.writeUTF(username == null ? "" : username);
            dos.writeUTF(modhash == null ? "" : modhash);

            // Store the cookieTable: first an integer telling how many UTF-8
            // encoded string entries to expect, then the Strings themselves
            dos.writeInt(cookieTable.size());
            Enumeration keys = cookieTable.keys();
            while (keys.hasMoreElements()) {
                String key = (String) keys.nextElement();
                String value = (String) cookieTable.get(key);
                dos.writeUTF(key + "|" + value);
            }

            // Add it to the record store
            byte[] b = baos.toByteArray();
            rs.addRecord(b, 0, b.length);
        }
        catch (RecordStoreException rse) {
            rse.printStackTrace();
        }
        catch (IOException ioe) {
            ioe.printStackTrace();
        }
        finally {
            if (rs != null) {
                try {
                    rs.closeRecordStore();
                }
                catch (Exception ex) {}
            }
        }
    }

    /**
     * Put a cookie in the cookie jar.
     *
     * @param cookieContent Contents of the HTTP "Set-Cookie" header
     */
    public void put(String cookieContent) {
        // The first token is sufficient for the scope of Reddit
        cookieContent = cookieContent.substring(0, cookieContent.indexOf(';'));
        int splitIdx = cookieContent.indexOf("=");
        String cookieName = cookieContent.substring(0, splitIdx);
        String cookieValue = cookieContent.substring(splitIdx + 1);

        // Store the cookie in the jar
        cookieTable.put(cookieName, cookieValue);

        save();
    }

    /**
     * Get a String representation of all the cookies stored in the jar.
     *
     * @return A string that can be directly set in the "Cookie" header
     */
    public String getCookieHeader() {
        StringBuffer cookieStr = new StringBuffer();
        Enumeration e = cookieTable.keys();
        while (e.hasMoreElements()) {
            // name1=value1; name2=value2
            String key = (String) e.nextElement();
            cookieStr.append(key).append("=").append(cookieTable.get(key)).append("; ");
        }

        if ("".equals(cookieStr.toString())) {
            return null;
        }
        return cookieStr.toString();
    }
}
