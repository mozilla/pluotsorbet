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
import com.nokia.example.rlinks.network.operation.LoginOperation.LoginListener;
import com.nokia.example.rlinks.SessionManager;
import com.nokia.example.rlinks.view.item.LoginStatusItem;
import com.nokia.example.rlinks.view.item.LoginStatusItem.SelectionListener;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Item;

/**
 * A convenience base class for custom views based on Form.
 */
public abstract class BaseFormView
    extends Form
    implements CommandListener {

    private static final String LOGIN_REQUIRED_LABEL = "Login required";
    private static final String LOGIN_REQUIRED_TEXT = "You need to be "
        + "logged in to comment or vote. Do you want to log in?";
    private static final String LOGIN_REQUIRED_YES = "Yes";
    private static final String LOGIN_REQUIRED_NO = "No";
    
    protected final BaseFormView self = this;

    protected final Command backCommand = new Command("Back", Command.BACK, 0);
    protected final Command loginCommand = new Command("Login", Command.SCREEN, 1);
    protected final Command logoutCommand = new Command("Logout", Command.SCREEN, 1);
    protected final Command refreshCommand = new Command("Refresh", Command.SCREEN, 2);
    protected final Command aboutCommand = new Command("About", Command.SCREEN, 2);
    
    protected final SessionManager session = SessionManager.getInstance();

    protected final BackCommandListener defaultBackListener = new BackCommandListener() {
        public void backCommanded() {
            setDisplay(self);
            show();
        }
    };

    /**
     * Listener for category changes.
     */
    public static interface CategorySelectionListener {
        public void categorySelected(String category);
    }

    /**
     * Listener for Back button presses.
     */
    public static interface BackCommandListener {
        void backCommanded();
    }

    protected static void setDisplay(Displayable display) {
        Display.getDisplay(Main.getInstance()).setCurrent(display);
    }

    protected static void setItem(Item item) {
        Display.getDisplay(Main.getInstance()).setCurrentItem(item);
    }

    protected static void showNetworkError() {
        Main.getInstance().showAlertMessage(
            "Network error", "Couldn't load data. Please try again.",
            AlertType.INFO
        );
    }

    public BaseFormView(String title, Item[] items) {
        super(title, items);
        setCommandListener(this);        
    }

    /**
     * Any initialization a view needs to do when it's about to be shown.
     */
    public abstract void show();

    protected abstract void setupCommands();

    public abstract void commandAction(Command command, Displayable displayable);

    protected void showAboutView() {
        final AboutView aboutView = new AboutView(defaultBackListener);
        setDisplay(aboutView);
        aboutView.show();
    }

    /**
     * Show a login required message on the screen.
     *
     * @param title
     * @param alertText
     * @param type
     */
    public final void showLoginRequiredMessage() {
        Alert alert = new Alert(LOGIN_REQUIRED_LABEL, LOGIN_REQUIRED_TEXT, 
            null, AlertType.INFO);
        alert.addCommand(new Command(LOGIN_REQUIRED_YES, Command.OK, 0));
        alert.addCommand(new Command(LOGIN_REQUIRED_NO, Command.CANCEL, 0));
        alert.setCommandListener(new CommandListener() {

            public void commandAction(Command c, Displayable d) {
                if(c.getCommandType() == Command.OK) {
                    showLoginView();
                } else {
                    setDisplay(self);
                }
            }
        });
        setDisplay(alert);
    }

    /**
     * Show the Login view.
     */
    protected void showLoginView() {
        final LoginView lv = new LoginView(new LoginListener() {
            public void loginSucceeded(String username, String modhash) {
                // Refresh commands to reflect the current login status
                setDisplay(self);
                setupCommands();
            }

            public void loginFailed(String reason) {}
        }, defaultBackListener);

        setDisplay(lv);
        lv.show();
    }

    protected void setupLoginCommands() {
        if (session.isLoggedIn()) {
            removeCommand(loginCommand);
            addCommand(logoutCommand);
        } else {
            removeCommand(logoutCommand);
            addCommand(loginCommand);
        }
    }

    /**
     * Add item to show current Login status. Tapping on it will also show
     * the login view.
     */
    protected void addLoginStatusItem() {
        // The login status item is the first item in the view. If it's
        // not, then let's not add it (again).
        if (size() > 0) {
            return;
        }
        
        append(new LoginStatusItem(getWidth(), new SelectionListener() {
            public void itemSelected() {
                if (!session.isLoggedIn()) {
                    showLoginView();
                }
            }
        }, this));
    }
}
