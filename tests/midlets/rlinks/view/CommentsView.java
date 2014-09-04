/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.view;

import com.nokia.example.rlinks.view.item.CommentItem;
import com.nokia.example.rlinks.Main;
import com.nokia.example.rlinks.model.CommentThing;
import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.model.Voteable;
import com.nokia.example.rlinks.network.operation.CommentsLoadOperation;
import com.nokia.example.rlinks.network.operation.CommentsLoadOperation.LoadCommentsListener;
import com.nokia.example.rlinks.network.HttpOperation;
import com.nokia.example.rlinks.network.operation.MoreCommentsLoadOperation;
import com.nokia.example.rlinks.view.CommentDetailsView.CommentDetailsBackListener;
import com.nokia.example.rlinks.view.item.LoaderItem;
import com.nokia.example.rlinks.view.item.CommentItem.CommentSelectionListener;
import com.nokia.example.rlinks.view.item.LinkItem;
import com.nokia.example.rlinks.view.item.LinkItem.LinkSelectionListener;
import com.nokia.example.rlinks.view.item.LinkMetadataItem;
import com.nokia.example.rlinks.view.item.MoreCommentsItem;
import com.nokia.example.rlinks.view.item.VoteItem;
import java.util.Vector;
import javax.microedition.io.ConnectionNotFoundException;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

/**
 * View displaying comments for a Link.
 */
