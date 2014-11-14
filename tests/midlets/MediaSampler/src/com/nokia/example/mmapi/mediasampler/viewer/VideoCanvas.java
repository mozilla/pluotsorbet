/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.viewer;

import java.io.*;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.*;
import javax.microedition.media.*;
import javax.microedition.media.control.*;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;
import java.util.Timer;
import java.util.TimerTask;

/**
 * VideoCanvas renders video on Canvas.
 */
class VideoCanvas extends Canvas implements CommandListener, PlayerListener {

    private PlayerController controller;
    private MediaSamplerMIDlet midlet;
    private Displayable returnScreen;
    private String videoFile;
    private Command stopCommand;
    private Command replayCommand;
    private Command backCommand;
    private Player player;
    private boolean initDone;
    private boolean playPending = false;

    /**
     * Constructor.
     * 
     * @param midlet
     *            MediaSamplerMIDlet
     * @param returnScreen
     *            Displayable to set visible when returned from this Canvas
     * @param videoFile
     *            String as path of the source viudeo file.
     */
    VideoCanvas(MediaSamplerMIDlet midlet, Displayable returnScreen, String videoFile) {
        this.midlet = midlet;
        this.returnScreen = returnScreen;
        this.videoFile = videoFile;
        controller = new PlayerController();

        replayCommand = new Command("Replay", Command.SCREEN, 1);
        stopCommand = new Command("Stop", Command.SCREEN, 2);
        backCommand = new Command("Back", Command.BACK, 1);
        addCommand(backCommand);
        setCommandListener(this);
    }

    /**
     * Set play status to "pending".
     */
    void prepareToPlay() {
        controller.start();
        playPending = true;
    }

