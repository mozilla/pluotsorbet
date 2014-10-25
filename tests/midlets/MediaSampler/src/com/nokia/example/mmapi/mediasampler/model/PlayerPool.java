/*
 * Copyright © 2013 Nokia Corporation. All rights reserved.
 * Nokia and Nokia Connecting People are registered trademarks of Nokia Corporation. 
 * Oracle and Java are trademarks or registered trademarks of Oracle and/or its
 * affiliates. Other product and company names mentioned herein may be trademarks
 * or trade names of their respective owners. 
 * See LICENSE.TXT for license information.
 */ 
package com.nokia.example.mmapi.mediasampler.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.media.Manager;
import javax.microedition.media.MediaException;
import javax.microedition.media.Player;
import javax.microedition.media.PlayerListener;
import javax.microedition.media.control.StopTimeControl;
import javax.microedition.media.control.ToneControl;
import javax.microedition.media.control.VolumeControl;

import com.nokia.example.mmapi.mediasampler.MediaSamplerMIDlet;
import com.nokia.example.mmapi.mediasampler.data.Media;

/**
 * PlayerPool enables multiple realized / prefetched players.
 */
public class PlayerPool implements PlayerListener {

    protected Vector players;
    protected Vector medias;
    protected MediaSamplerMIDlet midlet;
    protected int latency;
    boolean supportMultiPlayer = true;
    private int actualVolume = 0;
    private int globalVolume = 0;
    private int midletVolume = 100;
    private long stopTime = StopTimeControl.RESET;
    private int realizedSoundIndex;

    public PlayerPool(MediaSamplerMIDlet midlet, int latency) {
        this.midlet = midlet;
        this.latency = latency;
        players = new Vector();
        medias = new Vector();
    }

    /**
     * PlayerListener interface's method.
     */
    public void playerUpdate(Player player, String event, Object eventData) {
        if (event.equals(PlayerListener.VOLUME_CHANGED)) { // Gauge is adjusted
            actualVolume = (int) (((float) globalVolume / 100) * (float) midletVolume);
        } else if (event.equals("com.nokia.external.volume.event")) {
            // External volumes keys are pressed                
            globalVolume = Integer.parseInt(eventData.toString());
            actualVolume = (int) (((float) globalVolume / 100) * (float) midletVolume);
        }
        midlet.actualVolume = actualVolume;
        midlet.globalVolume = globalVolume;
        midlet.midletVolume = midletVolume;
        midlet.eventString = event;
        midlet.updateVolume();
    }

