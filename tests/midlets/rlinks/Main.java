/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks;

import com.nokia.example.rlinks.model.LinkThing;
import com.nokia.example.rlinks.view.CommentsView;
import com.nokia.example.rlinks.view.item.LinkItem.LinkSelectionListener;
import com.nokia.example.rlinks.view.LinksView;
import com.nokia.example.rlinks.view.BaseFormView;
import com.nokia.example.rlinks.view.BaseFormView.BackCommandListener;
import com.nokia.example.rlinks.view.BaseFormView.CategorySelectionListener;
import com.nokia.example.rlinks.view.ViewCache;
import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;

/**
 * The main application class.
 *
 * Contains basic control flow for switching between views.
 */
public class Main
    extends MIDlet
    implements BackCommandListener, LinkSelectionListener, CategorySelectionListener {

    private static Main self;

    private Display display = null;    
    private LinksView previousLinksView;
    private ViewCache viewCache = new ViewCache();
    private SessionManager session = SessionManager.getInstance();

    /**
     * @return MIDlet object
     */
    public static Main getInstance() {
        return self;
    }

    public static void executeInUIThread(Runnable r) {
        Main.getInstance().display.callSerially(r);
    }

    public void startApp() {
        if (display != null) {
            return;
        }

        self = this;
        display = Display.getDisplay(this);

        showLinksView(session.getCategory());
    }

    public void showView(BaseFormView view) {
        display.setCurrent(view);
        view.show();
    }

    /**
     * Show a view with links for the given category.
     *
     * @param category Name of category, or <em>null</em> to show popular links
     */
    private void showLinksView(String category) {
        final String cacheKey = category == null ? "frontpage" : category;
        final LinksView linksView;
        if (viewCache.contains(cacheKey)) {
            linksView = (LinksView) viewCache.get(cacheKey);
        } else {
            linksView = new LinksView(category, this, this);
            viewCache.put(cacheKey, linksView);
        }
        previousLinksView = linksView;
        showView(linksView);
    }

    /**
     * Show comments for a given reddit Link (as selected in the Links view).
     *
     * @param link A reddit link item whose comments to show
     */
    private void showCommentsView(LinkThing link) {
        CommentsView commentsView;
        if (viewCache.contains(link)) {
            commentsView = (CommentsView) viewCache.get(link);
        } else {
            commentsView = new CommentsView(link, this);
            viewCache.put(link, commentsView);
        }
        showView(commentsView);
    }

    /**
     * Show an alert message on the screen.
     *
     * @param title
     * @param alertText
     * @param type
     */
    public final void showAlertMessage(String title, String alertText, AlertType type) {
        Alert alert = new Alert(title, alertText, null, type);
        display.setCurrent(alert, display.getCurrent());
    }

    /**
     * A handler for a category change.
     *
     * @param category Name of selected category
     */
    public void categorySelected(String category) {
        session.setCategory(category);
        showLinksView(category);
    }

    /**
     * Handler for a link selection.
     *
     * @param link
     */
    public void linkSelected(LinkThing link) {
        showCommentsView(link);
    }

    /**
     * Handler for 'Back' option.
     */
    public void backCommanded() {
        showView(previousLinksView);
    }

    /**
     * Handler for 'Exit' option.
     */
    public void onExitCommanded() {
        destroyApp(true);
        notifyDestroyed();
    }

    /**
     * @see MIDlet#pauseApp() 
     */
    public void pauseApp() {
    }

    /**
     * @see MIDlet#destroyApp(boolean) 
     */
    public void destroyApp(boolean unconditional) {
        display = null;
    }

    /**
     * @return name of the MIDlet.
     */
    public String getName() {
        return getAppProperty("MIDlet-Name");
    }

    /**
     * @return vendor of the MIDlet.
     */
    public String getVendor() {
        return getAppProperty("MIDlet-Vendor");
    }

    /**
     * @return version of the MIDlet.
     */
    public String getVersion() {
        return getAppProperty("MIDlet-Version");
    }

    /*
     * Check whether TestMode has been set on in JAD or Manifest.
     */
    public boolean isInTestMode() {
        return "on".equalsIgnoreCase(getAppProperty("TestMode"));
    }
}
