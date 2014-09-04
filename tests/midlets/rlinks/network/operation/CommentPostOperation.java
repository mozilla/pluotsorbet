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

/**
 * An HttpOperation for posting a new comment.
 */
public class CommentPostOperation
    extends HttpOperation {

    private final String thingName;
    private final String text;
    private final String modhash;    
    private final PostCommentListener listener;

    /**
     * Interface used to signal caller about success or failure.
     */
    public interface PostCommentListener {
        public void commentingSucceeded(String thingName, String text);
        public void commentingFailed(String thingName, String text);
    }

    /**
     * Create a CommentPostOperation.
     *
     * @param thingName The Reddit name of the Thing we're commenting on, e.g. "t1_vxacv7e"
     * @param text Comment text
     * @param modhash Modhash of the logged in user
     * @param listener Listener to call with the result of the Operation
     */
    public CommentPostOperation(String thingName, String text, String modhash, PostCommentListener listener) {
        this.thingName = thingName;
        this.text = text;
        this.modhash = modhash;
        this.listener = listener;
    }

    /**
     * Format a request body using the data given in the constructor.
     *
     * @return A request body understood by the Reddit API
     */
    public String getRequestBody() {
        return
            "thing_id=" + UrlEncoder.encode(thingName) +
            "&uh=" + UrlEncoder.encode(modhash) +
            "&text=" + UrlEncoder.encode(text);
    }

    public String getRequestMethod() {
        return HttpConnection.POST;
    }

    public String getUrl() {
        return BASE_URL + "api/comment";
    }

    public void responseReceived(byte[] response) {
        finished = true;
        if (response == null || response.length == 0) {
            listener.commentingFailed(thingName, text);
            return;
        }
        parseResponse(new String(response));
    }

    /**
     * Parse response. The Reddit API description on what a successful
     * response looks like is very ambiguous; most of the time it should
     * be safe to assume that a request without known error codes was
     * successful.
     *
     * @param responseJson JSON API response
     */
    private void parseResponse(String responseJson) {
        if (responseJson.indexOf(".error.USER_REQUIRED") >= 0) {
            listener.commentingFailed(thingName, text);
        } else if (responseJson.indexOf(".error.RATELIMIT") >= 0) {
            listener.commentingFailed(thingName, text);
        } else {
            listener.commentingSucceeded(thingName, text);
        }
    }
}
