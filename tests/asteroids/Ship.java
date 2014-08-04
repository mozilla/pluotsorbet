/**
 * Copyright 2001 Jean-Francois Doue
 *
 * This file is part of Asteroid Zone. Asteroid Zone is free software;
 * you can redistribute it and/or modify it under the terms of the
 * GNU General Public License as published by the Free Software Foundation;
 * either version 2 of the License, or (at your option) any later version.
 * Asteroid Zone is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * You should have received a copy of the GNU General Public License
 * along with Asteroid Zone; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place, Suite 330,
 * Boston, MA  02111-1307  USA
 */

package asteroids;

import javax.microedition.lcdui.*;
import java.util.*;

/**
 * Class to implement the player's spaceship. The spaceship
 * can rotate, move, shoot and teleport itself.
 * @author Jean-Francois Doue
 * @version 1.4, 2002/10/14
 */
public class Ship extends Mobile {
    /**
     * False if the ship has been shot.
     */
    public boolean isAlive;

    /**
     * The orientation of the ship.
     */
    public byte angle;

    /**
     * The X coordinates of an array of points representing the ship
     * at various orientations.
     */
    public static byte[] xcoords;

    /**
     * The Y coordinates of an array of points representing the ship
     * at various orientations.
     */
    public static byte[] ycoords;

    /**
     * The size of the ship (in pixels).
     */
    public static byte radius;

    /**
     * The ship singleton
     */
    public static Ship ship;


    private byte _explosionFrame;
    private byte _latency;       // To limit the ship's firing power

    static {
        // Instantiate the ship.
        ship = new Ship();

        final byte[] pointx = {5,-3, 0,-3};
        final byte[] pointy = {0, 4, 0,-4};

        radius = (byte)(5 * ratioNum / ratioDenom);
        xcoords = new byte[128];
        ycoords = new byte[128];
        for (int i = 0; i < 32; i++) {
            int offset = i << 2;
            for (int j = 0; j < 4; j++) {
                xcoords[offset + j] = (byte)(((pointx[j] * cos[i] - pointy[j] * sin[i]) * ratioNum / ratioDenom) >> 6);
                ycoords[offset + j] = (byte)(((pointx[j] * sin[i] + pointy[j] * cos[i]) * ratioNum / ratioDenom) >> 6);
            }
        }
    }

    public Ship() {
    }

    /**
     * Positions the ship at its original location, orientation
     * and velocity.
     */
    public final void reset() {
        angle = 0;
        moveTo(Mobile.width >> 1, Mobile.height >> 1);
        vx = 0;
        vy = 0;
        isAlive = true;
        rotate(0);
    }

    /**
     * Called when the ship collides with an asteroid.
     */
    public final void explode() {
        isAlive = false;
        _explosionFrame = 0;
    }

    /**
     * Move the ship to its next position.
     */
    public final void move() {
        if (isAlive) {
           xold = x;
           yold = y;
           _x += vx;
           _y += vy;
           x = _x >> 8;
           y = _y >> 8;

           // If a border has been hit, wrap the trajectory around
           // the screen. The new origin is the projection of the
           // intersection point on the opposite border.
           if (x <= 0) {
               moveTo(width - 2, y);
           } else if (x >= width - 1) {
               moveTo(1, y);
           } else if (y <= 0) {
               moveTo(x, height - 2);
           } else if (y >= height - 1) {
               moveTo(x, 1);
           }

            if (_latency > 0) {
                _latency--;
            }
         } else {
            _explosionFrame++;
            if (_explosionFrame > 20) {
                reset();
            }
        }
    }

    /**
     * Changes the orientation of the ship.
     */
    public final void rotate(int direction) {
        if (isAlive) {
            angle += direction;
            if (angle > 31) {
            angle -= 32;
         } else if (angle < 0) {
            angle += 32;
         }
        }
    }

    /**
     * Adds velocity to the ship.
     */
    public final void burn() {
        if (isAlive) {
         int newvx = vx + (cos[angle] << 1);
         int newvy = vy + (sin[angle] << 1);
            // clamp the ship velocity.
            if (newvx * newvx + newvy * newvy < 262144) {
                vx = newvx;
                vy = newvy;
            }
        }
    }

    /**
     * Shoots a rocket.
     */
    public final Rocket shoot(Pool rockets) {
        if (isAlive) {
            if (_latency == 0) {
                Rocket rocket = (Rocket)rockets.addNewObject();
                if (rocket != null) {
                    int offset = angle << 2;
                    rocket.init(xcoords[offset] + x, ycoords[offset] + y, angle, vx, vy);

                    // Prevent the ship for shooting again for 3 frames
                    _latency = 3;
                }
            }
        }
        return null;
    }

    /**
     * Teleports the ship to another lcoation.
     */
    public final void teleport() {
        if (isAlive) {
            moveTo(Math.abs(Game.random.nextInt()) % width, Math.abs(Game.random.nextInt()) % height);
        }
    }

    /**
     * Draws the ship at the specified location and orientation.
     */
    public static void draw(int orientation, int xpos, int ypos, Graphics g) {
        int offset = orientation << 2;
        g.drawLine(xcoords[offset] + xpos, ycoords[offset] + ypos,
                   xcoords[offset + 1] + xpos, ycoords[offset + 1] + ypos);
        g.drawLine(xcoords[offset + 1] + xpos, ycoords[offset + 1] + ypos,
                   xcoords[offset + 2] + xpos, ycoords[offset + 2] + ypos);
        g.drawLine(xcoords[offset + 2] + xpos, ycoords[offset + 2] + ypos,
                   xcoords[offset + 3] + xpos, ycoords[offset + 3] + ypos);
        g.drawLine(xcoords[offset + 3] + xpos, ycoords[offset + 3] + ypos,
                   xcoords[offset + 0] + xpos, ycoords[offset + 0] + ypos);
    }

    /**
     * Draws the ship in the specified graphic context.
     */
    public final void draw(Graphics g) {
        if (isAlive) {
            draw(angle, x, y, g);
        } else {
            if (_explosionFrame < 6) {
                g.setColor(255, 0, 0);
                int radius = _explosionFrame;
                g.drawArc(x - radius, y - radius, radius << 1, radius << 1, 0, 360);
                radius = _explosionFrame + 2;
                g.drawArc(x - radius, y - radius, radius << 1, radius << 1, 0, 360);
                radius = _explosionFrame + 4;
                g.drawArc(x - radius, y - radius, radius << 1, radius << 1, 0, 360);
            }
        }
    }
}
