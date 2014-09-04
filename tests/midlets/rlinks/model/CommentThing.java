/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.model;

import com.nokia.example.rlinks.util.HtmlEntityDecoder;
import java.util.Date;
import org.json.me.JSONArray;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * Representation of a Reddit Comment item.
 *
 * @see https://github.com/reddit/reddit/wiki/thing
 */
public class CommentThing implements Voteable {

    /**
     * Disallow creating custom instances.
     */
    private CommentThing() {}

    /**
     * Create a CommentThing from a JSON data String.
     *
     * @param obj JSONObject containing data for the Comment
     * @return A CommentThing object
     * @throws JSONException If the given JSONObject can't be parsed
     */
    public static CommentThing fromJson(JSONObject obj) throws JSONException {
        CommentThing thing = new CommentThing();
        thing.setAuthor(obj.optString("author"));
        thing.setBody(HtmlEntityDecoder.decode(obj.optString("body")));

        // Comments have a created date, 'More' items don't
        if (obj.has("created_utc")) {
            try {
                // "1329913184.0" -> "13299131840000"
                String dateStr = obj.getString("created_utc").substring(0, 9) + "0000";
                thing.setCreated(new Date(Long.parseLong(dateStr)));
            }
            catch (Exception e) {
                System.out.println("Couldn't set date: " + e.getMessage());
            }
        }

        thing.setId(obj.optString("id"));        
        thing.setName(obj.optString("name"));
        thing.setScore(obj.optInt("ups"), obj.optInt("downs"));

        // 'Likes' can be true, false or null; map that to 1, -1, and 0
        thing.setVote(
            obj.isNull("likes") ? 0 : obj.getBoolean("likes") ? 1: - 1
        );

        // Set child IDs. These are used for dynamically fetching more replies
        // to a comment.
        JSONArray childrenArray = obj.optJSONArray("children");
        if (childrenArray != null) {
            int len = childrenArray.length();
            String[] childIds = new String[len];
            for (int i = 0; i < len; i++) {
                childIds[i] = childrenArray.getString(i);
            }
            thing.setChildIds(childIds);
        }
        return thing;
    }

    // Author of the Comment
    private String author;

    // Comment body (text content)
    private String body;

    // List of child IDs (hidden replies)
    private String[] childIds = new String[] {};

    // Creation date
    private Date created;

    // Internal Reddit-assigned ID
    private String id;

    // Level (depth) of the Comment. Used to represent a tree structure
    private int level;

    // A possible vote given to this comment by the current user, if available.
    // Possible values: -1 (voted down), 0 (not voted), 1 (voted up)
    private int vote;

    // The internal Reddit-assigned name (<thing type>_<ID>, e.g. "t1_c47xiv")
    private String name;

    // A combined sum of up and down votes
    private int score;

    public String getAuthor() {
        return this.author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String[] getChildIds() {
        return childIds;
    }

    public void setChildIds(String[] childIds) {
        this.childIds = childIds;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    /**
     * Return a formatted score for this Comment, i.e. "+572", "0", "-29".
     *
     * @return
     */
    public String getFormattedScore() {
        if (score == 0) {
            return "";
        }
        return " (" + (score > 0 ? "+" : "") + score + ")";
    }

    public int getHiddenChildCount() {
        return childIds.length;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        this.vote = vote;
    }

    public void setScore(int ups, int downs) {
        this.score = ups - downs;
    }

    public String toString() {
        return this.getAuthor() + " @ " + this.getCreated();
    }

    public String getText() {
        return body;
    }
}
