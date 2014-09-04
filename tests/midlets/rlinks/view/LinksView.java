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
import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.network.HttpClient;
import com.nokia.example.rlinks.network.HttpOperation;
import com.nokia.example.rlinks.network.operation.LinksLoadOperation;
import com.nokia.example.rlinks.network.operation.LinksLoadOperation.LoadLinksListener;
import com.nokia.example.rlinks.view.item.LinkItem;
import com.nokia.example.rlinks.view.item.LinkItem.LinkSelectionListener;
import com.nokia.example.rlinks.view.item.LoaderItem;
import com.nokia.example.rlinks.view.item.TextItem;
import java.util.Hashtable;
import java.util.Vector;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.Spacer;

/**
 * The LinksView is the default view of the application. It lists all links
 * under a certain reddit (category).
 */
public class LinksView
    extends BaseFormView
    implements LoadLinksListener {

    private final Command exitCommand = new Command("Exit", Command.EXIT, 0);
    private final Command categoryCommand = new Command("Category", Command.SCREEN, 0);    
        
    private final String category;
    private final CategorySelectionListener categoryListener;
    private final LinkSelectionListener linkListener;
    private final Hashtable imageCache = new Hashtable();

    private HttpOperation operation;

    private int loadingItemIndex = -1;    
    private Vector linkListItems;
    private static boolean firstShow = true;

    /**
     * Create a LinksView.
     *
     * @param category Category to show links for
     * @param categoryListener Listener to signal of category change
     * @param linkListener Listener to signal of link being selected
     */
    public LinksView(String category, CategorySelectionListener categoryListener, LinkSelectionListener linkListener) {
        super("RLinks", new Item[] {});

        this.category = category;
        this.categoryListener = categoryListener;
        this.linkListener = linkListener;

        if (firstShow) {
            append(new Spacer(1, 1));
        }
        setupCommands();
    }

    protected final void setupCommands() {
        // Only include 'Exit' on the front page, otherwise use 'Back'
        addCommand(category == null ? exitCommand : backCommand);
        addCommand(categoryCommand);
        addCommand(refreshCommand);
        addCommand(aboutCommand);

        setupLoginCommands();
    }

    /**
     * Show the view.
     */
    public void show() {
        if (firstShow) {
            if (!requireNetworkAccess()) {
                return;
            }
            deleteAll();
            firstShow = false;
        }

        setupCommands();
        setTitle(category == null ? "RLinks - Popular": "/r/" + category);
        addLoginStatusItem();
        loadLinks();
    }

    /**
     * Refresh the view.
     */
    private void refresh() {
        if (operation != null && !operation.isFinished()) {
            return;
        }

        operation = null;
        deleteAll();
        show();
    }

    /**
     * Load links to be shown in the view.
     */
    private void loadLinks() {       
        // Do not reload if already loaded, or if in process
        if (HttpOperation.reloadNeeded(operation)) {
            // Add 'Loading' item
            loadingItemIndex = append(new LoaderItem());
            operation = new LinksLoadOperation(category, this);
            operation.start();
        }
    }

   /**
     * Handle displaying incoming links.
     * 
     * @param links Vector of LinkThing items received
     */
    public synchronized void linksReceived(final Vector links) {
        if (links == null) {
            Main.executeInUIThread(new Runnable() {
                public void run() {
                    delete(loadingItemIndex);
                    showNetworkError();
                }
            });
            return;
        }

        linkListItems = createLinkItems(links);

        Main.executeInUIThread(new Runnable() {
            public void run() {
                for (int i = 0, len = links.size(); i < len; i++) {
                    append((LinkItem) linkListItems.elementAt(i));    
                }
                delete(loadingItemIndex);
            }
        });
    }

    /**
     * Create link items from a Vector of links.
     *
     * @param links LinkThing items populated from server data
     * @return Vector of LinkItem objects
     */
    private Vector createLinkItems(final Vector links) {
        final boolean showSubreddit = category == null;
        LinkThing link;
        LinkItem linkItem;
        Vector items = new Vector();

        for (int i = 0, len = links.size(); i < len; i++) {
            link = (LinkThing) links.elementAt(i);
            linkItem = new LinkItem(link, getWidth(), showSubreddit, linkListener, imageCache, this);
            items.addElement(linkItem);
        }
        return items;
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            // Going back equals going back to the first view (popular page)
            abortPendingOperations();
            categoryListener.categorySelected(null);
        }
        else if (command == exitCommand) {
            Main.getInstance().onExitCommanded();
        }
        else if (command == categoryCommand) {
            showCategoryView();
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
     * Abort any pending loading operation.
     */
    private void abortPendingOperations() {
        if (operation != null && !operation.isFinished()) {
            operation.abort();
            delete(loadingItemIndex);
        }
    }

    /**
     * Show the category selection view.
     */
    private void showCategoryView() {
        final CategorySelectView csv =
            new CategorySelectView(category, categoryListener, defaultBackListener);
        setDisplay(csv);
        csv.show();
    }

    private boolean requireNetworkAccess() {
        if (HttpClient.isAllowed()) {
            return true;
        }

        setTitle("Network required");
        append(
            new TextItem("Please restart and allow network access.",
            getWidth(),
            VisualStyles.LARGE_BOLD_FONT, this)
        );

        removeCommand(categoryCommand);
        addCommand(exitCommand);
        return false;
    }

}
