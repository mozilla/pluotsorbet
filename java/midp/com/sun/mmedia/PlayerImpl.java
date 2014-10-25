/*
 * 
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

import  javax.microedition.media.*;
import  javax.microedition.media.control.*;
import  javax.microedition.media.protocol.SourceStream;
import  javax.microedition.media.protocol.DataSource;
import  java.util.Enumeration;
import  java.util.Hashtable;
import  java.util.Vector;
import  com.sun.j2me.app.AppPackage;
import  com.sun.j2me.app.AppIsolate;
import  com.sun.j2me.log.Logging;
import  com.sun.j2me.log.LogChannels;
import com.sun.mmedia.PlayerStateSubscriber;

import java.io.IOException;

public class PlayerImpl implements Player {

    public PlayerStateSubscriber state_subscriber = null;

    protected BasicPlayer playerInst = null;
    public    BasicPlayer getPlayerInst(){ return playerInst; };
    
    protected DataSource  source;
    protected SourceStream stream;
    private boolean isClosed = false;

    private String  mediaFormat = null;
    private boolean handledByDevice = false;
    private boolean handledByJava = false;
    private int hNative;      // handle of native API library 


    private int loopCount = 0; /* if set in unrealized state */
    private Vector listeners = new Vector(2);

    /**
     * hastable to map playerID to instances
     */
    private static Hashtable mplayers = new Hashtable(4);
    /**
     * table of player states
     */
    private static Hashtable pstates = new Hashtable();
    /**
     * table of media times
     */
    private static Hashtable mtimes = new Hashtable();

    /**
     * VM paused?
     */
    private static boolean vmPaused = false;

    /**
     * global player id
     */
    private static int pcount = -1;
    /**
     * player ID of this player
     */
    protected int pID = 0;
    /**
     * lock object
     */
    private static Object idLock = new Object();
    
    // Init native library
    protected native int nInit(int appId, int pID, String URI);
    // Terminate native library
    protected native int nTerm(int handle);
    // Get Media Format
    protected native String nGetMediaFormat(int handle);
    // Need media Download in Java side?
    protected native boolean nIsHandledByDevice(int handle);

    // Realize native player
    protected native boolean nRealize(int handle, String mime);

    private static String PL_ERR_SH = "Cannot create a Player: ";
    
    /**
     * Constructor 
     */
    private PlayerImpl() {};

    public PlayerImpl(DataSource source) throws MediaException, IOException {
        // Get current application ID to support MVM
        int appId = AppIsolate.getIsolateId();

        synchronized (idLock) {
            pcount = (pcount + 1) % 32767;
            pID = pcount;
        }

        String locator = source.getLocator();
        hNative = nInit(appId, pID, locator);

        if (0 == hNative) {
            throw new MediaException("Unable to create native player");
        } else if (-1 == hNative) {
            throw new IOException("Unable to create native player");
        }

        mediaFormat     = nGetMediaFormat(hNative);

        if( mediaFormat.equals( BasicPlayer.MEDIA_FORMAT_UNSUPPORTED ) ) {
            /* verify if handled by Java */
            mediaFormat = Configuration.getConfiguration().ext2Format(source.getLocator());
            if( mediaFormat == null || mediaFormat.equals( BasicPlayer.MEDIA_FORMAT_UNSUPPORTED ) ) {
                nTerm(hNative);
                throw new MediaException("Unsupported Media Format:" + mediaFormat + " for " + source.getLocator());
            } else {
                handledByJava = true;
            }
        }

        if (locator != null && mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_UNKNOWN)) {
            if (locator.equals(Manager.TONE_DEVICE_LOCATOR)) {
                mediaFormat = BasicPlayer.MEDIA_FORMAT_DEVICE_TONE;
                handledByDevice = true;
            } else if (locator.equals(Manager.MIDI_DEVICE_LOCATOR)) {
                mediaFormat = BasicPlayer.MEDIA_FORMAT_DEVICE_MIDI;
                handledByDevice = true;
            }
        } else if (locator != null && locator.startsWith(Configuration.CAPTURE_LOCATOR)) {
            if (locator.startsWith(Configuration.AUDIO_CAPTURE_LOCATOR)) {
                mediaFormat = BasicPlayer.MEDIA_FORMAT_CAPTURE_AUDIO;
            } else if (locator.startsWith(Configuration.VIDEO_CAPTURE_LOCATOR)) {
                mediaFormat = BasicPlayer.MEDIA_FORMAT_CAPTURE_VIDEO;
            } else if (locator.startsWith(Configuration.RADIO_CAPTURE_LOCATOR)) {
                mediaFormat = BasicPlayer.MEDIA_FORMAT_CAPTURE_RADIO;
            }
            handledByDevice = true;
        }

        if (!handledByJava && !handledByDevice) {
            handledByDevice = nIsHandledByDevice(hNative);
        }

        this.source = source;

        if (!handledByDevice) {
            source.connect();
            SourceStream[] streams = source.getStreams();
            if (null == streams) {
                throw new MediaException("DataSource.getStreams() returned null");
            } else if (0 == streams.length) {
                throw new MediaException("DataSource.getStreams() returned an empty array");
            } else if (null == streams[0]) {
                throw new MediaException("DataSource.getStreams()[0] is null");
            } else {
                if (streams.length > 1 && Logging.REPORT_LEVEL <= Logging.INFORMATION) {
                    Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI,
                        "*** DataSource.getStreams() returned " + streams.length + 
                        " streams, only first one will be used!");
                }

                stream = streams[0];
            }
        }

        // Set event listener
        new MMEventListener();
    }

        
    /**
     * Constructs portions of the <code>Player</code> without
     * acquiring the scarce and exclusive resources.
     * This may include examining media data and may
     * take some time to complete.
     * <p>
     * When <code>realize</code> completes successfully, 
     * the <code>Player</code> is in the
     * <i>REALIZED</i> state.
     * <p>
     * If <code>realize</code> is called when the <code>Player</code> is in
     * the <i>REALIZED</i>, <i>PREFETCHTED</i> or <i>STARTED</i> state,
     * the request will be ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be realized.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to realize the <code>Player</code>.
     *
     */
    public void realize() throws MediaException {
        if (playerInst != null) {
            if (isClosed) {
                throw new IllegalStateException();
            }
            return;
        }
        String type = source.getContentType();
        if (type == null && stream != null && stream.getContentDescriptor() != null) {
            type = stream.getContentDescriptor().getContentType();
        }
        /* try to realize native player */
        if (!nRealize(hNative, type)) {
            throw new MediaException("Can not realize");
        }

        MediaDownload mediaDownload = null;

        if (!handledByDevice && !handledByJava) {
            mediaFormat = nGetMediaFormat(hNative);
            if (mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_UNSUPPORTED)) {
                String format;
                /* verify if handled by Java */
                if (type != null &&
                        (format = Configuration.getConfiguration().mime2Format(type)) != null && 
                        !format.equals(BasicPlayer.MEDIA_FORMAT_UNKNOWN) && 
                        !format.equals(BasicPlayer.MEDIA_FORMAT_UNSUPPORTED)) {
                    mediaFormat = format;
                    handledByJava = true;
                } else {
                    throw new MediaException("Unsupported media format");
                }
            }
            /* predownload media data to recognize media format and/or 
               specific media parameters (e.g. duration) */
            if (!mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_TONE)) {
                mediaDownload = new MediaDownload(hNative, stream);
                try {
                    mediaDownload.fgDownload();
                } catch(IOException ex1) {
                    ex1.printStackTrace();
                    throw new MediaException("Can not start download Thread: " + ex1);
                }catch(Exception ex) {
                    ex.printStackTrace();
                    throw new MediaException( "Can not start download Thread: " + ex );
                }
            }
        }

        if (mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_UNKNOWN)) {        
            /* ask media format if unknown */
            mediaFormat = nGetMediaFormat(hNative);

            if (mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_UNKNOWN)) {
                throw new MediaException("Unknown Media Format");
            }
            if (mediaFormat.equals(BasicPlayer.MEDIA_FORMAT_UNSUPPORTED)) {
                throw new MediaException("Unsupported Media Format");
            }
        }

        /* create Implementation Player */
        playerInst = getPlayerFromType(mediaFormat);

        playerInst.notificationSource = this;
        playerInst.hNative            = hNative;
        playerInst.mediaFormat        = mediaFormat;
        playerInst.handledByDevice    = handledByDevice;
        playerInst.pID                = pID;
        playerInst.mediaDownload      = mediaDownload;

        playerInst.setSource(source);

        if (loopCount != 0) {
            playerInst.setLoopCount(loopCount);
        }
        if (listeners.size() > 0) {
            for(int i=0; i<listeners.size(); i++)
                playerInst.addPlayerListener((PlayerListener)listeners.elementAt(i));
            listeners.removeAllElements();
        }
        mplayers.put(new Integer(pID), playerInst);

        playerInst.realize();
        if (null != state_subscriber) {
            state_subscriber.PlayerRealized(this);
        }
    }

    /**
     *  Gets the playerFromType attribute of the Manager class
     *
     * @param  type                Description of the Parameter
     * @return                     The playerFromType value
     * @exception  IOException     Description of the Exception
     * @exception  MediaException  Description of the Exception
     */
    private BasicPlayer getPlayerFromType(String type) throws MediaException {
        String className = null;

        if ("GIF".equals(type)) {
            className = "com.sun.mmedia.GIFPlayer";
        } else if (BasicPlayer.MEDIA_FORMAT_CAPTURE_VIDEO.equals(type)) {
            className = "com.sun.mmedia.DirectCamera";
        } else if (DirectPlayer.nIsToneControlSupported(hNative)) {
            className = "com.sun.mmedia.DirectTone";
        } else if (DirectPlayer.nIsMIDIControlSupported(hNative)) {
            className = "com.sun.mmedia.DirectMIDI";
        } else {
            className = "com.sun.mmedia.DirectPlayer";
        }              

        if ((type == null) || className == null) {
            throw new MediaException(PL_ERR_SH + "MediaFormat " + type + " is not supported");
        }

        BasicPlayer p = null;

        try {
            // ... try and instantiate the handler ...
            Class handlerClass = Class.forName(className);
            p = (BasicPlayer) handlerClass.newInstance();
        } catch (Exception e) {
            throw new MediaException(PL_ERR_SH + e.getMessage());
        }
        return p;
    }

    /**
     * Acquires the scarce and exclusive resources
     * and processes as much data as necessary
     * to reduce the start latency.
     * <p>
     * When <code>prefetch</code> completes successfully, 
     * the <code>Player</code> is in
     * the <i>PREFETCHED</i> state.
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> state,
     * it will implicitly call <code>realize</code>.
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code> 
     * is already in the <i>PREFETCHED</i> state, the <code>Player</code>
     * may still process data necessary to reduce the start
     * latency.  This is to guarantee that start latency can
     * be maintained at a minimum. 
     * <p>
     * If <code>prefetch</code> is called when the <code>Player</code> 
     * is in the <i>STARTED</i> state,
     * the request will be ignored.
     * <p>
     * If the <code>Player</code> cannot obtain all 
     * of the resources it needs, it throws a <code>MediaException</code>.
     * When that happens, the <code>Player</code> will not be able to
     * start.  However, <code>prefetch</code> may be called again when
     * the needed resource is later released perhaps by another
     * <code>Player</code> or application.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be prefetched.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to prefetch the <code>Player</code>.
     *
     */
    public void prefetch() throws MediaException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst == null) {
            realize();
        }
        if (playerInst != null) {
            playerInst.chkClosed(false);

            if (vmPaused) {
                return;
            }        

            playerInst.prefetch();

            if (null != state_subscriber) {
                state_subscriber.PlayerPrefetched(this);
            }
        }
    };

    /**
     * Starts the <code>Player</code> as soon as possible.
     * If the <code>Player</code> was previously stopped
     * by calling <code>stop</code> or reaching a preset
     * stop time, it will resume playback
     * from where it was previously stopped.  If the 
     * <code>Player</code> has reached the end of media,
     * calling <code>start</code> will automatically
     * start the playback from the start of the media.
     * <p>
     * When <code>start</code> returns successfully, 
     * the <code>Player</code> must have been started and 
     * a <code>STARTED</code> event will 
     * be delivered to the registered <code>PlayerListener</code>s.
     * However, the <code>Player</code> is not guaranteed to be in
     * the <i>STARTED</i> state.  The <code>Player</code> may have
     * already stopped (in the <i>PREFETCHED</i> state) because 
     * the media has 0 or a very short duration.
     * <p>
     * If <code>start</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i> state,
     * it will implicitly call <code>prefetch</code>.
     * <p>
     * If <code>start</code> is called when the <code>Player</code>
     * is in the <i>STARTED</i> state, 
     * the request will be ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code> cannot
     * be started.
     * @exception SecurityException Thrown if the caller does not
     * have security permission to start the <code>Player</code>.
     */
    public void start() throws MediaException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst == null) {
            realize();
        }
        if ( getState() == REALIZED )
        {
            prefetch();
        }
        if (playerInst != null) {
            playerInst.chkClosed(false);

            if (vmPaused) {
                return;
            }
            playerInst.start();
        }
    };

    /**
     * Stops the <code>Player</code>.  It will pause the playback at
     * the current media time.
     * <p>
     * When <code>stop</code> returns, the <code>Player</code> is in the 
     * <i>PREFETCHED</i> state.
     * A <code>STOPPED</code> event will be delivered to the registered
     * <code>PlayerListener</code>s.
     * <p>
     * If <code>stop</code> is called on
     * a stopped <code>Player</code>, the request is ignored.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @exception MediaException Thrown if the <code>Player</code>
     * cannot be stopped.
     */
    public void stop() throws MediaException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            playerInst.stop();
        }
    }

    /**
     * Release the scarce or exclusive
     * resources like the audio device acquired by the <code>Player</code>.
     * <p>
     * When <code>deallocate</code> returns, the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i> state.
     * <p>
     * If the <code>Player</code> is blocked at
     * the <code>realize</code> call while realizing, calling
     * <code>deallocate</code> unblocks the <code>realize</code> call and
     * returns the <code>Player</code> to the <i>UNREALIZED</i> state.
     * Otherwise, calling <code>deallocate</code> returns the
     * <code>Player</code> to  the <i>REALIZED</i> state.
     * <p>
     * If <code>deallocate</code> is called when the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>REALIZED</i>
     * state, the request is ignored.
     * <p>
     * If the <code>Player</code> is <code>STARTED</code>
     * when <code>deallocate</code> is called, <code>deallocate</code>
     * will implicitly call <code>stop</code> on the <code>Player</code>.
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     */
    public void deallocate() {
        if (playerInst != null) {
            playerInst.deallocate();
            if( null != state_subscriber && 
                ( getState() == PREFETCHED || getState() == STARTED ) ) {
                state_subscriber.PlayerDeallocated(this);
            }
        } else {
            // Player in the UNREALIZED or CLOSED state
            if (isClosed) {
                throw new IllegalStateException();
            }
        }
    };

    /**
     * Close the <code>Player</code> and release its resources.
     * <p>
     * When the method returns, the <code>Player</code> is in the
     * <i>CLOSED</i> state and can no longer be used.
     * A <code>CLOSED</code> event will be delivered to the registered
     * <code>PlayerListener</code>s.
     * <p>
     * If <code>close</code> is called on a closed <code>Player</code>
     * the request is ignored.
     */
    public void close() {
        if (playerInst != null) {
            playerInst.close();
            mplayers.remove(new Integer(pID));
        } else {
            if (!isClosed) {
                /* close source of unrealized player */
                if (source != null) {
                    source.disconnect();
                    source = null;
                }
                /* close native part of unrealized player */
                if(hNative != 0) {
                    nTerm(hNative);
                }
            }
        }
        isClosed = true;
    }
    
    /**
     * Sets the <code>TimeBase</code> for this <code>Player</code>.
     * <p>
     * A <code>Player</code> has a default <code>TimeBase</code> that
     * is determined by the implementation. 
     * To reset a <code>Player</code> to its default 
     * <code>TimeBase</code>, call <code>setTimeBase(null)</code>.
     *
     * @param master The new <CODE>TimeBase</CODE> or 
     * <CODE>null</CODE> to reset the <code>Player</code>
     * to its default <code>TimeBase</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>UNREALIZED</i>, <i>STARTED</i> or <i>CLOSED</i> state.
     * @exception MediaException Thrown if
     * the specified <code>TimeBase</code> cannot be set on the 
     * <code>Player</code>.
     * @see #getTimeBase
     */
    public void setTimeBase(TimeBase master) throws MediaException {
        if (playerInst != null) {
            playerInst.setTimeBase(master);
        } else {
            // Player in the UNREALIZED or CLOSED state
            throw new IllegalStateException();
        }
    };

    /**
     * Gets the <code>TimeBase</code> that this <code>Player</code> is using.
     * @return The <code>TimeBase</code> that this <code>Player</code> is using.
     * @see #setTimeBase
     *
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>CLOSED</i> state.
     */
    public TimeBase getTimeBase() {
        if (playerInst != null) {
            return playerInst.getTimeBase();
        }
        // Player in the UNREALIZED or CLOSED state
        throw new IllegalStateException();
    };

    /**
     * Sets the <code>Player</code>'s&nbsp;<i>media time</i>.
     * <p>
     * For some media types, setting the media time may not be very
     * accurate.  The returned value will indicate the 
     * actual media time set.
     * <p>
     * Setting the media time to negative values will effectively
     * set the media time to zero.  Setting the media time to
     * beyond the duration of the media will set the time to
     * the end of media.
     * <p>
     * There are some media types that cannot support the setting
     * of media time.  Calling <code>setMediaTime</code> will throw
     * a <code>MediaException</code> in those cases.
     * 
     * @param now The new media time in microseconds.
     * @return The actual media time set in microseconds.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>CLOSED</i> state.
     * @exception MediaException Thrown if the media time
     * cannot be set.
     * @see #getMediaTime
     */
    public long setMediaTime(long now) throws MediaException {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            return playerInst.setMediaTime(now);
        }
        throw new IllegalStateException();
    };

    /**
     * Gets this <code>Player</code>'s current <i>media time</i>.
     * <p>
     * <code>getMediaTime</code> may return <code>TIME_UNKNOWN</code> to
     * indicate that the media time cannot be determined. 
     * However, once <code>getMediaTime</code> returns a known time 
     * (time not equals to <code>TIME_UNKNOWN</code>), subsequent calls 
     * to <code>getMediaTime</code> must not return 
     * <code>TIME_UNKNOWN</code>.
     *
     * @return The current <i>media time</i> in microseconds or 
     * <code>TIME_UNKNOWN</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #setMediaTime
     */
    public long getMediaTime() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            return playerInst.getMediaTime();
        } else {
            return Player.TIME_UNKNOWN;
        }
    };

    /**
     * Gets the current state of this <code>Player</code>.
     * The possible states are: <i>UNREALIZED</i>,
     * <i>REALIZED</i>, <i>PREFETCHED</i>, <i>STARTED</i>, <i>CLOSED</i>.
     * 
     * @return The <code>Player</code>'s current state.
     */
    public int getState() {
        if (playerInst != null) {
            return playerInst.getState();
        }
        return isClosed ? Player.CLOSED : Player.UNREALIZED;
    };

    /**
     * Get the duration of the media.
     * The value returned is the media's duration
     * when played at the default rate.
     * <br>
     * If the duration cannot be determined (for example, the
     * <code>Player</code> is presenting live
     * media)  <CODE>getDuration</CODE> returns <CODE>TIME_UNKNOWN</CODE>.
     *
     * @return The duration in microseconds or <code>TIME_UNKNOWN</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     */
    public long getDuration() {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            return playerInst.getDuration();
        } else {
            return Player.TIME_UNKNOWN;
        }
    };

    /**
     * Get the content type of the media that's
     * being played back by this <code>Player</code>.
     * <p>
     * See <a href="Manager.html#content-type">content type</a>
     * for the syntax of the content type returned.
     *
     * @return The content type being played back by this 
     * <code>Player</code>.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>UNREALIZED</i> or <i>CLOSED</i> state.
     */
    public String getContentType() {
        if (playerInst != null) {
            return playerInst.getContentType();
        }
        throw new IllegalStateException();
    };


    /**
     * Set the number of times the <code>Player</code> will loop
     * and play the content.
     * <p>
     * By default, the loop count is one.  That is, once started,
     * the <code>Player</code> will start playing from the current
     * media time to the end of media once.
     * <p>
     * If the loop count is set to N where N is bigger than one,
     * starting the <code>Player</code> will start playing the
     * content from the current media time to the end of media.
     * It will then loop back to the beginning of the content 
     * (media time zero) and play till the end of the media.
     * The number of times it will loop to the beginning and 
     * play to the end of media will be N-1.
     * <p>
     * Setting the loop count to 0 is invalid.  An 
     * <code>IllegalArgumentException</code> will be thrown.
     * <p>
     * Setting the loop count to -1 will loop and play the content
     * indefinitely.
     * <p>
     * If the <code>Player</code> is stopped before the preset loop
     * count is reached either because <code>stop</code> is called or
     * a preset stop time (set with the <code>StopTimeControl</code>) 
     * is reached, calling <code>start</code> again will
     * resume the looping playback from where it was stopped until it
     * fully reaches the preset loop count. 
     * <p> 
     * An <i>END_OF_MEDIA</i> event will be posted 
     * every time the <code>Player</code> reaches the end of media.
     * If the <code>Player</code> loops back to the beginning and
     * starts playing again because it has not completed the loop
     * count, a <i>STARTED</i> event will be posted.
     * 
     * @param count indicates the number of times the content will be
     * played.  1 is the default.  0 is invalid.  -1 indicates looping 
     * indefintely.
     * @exception IllegalArgumentException Thrown if the given
     * count is invalid.
     * @exception IllegalStateException Thrown if the 
     * <code>Player</code> is in the <i>STARTED</i> 
     * or <i>CLOSED</i> state. 
     */

    public void setLoopCount(int count) {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            playerInst.setLoopCount(count);
        } else {
            if (count != 0) {
                loopCount = count;
            } else {
                throw new IllegalArgumentException();
            }
        }
    };

    /**
     * Add a player listener for this player.
     *
     * @param playerListener the listener to add.
     * If <code>null</code> is used, the request will be ignored.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #removePlayerListener
     */
    public void addPlayerListener(PlayerListener playerListener) {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            playerInst.addPlayerListener(playerListener);
        } else {
            if (playerListener != null) {
                listeners.addElement(playerListener);
            }
        }
    };

    /**
     * Remove a player listener for this player.
     *
     * @param playerListener the listener to remove.
     * If <code>null</code> is used or the given 
     * <code>playerListener</code> is not a listener for this
     * <code>Player</code>, the request will be ignored.
     * @exception IllegalStateException Thrown if the <code>Player</code>
     * is in the <i>CLOSED</i> state.
     * @see #addPlayerListener
     */
    public void removePlayerListener(PlayerListener playerListener) {
        if (isClosed) {
            throw new IllegalStateException();
        }
        if (playerInst != null) {
            playerInst.removePlayerListener(playerListener);
        } else {
            listeners.removeElement(playerListener);
        }
    };

    /**
     *  Gets the controls attribute of the BasicPlayer object
     *
     * @return    The controls value
     */
    public final Control[] getControls() {
        if (playerInst != null) {
            return playerInst.getControls();
        }
        throw new IllegalStateException();
    }


    /**
     * Gets the <code>Control</code> that supports the specified
     * class or interface. The full class
     * or interface name should be specified.
     * <code>Null</code> is returned if the <code>Control</code>
     * is not supported.
     *
     * @param  type  Description of the Parameter
     * @return       <code>Control</code> for the class or interface
     * name.
     */
    public Control getControl(String type) {
        if (playerInst != null) {
            return playerInst.getControl(type);
        }
        throw new IllegalStateException();
    }

    /**
     * For global PlayerID management
     *
     * @param  pid  Description of the Parameter
     * @return      Description of the Return Value
     */
    public static BasicPlayer get(int pid) {
        return (BasicPlayer) (mplayers.get(new Integer(pid)));
    }

    /**
     * Send external volume changed event to all of the player from this VM
     */
    public static void sendExternalVolumeChanged(String evt, int volume) {
        if (mplayers == null) {
            return;
        }

        /* Send event to player if this player is in realized state (or above) */
        for (Enumeration e = mplayers.elements(); e.hasMoreElements();) {
            BasicPlayer p = (BasicPlayer) e.nextElement();
            int state = p.getState();
            if (state >= Player.REALIZED) {
                VolumeControl vc = (VolumeControl)p.getControl("VolumeControl");
                if (vc != null) {
                    vc.setLevel(volume);
                }
            }
        }
    }

    /**
     *  Pauses and deallocates all media players.
     *
     *  After this call all players are either in realized
     *  or unrealized state.  
     *
     *  Resources are being released during deallocation.
     */
    public static void pauseAll() {
        vmPaused = true;
    
        if (mplayers == null) {
            return;
        }

        for (Enumeration e = mplayers.elements(); e.hasMoreElements();) {
            BasicPlayer p = (BasicPlayer) e.nextElement();

            int state = p.getState();
            long time = p.getMediaTime();
            
            // save the player's state
            pstates.put(p, new Integer(state));
            // save the player's media time
            mtimes.put(p, new Long(time));

            try {
                // Stop the player
                if (state == Player.STARTED) {
                    p.stop();
                }
            } catch(MediaException ex) {
            }
        }
    }


    /**
     *  Resumes all media players' activities.
     *
     *  Players that were in STARTED state when pause
     *  was called will resume playing at the media time
     *  they were stopped and deallocated.
     */
    public static void resumeAll() {
        vmPaused = false;
        
        if (mplayers == null || pstates.size() == 0) {
            return;
        }
        
        for (Enumeration e = mplayers.elements(); e.hasMoreElements();) {
            BasicPlayer p = (BasicPlayer) e.nextElement();

            int state = ((Integer) pstates.get(p)).intValue();
            long time = ((Long) mtimes.get(p)).longValue();

            switch (state) {
                /*
                case Player.PREFETCHED:
                    try {
                        p.prefetch();
                        p.setMediaTime(time);
                    } catch (MediaException ex) {
                    }
                    break;
                */
                case Player.STARTED:
                    try {
                        //p.realize();
                        //p.prefetch();
                        if (p.getState() != Player.STARTED) {
                            p.setMediaTime(time);
                            p.start();
                        }
                    } catch (MediaException ex) {
                    }
                    break;
            }
        }

        // clear player states and media times
        pstates.clear();
        mtimes.clear();
    }

}

