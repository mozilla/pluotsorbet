/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network.operation;

import com.nokia.example.rlinks.model.CommentThing;
import com.nokia.example.rlinks.network.HttpOperation;
import java.util.Vector;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * An HttpOperation that loads comments for a given Reddit link ID.
 * Uses a listener class to broadcast back the results of the operation.
 */
public class CommentsLoadOperation
    extends HttpOperation {

    protected final LoadCommentsListener listener;
    protected final String linkId;
    protected final int limit;

    /**
     * Listener interface for methods asynchronously fetching Reddit comments.
     *
     * Will be called with a null value if the loading should fail.
     */
    public interface LoadCommentsListener {
        public void commentsReceived(Vector comments);
    }

    /**
     * Create a CommentsLoadOperation.
     *
     * @param linkId The ID of the Link to load comments for
     * @param limit Maximum number of comments to load, or 0 for no limit
     * @param listener Listener to call with the results
     */
    public CommentsLoadOperation(String linkId, int limit, LoadCommentsListener listener) {
        this.linkId = linkId;
        this.limit = limit;
        this.listener = listener;
    }

    public String getUrl() {
        String url = BASE_URL + "comments/" + linkId + ".json";
        if (limit > 0) {
            url += "?limit=" + limit;
        }
        return url;
    }

    public void responseReceived(byte[] response) {
        if (response == null || response.length == 0) {
            finished = true;
            listener.commentsReceived(null);
            return;
        }
        parseComments(new String(response), 0);
    }

    /**
     * Parse a JSON data string into a Vector of CommentThing objects.
     * 
     * @param commentsJson JSON reply to parse into Comments
     * @param startLevel Level of hierarchy to start at (most of the time 0)
     */
    protected void parseComments(final String commentsJson, final int startLevel) {
        Vector comments = new Vector();
        JSONObject listingJsonObject;
        try {
            // Recurse through any replies and their replies and ...
            listingJsonObject = new JSONArray(commentsJson).getJSONObject(1);
            recursivelyAddReplies(comments, listingJsonObject, startLevel);
        }
        catch (JSONException e) {
            System.out.println("Error parsing JSON response: " + e.getMessage());
        }

        // Signal the listener when done
        finished = true;
        if (!aborted) {
            listener.commentsReceived(comments);
        }
    }

    /**
     * Recursively parse the given listingJsonObject for Comments, adding them
     * into the Vector specified. Will call itself until the comment tree is
     * whole (until there are no more child JSON objects to parse).
     * 
     * @param comments Vector of comments
     * @param listingJsonObject JSON object (comment listing) to parse
     * @param level Depth level
     * @throws JSONException In case of a parsing error
     */
    protected void recursivelyAddReplies(Vector comments, JSONObject listingJsonObject, int level) throws JSONException {
        JSONArray childrenJsonArray = listingJsonObject
                .getJSONObject("data")
                .getJSONArray("children");

        // Reuse the same objects to avoid unnecessary overhead
        JSONObject thingDataObj;
        CommentThing comment;
        
        for (int i = 0, len = childrenJsonArray.length(); i < len && !aborted; i++) {

            thingDataObj = childrenJsonArray.getJSONObject(i).getJSONObject("data");

            try {
                // Create a comment item and append it to the list
                comment = CommentThing.fromJson(thingDataObj);
                comment.setLevel(level);
                comments.addElement(comment);
            }
            catch (JSONException e) {
                System.out.println("Could not parse comment JSON: " + e.getMessage());
            }

            // Process any further replies
            JSONObject repliesJsonObject = thingDataObj.optJSONObject("replies");
            if (repliesJsonObject != null) {
                recursivelyAddReplies(comments, repliesJsonObject, level + 1);
            }
        }
    }

    public String toString() {
        return "LoadComments(url=" + getUrl() + ")";
    }
}
