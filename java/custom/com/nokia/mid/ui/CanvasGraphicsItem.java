package com.nokia.mid.ui;

import com.nokia.mid.ui.CanvasItem;
import javax.microedition.lcdui.Graphics;

public abstract class CanvasGraphicsItem extends CanvasItem {
    public CanvasGraphicsItem(int width, int height) throws IllegalArgumentException {
        System.out.println("CanvasGraphicsItem::CanvasGraphicsItem(int,int) not implemented");
    }

    // Set the parent object of this CanvasItem.
    public void setParent(Object theParent) {
        throw new RuntimeException("CanvasGraphicsItem::setParent(Object) not implemented");
    }

    // Renders the CanvasGraphicsItem.
    protected abstract void paint(Graphics g);

    // Requests a repaint for the entire CanvasGraphicsItem.
    public void repaint() {
        throw new RuntimeException("CanvasGraphicsItem::repaint() not implemented");
    }

    // Requests a repaint for the specified region of the CanvasGraphicsItem.
    public void repaint(int x, int y, int width, int height) {
        throw new RuntimeException("CanvasGraphicsItem::repaint(int,int,int,int) not implemented");
    }

    // Gets the content's width in pixels.
    public int getContentWidth() {
        throw new RuntimeException("CanvasGraphicsItem::getContentWidth() not implemented");
    }

    // Gets the content's height in pixels.
    public int getContentHeight() {
        throw new RuntimeException("CanvasGraphicsItem::getContentHeight() not implemented");
    }
}

