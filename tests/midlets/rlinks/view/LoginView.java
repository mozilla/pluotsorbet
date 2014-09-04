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
import com.nokia.example.rlinks.network.HttpOperation;
import com.nokia.example.rlinks.network.operation.LoginOperation;
import com.nokia.example.rlinks.network.operation.LoginOperation.LoginListener;
import com.nokia.example.rlinks.view.item.AbstractCustomItem;
import com.nokia.example.rlinks.view.item.LoaderItem;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.ItemCommandListener;
import javax.microedition.lcdui.StringItem;
import javax.microedition.lcdui.TextField;

/**
 * View for logging in.
 */
public class LoginView
    extends BaseFormView
    implements LoginListener, ItemCommandListener {

    private final Command submitCommand = new Command("Submit", Command.SCREEN, 0);
    private final BackCommandListener backListener;
    private final LoginOperation.LoginListener loginListener;
    private final TextField username;
    private final TextField password;
    private final StringItem submit;
    
    private HttpOperation loginOperation;

    /**
     * Create a LoginView.
     * 
     * @param loginListener Listener to signal of login events
     * @param backListener Listener to signal of back button presses
     */
    public LoginView(LoginListener loginListener, BackCommandListener backListener) {
        super("Login", new Item[] {});

        this.backListener = backListener;
        this.loginListener = loginListener;
        this.username = new TextField("Username", session.getUsername(), 20, TextField.NON_PREDICTIVE & ~TextField.INITIAL_CAPS_WORD);
        this.password = new TextField("Password", null, 40, TextField.PASSWORD);
        this.submit = new StringItem(null, "Submit", StringItem.BUTTON);
        submit.setDefaultCommand(submitCommand);
        submit.setItemCommandListener(this);

        setupCommands();
    }

    protected final void setupCommands() {
        addCommand(backCommand);
        addCommand(submitCommand);
    }

    public void show() {
        deleteAll();
        append(username);
        append(password);
        if (AbstractCustomItem.isFTDevice) {
            append(submit);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            backListener.backCommanded();
        } else if (command == submitCommand) {
            submitLogin();
        }
    }

    
    public void commandAction(Command command, Item item) {
        commandAction(command, (Displayable) null);
    }

    /**
     * Submit a login request.
     */
    private void submitLogin() {
        if (loginOperation != null && !loginOperation.isFinished()) {
            return;
        }

        final String user = username.getString();
        final String pass = password.getString();
        if (user == null || pass == null) {
            return;
        }

        loginOperation = new LoginOperation(user.toLowerCase(), pass, this);
        loginOperation.start();

        deleteAll();
        append(new LoaderItem());
    }

    /**
     * Handle and signal a successful login.
     */
    public void loginSucceeded(String username, String modhash) {
        session.setLoggedIn(username, modhash);
        loginListener.loginSucceeded(username, modhash);
    }

    /**
     * Handle and signal a failed login.
     */
    public void loginFailed(String reason) {
        // Show an error message and refresh the view
        final String message =
            "Login failed" +
            (reason != null ? " (" + reason + ")" : "") +
            ". Please try again.";

        Main.getInstance().showAlertMessage(
            "Login failed",
            message,
            AlertType.INFO
        );

        show();
        loginListener.loginFailed(reason);
    }
}