public class CommentsView
    extends BaseFormView
    implements LoadCommentsListener, CommentSelectionListener {

    private static final int COMMENT_CHUNK_SIZE = 20;

    private final Command commentCommand = new Command("Comment", "Comment/Vote", Command.SCREEN, 0);    
    private final LinkThing link;
    private final BackCommandListener backListener;
    
    private LinkItem linkItem;
    private HttpOperation operation;
    private int loadingItemIndex = -1;

    /**
     * Create a CommentsView.
     *
     * @param link Link item to show comments for
     * @param backListener Listener signaling back commands to
     */
    public CommentsView(LinkThing link, BackCommandListener backListener) {
        super("Comments", new Item[]{});
        this.link = link;
        this.backListener = backListener;

        setupCommands();
    }

    protected final void setupCommands() {
        addCommand(backCommand);
        addCommand(commentCommand);
        addCommand(refreshCommand);
        addCommand(aboutCommand);
    }

    public void show() {
        // Update Login/Logout commands depending on current login status
        setupLoginCommands();

        if (size() > 0) {
            setItem(get(0));
            return;
        }
        addLoginStatusItem();
        addMetaItems();
        loadComments();
    }

    private void refresh() {
        if (operation != null && !operation.isFinished()) {
            return;
        }
        operation = null;
        deleteAll();
        show();
    }

    /**
     * Adds the meta items:
     * 
     * - related link item as the topmost item in the list view.
     * - item for voting (when logged in)
     * - item showing metadata (author, date)
     */
    private void addMetaItems() {
        append(createLinkItem());
        append(new VoteItem(link, getWidth(), new VoteItem.VoteListener() {
            public void voteSubmitted(int vote) {
                link.setVote(vote);
                linkItem.refresh();
            }
        }, this, this));
        append(new LinkMetadataItem(link, getWidth(), this));
    }

    /**
     * Create a Link item to be shown as the topmost item in the view.
     *
     * Tapping on the link will prompt to open the link URL in the
     * platform browser.
     *
     * @return Link item
     */
    private LinkItem createLinkItem() {
        // A listener to invoke whenever the link gets selected; either by
        // direct touch selection or by the selection command
        final LinkSelectionListener selectionListener = new LinkSelectionListener() {
            public void linkSelected(LinkThing link) {
                try {
                    Main.getInstance().platformRequest(link.getUrl());
                }
                catch (ConnectionNotFoundException ex) {
                    System.out.println("Connection not found: " + ex.getMessage());
                }
            }
        };
        linkItem = new LinkItem(link, getWidth(), true, selectionListener, null, this);

        return linkItem;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == commentCommand) {
            openDetailsView(null);
        }
        else if (command == backCommand) {
            abortLoadingComments();
            backListener.backCommanded();
        }
        else if (command == refreshCommand) {
            refresh();
        }
        else if (command == aboutCommand) {
            showAboutView();
        }
        else if (command == loginCommand) {
            showLoginView();
        }
        else if (command == logoutCommand) {
            session.setLoggedOut();
            setupCommands();
        }
    }

    /**
     * Load comments to be shown in the view.
     */
    private void loadComments() {
        if (link.getNumComments() == 0) {
            return;
        }

        // Do not reload if already loaded, or if in process
        if (HttpOperation.reloadNeeded(operation)) {
            // Add 'Loading' item
            loadingItemIndex = append(new LoaderItem());
            operation = new CommentsLoadOperation(link.getId(), COMMENT_CHUNK_SIZE, this);
            operation.start();
        }
    }
    
    /**
     * Abort any unfinished comment loading operations.
     */
    private void abortLoadingComments() {
        if (operation != null && !operation.isFinished()) {
            operation.abort();
            deleteAll();
        }
    }

    /**
     * Load more comments for the given comment.
     *
     * This solution aims to be scalable in that it:
     * - only gets one new child item (and its children) at a time,
     * - updates the list of child IDs for the original selection, and
     * - creates a new 'load more replies' item with the indexes taken care of.
     *
     * When there are no more children to be loaded, a new 'Load more' item
     * will not be added.
     *
     * @param comment Comment to get children for
     * @param itemIndex Index of the 'Load more' item that was selected
     */
    private void loadMoreComments(final CommentThing comment, final int itemIndex) {
        final String[] childIds = comment.getChildIds();
        final MoreCommentsItem moreItem = ((MoreCommentsItem) get(itemIndex));
        moreItem.setLoading();

        LoadCommentsListener listener = new LoadCommentsListener() {
            public synchronized void commentsReceived(final Vector comments) {

                // First, create custom list items from the comment items
                final CommentItem[] commentItems = createCommentItems(comments);

                // Update the view in the main UI thread
                Runnable updateView = new Runnable() {
                    public void run() {
                        // Remove the original "Load more comments" item
                        if (get(itemIndex) == moreItem) {
                            delete(itemIndex);
                        }

                        // Insert items
                        int lastIndex = insertCommentItems(commentItems, itemIndex);

                        // If there's more content to be loaded, create a new
                        // 'Load More Comments' item
                        if (childIds.length > 1) {
                            createMoreItem(comment, lastIndex + 1);
                        }

                        // Update indexes for items after the inserted items
                        for (int i = lastIndex, len = size(); i < len; i++) {
                            ((CommentItem) get(i)).setItemIndex(i);
                        }
                    }
                };
                Main.executeInUIThread(updateView);
            }
        };

        // Load comments for the first child only, add the rest as "more" items
        operation = new MoreCommentsLoadOperation(
            link.getId(), childIds[0], comment.getLevel(), COMMENT_CHUNK_SIZE, listener);
        operation.start();
    }

    /**
     * Create an array custom list items from a Vector of CommentThings.
     *
     * @param comments Vector of comment items
     * @return Array of RedditCommentItem items
     */
    private CommentItem[] createCommentItems(final Vector comments) {
        int numComments = comments.size();
        final CommentItem[] commentItems = new CommentItem[numComments];
        final int width = getWidth();

        CommentThing comment;
        for (int i = 0; i < numComments; i++) {
            comment = (CommentThing) comments.elementAt(i);
            if (comment.getHiddenChildCount() > 0) {
                commentItems[i] = new MoreCommentsItem(comment, this, width, this);
            } else {
                commentItems[i] = new CommentItem(comment, this, width, this);
            }
        }

        return commentItems;
    }

    /**
     * Insert given comment items at a specified index, maintaining the
     * 'itemIndex' property of the items.
     *
     * @param commentItems Array of comment items to add
     * @param startIndex Index to start adding at
     * @return The index of the last item that was added
     */
    private synchronized int insertCommentItems(final CommentItem[] commentItems, int startIndex) {
        int idx = startIndex;
        for (int i = 0, len = commentItems.length; i < len; i++) {
            idx = startIndex + i;
            commentItems[i].setItemIndex(idx);
            insert(idx, commentItems[i]);
        }
        return idx;
    }

    /**
     * Create a new 'Load more' item with the first child item ID removed.
     *
     * @param comment Comment to be updated
     * @param index Index of the item
     */
    private void createMoreItem(CommentThing comment, int index) {
        String[] childIds = comment.getChildIds();
        String[] newChildIds = new String[childIds.length - 1];

        try {
            System.arraycopy(childIds, 1, newChildIds, 0, childIds.length - 1);
        }
        catch (Exception e) {
            System.out.println("Arraycopy failed: " + e.getMessage());
        }
        comment.setChildIds(newChildIds);

        // Add a new More item
        CommentItem more = new MoreCommentsItem(comment, this, getWidth(), this);
        more.setItemIndex(index);
        insert(index, more);
    }

    /**
     * Process comments received.
     *
     * @param comments Vector of comments loaded
     */
    public synchronized void commentsReceived(Vector comments) {
        if (comments == null) {
            Main.executeInUIThread(new Runnable() {
                public void run() {
                    delete(loadingItemIndex);
                    loadingItemIndex = -1;
                    showNetworkError();
                }
            });         
            return;
        }

        // First, create custom list items from the comment items
        final CommentItem[] commentItems = createCommentItems(comments);
        comments = null;

        // Then add the comment items to the view in the UI thread
        Main.executeInUIThread(new Runnable() {
            public void run() {
                // Remove 'Loading' item
                delete(loadingItemIndex);
                insertCommentItems(commentItems, size());
            }
        });
    }

    /**
     * Act on a comment (or "load more replies" item) being selected.
     *
     * @param comment Comment selected
     * @param itemIndex Its index in the list
     */
    public void commentSelected(final CommentThing comment, final int itemIndex) {
        final boolean operationPending =
            operation != null && !operation.isFinished();

        // Only allow one operation at a time
        if (operationPending) {
            return;
        }        
        else if (comment.getHiddenChildCount() > 0) {
            loadMoreComments(comment, itemIndex);
        }
        // In case an ordinary Comment is selected, open up comment details view
        else {
            openDetailsView(comment);
        }
    }

    /**
     * Show detalis view for a given comment.
     *
     * @param comment Comment to show details for
     */
    private void openDetailsView(Voteable comment) {
        // When writing a top-level comment, the Thing replied to is the link.
        // When replying to comment, the Thing is the parent comment itself.
        final Voteable item = comment != null ? comment : link;

        CommentDetailsView cv = new CommentDetailsView(
            item,
            new CommentDetailsBackListener() {
                public void backCommanded(boolean commentAdded) {
                    // New comment added, refreshing
                    if (commentAdded) {
                        link.setNumComments(link.getNumComments() + 1);
                        refresh();
                    }
                    setDisplay(self);
                }
            },
            new VoteItem.VoteListener() {
                public void voteSubmitted(int vote) {
                    item.setVote(vote);
                }
            }
        );
        setDisplay(cv);
        cv.show();
    }
}
