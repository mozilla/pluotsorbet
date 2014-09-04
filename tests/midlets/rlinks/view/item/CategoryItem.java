/**
* Copyright (c) 2012-2013 Nokia Corporation. All rights reserved.
* Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
* Oracle and Java are trademarks or registered trademarks of Oracle and/or its
* affiliates. Other product and company names mentioned herein may be trademarks
* or trade names of their respective owners. 
* See LICENSE.TXT for license information.
*/

package com.nokia.example.rlinks.view.item;

import com.nokia.example.rlinks.VisualStyles;
import com.nokia.example.rlinks.util.TouchChecker;
import com.nokia.example.rlinks.view.BaseFormView.CategorySelectionListener;
import com.nokia.mid.ui.LCDUIUtil;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * A custom view item for representing a category.
 */
public class CategoryItem
    extends CustomItem {

    private static final String DEFAULT_CATEGORY_NAME = "Popular (default)";

    private static final int H_SPACE = VisualStyles.CATEGORY_H_SPACE;
    private static final int V_SPACE = VisualStyles.CATEGORY_V_SPACE;
    private static final Font FONT = VisualStyles.LARGE_FONT;
    private static final Font FONT_SELECTED = VisualStyles.LARGE_BOLD_FONT;

    private final int height;
    private final int width;
    private final int preferredWidth;

    private final CategorySelectionListener listener;
    private final String category;
    private boolean selected;

    /**
     * Create a CategoryItem.
     *
     * @param category Category name represented by this item
     * @param preferredWidth Preferred width
     * @param listener Listener to signal of category selections
     */
    public CategoryItem(String category, int preferredWidth, CategorySelectionListener listener) {
        super(null);

        this.category = category;
        this.preferredWidth = preferredWidth;
        this.listener = listener;
        this.width = preferredWidth;
        this.height = getPrefContentHeight(preferredWidth);

        if (TouchChecker.DIRECT_TOUCH_SUPPORTED) {
            LCDUIUtil.setObjectTrait(this, "nokia.ui.s40.item.direct_touch", new Boolean(true));
        }
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    protected int getMinContentWidth() {
        return width;
    }

    protected int getMinContentHeight() {
        return height;
    }

    protected int getPrefContentWidth(int height) {
        return preferredWidth;
    }

    protected int getPrefContentHeight(int width) {
        return
            V_SPACE +
            FONT.getHeight() +
            V_SPACE;
    }

    /**
     * Draw the item.
     */
    protected void paint(final Graphics g, final int w, final int h) {
        final String displayCategory =
            category != null ? category : DEFAULT_CATEGORY_NAME;
        
        g.setFont(selected ? FONT_SELECTED : FONT);
        g.setColor(VisualStyles.COLOR_FOREGROUND);
        g.drawString(displayCategory, H_SPACE, V_SPACE, Graphics.TOP | Graphics.LEFT);
    }

    protected void pointerReleased(int x, int y) {
        super.pointerReleased(x, y);

        listener.categorySelected(category);
    }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
        return false;
    }
}
