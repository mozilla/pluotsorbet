/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network.operation;

import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.network.HttpOperation;
import java.util.Vector;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * An HttpOperation for loading links for a given category.
 */
public class LinksLoadOperation
    extends HttpOperation {

    private final LoadLinksListener linkListener;
    private final String subreddit;
    
    private String url;

    /**
     * Listener interface for methods asynchronously fetching Reddit links.
     *
     * Will be called with a null value if the loading should fail.
     */
    public interface LoadLinksListener {
        public void linksReceived(Vector links);
    }

    /**
     * Create a LinksLoadOperation.
     * 
     * @param subreddit Subreddit (category) to load links for
     * @param listener Listener to signal of links being loaded
     */
    public LinksLoadOperation(String subreddit, LoadLinksListener listener) {
        this.subreddit = subreddit;
        this.linkListener = listener;
    }

    public String getUrl() {
        if (url == null) {
            String subredditPart = "";
            if (subreddit != null) {
                subredditPart = "r/" + subreddit + "/";
            }
            url = BASE_URL + subredditPart + ".json";
        }
        return url;
    }

    public void responseReceived(byte[] response) {
        if (response == null || response.length == 0) {
            finished = true;
            linkListener.linksReceived(null);
            return;
        }
        parseLinks(new String(response));
    }

    /**
     * Parse a JSON representation of Reddit links into a Vector
     * of RedditLink objects.
     *
     * @param linksJson The JSON response from the server
     */
    private void parseLinks(String linksJson) {
        JSONObject jsonResponse;
        JSONArray jsonItems;
        try {
            try {
                jsonResponse = new JSONObject(linksJson);
            }
            catch (NumberFormatException nfe) {
                System.out.println("NFE: " + nfe.getMessage());
                return;
            }
            jsonItems = jsonResponse.getJSONObject("data").getJSONArray("children");
        }
        catch (JSONException e) {
            System.out.println("Could not populate from JSON data: " + e.getMessage());
            return;
        }

        Vector links = new Vector();
        int numItems = jsonItems.length();

        if (numItems > 0) {
            LinkThing item;
            JSONObject jsonObj;
            for (int i = 0; i < numItems; i++) {
                try {
                    jsonObj = jsonItems.getJSONObject(i).getJSONObject("data");
                    item = LinkThing.fromJson(jsonObj);
                    links.addElement(item);
                }
                catch (JSONException e) {
                    System.out.println("Could not parse JSON object: " + e.getMessage());
                }
            }
        }

        finished = true;
        linkListener.linksReceived(links);
    }

    public String toString() {
        return "LoadLinks(url=" + url + ")";
    }
}
