/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.model;

/**
 * Denotes an item that can be voted: in practice a Comment or a Link.
  */
public interface Voteable {

    public String getAuthor();

    public String getName();

    public int getVote();

    /**
     * Set the active user's vote for this item.
     *
     * @param vote -1 (voted down), 0 (not voted), 1 (voted up).
     */
    public void setVote(int vote);
    
    public String getText();
}