    /**
     * Play video only when we're displayed. Use playPending flag to avoid
     * restarting if a system screen is visiable.
     */
    protected void showNotify() {
        if (playPending) {
            playPending = false;
            controller.playVideo();
        }
        final VideoCanvas self = this;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run() {
                self.repaint();
                self.serviceRepaints();
            }
        }, 200);
    }

    /**
     * Renders the Canvas.
     */
    public void paint(Graphics g) {
        g.setColor(0x00FFFF00); // yellow
        g.fillRect(0, 0, getWidth(), getHeight());
    }

    /**
     * Implemented CommandListener method. Indicates that a command event has
     * occurred on Displayable d.
     */
    public void commandAction(Command c, Displayable d) {
        if (c == stopCommand) {
            controller.stopVideo();
        } else if (c == replayCommand) {
            controller.playVideo();
        } else if (c == backCommand) {
            discardPlayer();
        }
    }

    /**
     * Overriden Canvas method.
     */
    public void keyPressed(int keyCode) {
        if (getGameAction(keyCode) == FIRE) {
            int state = player.getState();
            if (state == Player.STARTED) {
                controller.stopVideo();
            } else {
                controller.playVideo();
            }
        }
    }

    /**
     * Reads the content from the specified HTTP URL and returns InputStream
     * where the contents are read.
     * 
     * @return InputStream
     * @throws IOException
     */
    private InputStream urlToStream(String url) throws IOException {
        // Open connection to the http url...
        HttpConnection connection = (HttpConnection) Connector.open(url);
        DataInputStream dataIn = connection.openDataInputStream();
        byte[] buffer = new byte[1000];
        int read = -1;
        // Read the content from url.
        ByteArrayOutputStream byteout = new ByteArrayOutputStream();
        while ((read = dataIn.read(buffer)) >= 0) {
            byteout.write(buffer, 0, read);
        }
        dataIn.close();
        connection.close();
        // Fill InputStream to return with content read from the URL.
        ByteArrayInputStream byteIn = new ByteArrayInputStream(byteout.toByteArray());
        return byteIn;
    }

    /**
     * Stops the Player.
     */
    void doStop() {
        if (player != null) {
            try {
                player.stop();
            } catch (MediaException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Initializes and starts the Player.
     */
    void play() {
        try {
            if (!initDone || player == null) {
                initPlayer();
            }

            int state = player.getState();
            if (state == Player.CLOSED) {
                player.prefetch();
            } else if (state == Player.UNREALIZED) {
                player.realize();
            } else if (state == Player.REALIZED) {
                player.prefetch();
            }
            player.start();
        } catch (MediaException me) {
            discardPlayer();
            midlet.alertError("MediaException: " + me.getMessage());
        } catch (SecurityException se) {
            discardPlayer();
            midlet.alertError("SecurityException: " + se.getMessage());
        } catch (Exception e) {
            discardPlayer();
            midlet.alertError("Exception: " + e.getMessage());
        }
    }

    /**
     * Initializes the video player.
     * 
     * Player is initialized only once to save the memory resorces and to
     * increase performance.
     */
    void initPlayer() {
        try {
            initDone = false;
            if (videoFile == null) {
                midlet.alertError("No video file specified");
                return;
            }
            boolean fromHttp = videoFile.startsWith("http://");
            InputStream is = fromHttp ? urlToStream(videoFile) : getClass().getResourceAsStream(videoFile);
            player = Manager.createPlayer(is, "video/3gpp");
            player.addPlayerListener(this);
            player.prefetch();
            player.realize();

            // get the video control and attach it to our canvas
            VideoControl videoControl = (VideoControl) (player.getControl("VideoControl"));
            if (videoControl == null) {
                midlet.alertError("VideoControl not supported");
            } else {
                videoControl.initDisplayMode(VideoControl.USE_DIRECT_VIDEO, this);
                videoControl.setVisible(true);
            }
            initDone = true;
        } catch (IOException ioe) {
            discardPlayer();
            midlet.alertError("IOException: " + ioe.getMessage());
        } catch (MediaException me) {
            discardPlayer();
            midlet.alertError("MediaException: " + me.getMessage());
        } catch (SecurityException se) {
            discardPlayer();
            midlet.alertError("SecurityException: " + se.getMessage());
        } catch (Exception ex) {
            discardPlayer();
            midlet.alertError("Exception: " + ex.getMessage());
        }
    }

    /**
     * Called in case of exception to make sure invalid players are closed
     */
    void discardPlayer() {
        if (player != null) {
            controller.setStopped();
            player.close();
            player = null;
        }
        Display.getDisplay(midlet).setCurrent(returnScreen);
    }

    /**
     * Implemented javax.microedition.media.PlayerListener method.
     */
    public void playerUpdate(final Player p, final String event, final Object eventData) {
        // queue a call to updateEvent in the user interface event queue
        Display display = Display.getDisplay(midlet);
        display.callSerially(new Runnable() {

            public void run() {
                VideoCanvas.this.updateEvent(p, event, eventData);
            }
        });
    }

    /**
     * Handles playerUpdate events of the Player.
     * 
     * @param p
     * @param event
     * @param eventData
     */
    void updateEvent(Player p, String event, Object eventData) {
        if (event.equals(END_OF_MEDIA)) {
            repaint(); // to ensure smooth operation of the menu button
            removeCommand(stopCommand);
            addCommand(replayCommand);
        } else if (event.equals(CLOSED)) {
            removeCommand(stopCommand);
            addCommand(replayCommand);
        } else if (event.equals(STARTED)) {
            removeCommand(replayCommand);
            addCommand(stopCommand);
        } else if (event.equals(ERROR)) {
        }
    }

    /**
     * PlayerController calls the play and stop methods of the Player. The
     * purpose of this class is only to isolate Player method calls from the
     * event threads (such commandAction(...))
     */
    public class PlayerController extends Thread {

        private boolean running;
        // Lock object of this Thread
        private Object controlLock = new Object();

        public PlayerController() {
        }

        // Activates the Player
        public void playVideo() {
            synchronized (controlLock) {
                controlLock.notify();
            }
        }

        // Deactivates the player
        public void stopVideo() {
            synchronized (controlLock) {
                doStop();
            }
        }

        // Terminates this thread
        public void setStopped() {
            running = false;
            synchronized (controlLock) {
                controlLock.notify();
            }
        }

        public void run() {
            VideoCanvas.this.play();
            running = true;
            while (running) {
                try {
                    synchronized (controlLock) {
                        // Set this thread to wait for playVideo() method call.
                        controlLock.wait();
                        if (!running) { // Leave if controller is stopped.
                            break;
                        }
                        VideoCanvas.this.play();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
