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
 * An HttpOperation for voting on a Thing (comment or link).
 */
public class VotePostOperation
    extends HttpOperation {

    public static final int VOTE_UP = 1;
    public static final int VOTE_NONE = 0;
    public static final int VOTE_DOWN = -1;

    private final String thingName;
    private final int vote;
    private final String modhash;
    private final PostVoteListener listener;

    /**
     * Listener interface used to signal caller about success or failure.
     */
    public interface PostVoteListener {
        public void votingSucceeded(String thingName, int vote);
        public void votingFailed(String thingName, int vote);
    }

    /**
     * Create a VotePostOperation.
     *
     * @param thingName The Reddit name of the Thing we're voting on, e.g. "t1_vxacv7e"
     * @param vote The vote to cast (VOTE_UP, VOTE_NONE, VOTE_DOWN)
     * @param modhash Modhash of the logged in user
     * @param listener Listener to call with the result of the Operation
     */
    public VotePostOperation(String thingName, int vote, String modhash, PostVoteListener listener) {
        this.thingName = thingName;
        this.vote = vote;
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
            "id=" + UrlEncoder.encode(thingName) +
            "&dir=" + vote +
            "&uh=" + UrlEncoder.encode(modhash);
    }

    public String getRequestMethod() {
        return HttpConnection.POST;
    }

    public String getUrl() {
        return BASE_URL + "api/vote";
    }

    public void responseReceived(byte[] response) {
        finished = true;
        if (response == null || response.length == 0) {
            listener.votingFailed(thingName, vote);
            return;
        }

        // A successful Vote operation should only reply with '{}'
        if (response != null && new String(response).equals("{}")) {
            listener.votingSucceeded(thingName, vote);
        } else {
            listener.votingFailed(thingName, vote);
        }
    }
}
