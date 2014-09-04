/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.view;

import com.nokia.example.rlinks.Main;
import com.nokia.example.rlinks.model.Voteable;
import com.nokia.example.rlinks.network.operation.CommentPostOperation;
import com.nokia.example.rlinks.network.operation.CommentPostOperation.PostCommentListener;
import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.view.item.TextItem;
import com.nokia.example.rlinks.view.item.VoteItem;
import com.nokia.example.rlinks.view.item.VoteItem.VoteListener;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

/**
 * View for voting on and replying to a comment.
 *
 * This view shows three items by default:
 * - a "Replying to <author name>:" label
 * - a truncated preview of the item replying to
 * - VoteItem displaying vote up / down controls
 *
 * It also provides a 'Reply' command which displays a TextBox text input
 * that can be used
 */
public class CommentDetailsView
    extends BaseFormView
    implements PostCommentListener {

    private static final int DESCRIPTION_MAX_LENGTH = 150;
    
    private final Command replyCommand = new Command("Reply", Command.SCREEN, 0);

    private final CommentDetailsBackListener commentBackListener;
    private final VoteListener voteListener;
    private final Voteable item;
    private final String title;
    private final String description;

    private CommentPostOperation replyOperation;

    private TextBox replyView;

    public static interface CommentDetailsBackListener {
        public void backCommanded(boolean commentAdded);
    }

    /**
     * Create a CommentDetailsView.
     *
     * @param item A Comment or Link whose details to show
     * @param backListener Listener to signal of back presses
     * @param voteListener Listener to signal of voting results
     */
    public CommentDetailsView(Voteable item, CommentDetailsBackListener backListener, VoteListener voteListener) {
        super("Comment", new Item[] {});

        this.item = item;
        this.title = "Replying to " + item.getAuthor() + ":";
        this.description = "\"" + truncate(item.getText()) + "\"";
        this.commentBackListener = backListener;
        this.voteListener = voteListener;

        setupCommands();
    }

    protected final void setupCommands() {
        addCommand(backCommand);
        addCommand(replyCommand);
        setupLoginCommands();
    }

    private String truncate(String text) {
        if (text.length() > DESCRIPTION_MAX_LENGTH) {
            return text.substring(0, DESCRIPTION_MAX_LENGTH) + "...";
        }
        return text;
    }

    /**
     * Show the view contents.
     */
    public void show() {
        if (size() > 0) {
            return;
        }

        addLoginStatusItem();
        append(new TextItem(title, getWidth(), VisualStyles.MEDIUM_BOLD_FONT, this));
        append(new TextItem(description, getWidth(), VisualStyles.MEDIUM_FONT, this));
        append(new VoteItem(item, getWidth(), voteListener, this, this));
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            commentBackListener.backCommanded(false);
        }
        else if (command == replyCommand) {
            // Require logged-in user to access the Reply view
            if (!session.isLoggedIn()) {
                showLoginRequiredMessage();
                return;
            }
            showReplyView();
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
     * Show the 'Reply' view (a full-screen TextBox).
     */
    private void showReplyView() {
        final Command cancelCommand = new Command("Cancel", Command.BACK, 0);
        final Command sendCommand = new Command("Send", Command.OK, 0);

        replyView = new TextBox("Reply", null, 2000, TextField.ANY);
        replyView.addCommand(cancelCommand);
        replyView.addCommand(sendCommand);

        replyView.setCommandListener(new CommandListener() {
            public void commandAction(Command command, Displayable d) {
                // Cancel and return back to previous view
                if (command == cancelCommand) {
                    setDisplay(self);
                }
                // Submit command
                else if (command == sendCommand) {
                    String modhash = session.getModhash();
                    replyOperation = new CommentPostOperation(
                        item.getName(),
                        replyView.getString(),
                        modhash,
                        (PostCommentListener) self
                    );
                    replyOperation.start();
                }
            }
        });
        setDisplay(replyView);
    }

    public void commentingSucceeded(String thingName, String text) {
        commentBackListener.backCommanded(true);
    }

    public void commentingFailed(String thingName, String text) {
        Main.getInstance().showAlertMessage(
            "Not sent",
            "The comment could not be sent. Please try again.",
            AlertType.INFO
        );
    }
}
