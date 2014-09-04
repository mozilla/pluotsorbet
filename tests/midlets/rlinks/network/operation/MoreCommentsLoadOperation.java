/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.network.operation;

/**
 * An HttpOperation for loading comments that were not initially shown.
 */
public class MoreCommentsLoadOperation
    extends CommentsLoadOperation {

    private final String commentId;
    private final int level;

    /**
     * Create a MoreCommentsLoadOperation.
     *
     * @param linkId Link ID to get more comments for
     * @param commentId Parent comment ID
     * @param level Level at which the comments belong
     * @param limit Maximum number of comments to fetch
     * @param listener Listener to signal of comments being loaded
     */
    public MoreCommentsLoadOperation(String linkId, String commentId, int level, int limit, LoadCommentsListener listener) {
        super(linkId, limit, listener);

        this.commentId = commentId;
        this.level = level;
    }

    public String getUrl() {
        String url = BASE_URL + "comments/" + linkId + "/_/" + commentId + ".json";
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

        // Parse using the parent class
        parseComments(new String(response), level);
    }
}
