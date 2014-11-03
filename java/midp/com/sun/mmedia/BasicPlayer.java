/*
 *  Copyright  1990-2009 Sun Microsystems, Inc. All Rights Reserved.
 *  DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER
 *  
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU General Public License version
 *  2 only, as published by the Free Software Foundation.
 *  
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 *  General Public License version 2 for more details (a copy is
 *  included at /legal/license.txt).
 *  
 *  You should have received a copy of the GNU General Public License
 *  version 2 along with this work; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 *  02110-1301 USA
 *  
 *  Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa
 *  Clara, CA 95054 or visit www.sun.com if you need additional
 *  information or have any questions.
 */
package com.sun.mmedia;

import com.sun.j2me.log.Logging;
import com.sun.j2me.log.LogChannels;

import java.io.*;
import java.util.Vector;
import java.util.Enumeration;
import javax.microedition.media.*;
import javax.microedition.media.control.*;
import javax.microedition.media.protocol.*;
import com.sun.mmedia.control.*;
import com.sun.mmedia.protocol.*;

/**
 * BasicPlayer provides basic implementation for the Player methods.
 * Many of the methods call do<method> to do the actual work that can
 * be overridden by subclasses.
 *
 */
public abstract class BasicPlayer
    implements Player, TimeBase, StopTimeControl
{
    /** Unknown media format */
    static final String MEDIA_FORMAT_UNKNOWN = "UNKNOWN";
    /** Unsupported media format */
    static final String MEDIA_FORMAT_UNSUPPORTED = "UNSUPPORTED";
    /** Device Tone format */
    static final String MEDIA_FORMAT_DEVICE_TONE = "DEVICE_TONE";
    /** Device MIDI format */
    static final String MEDIA_FORMAT_DEVICE_MIDI = "DEVICE_MIDI";
    /** Audio Capture format*/
    static final String MEDIA_FORMAT_CAPTURE_AUDIO = "CAPTURE_AUDIO";
    /** Video Capture format*/
    static final String MEDIA_FORMAT_CAPTURE_VIDEO = "CAPTURE_VIDEO";
    /** Radio Capture format*/
    static final String MEDIA_FORMAT_CAPTURE_RADIO = "CAPTURE_RADIO";
    /** Tone format */
    static final String MEDIA_FORMAT_TONE = "TONE";


    protected String mediaFormat = null;
    protected boolean handledByDevice = false;
    protected int hNative;      // handle of native API library 
    protected int pID;          // unique ID of the player

    protected Player notificationSource = null; // if not null, will substitute 'this'
                                                // in playerUpdate calls
                                                
    protected MediaDownload mediaDownload = null;                                                   
    
    /**
     * the state of this player
     */
    protected int state = UNREALIZED;

    /**
     * the loopCount of this player
     */
    int loopCountSet = 1, loopCount;

    /**
     * the flag to indicate whether the Player is currently paused at EOM.
     * If true, the Player will seek back to the beginning when
     * restarted.
     */
    boolean EOM = false;

    /**
     * the flag to indicate looping after EOM.
     */
    boolean loopAfterEOM = false;

    /**
     * this player's playerlisteners
     */
    Vector listeners = new Vector(2);

    /**
     * Asynchronous event mechanism.
     */
    EvtQ evtQ = null;

    /**
     * event queue lock obj
     */
    Object evtLock = new Object();

    /**
     * Control package name
     */
    protected final static String pkgName = "javax.microedition.media.control.";

    /**
     * Centralized control management with string constants for each
     * implemented Control.
     * <p>
     * For adding a new control interfaces, follow the following steps:
     * <ol>
     *  <li>Add new control name here. If it is not in the package
     *     javax.microedition.media.control, then the full package
     *     name of the control must be used.
     *  <li>Add the control's name field to the allJsr135Ctrls array (see below)
     * </ol>
     * <p>
     * Size note: it would be space saving to declare the array allJsr135Ctrls
     * with the strings directly and not define the strings constants here.
     * However, it is more convenient for descendants to use
     * these constants, instead of e.g.
     * <code>&nbsp;&nbsp;&nbsp;allJsr135Ctrls[4]; // RateControl</code>
     *
     * @see    #getControls()
     * @see    #doGetControl(String)
     * @see    #allJsr135Ctrls
     */

    protected final static String fpcName = "FramePositioningControl";
    /**
     *  Description of the Field
     */
    protected final static String mdcName = "MetaDataControl";
    /**
     *  Description of the Field
     */
    protected final static String micName = "MIDIControl";
    /**
     *  Description of the Field
     */
    protected final static String picName = "PitchControl";
    /**
     *  Description of the Field
     */
    protected final static String racName = "RateControl";
    /**
     *  Description of the Field
     */
    protected final static String recName = "RecordControl";
    /**
     *  Description of the Field
     */
    protected final static String stcName = "StopTimeControl";
    /**
     *  Description of the Field
     */
    protected final static String tecName = "TempoControl";
    /**
     *  Description of the Field
     */
    protected final static String guiName = "GUIControl";
    /**
     *  Description of the Field
     */
    protected final static String vicName = "VideoControl";
    /**
     *  Description of the Field
     */
    protected final static String rtspName = "RtspControl";

    /**
     *  Description of the Field
     */
    protected final static String tocName = "ToneControl";
    /**
     *  Description of the Field
     */
    protected final static String dtocName = "com.sun.mmedia.control.DualToneControl";
    /**
     *  Description of the Field
     */
    protected final static String vocName = "VolumeControl";

    /**
     * An array containing all available JSR-135 controls in Players
     * extending BasicPlayer.
     */
    private final static String[] allJsr135Ctrls = {
        fpcName,
        mdcName,
        micName,
        picName,
        racName,
        recName,
        stcName,
        tecName,
        guiName,
        vicName,
        rtspName,
        tocName,
        dtocName,
        vocName,
        };

    /**
     * An array containing all available controls
     * in Players extending BasicPlayer, including JSR-234 controls
     * if available. 
     * DO NOT USE DIRECTLY! Use getPossibleControlNames() instead.
     */
    private static String[] allCtrls;
    
    /**
     * Locator string
     */
    protected String locator;

    /**
     * The input DataSource
     */
    protected DataSource source;

    /**
     * The input SourceStream from the DataSource
     */
    protected SourceStream stream;

    /**
     * The Player's TimeBase.
     */
    private TimeBase timeBase = this;

    /**
     * For StopTimeControl - initially reset
     */
    protected long stopTime = StopTimeControl.RESET;

    /**
     * the default size of the event queue
     * can be overridden by descendants
     */
    int eventQueueSize = 20;

    /**
     * flag to prevent delivering events after the CLOSED event
     */
    private boolean closedDelivered;

    /**
     *Constructor for the BasicPlayer object
     */
    protected BasicPlayer() {
        hNative            = 0;
        mediaFormat        = MEDIA_FORMAT_UNKNOWN;
        handledByDevice    = false;
        pID                = 0;

        // Initialize sysOffset to the current time.
        // This is used for TimeBase calculations.
        sysOffset = System.currentTimeMillis() * 1000L;
    }

    /**
     * Check to see if the Player is closed.  If the
     * unrealized boolean flag is true, check also to
     * see if the Player is UNREALIZED.
     *
     * @param  unrealized  Description of the Parameter
     */
    protected void chkClosed(boolean unrealized) {
        if (state == CLOSED || (unrealized && state == UNREALIZED)) {
            throw new IllegalStateException("Can't invoke the method at the " +
                (state == CLOSED ? "closed" : "unrealized") +
                " state");
        }
    }


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
     * @param  count                         indicates the number of times the content will be
     * played.  1 is the default.  0 is invalid.  -1 indicates looping
     * indefintely.
     */
    public void setLoopCount(int count) {
        chkClosed(false);
        
        if (state == STARTED) {
            throw new IllegalStateException("setLoopCount");
        }
        
        if (count == 0 || count < -1) {
            throw new IllegalArgumentException("setLoopCount");
        }

        loopCountSet = count;
        loopCount = count;
    }


    /**
     *  Description of the Method
     *
     * @param  count  Description of the Parameter
     */
    protected void doSetLoopCount(int count) {
    }

    protected void doReceiveRSL()
    {
        if (Logging.REPORT_LEVEL <= Logging.INFORMATION) {
            Logging.report(Logging.INFORMATION, LogChannels.LC_MMAPI,
            "[basic] received RSL");
        }
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
     * @exception  MediaException         Thrown if the <code>Player</code> cannot
     * be realized.
     */
    public synchronized void realize() throws MediaException {
        chkClosed(false);

        if (state >= REALIZED) {
            return;
        }

        // BasicPlayer only handles the first stream from
        // a DataSource.
        if ((source != null) && (stream == null)) {
            stream = source.getStreams()[0];
        } else {
            if (!isCapturePlayer() && !isDevicePlayer()) {
                state = UNREALIZED;
                throw new MediaException("Unable to realize");
            }
        }

        doRealize();
        
        state = REALIZED;
    }

    protected boolean hasToneSequenceSet = false;

    protected boolean isCapturePlayer()
    {
        return (mediaFormat.equals(MEDIA_FORMAT_CAPTURE_AUDIO) ||
                mediaFormat.equals(MEDIA_FORMAT_CAPTURE_VIDEO) ||
                mediaFormat.equals(MEDIA_FORMAT_CAPTURE_RADIO) );
    }

    protected boolean isDevicePlayer()
    {
        return (mediaFormat.equals(MEDIA_FORMAT_DEVICE_MIDI) ||
                mediaFormat.equals(MEDIA_FORMAT_DEVICE_TONE));
    }

    /**
     * Subclasses need to implement this to realize
     * the <code>Player</code>.
     *
     * @exception  MediaException  Description of the Exception
     */
    protected abstract void doRealize() throws MediaException;


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
     * @exception  MediaException         Thrown if the <code>Player</code> cannot
     * be prefetched.
     */
    public synchronized void prefetch() throws MediaException {

        if (state >= PREFETCHED) {
            return;
        }

        if (state < REALIZED) {
            realize();
        }

        doPrefetch();

        VolumeControl vc = (VolumeControl)doGetControl(pkgName + vocName);
        if (vc != null && (vc.getLevel() == -1)) {
               vc.setLevel(100);
        }

        state = PREFETCHED;
    }


    /**
     * Subclasses need to implement this to prefetch
     * the <code>Player</code>.
     *
     * @exception  MediaException  Description of the Exception
     */
    protected abstract void doPrefetch() throws MediaException;


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
     * @exception  MediaException         Thrown if the <code>Player</code> cannot
     * be started.
     */
    public synchronized void start() throws MediaException {

        if (state >= STARTED) {
            return;
        }

        if (state < REALIZED) {
            realize();
        }

        if (state < PREFETCHED) {
            prefetch();
        }

        if (isDevicePlayer() && !hasToneSequenceSet) {
            sendEvent(PlayerListener.STARTED, new Long(0));
            sendEvent( PlayerListener.END_OF_MEDIA, new Long(0) );
            return;
        }

        // Update the time base to use the player's
        // media time before starting.
        updateTimeBase(true);

        // Check for any preset stop time.
        if (stopTime != StopTimeControl.RESET) {
            if (stopTime <= getMediaTime()) {
                satev();
                // Send STOPPED_AT_TIME event
                return;
            }
        }

        // If it's at the EOM, it will automatically
        // loop back to the beginning.
        if (EOM) {
            try {
            setMediaTime(0);
            } catch(Exception e) {
                // do nothing...
            }
        }

        if (!doStart()) {
            throw new MediaException("start");
        }

        state = STARTED;
        sendEvent(PlayerListener.STARTED, new Long(getMediaTime()));

        // Finish any pending startup stuff in subclass
        // Typically used to start any threads that might potentially
        // generate events before the STARTED event is delivered
        doPostStart();

    }


    /**
     * Subclasses need to implement this start
     * the <code>Player</code>.
     *
     * @return    Description of the Return Value
     */
    protected abstract boolean doStart();


    /**
     * Subclasses can override this method to do the actual starting
     * of worker threads.
     */
    protected void doPostStart() {
    }


    protected void continueDownload() {
    }
    
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
     * @exception  MediaException         Thrown if the <code>Player</code>
     * cannot be stopped.
     */
    public synchronized void stop() throws MediaException {
        chkClosed(false);

        loopAfterEOM = false;

        if (state < STARTED) {
            return;
        }

        doPreStop();
        doStop();

        // Update the time base to use the system time
        // before stopping.
        updateTimeBase(false);

        state = PREFETCHED;
        sendEvent(PlayerListener.STOPPED, new Long(getMediaTime()));
    }

    /**
     * Subclasses need to implement this to realize
     * the <code>Player</code>.
     *
     * @exception  MediaException  Description of the Exception
     */
    protected abstract void doStop() throws MediaException;

    /**
     * Subclasses can override this method to do pre stop works
     */
    protected void doPreStop() {
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
     */
    public synchronized void deallocate() {
        chkClosed(false);

        loopAfterEOM = false;

        if (state < PREFETCHED) {
            return;
        }

        if (state == STARTED) {
            try {
                stop();
            } catch (MediaException e) {
                // Not much we can do here.
            }
        }

        doDeallocate();

        if (stream != null) {
            // if stream is not seekable, just return
            if (NOT_SEEKABLE != stream.getSeekType()) {
                try {
                    // seek to start position
                    stream.seek(0);
                } catch(IOException e) {
                    // System.out.println("[direct] doDeallocate seek IOException");
                }
            }
        }

        state = REALIZED;
    }


    /**
     * Subclasses need to implement this to deallocate
     * the <code>Player</code>.
     */
    protected abstract void doDeallocate();


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
    public synchronized void close() {
        if (state == CLOSED) {
            return;
        }

        if (state == STARTED) {
            try {
                stop();
            } catch (MediaException e) {
                // Not much we can do here.
            }
        }

        doDeallocate();
        doClose();

        state = CLOSED;

        if (source != null) {
            source.disconnect();
        }

        sendEvent(PlayerListener.CLOSED, null);
    }


    /**
     * Subclasses need to implement this to close
     * the <code>Player</code>.
     */
    protected abstract void doClose();


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
     * @param  now                        The new media time in microseconds.
     * @return                            The actual media time set in microseconds.
     * @exception  MediaException         Thrown if the media time
     * cannot be set.
     * @see                               #getMediaTime
     */
    public synchronized long setMediaTime(long now) throws MediaException {
        chkClosed(true);

        if (now < 0) {
            now = 0;
        }

        // Time-base-time needs to be updated if player is started.
        if (state == STARTED) {
            origin = getTime();
        }

        long theDur = doGetDuration();
        if ((theDur != TIME_UNKNOWN) && (now > theDur)) {
            now = theDur;
        }

        long rtn = doSetMediaTime(now);
        EOM = false;

        // Time-base-time needs to be updated if player is started.
        if (state == STARTED) {
            offset = rtn;
        }

        return rtn;
    }


    /**
     * Subclasses need to implement this to set the media time
     * of the <code>Player</code>.
     *
     * @param  now                 Description of the Parameter
     * @return                     Description of the Return Value
     * @exception  MediaException  Description of the Exception
     */
    protected abstract long doSetMediaTime(long now) throws MediaException;


    /**
     * Gets this <code>Player</code>'s current <i>media time</i>.
     * If the <i>media time</i> cannot be determined,
     * <code>getMediaTime</code> returns <code>TIME_UNKNOWN</code>.
     *
     * @return                            The current <i>media time</i> in microseconds or
     * <code>TIME_UNKNOWN</code>.
     * @see                               #setMediaTime
     */
    public long getMediaTime() {
        chkClosed(false);
        return doGetMediaTime();
    }


    /**
     * Subclasses need to implement this to get the media time
     * of the <code>Player</code>
     *
     * @return    Description of the Return Value
     */
    protected abstract long doGetMediaTime();


    /**
     * Gets the current state of this <code>Player</code>.
     * The possible states are: <i>UNREALIZED</i>,
     * <i>REALIZED</i>, <i>PREFETCHED</i>, <i>STARTED</i>, <i>CLOSED</i>.
     *
     * @return    The <code>Player</code>'s current state.
     */
    public int getState() {
        return state;
    }


    /**
     * Get the duration of the media.
     * The value returned is the media's duration
     * when played at the default rate.
     * <br>
     * If the duration cannot be determined (for example, the
     * <code>Player</code> is presenting live
     * media)  <CODE>getDuration</CODE> returns <CODE>TIME_UNKNOWN</CODE>.
     *
     * @return                            The duration in microseconds or <code>TIME_UNKNOWN</code>.
     */
    public long getDuration() {
        chkClosed(false);
        return doGetDuration();
    }


    /**
     * Subclasses need to implement this to get the duration
     * of the <code>Player</code>.
     *
     * @return    Description of the Return Value
     */
    protected abstract long doGetDuration();


    /**
     * Add a player listener for this player.
     *
     * @param  playerListener             the listener to add.
     * If <code>null</code> is used, the request will be ignored.
     * @see                               #removePlayerListener
     */
    public void addPlayerListener(PlayerListener playerListener) {
        chkClosed(false);
        if (playerListener != null) {
            listeners.addElement(playerListener);
        }
    }


    /**
     * Remove a player listener for this player.
     *
     * @param  playerListener             the listener to remove.
     * If <code>null</code> is used or the given
     * <code>playerListener</code> is not a listener for this
     * <code>Player</code>, the request will be ignored.
     * @see                               #addPlayerListener
     */
    public void removePlayerListener(PlayerListener playerListener) {
        chkClosed(false);
        listeners.removeElement(playerListener);
    }


    /**
     * Send event to player
     *
     * @param  evt      event type
     * @param  evtData  event data
     */
    public void sendEvent(String evt, Object evtData) {
        //  There's always one listener for EOM -- itself.
        //  "Deliver" the CLOSED event so that the evtQ thread may terminate
        if (listeners.size() == 0 && !evt.equals( PlayerListener.END_OF_MEDIA )
                                  && !evt.equals( PlayerListener.CLOSED )
                                  && !evt.equals(PlayerListener.ERROR)) {
            return;
        }

        // Safeguard against sending events after CLOSED event to avoid
        // deadlock in event delivery thread.
        if (closedDelivered) {
            return;
        }

        // Deliver the event to the listeners.
        synchronized (evtLock) {
            if (evtQ == null) {
                evtQ = new EvtQ(this);
            }
            evtQ.sendEvent(evt, evtData);
            // try to let listener run
            Thread.currentThread().yield();
        }

        if (evt == PlayerListener.CLOSED || evt == PlayerListener.ERROR) {
            closedDelivered = true;
        }
    }


    /**
     *  Description of the Method
     */
    synchronized void doLoop() {
        // If a loop count is set, we'll loop back to the beginning.
        if ((loopCount > 1) || (loopCount == -1)) {
            try {
                setMediaTime(0);
            } catch(MediaException e){ 
                // Do nothing...
            }
            try {
                if (loopCount > 1) {
                    loopCount--;
                }
                start();
            } catch (MediaException ex) {
                // System.out.println("[basic] doLoop exception " + ex.getMessage());
                loopCount = 1;
            }
        } else if (loopCountSet > 1) {
            loopCount = loopCountSet;
        }

        loopAfterEOM = false;
    }


    // "final" to verify that no subclass overrides getControls.
    // can be removed if overload necessary
    /**
     *  Gets the controls attribute of the BasicPlayer object
     *
     * @return    The controls value
     */
    public final Control[] getControls() {
        chkClosed(true);

        Vector v = new Vector(3);
        // average maximum number of controls
        
        String [] ctrlNames = getPossibleControlNames();
        for (int i = 0; i < ctrlNames.length; i++) {
            Object c = getControl(ctrlNames[i]);
            if ((c != null) && !v.contains(c)) {
                v.addElement(c);
            }
        }
        Control[] ret = new Control[v.size()];
        v.copyInto(ret);
        return ret;
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
        chkClosed(true);

        if (type == null) {
            throw new IllegalArgumentException();
        }

        // Prepend the package name if the type given does not
        // have the package prefix.
        if (type.indexOf('.') < 0) {
            // for non-fully qualified control names,
            // look up the package in the allJsr135Ctrls array
            for (int i = 0; i < allJsr135Ctrls.length; i++) {
                if (allJsr135Ctrls[i].equals(type)) {
                    // standard controls are specified
                    // without package name in allJsr135Ctrls
                    return doGetControl(pkgName + type);
                } else if (allJsr135Ctrls[i].endsWith(type)) {
                    // non-standard controls are with
                    // full package name in allJsr135Ctrls
                    return doGetControl(allJsr135Ctrls[i]);
                }
            }
        }
        return doGetControl(type);
    }


    /**
     * Returns the array containing all available JSR-135 and JSR-234 controls
     * in Players extending BasicPlayer. 
     */
    private static String [] getPossibleControlNames()
    {
        if( null == allCtrls )
        {
            String [] ammsCtrlNames = 
                    Jsr234Proxy.getInstance().getJsr234PlayerControlNames();
            allCtrls = new 
                    String [ allJsr135Ctrls.length + ammsCtrlNames.length ];
            int i = 0;
            for( i = 0; i < allJsr135Ctrls.length; i++ )
            {
                allCtrls[ i ] = allJsr135Ctrls[ i ];
            }
            for( int j = 0; i < allCtrls.length; i++, j++ )
            {
                allCtrls[ i ] = ammsCtrlNames[ j ];
            }
        }
        return allCtrls;
        
    }
    
    /**
     * The worker method to actually obtain the control.
     *
     * @param  type  the class name of the <code>Control</code>.
     * @return       <code>Control</code> for the class or interface
     * name.
     */
    protected abstract Control doGetControl(String type);

    /**
     * Implementation method for VolumeControl
     *
     * @param  ll  Description of the Parameter
     * @return     Description of the Return Value
     */
    int doSetLevel(int ll) {
        return ll;
    }

    /**
     * MMAPI Full specific methods.
     *
     * @param  source              The new source value
     * @exception  IOException     Description of the Exception
     * @exception  MediaException  Description of the Exception
     */

    /**
     * Sets the media source
     *
     * @param  source              The new source value
     * @exception  MediaException  Description of the Exception
     */
    public void setSource(DataSource source)
         throws MediaException {
        this.source = source;
    }

    /**
     * Sets the media locator
     * 
     * @param locator               The new source locator
     */
    public void setLocator(String locator) {
        this.locator = locator;
    }

    /**
     * Gets the media locator
     * 
     * @returns locator     Returns current Media Locator
     */
    public String getLocator() {
        if (locator != null) {
            return locator;
        } else if(source != null) {
            return source.getLocator();
        }
        return null;
    }

    /**
     * Sets the <code>TimeBase</code> for this <code>Player</code>.
     * <p>
     * A <code>Player</code> has a default <code>TimeBase</code> that
     * is determined by the implementation.
     * To reset a <code>Player</code> to its default
     * <code>TimeBase</code>, call <code>setTimeBase(null)</code>.
     *
     * @param  master                     The new <CODE>TimeBase</CODE> or
     * <CODE>null</CODE> to reset the <code>Player</code>
     * to its default <code>TimeBase</code>.
     * @exception  MediaException         Thrown if
     * the specified <code>TimeBase</code> cannot be set on the
     * <code>Player</code>.
     * @see                               #getTimeBase
     */
    public void setTimeBase(TimeBase master) throws MediaException {
        chkClosed(true);
        if (state == STARTED) {
            throw new IllegalStateException("Cannot call setTimeBase on a player in the STARTED state");
        }

        if (master == null) {
            return;
        }
        if (master != this) {
            throw new MediaException("Incompatible TimeBase");
        }
    }
    
    /**
     * Gets the <code>TimeBase</code> that this <code>Player</code> is using.
     *
     * @return                            The <code>TimeBase</code> that this <code>Player</code> is using.
     * @see                               #setTimeBase
     */
    public TimeBase getTimeBase() {
        chkClosed(true);
        return timeBase;
    }

    /**
     * Get the content type of the media that's
     * being played back by this <code>Player</code>.
     * <p>
     * See <a href="Manager.html#content-type">content type</a>
     * for the syntax of the content type returned.
     *
     * @return                            The content type being played back by this
     * <code>Player</code>.
     */
    public String getContentType() {
        chkClosed(true);

        if (source == null) {
            // Call helper function to get a content type
            return DefaultConfiguration.getContentType(locator);
        } else {
            return source.getContentType();
        }
    }

    /**
     * StopTimeControl base implementation
     *
     * @return    The stopTime value
     */

    // NOTE: Right now, BasicPlayer implements StopTimeControl, but only
    // provides partial implementation. That means each individual player
    // must complete the implementation. I.E., somehow it must keep polling
    // current media time and compare it to the set stop time. If the time
    // is reached, it should call doStop and satev.

    /**
     * Returns the current stop time
     *
     * @return    The stopTime value
     */
    public long getStopTime() {
        return stopTime;
    }


    /**
     * Sets the stop time
     *
     * @param  time  The new stopTime value
     */
    public synchronized void setStopTime(long time) {
        if (state == STARTED) {
            /*
             *  If the stop time has already been set and its being set again,
             *  throw an exception
             */
            if (stopTime != StopTimeControl.RESET && time != StopTimeControl.RESET) {
                throw new IllegalStateException("StopTime already set");
            }
            /*
             *  If the new stop time is before the current media time,
             *  stop the player and send an event
             */
            if (time < getMediaTime()) {
                try {
                    doPreStop();
                    doStop();
                } catch (MediaException e) {
                    // Not much we can do here.
                }
                satev();
                /*
                 *  Send STOPPED_AT_TIME event
                 */
                return;
            }
        }
        stopTime = time;
        doSetStopTime(stopTime);
    }


    /**
     * Subclasses can override this to be notified of a change
     * in the stop time
     *
     * @param  time  Description of the Parameter
     */
    protected void doSetStopTime(long time) {
    }


    /**
     * Send STOPPED_AT_TIME event. Call this after stopping the player
     */
    protected void satev() {
        // Update the time base to use the system time
        // before stopping.
        updateTimeBase(false);
        state = PREFETCHED;
        stopTime = StopTimeControl.RESET;
        sendEvent(PlayerListener.STOPPED_AT_TIME, new Long(getMediaTime()));
    }

    /**
     * TimeBase related functions.
     */
    private long origin = 0;
    private long offset = 0;
    private long time = 0;
    private long sysOffset = 0;
    private boolean useSystemTime = true;
    private Object timeLock = new Object();


    /**
     * Get the current time of this <code>TimeBase</code>.  The values
     * returned must be non-negative and non-decreasing over time.
     *
     * @return    the current <code>TimeBase</code> time in microseconds.
     */
    public long getTime() {
        synchronized (timeLock) {
            if (useSystemTime) {

                time = origin + (System.currentTimeMillis() * 1000L -
                    sysOffset) - offset;
            } else {

                time = origin + getMediaTime() - offset;
            }
            return time;
        }
    }


    /**
     * This method needs to be called when the Player transitions
     * back and forth the STARTED and STOPPED states.  This is
     * to make sure that the correct time base time can be computed.
     *
     * @param  started  Description of the Parameter
     */
    public void updateTimeBase(boolean started) {
        synchronized (timeLock) {
            origin = getTime();
            useSystemTime = !started;
            if (started) {
                // Computes the starting offset based on the media time.
                offset = getMediaTime();
            } else {
                // Computes the starting offset based on the system time.
                offset = System.currentTimeMillis() * 1000L - sysOffset;
            }
        }
    }

    /**
     * The value returned by <code>getSeekType</code> indicating that this
     * <code>SourceStream</code> is not seekable.
     * <p>
     * Value 0 is assigned to <code>NOT_SEEKABLE</code>.
     */
    int NOT_SEEKABLE = SourceStream.NOT_SEEKABLE;

    /**
     * The value returned by <code>getSeekType</code> indicating that this
     * <code>SourceStream</code> can be seeked only to the beginning
     * of the media stream.
     * <p>
     * Value 1 is assigned to <code>SEEKABLE_TO_START</code>.
     */
    int SEEKABLE_TO_START = SourceStream.SEEKABLE_TO_START;

    /**
     * The value returned by <code>getSeekType</code> indicating that this
     * <code>SourceStream</code> can be seeked anywhere within the media.
     * <p>
     * Value 2 is assigned to <code>RANDOM_ACCESSIBLE</code>.
     */
    int RANDOM_ACCESSIBLE = SourceStream.RANDOM_ACCESSIBLE;


    /**
     * Reads <code>len</code>
     * bytes from the first StreamSource of the DataSource (member
     * variable <code>stream</code>.
     * <p>
     * This method
     * blocks until one of the following conditions
     * occurs:<p>
     * <ul>
     * <li><code>len</code> bytes
     * of input data are available, in which case
     * a normal return is made.
     *
     * <li>End of file
     * is detected, in which case an <code>MediaException</code>
     * is thrown.
     *
     * <li>An I/O error occurs, in
     * which case an <code>IOException</code> is thrown.
     * </ul>
     * <p>
     * If <code>len</code> is zero,
     * then no bytes are read. Otherwise, the first
     * byte read is stored into element <code>b[off]</code>,
     * the next one into <code>b[off+1]</code>,
     * and so on. The number of bytes read is,
     * at most, equal to <code>len</code>.
     *
     * @param  b                the buffer into which the data is read.
     * @param  off              an int specifying the offset into the data.
     * @param  len              an int specifying the number of bytes to read.
     * @return                  Description of the Return Value
     * @exception  IOException  if an I/O error occurs.
     */
    protected int readFully(byte b[], int off, int len) throws IOException {
        int n = 0;
        while (n < len) {
            int count = stream.read(b, off + n, len - n);
            if (count < 0) {
                throw new IOException("premature end of stream");
            }
            n += count;
        }
        return len;
    }


    private final static int MAX_SKIP = 2048;
    private static byte[] skipArray;


    /**
     *  Description of the Method
     *
     * @param  numBytes         Description of the Parameter
     * @return                  Description of the Return Value
     * @exception  IOException  Description of the Exception
     */
    protected long skipFully(int numBytes) throws IOException {
        long target = stream.tell() + numBytes;
        if (stream.getSeekType() == SourceStream.RANDOM_ACCESSIBLE) {
            if (stream.seek(target) != target) {
                throw new IOException("skipped past end");
            }
            return numBytes;
        }

        if (numBytes < 0) {
            throw new IOException("bad param");
        }

        // Allocate memory for skip array
        int toSkip = numBytes;
        int min = numBytes;
        if (min > MAX_SKIP) {
            min = MAX_SKIP;
        }
        if (skipArray == null || skipArray.length < min) {
            skipArray = new byte[min];
        }

        // Skip over numBytes
        while (toSkip > 0) {
            min = toSkip;
            if (min > MAX_SKIP) {
                min = MAX_SKIP;
            }

            if (stream.read(skipArray, 0, min) < min) {
                throw new IOException("skipped past end");
            }
            toSkip -= min;
        }

        return numBytes;
    }


    /**
     * In source stream, seek to a particular position
     *
     * @param  where            the position intended to seek to.
     * @return                  the actual position seeked to.
     * @exception  IOException  Description of the Exception
     */
    protected long seekStrm(long where) throws IOException {
        return stream.seek(where);
    }


    /**
     *  Gets the seekType attribute of the BasicPlayer object
     *
     * @return    The seekType value
     */
    protected int getSeekType() {
        return stream.getSeekType();
    }
}

/**
 * The thread that's responsible for delivering Player events.
 * This class lives for only 5 secs.  If no event comes in
 * 5 secs, it will exit.
 *
 * @created    January 13, 2005
 */
class EvtQ extends Thread {
    /**
     * the player instance
     */
    private BasicPlayer p;
    /**
     * event type array
     */
    private String[] evtQ;
    /**
     * event data array
     */
    private Object[] evtDataQ;
    /**
     * head and tail pointer of the event queue
     */
    private int head, tail;


    /**
     * The constructor
     *
     * @param  p  the instance of BasicPlayer intending to post event to
     *        this event queue.
     */
    EvtQ(BasicPlayer p) {
        this.p = p;
        evtQ = new String[p.eventQueueSize];
        evtDataQ = new Object[p.eventQueueSize];
        start();
    }


    /**
     * Put an event in the event queue and wake up the thread to
     * deliver it.  If the event queue is filled, block.
     *
     * @param  evt      Description of the Parameter
     * @param  evtData  Description of the Parameter
     */
    synchronized void sendEvent(String evt, Object evtData) {

        // Wait if the event queue is full.
        // This potentially will block the Player's main thread.
        while ((head + 1) % p.eventQueueSize == tail) {
            try {
                wait(1000);
            } catch (Exception e) {}
        }
        evtQ[head] = evt;
        evtDataQ[head] = evtData;
        if (++head == p.eventQueueSize) {
            head = 0;
        }
        notifyAll();
        // try to let other threads run
        Thread.currentThread().yield();
    }


    /**
     * Event handling thread.
     */
    public void run() {

        String evt = "";
        Object evtData = null;
        boolean evtToGo = false;
        // true if there is an event to send
        // true if at least one event is sent,
        // in case that posting the initial event
        // takes a long time
        boolean evtSent = false;

        for (; ; ) {

            synchronized (this) {

                // If the queue is empty, we'll wait
                if (head == tail) {
                    try {
                        wait(1000);
                    } catch (Exception e) {
                    }
                }
                if (head != tail) {
                    evt = evtQ[tail];
                    evtData = evtDataQ[tail];
                    // For garbage collection.
                    evtDataQ[tail] = null;
                    evtToGo = true;
                    if (++tail == p.eventQueueSize) {
                        tail = 0;
                    }
                    notifyAll();
                } else {
                    evtToGo = false;
                }

            }
            // synchronized this

            if (evtToGo) {
                // First, check and handle EOM.
                if (evt == PlayerListener.END_OF_MEDIA) {
                    synchronized (p) {
                        p.EOM = true;
                        p.loopAfterEOM = false;
                      
                        if (p.state > Player.PREFETCHED) {
                            p.updateTimeBase(false);
                            p.state = Player.PREFETCHED;
                            if (p.loopCount > 1 || p.loopCount == -1) {

                                p.loopAfterEOM = true;
                            }
                        }
                    }
                }

                // Notify the PlayerListeners.
                Enumeration en;
                synchronized (p.listeners) {
                    en = p.listeners.elements();
                }
                
                PlayerListener l;
                
                Player src_p = ( null != p.notificationSource ) 
                               ? p.notificationSource 
                               : p;
                                    
                while (en.hasMoreElements()) {
                    try {
                        l = (PlayerListener)en.nextElement();
                        l.playerUpdate(src_p, evt, evtData);
                    } catch (Exception e) {
                        e.printStackTrace();
                        System.err.println("Error in playerUpdate " +
                            "while delivering event "
                             + evt + ": " + e);
                    }
                }

                if (p.loopAfterEOM) {
                    // We'll need to loop back because looping was set.
                    p.doLoop();
                }

                evtSent = true;

            }

            // We'll exit the event thread if we have already sent one
            // event and there's no more event after 5 secs; or if the
            // Player is closed.

            if (evt == PlayerListener.CLOSED || evt == PlayerListener.ERROR) {
                // will cause a deadlock if the queue
                // is full
                synchronized (p.evtLock) {
                    p.evtQ = null;
                    break;
                    // Exit the event thread.
                }
            }

            synchronized (this) {
                if (head == tail && evtSent && !evtToGo) {
                    synchronized (p.evtLock) {
                        p.evtQ = null;
                        break;
                        // Exit the event thread.
                    }
                }
            }
        }
    }
}


