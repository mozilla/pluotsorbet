/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network.operation;

import com.nokia.example.rlinks.network.HttpOperation;
import com.nokia.example.rlinks.util.UrlEncoder;
import javax.microedition.io.HttpConnection;
import org.json.me.JSONArray;
import org.json.me.JSONObject;

/**
 * An HttpOperation that logs an user into Reddit.
 * 
 * A successful call will return two items that are required to maintain
 * a user session (and to successfully use operations that require user
 * authentication, such as commenting and voting):
 * 
 * - a "modhash" String
 * - a "reddit_session" cookie, handled by the HttpClient
 * 
 * The user account must have been created beforehand (e.g. in www.reddit.com).
 *
 * @see https://github.com/reddit/reddit/wiki/API%3A-login
 */
public class LoginOperation
    extends HttpOperation {

    private final String username;
    private final String password;
    private final LoginListener listener;

    /**
     * Listener interface used to signal the result of the login back to
     * the caller.
     */
    public interface LoginListener {
        
        /** Callback handler for a successful login. */
        public void loginSucceeded(String username, String modhash);

        /** Callback handler for a failed login. */
        public void loginFailed(String reason);
    }

    /**
     * Create a LoginOperation.
     *
     * @param username Username for login
     * @param password Password for login
     * @param listener Listener to signal success or failure to
     */
    public LoginOperation(String username, String password, LoginListener listener) {
        this.username = username;
        this.password = password;
        this.listener = listener;
    }

    /**
     * Format a request body using the data given in the constructor.
     *
     * @return A request body understood by the Reddit API
     */
    public String getRequestBody() {
        return
            "api_type=json" +
            "&user=" + UrlEncoder.encode(username) +
            "&passwd=" + UrlEncoder.encode(password);
    }

    /**
     * This operation uses the HTTP POST method.
     */
    public String getRequestMethod() {
        return HttpConnection.POST;
    }

    public String getUrl() {
        return BASE_URL + "api/login/" + UrlEncoder.encode(username);
    }

    public void responseReceived(byte[] response) {
        if (response == null || response.length == 0) {
            finished = true;
            listener.loginFailed(null);
            return;
        }
        parseResponse(new String(response));
    }

    /**
     * Parse a login JSON response.
     *
     * A successful login response has:
     * - a modhash in the 'data' object
     * - an empty 'errors' array
     *
     * If the response doensn't meet this criteria, the login is interpreted
     * as failed. No further error codes are parsed in the scope of this app.
     *
     * Example of a failed login:
     * {"json": {"errors": [["WRONG_PASSWORD", "invalid password", "passwd"]]}}
     *
     * @param responseJson Login response as JSON
     */
    private void parseResponse(String responseJson) {
        String reason = null;
        String modhash = null;
        try {
            JSONObject obj = new JSONObject(responseJson).getJSONObject("json");

            JSONArray errors = obj.getJSONArray("errors");
            JSONObject data = obj.optJSONObject("data");
            if (data != null) {
                modhash = data.optString("modhash");
            }

            // In case of a successful login, invoke the success callback
            if (errors.length() == 0 && modhash != null) {
                finished = true;
                listener.loginSucceeded(username, modhash);
                return;
            }
            // Otherwise try to interpret errors
            else if (errors.length() > 0) {
                String errorCode = errors.getJSONArray(0).getString(0);
                if ("WRONG_PASSWORD".equals(errorCode)) {
                    reason = "wrong password";
                } else if ("RATELIMIT".equals(errorCode)) {
                    reason = "trying too often";
                }
            }
        }
        catch (Exception e) {
            System.out.println("Error parsing JSON response: " + e.getMessage());
        }

        // If the login conditions were met (or the response couldn't be parsed),
        // invoke the failure callback.
        finished = true;
        listener.loginFailed(reason);
    }
}
