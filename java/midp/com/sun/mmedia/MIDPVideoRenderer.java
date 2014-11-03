/*
 * Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License version
 * 2 only, as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * General Public License version 2 for more details (a copy is
 * included at /legal/license.txt).
 * 
 * You should have received a copy of the GNU General Public License
 * version 2 along with this work; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA
 * 
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 * Clara, CA 95054 or visit www.sun.com if you need additional
 * information or have any questions.
 */
package com.sun.mmedia;

import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;


import javax.microedition.media.Control;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.MediaException;
import javax.microedition.media.control.VideoControl;

/**
 * VideoControl implementation for MIDP
 */
public final class MIDPVideoRenderer extends VideoRenderer
    implements VideoControl, MIDPVideoPainter {

    /** If the application requests an Item */
    private MMItem mmItem;
    /** LCDUI notifications registration */
    private MMHelper mmHelper = null;
    /** If the application requests to draw in a Canvas */
    private Canvas canvas;
    /** Full screen mode flag */
    private boolean fsmode;
    /** Is the player closed */
    private boolean closed;
    /** The display mode */
    private int mode = -1;
    /** Container visible flag. True if the Canvas is visible */
    private boolean cvis;
    /** Application specified visibility flag. True if setVisible(true) */
    private boolean pvis;
    /** Player which is being controlled */
    private BasicPlayer player;

    /** Display X */
    private int dx, tmpdx;
    /** Display Y */
    private int dy, tmpdy;
    /** Display Width */
    private int dw, tmpdw;
    /** Display Height */
    private int dh, tmpdh;

    /** Source width */
    private int videoWidth;
    /** Source height */
    private int videoHeight;

    /** To check the frame rate */
    private static final boolean TRACE_FRAMERATE = false;
    /** To check the frame rate */
    private int frameCount;
    /** To check the frame rate */
    private long frameStartTime = 0;

    public static final String SNAPSHOT_RGB888 = "rgb888";
    public static final String SNAPSHOT_BGR888 = "bgr888";
    public static final String SNAPSHOT_RGB565 = "rgb565";
    public static final String SNAPSHOT_RGB555 = "rgb555";
    public static final String SNAPSHOT_ENCODINGS = SNAPSHOT_RGB888 + " "
            + SNAPSHOT_BGR888 + " " + SNAPSHOT_RGB565 + " " + SNAPSHOT_RGB555;

    /** used to protect dx, dy, dw, dh set & read */
    private Object dispBoundsLock = new Object();
    
    /*
     * Locator string used to open the Player instance which this VideoControl is assoicated with. 
     */
    private String locatorString;

    /****************************************************************
     * VideoControl implementation
     ****************************************************************/

    MIDPVideoRenderer(Player p) {
        if (p instanceof BasicPlayer) {
            this.player = (BasicPlayer)p;
            locatorString = player.getLocator();
        } else {
            System.err.println("video renderer can't work with Players of this class: " + p.toString());
        }
    }

    private void checkState() {
        if (mode == -1)
            throw new IllegalStateException("initDisplayMode not called yet");
    }

    public Object initDisplayMode(int mode, Object container) {
        if (this.mode != -1)
            throw new IllegalStateException("mode is already set");
        
        if (mode == USE_DIRECT_VIDEO) {
            if (!(container instanceof Canvas))
                throw new IllegalArgumentException(
                    "container needs to be a Canvas for USE_DIRECT_VIDEO mode");
            
            if (mmHelper == null) {
                mmHelper = MMHelper.getMMHelper();
                if (mmHelper == null)
                    throw new IllegalArgumentException(
                            "unable to set USE_DIRECT_VIDEO mode");
            }

            this.mode = mode;
            fsmode = false;
            cvis = true;
            canvas = (Canvas) container;
            mmHelper.registerPlayer(canvas, this);
            setVisible(false); // By default video is not shown in USE_DIRECT_VIDEO mode
            return null;
            
        } else if (mode == USE_GUI_PRIMITIVE) {
            if (container != null && 
                (!(container instanceof String) ||
                 !(container.equals("javax.microedition.lcdui.Item"))))
                throw new IllegalArgumentException("container needs to be a javax.microedition.lcdui.Item for USE_GUI_PRIMITIVE mode");

            this.mode = mode;
            fsmode = false;
            cvis = true;
            mmItem = new MMItem();
            setVisible(true);
            return mmItem;
            
        } else {
            throw new IllegalArgumentException("unsupported mode");
        }
    }

    public void setDisplayLocation(int x, int y) {
        checkState();
        // Applicable only in USE_DIRECT_VIDEO mode
        if (mode == USE_DIRECT_VIDEO) {
            if (fsmode) { // Just store location in fullscreen mode
                synchronized (dispBoundsLock) {
                    tmpdx = x;
                    tmpdy = y;
                }
            } else {
                synchronized (dispBoundsLock) {
                    dx = x;
                    dy = y;
                }
                if (pvis && cvis)
                    canvas.repaint();
            }
        }
    }


    public int getDisplayX() {
        return dx;
    }
        
    public int getDisplayY() {
        return dy;
    }

    /**
     * Check for the image snapshot permission.
     *
     * @exception SecurityException if the permission is not
     *            allowed by this token
     */
    private void checkPermission() throws SecurityException {
        try {
            PermissionAccessor.checkPermissions(locatorString, PermissionAccessor.PERMISSION_SNAPSHOT);
    	} catch (InterruptedException e) {
    	    throw new SecurityException("Interrupted while trying to ask the user permission");
    	}
    }

    public void setVisible(boolean visible) {
        checkState();
        pvis = visible;
        if (canvas != null) // USE_DIRECT_VIDEO
            canvas.repaint();
        else if (mmItem != null) // USE_GUI_PRIMITIVE
            mmItem.refresh(false);
    }

    public void setDisplaySize(int width, int height)
        throws javax.microedition.media.MediaException {
        checkState();
        if (width < 1 || height < 1)
            throw new IllegalArgumentException("Invalid size");
        
        boolean sizeChanged = (dw != width || dh != height);
        
        if (fsmode) { // Just store sizes in fullscreen mode
            synchronized (dispBoundsLock) {
                tmpdw = width;
                tmpdh = height;
            }
        } else {
            synchronized (dispBoundsLock) {
                dw = width;
                dh = height;
            }
            scaleToDest();
            if (pvis)
                if (mmItem != null)
                    mmItem.refresh(true);
                else if (cvis)                   
                    canvas.repaint();
        }
        // Makes sense only if NOT in Full Screen mode
        if (sizeChanged && !fsmode)
            player.sendEvent(PlayerListener.SIZE_CHANGED, this);
    }

    public void setDisplayFullScreen(boolean fullScreenMode)
        throws javax.microedition.media.MediaException {
        checkState();
        if (fsmode != fullScreenMode) {
            fsmode = fullScreenMode;
            if (fsmode) { //switching from Normal to Full Screen
                synchronized (dispBoundsLock) {
                    tmpdx = dx;
                    tmpdy = dy;
                    tmpdw = dw;
                    tmpdh = dh;
                }
                if (mode == USE_DIRECT_VIDEO) {
                    canvas.setFullScreenMode(true);
                } else {
                    canvas = mmItem.toFullScreen(this, this);
                    if (canvas == null) {
                        // No owner or no display - thus invisible
                        // Do nothing, but simulate fullscreen (lock sizes - for compliance)
                        return;
                    }                        
                }
                synchronized (dispBoundsLock) {
                    dx = 0;
                    dy = 0;
                                        
                    // Keep aspect ratio
                    int scrw = canvas.getWidth();
                    int scrh = canvas.getHeight();
                    dw = scrh * videoWidth / videoHeight;
                    if (dw > scrw) {
                        dw = scrw;
                        dh = scrw * videoHeight / videoWidth;
                        dy = (scrh - dh) / 2;
                    } else {
                        dh = scrh;
                        dx = (scrw - dw) / 2;
                    }
                }
                scaleToDest();
                if (cvis)
                    canvas.repaint();

            } else { //switching from Full to Normal Screen
                synchronized (dispBoundsLock) {
                    dx = tmpdx;
                    dy = tmpdy;
                    dw = tmpdw;
                    dh = tmpdh;
                }
                scaleToDest();
                if (mode == USE_DIRECT_VIDEO) {
                    canvas.setFullScreenMode(false);
                    if (pvis && cvis)
                        canvas.repaint();
                } else {
                    mmItem.toNormal();
                    canvas = null;
                    if (pvis)
                        mmItem.refresh(false);
                }
            }
            player.sendEvent(PlayerListener.SIZE_CHANGED, this);
        }
    }

    public int getDisplayWidth() {
        checkState();
        return dw;
    }

    public int getDisplayHeight() {
        checkState();
        return dh;
    }
    
    public int getSourceWidth() {
        return videoWidth;
    }
        
    public int getSourceHeight() {
        return videoHeight;
    }

    public byte[] getSnapshot(String imageType)
        throws MediaException, SecurityException {
        checkState();
	checkPermission();
        /* REVISIT: Not currently supported.
         * Need to update  video.snapshot.encodings property accordingly
         *
        int format = 0, pixelsize = 0;
        if (imageType == null || imageType.equalsIgnoreCase(SNAPSHOT_RGB888)) {
            format = 1;
            pixelsize = 3;
        } else if (imageType.equalsIgnoreCase(SNAPSHOT_BGR888)) {
            format = 2;
            pixelsize = 3;
        } else if (imageType.equalsIgnoreCase(SNAPSHOT_RGB565)) {
            format = 3;
            pixelsize = 2;
        } else if (imageType.equalsIgnoreCase(SNAPSHOT_RGB555)) {
            format = 4;
            pixelsize = 2;
        } else */
            throw new MediaException("Image format " + imageType + " not supported");
        /*
        if (rgbData == null)
            throw new IllegalStateException("No image available");
        
        byte [] arr = new byte[pixelsize * rgbData.length];
        int idx = 0;
        switch (format) {
            case 1: // RGB888
                for (int i = 0; i < rgbData.length; i++) {
                    arr[idx++] = (byte)((rgbData[i] >> 16) & 0xFF);
                    arr[idx++] = (byte)((rgbData[i] >> 8) & 0xFF);
                    arr[idx++] = (byte)(rgbData[i] & 0xFF);
                }
                break;
            case 2: // BGR888
                for (int i = 0; i < rgbData.length; i++) {
                    arr[idx++] = (byte)((rgbData[i] >> 16) & 0xFF);
                    arr[idx++] = (byte)((rgbData[i] >> 8) & 0xFF);
                    arr[idx++] = (byte)(rgbData[i] & 0xFF);
                }
                break;
            case 3: // RGB565
                for (int i = 0; i < rgbData.length; i++) {
                    int r = (rgbData[i] >> 19) & 0x1F;
                    int g = (rgbData[i] >> 10) & 0x3F;
                    int b = (rgbData[i] >> 3) & 0x1F;
                    arr[idx++] = (byte)((r << 3) | (g >> 3));
                    arr[idx++] = (byte)((g << 5) | b);
                }
                break;
            case 4: // RGB555
                for (int i = 0; i < rgbData.length; i++) {
                    int r = (rgbData[i] >> 19) & 0x1F;
                    int g = (rgbData[i] >> 11) & 0x1F;
                    int b = (rgbData[i] >> 3) & 0x1F;
                    arr[idx++] = (byte)((r << 2) | (g >> 3));
                    arr[idx++] = (byte)((g << 5) | b);
                }
                break;
        }
        return arr; */
    }

    /*private int tryParam(String tok, String prop, int def) {
        if (tok.startsWith(prop)) {
            tok = tok.substring(prop.length(), tok.length());
            try {
                return Integer.parseInt(tok);
            } catch (NumberFormatException nfe) {
            }
        }
        return def;
    }*/
    
    /****************************************************************
     * Rendering interface
     ****************************************************************/
    
    //private int colorMode;
    //private boolean nativeRender;
    private boolean useAlpha;
    private int [] rgbData;
    private int [] scaledData;
    private boolean scaled;
    private volatile boolean painting; // to prevent deadlocks
    //private Image image;

    public Control getVideoControl() {
        return this;
    }
    
    public void initRendering(int mode, int width, int height) {
        //colorMode = mode & 0x7F; // mask out NATIVE_RENDER
        //nativeRender = (mode & NATIVE_RENDER) > 0;
        useAlpha = (mode & USE_ALPHA) > 0;
        if (width < 1 || height < 1)
            throw new IllegalArgumentException("Positive width and height expected");
        
        if ((mode & ~(USE_ALPHA | NATIVE_RENDER)) != XRGB888)
            throw new IllegalArgumentException("Only XRGBA888 mode supported");
        
        videoWidth = width;
        videoHeight = height;

        // Default display width and height
        synchronized (dispBoundsLock) {
            dw = videoWidth;
            dh = videoHeight;
        }
        
        rgbData = null;
        scaledData = null;
        scaled = false;
        painting = false;
        //image = null;
    }

    /**
     * Public render method
     */
    public void render(int[] colorData) {
        rgbData = colorData;
        scaleToDest();

        if (!pvis)
            return;
                
        if (canvas != null) {
            if (cvis) {
                canvas.repaint(dx, dy, dw, dh);
            }
        } else if (mmItem != null) {
            mmItem.refresh(false);
        }
    }
    
    /**
     * Public render method
     */
    public void render(byte[] colorData) {
        throw new IllegalStateException("Only 32 bit pixel format supported");
    }
    
    /**
     * Public render method
     */
    public void render(short[] colorData) {
        throw new IllegalStateException("Only 32 bit pixel format supported");
    }

    public void close() {
        if (!closed && canvas != null)
            mmHelper.unregisterPlayer(canvas, this);
        if (rgbData != null)
            synchronized (rgbData) {
                rgbData = null;
                scaledData = null;
            }
        //image = null;
        closed = true;
    }

    /**
     * Scales an input rgb image to the destination size.
     */
    private void scaleToDest() {
        int ldw = 0;
        int ldh = 0;
        synchronized (dispBoundsLock) {
            ldw = dw;
            ldh = dh;
        }
        if (rgbData != null)
            synchronized (rgbData) { // To avoid interference with close()
                scaled = ldw != videoWidth || ldh != videoHeight;
                if (scaled) {
                    if (scaledData == null || scaledData.length < ldw * ldh)
                        scaledData = new int[ldw * ldh];
                    // Scale using nearest neighbor
                    int dp = 0;
                    for (int y = 0; y < ldh; y++)
                        for (int x = 0; x < ldw; x++)
                            scaledData[dp++] = rgbData[((y * videoHeight) / ldh)
                                        * videoWidth + ((x * videoWidth) / ldw)];
                }
            }
    }

    /**
     * Scale an image to the destination size. This first gets the
     * pixels from the image and then uses the other scaleToDist()
     * to do the scaling.
     */
    /*private void scaleToDest(Image img) {
        if (rgbData == null)
            rgbData = new int[videoWidth * videoHeight];
        int width = img.getWidth();
        int height = img.getHeight();
        // REVISIT: width and height need to be stored...
        img.getRGB(rgbData, 0, videoWidth, 0, 0, width, height);
        scaleToDest();
    }*/

    /****************************************************************
     * MIDPVideoPainter interface
     ****************************************************************/
    /**
     * Paint video into canvas - in USE_DIRECT_VIDEO mode
     */
    public void paintVideo(Graphics g) {
        // Don't paint if Canvas visible flag is false
        if (!pvis || !cvis || painting)
            return;
        
        painting = true;
        
        // Save the clip region
        int cx = g.getClipX();
        int cy = g.getClipY();
        int cw = g.getClipWidth();
        int ch = g.getClipHeight();
        // Change the clip to clip the video area
        g.clipRect(dx, dy, dw, dh);
        
        // Check if its within the bounds
        if (g.getClipWidth() > 0 && g.getClipHeight() > 0 && pvis) {
            int w = dw, h = dh;
            if (w > videoWidth)
                w = videoWidth;
            if (h > videoHeight)
                h = videoHeight;
            try {
                if (rgbData != null) {
                    synchronized (rgbData) {
                        if (scaled) {
                            g.drawRGB(scaledData, 0, dw, dx, dy, dw, dh, useAlpha);
                        } else {
                            g.drawRGB(rgbData, 0, videoWidth, dx, dy, w, h, useAlpha);
                        }
                    }
                }
            } finally {
                // Revert the clip region
                g.setClip(cx, cy, cw, ch);
                painting = false;
            }
        } else {
            g.setClip(cx, cy, cw, ch);
            painting = false;
        }
        if (TRACE_FRAMERATE) {
            if (frameStartTime == 0) {
                frameStartTime = System.currentTimeMillis();
            } else {
                frameCount++;
                if ((frameCount % 30) == 0) {
                    int frameRate = (int) ( (frameCount * 1000) / (System.currentTimeMillis() - frameStartTime + 1));
                    System.err.println("Frame Rate = " + frameRate);
                }
            }
        }
    }

    /**
     * Enable/disable rendering for canvas (USE_DIRECT_VIDEO mode)
     */
    public void showVideo() {
        if (canvas != null && !cvis) {
            cvis = true;
            canvas.repaint();
        }
    }
       
    public void hideVideo() {
        if (canvas != null && cvis) {
            cvis = false;
            canvas.repaint();
        }
    }

    /****************************************************************
     * MMItem (CustomItem) - USE_GUI_PRIMITIVE mode
     ****************************************************************/
    
    final class MMItem extends MMCustomItem {
        
        public MMItem() {
            super("");
        }

        public void refresh(boolean resize) {
            if (resize) {
                invalidate();
                repaint();
            } else
                repaint(dx, dy, dw, dh);
        }

        protected void paint(Graphics g, int w, int h) {
            // Don't paint if VideoControl visible flag is false
            if (!pvis || painting)
                return;

            painting = true;
            if (rgbData != null) {
                synchronized (rgbData) {
                    if (scaled) {
                        g.drawRGB(scaledData, 0, dw, 0, 0, dw, dh, useAlpha);
                    } else {
                        g.drawRGB(rgbData, 0, videoWidth, 0, 0, videoWidth, videoHeight, useAlpha);
                    }
                }
            }
            painting = false;
        }

        protected int getMinContentWidth() {
            return 1;
        }

        protected int getMinContentHeight() {
            return 1;
        }

        protected int getPrefContentWidth(int h) {
            return dw;
        }
        
        protected int getPrefContentHeight(int w) {
            return dh;
        }

        protected void hideNotify() {
            super.hideNotify();
        }
    }
}