    /**
     * Returns boolean indication is media mimetype supported by the Device.
     * 
     * @param mimeType
     *            String as media mime-type
     * @return boolean is supported
     */
    public boolean isMediaSupported(String mimeType) {
        String[] types = Manager.getSupportedContentTypes(null);
        for (int i = 0; i < types.length; i++) {
            if (mimeType.toLowerCase().equals(types[i].toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Add media to pool.
     * 
     * @param media -
     *            Media to add
     */
    public void addMedia(Media media) throws MediaException {
        String mimeType = media.getType();
        if (!isMediaSupported(mimeType)) {
            throw new MediaException("Type " + mimeType + " is not supported!");
        }
        addSoundObject(media);
    }

    /**
     * Return true if device supports multiple simultaneous players.
     * 
     * @return a boolean indication are multiple players supported.
     */
    public boolean supportsMulplePlayers() {
        return supportMultiPlayer;
    }

    /**
     * Adds Tone Sequence to this
     * 
     * @param sequence
     * @throws MediaException -
     *             if tone sequence is not supported
     */
    public void addToneSequence(byte[] sequence) throws MediaException {
        if (!isMediaSupported("audio/x-tone-seq")) {
            throw new MediaException("Tone (audio/x-tone-seq) is not supported!");
        }
        addSoundObject(sequence);
    }

    /**
     * Creates and initializes the Player
     * 
     * @param sequence -
     *            tone sequence data in byte array
     * @return realized tone sequence Player
     * @throws MediaException
     *             thrown by the system while creating the player
     * @throws IOException
     *             thrown by the system while creating the player
     */
    private Player createTonePlayer(byte[] sequence) throws MediaException, IOException {
        Player player = Manager.createPlayer(Manager.TONE_DEVICE_LOCATOR);
        player.addPlayerListener(this);
        player.realize();
        ToneControl tc = (ToneControl) (player.getControl("ToneControl"));
        tc.setSequence(sequence);
        return player;
    }

    /**
     * Creates and initializes the Player.
     * 
     * @param media -
     *            Media datatype that represents the data
     * @return realized Player
     * @throws MediaException
     *             occured while creating the player
     * @throws IOException
     *             occured while creating the player
     */
    private Player createPlayer(Object media) throws MediaException, IOException {
        if (media instanceof byte[]) {
            return createTonePlayer((byte[]) media);
        }
        return createPlayer((Media) media);
    }

    /**
     * Creates and initializes the Player.
     * 
     * @param media -
     *            Media datatype that represents the data
     * @return realized Player
     * @throws MediaException
     *             occured while creating the player
     * @throws IOException
     *             occured while creating the player
     */
    private Player createPlayer(Media media) throws MediaException, IOException {
        InputStream is = media.getInputStream();
        String mediaType = media.getType();
        Player player = Manager.createPlayer(is, mediaType);
        player.addPlayerListener(this);
        player.realize();
        player.prefetch();
        return player;
    }

    /**
     * Closes the players and removes all references.
     */
    public void releaseResources() {
        discardAll();
        players.removeAllElements();
        medias.removeAllElements();
    }

    /**
     * Set the volume level of players.
     * 
     * @param volume
     *            int as volume level in range 0-100.
     */
    public void setVolumeLevel(int volume) {
        midletVolume = volume;
        updateVolumeLevel();
    }

    /**
     * Set the stop time of players.
     * 
     * @param time
     *            long as stop time in millis
     */
    public void setStopTime(long time) {
        this.stopTime = time;
        updateStopTime();
    }

    /**
     * Plays the specified sound.
     * 
     * @param index
     *            int as sound index to play
     */
    public void playSound(int index) throws MediaException {
        if (supportMultiPlayer) {
            if (index >= 0 && index < players.size()) {
                Player player = (Player) players.elementAt(index);
                startPlayer(player);
            }
        } else {
            // use old player if already exists (has been previously played)
            if (index == realizedSoundIndex) {
                Player player = (Player) players.elementAt(0);
                startPlayer(player);
                return;
            }
            // otherwise close old player and create a new one.
            discardAll();
            players.removeAllElements();
            try {
                Object obj = medias.elementAt(index);
                Player player = createPlayer(obj);
                players.addElement(player);
                updateStopTime();
                updateVolumeLevel();
                realizedSoundIndex = index;
                startPlayer(player);
            } catch (IOException ioe) {
                midlet.alertError("IOException: " + ioe.getMessage());
            }
        }
    }

    private void startPlayer(Player player) {
        try {
            if (player != null) {
                if (player.getState() == Player.UNREALIZED) {
                    player.prefetch();
                    player.realize();
                }

                if (player.getState() != Player.CLOSED) {
                    Thread.sleep(latency);
                    player.start();
                }
            }
            Thread.sleep(latency);
        } catch (InterruptedException e) {
        } catch (MediaException e) {
            discardPlayer(player);
            midlet.alertError("MediaException: " + e.getMessage());
        }
    }

    public void discardPlayer(Player player) {
        if (player != null) {
            player.close();
        }
    }

    public void discardAll() {
        for (int i = 0; i < players.size(); i++) {
            Player player = (Player) players.elementAt(i);
            discardPlayer(player);
        }
    }

    /**
     * Set volume level to existing players.
     */
    private void updateVolumeLevel() {
        int size = players.size();
        for (int i = 0; i < size; i++) { // Set the same volume level for all players
            Player player = (Player) players.elementAt(i);
            VolumeControl control = (VolumeControl) player.getControl("VolumeControl");
            actualVolume = (int) (((float) globalVolume / 100) * (float) midletVolume);
            control.setLevel(midletVolume);
        }
    }

    /**
     * Set current stop-time to existing players.
     */
    private void updateStopTime() {
        int size = players.size();
        for (int i = 0; i < size; i++) { // Set the same stop time for all players
            Player player = (Player) players.elementAt(i);
            StopTimeControl control = (StopTimeControl) player.getControl("StopTimeControl");
            control.setStopTime(stopTime);
        }
    }

    private void addSoundObject(Object sndObject) throws MediaException {
        if (!supportMultiPlayer) {
            medias.addElement(sndObject);
        } else {
            try {
                Player player = createPlayer(sndObject);
                players.addElement(player);
                medias.addElement(sndObject);
            } catch (MediaException se) {
                // Let's assume that second player creation failes even media
                // type should be supported.
                if (supportMultiPlayer && players.size() == 1) {
                    Player failedPlayer = (Player) players.elementAt(0);
                    discardPlayer(failedPlayer);
                    players.removeElementAt(0);
                    try {
                        Player player = createPlayer(sndObject);
                        // Assuming that Player creation works if there is not other
                        // instances, we let the pool work in single Player instance mode.
                        supportMultiPlayer = false;
                        medias.addElement(sndObject);
                        // Update player vector to contain only current.
                        players.removeAllElements();
                        players.addElement(player);
                        realizedSoundIndex = medias.size() - 1;
                    } catch (MediaException me) {
                        // Cannot create player.
                        throw me;
                    } catch (IOException ioe) {
                        midlet.alertError("IOException: " + ioe.getMessage());
                    }
                } else {
                    // Reject this media and route Exception to method caller.
                    throw se;
                }
            } catch (IOException ioe) {
                midlet.alertError("IOException: " + ioe.getMessage());
            }
        }
    }

    public void closeAllPlayers() {
        if(players!= null && players.size()>0){
            for (int i = 0; i < players.size(); i++) {
                try {
                    Player player = (Player)players.elementAt(i);
                    player.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            
            players.removeAllElements();
        }
    }
}
