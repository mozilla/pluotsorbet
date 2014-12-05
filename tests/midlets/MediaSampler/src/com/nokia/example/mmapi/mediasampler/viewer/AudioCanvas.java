/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.viewer;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.media.MediaException;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;
import com.nokia.example.mmapi.mediasampler.data.Media;
import com.nokia.example.mmapi.mediasampler.data.MediaFactory;
import com.nokia.example.mmapi.mediasampler.model.PlayerPool;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Audio play canvas. Audio clip is played when a key is pressed.
 */
public class AudioCanvas extends Canvas implements CommandListener {

    private PlayerPool pool;
    private MediaSamplerMIDlet midlet;
    private Displayable returnScreen;
    protected String[] supportedMediaNames;
    protected String[] unsupportedMediaNames;
    protected int countOfPlayers = 0;
    protected int midletVolume = 100;
    protected int pressed_x = 0;
    protected int pressed_y = 0;
    protected int selected = -1;
    private boolean infoMode;
    private boolean nHD_portrait = false;
    private boolean touch = false;
    private boolean pressed = false;
    private Font fontPlain;
    private Font fontBold;
    private Command infoCommand = new Command("Info", Command.SCREEN, 1);
    private Command backCommand = new Command("Back", Command.BACK, 1);

    public AudioCanvas(MediaSamplerMIDlet midlet, Displayable returnScreen, int latency) {
        this.midlet = midlet;
        this.returnScreen = returnScreen;
        pool = new PlayerPool(midlet, latency);
        initSounds();
        // Init volume level of Players in pool
        pool.setVolumeLevel(midletVolume);
        addCommand(backCommand);
        addCommand(infoCommand);
        setCommandListener(this);
    }

    /**
     * Release loaded resources
     */
    public void releaseResources() {
        pool.releaseResources();
    }

    /**
     * Implemented CommandListener method.
     */
    public void commandAction(Command cmd, Displayable d) {
        if (cmd == backCommand) {
            if (infoMode) {
                infoMode = false;
                addCommand(infoCommand);
                repaint();
            } else {
                Display.getDisplay(midlet).setCurrent(returnScreen);
                pool.closeAllPlayers();
            }
            
        } else if (cmd == infoCommand) {
            infoMode = true;
            removeCommand(infoCommand);
            repaint();
        }
    }

