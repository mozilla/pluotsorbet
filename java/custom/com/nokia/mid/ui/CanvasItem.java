package com.nokia.mid.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Display;

public abstract class CanvasItem {
    public static final int SCALE_NOT_ALLOWED = 0;
    public static final int SCALE_NEAREST = 1;
    public static final int SCALE_AVERAGE = 2;

    Object parent = null;

    native private void attachNativeImpl();
    native private void detachNativeImpl();

    // Set the parent object of this CanvasItem.
    public void setParent(Object theParent) {
        if (theParent != null && parent != null && theParent != parent) {
            throw new IllegalArgumentException("CanvasItem already associated with parent");
        }

        parent = theParent;

        // We need to attach/detach the native implementation at some point.
        // We initially considered init/finalize, but our VM doesn't appear
        // to ever call finalize.  So we do it here, attaching the native impl.
        // when setParent is called with an object reference, then detaching it
        // when setParent is called with a null value; which appears to be
        // expected usage when creating/destroying CanvasItem objects.
        if (parent != null) {
            attachNativeImpl();
        } else {
            detachNativeImpl();
        }
    }

    // Sets the size of this CanvasItem in pixels.
    native public void setSize(int width, int height);

    // Sets the size of this Window in pixels and resets the current anchor position.
    public void setSize(int x, int y, int width, int height) {
        throw new RuntimeException("CanvasItem::setSize(int,int,int,int) not implemented");
    }

    // Gets the width of this CanvasItem in pixels.
    native public int getWidth();

    // Gets the height of this CanvasItem in pixels.
    native public int getHeight();

    // Sets the rendering position of this CanvasItem.
    native public void setPosition0(int x, int y);

    public void setPosition(int x, int y) {
        setPosition0(x, y);
    }

    // Sets the Z-position, or the elevation, of the item.
    public void setZPosition(int z) throws IllegalArgumentException {
        throw new RuntimeException("CanvasItem::setZPosition(int) not implemented");
    }

    // Gets the rendering position of this CanvasItem.
    native public int getPositionX();

    // Gets the rendering position of this CanvasItem.
    native public int getPositionY();

    // Returns the Z-position, or the elevation, of the item.
    public int getZPosition() {
        throw new RuntimeException("CanvasItem::getZPosition() not implemented");
    }

    // Sets the visibility value of CanvasItem.
    native public void setVisible(boolean vis);

    // Returns the current visibility of this CanvasItem.
    native public boolean isVisible();

    // Scales CanvasItem to the specified size as per current scaling mode.
    public void scale(int width, int height) {
        throw new RuntimeException("CanvasItem::scale(int,int) not implemented");
    }

    // Scales CanvasItem to the specified size as per current scaling mode and resets the current anchor position.
    public void scale(int x, int y, int width, int height) {
        throw new RuntimeException("CanvasItem::scale(int,int,int,int) not implemented");
    }

    // This method is called to define how size change / scaling should be performed.
    public void setScalingMode(int scaleMode, boolean aspectRatioPreserved, int anchor) {
        throw new RuntimeException("CanvasItem::setZPosition(int) not implemented");
    }

    // Gets the current parent of this CanvasItem.
    public Object getParent() {
        return parent;
    }

    // Tests whether anchor has valid values
    protected static boolean isValidImageAnchor(int anchor) {
        throw new RuntimeException("CanvasItem::isValidImageAnchor(int) not implemented");
    }
}

