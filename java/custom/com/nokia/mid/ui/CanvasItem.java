package com.nokia.mid.ui;

public abstract class CanvasItem {
    public static final int SCALE_NOT_ALLOWED = 0;
    public static final int SCALE_NEAREST = 1;
    public static final int SCALE_AVERAGE = 2;

    // Set the parent object of this CanvasItem.
    public void setParent(Object theParent) {
        throw new RuntimeException("CanvasItem::setParent(Object) not implemented");
    }

    // Sets the size of this Window in pixels.
    public void setSize(int width, int height) throws IllegalArgumentException {
        throw new RuntimeException("CanvasItem::setSize(int,int) not implemented");
    }

    // Sets the size of this Window in pixels and resets the current anchor position.
    public void setSize(int x, int y, int width, int height) {
        throw new RuntimeException("CanvasItem::setSize(int,int,int,int) not implemented");
    }

    // Gets the height of this CanvasItem in pixels.
    public int getHeight() {
        throw new RuntimeException("CanvasItem::getHeight() not implemented");
    }

    // Gets the width of this CanvasItem in pixels.
    public int getWidth() {
        throw new RuntimeException("CanvasItem::getWidth() not implemented");
    }

    // Sets the rendering position of this CanvasItem.
    public void setPosition(int x, int y) {
        throw new RuntimeException("CanvasItem::setPosition(int,int) not implemented");
    }

    // Sets the Z-position, or the elevation, of the item.
    public void setZPosition(int z) throws IllegalArgumentException {
        throw new RuntimeException("CanvasItem::setZPosition(int) not implemented");
    }

    // Gets the rendering position of this CanvasItem.
    public int getPositionX() {
        throw new RuntimeException("CanvasItem::getPositionX() not implemented");
    }

    // Gets the rendering position of this CanvasItem.
    public int getPositionY() {
        throw new RuntimeException("CanvasItem::getPositionY(int) not implemented");
    }

    // Returns the Z-position, or the elevation, of the item.
    public int getZPosition() {
        throw new RuntimeException("CanvasItem::getZPosition() not implemented");
    }

    // Sets the visibility value of CanvasItem.
    public void setVisible(boolean visible) {
        throw new RuntimeException("CanvasItem::setVisible(boolean) not implemented");
    }

    // Returns the current visibility of this CanvasItem.
    public boolean isVisible() {
        throw new RuntimeException("CanvasItem::isVisible() not implemented");
    }

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
        throw new RuntimeException("CanvasItem::getParent() not implemented");
    }

    // Tests whether anchor has valid values
    protected static boolean isValidImageAnchor(int anchor) {
        throw new RuntimeException("CanvasItem::isValidImageAnchor(int) not implemented");
    }
}

