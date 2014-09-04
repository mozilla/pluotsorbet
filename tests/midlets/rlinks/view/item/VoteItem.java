/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/
package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.model.Voteable;
import com.nokia.example.rlinks.network.HttpOperation;
import com.nokia.example.rlinks.network.operation.VotePostOperation;
import com.nokia.example.rlinks.network.operation.VotePostOperation.PostVoteListener;
import com.nokia.example.rlinks.SessionManager;
import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.util.TouchChecker;
import com.nokia.example.rlinks.view.BaseFormView;
import com.nokia.mid.ui.LCDUIUtil;
import java.io.IOException;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

/**
 * A custom view item for voting on a Voteable (a LinkThing or a CommentThing).
 */
public class VoteItem
    extends AbstractCustomItem {

    protected static final int H_SPACE = VisualStyles.LINK_H_SPACE;
    protected static final int V_SPACE = VisualStyles.LINK_V_SPACE / 2;
    protected static final Font FONT = VisualStyles.MEDIUM_BOLD_FONT;
    protected static Image voteDownImage = Image.createImage(28, 29);
    protected static Image voteDownActiveImage = voteDownImage;
    protected static Image voteUpImage = voteDownImage;
    protected static Image voteUpActiveImage = voteDownImage;
    protected final int voteImageHeight;
    protected final int voteImageWidth;
    protected final int height;
    protected int centerX;
    protected final VoteListener listener;
    protected final Voteable item;
    protected final VoteItem self = this;
    protected final SessionManager session = SessionManager.getInstance();
    protected final BaseFormView parent;

    /**
     * Listener interface used to signal an item has been voted on.
     */
    public interface VoteListener {

        public void voteSubmitted(int vote);
    }

    // Load the images once
    static {
        try {
            voteDownImage = Image.createImage("/midlets/rlinks/images/down_inactive.png");
            voteDownActiveImage = Image.createImage("/midlets/rlinks/images/down_active.png");
            voteUpImage = Image.createImage("/midlets/rlinks/images/up_inactive.png");
            voteUpActiveImage = Image.createImage("/midlets/rlinks/images/up_active.png");
        }
        catch (IOException ex) {
            System.out.println("Can't load image: " + ex.getMessage());
        }
    }

    /**
     * Create a VoteItem.
     *
     * @param item Item we're voting on
     * @param preferredWidth Preferred width
     * @param listener Listener to signal of vote results
     * @param form parent BaseFormView
     */
    public VoteItem(Voteable item, int preferredWidth, VoteListener listener,
        Form form, BaseFormView parent) {
        super(form, preferredWidth, null);

        this.item = item;
        this.parent = parent;
        this.voteImageWidth = voteDownImage.getWidth();
        this.voteImageHeight = voteDownImage.getHeight();
        this.height = getPrefContentHeight(width);
        this.listener = listener != null ? listener : new VoteListener() {

            public void voteSubmitted(int vote) {
            }
        };
        this.centerX = width / 2;

        if (TouchChecker.DIRECT_TOUCH_SUPPORTED) {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.s40.item.direct_touch",
                new Boolean(true));
        }
    }

    protected int getMinContentWidth() {
        return width;
    }

    protected int getMinContentHeight() {
        return height;
    }

    protected int getPrefContentWidth(int height) {
        return width;
    }

    protected int getPrefContentHeight(int width) {
        return V_SPACE * 2 + voteImageHeight + V_SPACE * 2;
    }
    
    /**
     * Detect touches (left side = vote down, right side = vote up).
     */
    public void pointerReleased(int x, int y) {
        if (!dragging) {
            if (!session.isLoggedIn()) {
                parent.showLoginRequiredMessage();
                return;
            }

            // Vote down or up, depending on which side of the Item was clicked
            voteItemPressed(x < centerX ? VotePostOperation.VOTE_DOWN
                    : VotePostOperation.VOTE_UP);
        }
        super.pointerReleased(x, y);
    }

    /**
     * Act on a voting request: down or up.
     *
     * - Selecting an already active vote item nullifies the existing vote.
     * - Selecting a previously unselected up or down changes the vote.
     *
     * @param requestedVote
     */
    private void voteItemPressed(int requestedVote) {
        // Nullifying vote if the same option is selected again
        final int oldVote = item.getVote();
        if (requestedVote == oldVote) {
            requestedVote = VotePostOperation.VOTE_NONE;
        }

        // Store the old vote in case the voting fails
        item.setVote(requestedVote);
        repaint();
        listener.voteSubmitted(requestedVote);

        HttpOperation voteOperation = new VotePostOperation(
            item.getName(),
            requestedVote,
            session.getModhash(),
            new PostVoteListener() {

                public void votingSucceeded(String thingName, int vote) {
                    listener.voteSubmitted(vote);
                }

                public void votingFailed(String thingName, int vote) {
                    // In case the vote wasn't successful, reflect that in the UI
                    item.setVote(oldVote);
                    repaint();
                    listener.voteSubmitted(oldVote);
                }
            });
        voteOperation.start();
    }

    /**
     * Draw the item.
     */
    protected void paint(final Graphics g, final int w, final int h) {
        g.setColor(VisualStyles.COLOR_FOREGROUND_DIM);
        g.setFont(FONT);

        final int y = V_SPACE * 2;
        final int fontOffset = FONT.getHeight() / 2;
        final int vote = item.getVote();
        final int leftX = H_SPACE * 2;
        final int rightX = w - 2 * H_SPACE;

        g.drawImage(vote == -1 ? voteDownActiveImage : voteDownImage, leftX, y,
            Graphics.TOP | Graphics.LEFT);
        g.drawImage(vote == 1 ? voteUpActiveImage : voteUpImage, rightX, y,
            Graphics.TOP | Graphics.RIGHT);
        g.drawString("Vote down", leftX + voteImageWidth + H_SPACE, y
            + fontOffset, Graphics.TOP | Graphics.LEFT);
        g.drawString("Vote up", rightX - voteImageWidth - H_SPACE, y
            + fontOffset, Graphics.TOP | Graphics.RIGHT);
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight,
        int[] visRect_inout) {
        return false;
    }
}
