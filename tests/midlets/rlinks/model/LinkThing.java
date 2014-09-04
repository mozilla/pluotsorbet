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

import javax.microedition.lcdui.Image;
import org.json.me.JSONException;
import org.json.me.JSONObject;

/**
 * Representation of a Reddit Link item.
 *
 * @see https://github.com/reddit/reddit/wiki/thing
 */
public class LinkThing implements Voteable {

    /**
     * Disallow creating custom instances.
     */
    private LinkThing() {}

    /**
     * Create a LinkThing from a JSON data String.
     *
     * @param obj JSONObject containing data for the Link
     * @return A LinkThing object
     * @throws JSONException If the given JSONObject can't be parsed
     */
    public static LinkThing fromJson(JSONObject obj) throws JSONException {
        LinkThing thing = new LinkThing();
        thing.setAuthor(obj.getString("author"));

        try {
            // "1329913184.0" -> "13299131840000"
            String dateStr = obj.getString("created_utc").substring(0, 9) + "0000";
            thing.setCreated(new Date(Long.parseLong(dateStr)));
        }
        catch (Exception e) {
            System.out.println("Couldn't set date: " + e.getMessage());
        }
        thing.setDomain(obj.getString("domain"));
        thing.setId(obj.getString("id"));
        thing.setName(obj.getString("name"));
        thing.setNumComments(obj.getInt("num_comments"));
        thing.setPermalink(obj.getString("permalink"));
        thing.setSubreddit(obj.getString("subreddit"));
        thing.setScore(obj.getInt("score"));
        thing.setThumbnail(obj.getString("thumbnail"));
        thing.setTitle(HtmlEntityDecoder.decode(obj.getString("title")));
        thing.setUrl(obj.getString("url"));

        // 'Likes' can be true, false or null; map that to 1, -1, and 0
        thing.setVote(
            obj.isNull("likes") ? 0 : obj.getBoolean("likes") ? 1: - 1
        );

        return thing;
    }

    public String toString() {
        return this.getId() + " (" + this.getNumComments() + ") - " + this.getTitle();
    }

    // Author of the Link
    private String author;

    // Creation date
    private Date created;

    // Domain of the Link, e.g. "en.wikipedia.org"
    private String domain;

    // Internal Reddit-assigned ID
    private String id;

    // Application-assigned image for the Link (when available)
    private Image image;

    // The internal Reddit-assigned name (<thing type>_<ID>, e.g. "t1_c47xiv")
    private String name;

    // Number of comments for this Link
    private int numComments;

    // Permanent URL pointing to this Link
    private String permalink;

    // A combined sum of up and down votes
    private int score;

    // The subreddit (category) of this Link
    private String subreddit;

    // Link title. This is what gets displayed most of the time
    private String title;

    // A thumbnail image location for this Link, if available
    private String thumbnail;

    // The URL (source) of this Link.
    private String url;

    // A possible vote given to this comment by the current user, if available.
    // Possible values: -1 (voted down), 0 (not voted), 1 (voted up)
    private int vote;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public Date getCreated() {
        return created;
    }

    public void setCreated(Date created) {
        this.created = created;
    }

    public String getDomain() {
        return domain;
    }

    public void setDomain(String domain) {
        this.domain = domain;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Image getImage() {
        return image;
    }

    public void setImage(Image image) {
        this.image = image;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getNumComments() {
        return numComments;
    }

    public void setNumComments(int mNumComments) {
        this.numComments = mNumComments;
    }

    public String getPermalink() {
        return permalink;
    }

    public void setPermalink(String permalink) {
        this.permalink = permalink;
    }

    public int getScore() {
        return score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public String getSubreddit() {
        return subreddit;
    }

    public void setSubreddit(String subreddit) {
        this.subreddit = subreddit;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(String thumbnail) {
        // Thumbnail links are only useful if our HTTP client is able to
        // get them: only accept obvious URLs
        this.thumbnail = thumbnail == null || !thumbnail.startsWith("http") ?
            null :
            thumbnail;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getVote() {
        return vote;
    }

    public void setVote(int vote) {
        // Reset possible old vote, set new vote, add new vote
        score -= this.vote;
        this.vote = vote;
        score += vote;
    }

    public String getText() {
        return title;
    }
}
