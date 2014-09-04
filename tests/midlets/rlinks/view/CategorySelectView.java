/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.view;

import com.nokia.example.rlinks.view.item.CategoryItem;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Item;

/**
 * A view for selecting active category (subreddit).
 */
public class CategorySelectView
    extends BaseFormView {

    /**
     * A prepopulated list of common subreddits.
     * There are tens of thousands in total.
     */
    private static final String[] CATEGORIES = {
        "AdviceAnimals",
        "AskReddit",
        "askscience",
        "aww",
        "bestof",
        "funny",
        "gaming",
        "IAmA",
        "Music",
        "nokia",
        "pics",
        "politics",
        "science",
        "technology",
        "todayilearned",
        "worldnews",
        "WTF"
    };

    private final CategorySelectionListener categoryListener;
    private final BackCommandListener backListener;
    private final String currentCategory;

    /**
     * Create a new view.
     *
     * @param currentCategory Currently selected category (null if none)
     * @param categoryListener Listener to signal about category changes
     * @param backListener Listener to signal about back button presses
     */
    public CategorySelectView(String currentCategory, CategorySelectionListener categoryListener, BackCommandListener backListener) {
        super("Select category", new Item[]{});
        
        this.currentCategory = currentCategory;
        this.categoryListener = categoryListener;
        this.backListener = backListener;
        
        setupCommands();
    }

    protected final void setupCommands() {
        addCommand(backCommand);        
    }

    public void show() {
        final int width = getWidth();
        CategoryItem item;

        // Add default item 'Top links'
        item = new CategoryItem(null, width, categoryListener);
        if (currentCategory == null) {
            item.setSelected(true);
        }
        append(item);

        // Add other items for categories
        for (int i = 0, len = CATEGORIES.length; i < len; i++) {
            item = new CategoryItem(CATEGORIES[i], width, categoryListener);
            if (CATEGORIES[i].equals(currentCategory)) {
                item.setSelected(true);
            }
            append(item);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (command == backCommand) {
            backListener.backCommanded();
        }
    }
}