    public void keyPressed(int key) {
        int keyCode = key - KEY_NUM0;
        int gameAction = getGameAction(key);

        // Check is the selected audio available for playing.
        if (keyCode > 0 && keyCode <= countOfPlayers) {
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
                // Swallow the exception.
            }
        } else if (key == 114) { // R key in QWERTY keyboard
            keyCode = 1;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (key == 116) { // T key in QWERTY keyboard
            keyCode = 2;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (key == 121) { // Y key in QWERTY keyboard
            keyCode = 3;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (key == 102) { // F key in QWERTY keyboard
            keyCode = 4;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (key == 103) { // G key in QWERTY keyboard
            keyCode = 5;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (key == 104) { // H key in QWERTY keyboard
            keyCode = 6;
            try {
                pool.playSound(keyCode - 1);
            } catch (MediaException e) {
            }
        } else if (gameAction == UP) {
            increaseVolume();
        } else if (gameAction == DOWN) {
            decreaseVolume();
        }
    }

    /**
     * Paint the canvas.
     */
    protected void paint(Graphics g) {
        int x = 0;
        int y = 0;
        int w = getWidth();
        int h = getHeight();
        int fontSize = Font.SIZE_SMALL;
        g.setColor(0xFFFFFF);
        g.fillRect(x, y, w, h);
        if (w == 360) { //nHD screen in portrait mode?
            fontSize = Font.SIZE_MEDIUM;
            nHD_portrait = true;
        } else if (w == 240 && this.hasPointerEvents()) { // Series 40 touch devices, portrait QVGA
            touch = true;
        }
        fontPlain = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, fontSize);
        fontBold = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_BOLD, fontSize);
        g.setColor(0x000000);
        if (infoMode) {
            boolean multiSupport = pool.supportsMulplePlayers();
            boolean mixSupport = "true".equals(System.getProperty("supports.mixing"));
            g.setFont(fontBold);
            y = paintTextRow(g, "Supports multiple players:", x, y);
            g.setFont(fontPlain);
            y = paintTextRow(g, "" + multiSupport, x, y);
            g.setFont(fontBold);
            y = paintTextRow(g, "Supports audio mixing:", x, y);
            g.setFont(fontPlain);
            y = paintTextRow(g, "" + mixSupport, x, y);
            if (unsupportedMediaNames.length > 0) {
                g.setFont(fontBold);
                g.setColor(0x000000);
                y = paintTextRow(g, "Unsupported sounds:", x, y);
                g.setFont(fontPlain);
                for (int i = 0; i < unsupportedMediaNames.length; i++) {
                    String str = unsupportedMediaNames[i];
                    String strToPaint = str;
                    y = paintTextRow(g, strToPaint, x, y);
                }
            }
        } else {
            g.setFont(fontBold);
            paintTextRow(g, "Sound key mapping:", x, y);
            y = fontBold.getHeight();
            //
            for (int i = 0; i < supportedMediaNames.length; i++) {
                if ((selected - 1) == i && pressed) {
                    g.setFont(fontBold);
                } else {
                    g.setFont(fontPlain);
                }
                String str = supportedMediaNames[i];
                String strToPaint = (i + 1) + " = " + str;
                y = paintTextRow(g, strToPaint, x, y);
            }
            g.setFont(fontPlain);
            if (!touch) {
                paintTextRow(g, "Global volume: " + midlet.globalVolume, x, y);
                y = y + fontPlain.getHeight();
            }
            paintTextRow(g, "MIDlet volume: " + midlet.midletVolume, x, y);
            y = y + fontPlain.getHeight();
            if (!touch) {
                paintTextRow(g, "Actual volume: " + midlet.actualVolume, x, y);
                y = y + fontPlain.getHeight();
            }
            y = paintTextRow(g, "Player event: " + midlet.eventString, x, y);
        }
        if (pressed) {
            g.setColor(255, 0, 0);
            for (int i = 1; i < 4; i++) {
                int size = i * 10;
                g.drawArc(pressed_x - size, pressed_y - size, size * 2, size * 2, 0, 360);
            }
            g.setColor(0, 0, 0);
        }
    }

    protected void pointerPressed(int x, int y) {
        if (!touch && !nHD_portrait) {
            return;
        }
        pressed = true;
        pressed_x = x;
        pressed_y = y;
        selected = findSelectedArea(pressed_y);
        repaint();
        serviceRepaints();
        if (selected > 0 && selected <= countOfPlayers) {
            try {
                pool.playSound(selected - 1);
            } catch (MediaException e) {
                // Swallow the exception.
            }
        } else {
            if (midletVolume < 100) {
                midletVolume += 10;
            } else if (midletVolume >= 100) {
                midletVolume = 0;
            }
            pool.setVolumeLevel(midletVolume);
            midlet.midletVolume = midletVolume;
            repaint();
        }
    }

    protected void pointerReleased(int x, int y) {
        pressed = false;
        repaint();
        serviceRepaints();
    }

    protected void pointerDragged(int x, int y) {
    }

    protected void sizeChanged(int w, int h) {
        repaint();
    }
    
    protected void showNotify() {
        final AudioCanvas self = this;
        Timer timer = new Timer();
        timer.schedule(new TimerTask(){
            public void run() {
                self.repaint();
                self.serviceRepaints();
            }
        }, 200);
    }

    private int findSelectedArea(int y) {
        int y_value = y;
        int item = 0;
        int plain = fontPlain.getHeight() * 2;
        int bold = fontBold.getHeight();
        y_value = y_value - bold;
        while (y_value > 0) {
            y_value = y_value - plain;
            item++;
        }
        return item;
    }

    /**
     * Renders a text row to Canvas.
     */
    private int paintTextRow(Graphics g, String text, int x, int y) {
        int w = getWidth();
        Font font = g.getFont();
        for (int j = 0; j < text.length(); j++) {
            char c = text.charAt(j);
            int cw = font.charWidth(c);
            if (x + cw > w) {
                x = 0;
                y += font.getHeight();
            }
            g.drawChar(c, x, y, Graphics.TOP | Graphics.LEFT);
            x += cw;
        }
        if (touch || nHD_portrait) {
            y += (2 * font.getHeight());
        } else {
            y += font.getHeight();
        }
        return y;
    }

    private void increaseVolume() {
        midletVolume += 10;
        if (midletVolume > 100) {
            midletVolume = 100;
        }
        pool.setVolumeLevel(midletVolume);
        midlet.midletVolume = midletVolume;
        repaint();
    }

    private void decreaseVolume() {
        midletVolume -= 10;
        if (midletVolume < 0) {
            midletVolume = 0;
        }
        pool.setVolumeLevel(midletVolume);
        midlet.midletVolume = midletVolume;
        repaint();
    }

    /**
     * Loads the medias available on this canvas. Loaded sounds are passed to
     * PlayerPool class which creates players and initializes player states.
     */
    protected void initSounds() {
        countOfPlayers = 0;
        Vector supportedMedias = new Vector();
        Vector unsupportedMedias = new Vector();

        // Sound media clips
        Media[] medias = MediaFactory.getSoundMedias();
        for (int i = 0; i < medias.length; i++) {
            Media media = medias[i];
            String mediaName = null;
            try {
                mediaName = media.getFile() + " [" + media.getType() + "]";
                pool.addMedia(media);
                supportedMedias.addElement(mediaName);
                countOfPlayers++;
            } catch (MediaException e) {
                unsupportedMedias.addElement(mediaName);
            }
        }

        // Tone sequences
        String mediaName = null;
        try {
            mediaName = "Tone sequence";
            pool.addToneSequence(MediaFactory.getToneSequence());
            supportedMedias.addElement(mediaName);
            countOfPlayers++;
        } catch (MediaException e) {
            unsupportedMedias.addElement(mediaName);
        }

        // Stores results of success and failed Players to String array
        supportedMediaNames = new String[supportedMedias.size()];
        supportedMedias.copyInto(supportedMediaNames);

        unsupportedMediaNames = new String[unsupportedMedias.size()];
        unsupportedMedias.copyInto(unsupportedMediaNames);
    }

}
