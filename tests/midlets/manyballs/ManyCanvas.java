/*
 * 
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

package com.sun.midp.demos.manyballs;

import javax.microedition.lcdui.*;

public class ManyCanvas extends javax.microedition.lcdui.Canvas {

    Display display;

    // a set of free roaming balls
    SmallBall[] balls;
    int numBalls;
    int width, height;
    boolean paused;
    static int NUM_HISTORY = 8;
    long times[] = new long[NUM_HISTORY];
    int times_idx;

    public ManyCanvas(Display d, int maxBalls) {

	display = d;		// save the display

	// initialize the array of balls
	balls = new SmallBall[maxBalls];
	
	width = getWidth();
	height = getHeight();

	// Start with one ball
	balls[0] = new SmallBall(this, 0, 0, width, height-12-20);
	numBalls = 1;
	paused = true;
    }

    /**
     * Draws the drawing frame (which also contains the ball) and the
     * controls.
     */
    String msg = null;
    protected void paint(Graphics g) {
        int x = g.getClipX();
        int y = g.getClipY();
        int w = g.getClipWidth();
        int h = g.getClipHeight();

	// Draw the frame 
	g.setColor(0xffffff);
	g.fillRect(x, y, w, h);

	// Draw each ball
	for (int i = 0; i < numBalls; i++) {
            if (balls[i].inside(x, y, x + w, y + h)) {
                balls[i].paint(g);
            }
	}

	g.setColor(0);
	g.drawRect(0, 0, width-1, height-1);

	long now = System.currentTimeMillis();
        String str = null;
        if (times_idx >= NUM_HISTORY) {
            long oldTime = times[times_idx % NUM_HISTORY];
            if (oldTime == now) {
                // in case of divide-by-zero
                oldTime = now - 1;
            }
            long fps = ((long)1000 * (long)NUM_HISTORY) / (now - oldTime);
            if (times_idx % 20 == 0) {
                str = numBalls + " Ball(s) " + fps + " fps";
            }
        } else {
            if (times_idx % 20 == 0) {
                str =  numBalls + " Ball(s)";
            }
        }

        if (msg != null) {
            g.setColor(0xffffff);
            g.setClip(0, height-14, width, height);
            g.fillRect(0, height-20, width-2, 18);

            g.setColor(0);
            g.drawString(msg, 5, height-14, 0);
            g.drawRect(0, 0, width-1, height-1);
            msg = null;
        }
        if (str != null) {
            /*
             * Do a complete repaint, so that the message will
             * be shown even in double-buffer mode.
             */
            repaint();
            msg = str;
        }

        times[times_idx % NUM_HISTORY] = now;
        ++ times_idx;

    }

    /**
     * Handle a pen down event.
     */
    public void keyPressed(int keyCode) {

	int action = getGameAction(keyCode);

	switch (action) {
	case LEFT:
	    // Reduce the number of threads
	    if (numBalls > 0) {

		// decrement the counter
		numBalls = numBalls - 1;

		// stop the thread and remove the reference to it
		balls[numBalls].stop = true;
		balls[numBalls] = null;
	    }
	    break;

	case RIGHT:
	    // Increase the number of threads
	    if (numBalls < balls.length) {

		// create a new ball and start it moving
		balls[numBalls] = 
                    new SmallBall(this, 0, 0, width, height-12-20);
		new Thread(balls[numBalls]).start();

		// increment the counter
		numBalls = numBalls + 1;
	    }
	    break;

	case UP:
	    // Make them move faster
	    SmallBall.faster();
	    break;

	case DOWN:
	    // Make them move slower
	    SmallBall.slower();
	    break;
	}
	repaint();
    }

    /**
     * Destroy
     */
    void destroy() {
	// kill all the balls and terminate
	for (int i = 0; i < balls.length && balls[i] != null; i++) {
	    balls[i].stop = true;

	    // enable the balls to be garbage collected
	    balls[i] = null;
	}
	numBalls = 0;
    }


    /*
     * Return whether the canvas is paused or not.
     */
    boolean isPaused() {
	return paused;
    }

    /**
     * Pause the balls by signaling each of them to stop.
     * The ball object still exists and holds the current position
     * of the ball.  It may be restarted later.
     * The thread will terminate.
     * TBD: is a join needed?
     */
    void pause() {
	if (!paused) {
	    paused = true;
	    for (int i = 0; i < balls.length && balls[i] != null; i++) {
		balls[i].stop = true;
	    }
	}
	repaint();
    }

    /*
     * Start creates a new thread for each ball and start it.
     */
    void start() {
	if (paused) {
	    paused = false;
	    display.setCurrent(this);
	    for (int i = 0; i < balls.length && balls[i] != null; i++) {
		Thread t = new Thread(balls[i]);
		t.start();
	    }
	}
	repaint();
    }
	

}
