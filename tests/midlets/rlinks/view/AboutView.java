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
import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.view.item.TextItem;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

/**
 * A view showing information about the application.
 */
public class AboutView
    extends BaseFormView {

    private static final String ABOUT_TEXT =
        "An example application demonstrating " +
        "how to read, comment and vote on Reddit posts.\n\n" +
        "The application project is hosted at:\n" +
        "projects.developer.nokia.com/rlinks";

    private final BackCommandListener backListener;

    public AboutView(BackCommandListener backListener) {
        super("About", new Item[] {});

        this.backListener = backListener;
        Main main = Main.getInstance();
        append(new TextItem(main.getName() + "\nversion " + main.getVersion() 
            + "\nby " + main.getVendor(), getWidth(), 
            VisualStyles.LARGE_BOLD_FONT, this));
        append(new TextItem(ABOUT_TEXT, getWidth(), VisualStyles.MEDIUM_FONT, this));
    }

    public void show() {
        setupCommands();
    }

    protected void setupCommands() {
        addCommand(backCommand);
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            backListener.backCommanded();
        }
    }
}
