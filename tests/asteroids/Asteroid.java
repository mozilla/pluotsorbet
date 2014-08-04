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
 * Class to implement an asteroid. Asteroids come in three sizes.
 * @author Jean-Francois Doue
 * @version 1.2, 2001/10/24
 */
public class Asteroid extends Mobile {
    /**
     * A size constant for small asteroids.
     */
    public static final byte SIZE_SMALL = 0;

    /**
     * A size constant for medium asteroids.
     */
    public static final byte SIZE_MEDIUM = 1;

    /**
     * A size constant for large asteroids.
     */
    public static final byte SIZE_LARGE = 2;

    /**
     * The x coordinates of the asteroid's vertices
     */
    public static byte[][] xcoords;

    /**
     * The y coordinates of the asteroid's vertices
     */
    public static byte[][] ycoords;

    /**
     * The radius of the asteroid.
     */
    public static byte[] radii;

    /**
     * The diameter of the asteroid.
     */
    public static int[] diameters;


    /**
     * Scan converted asteroids used for collision detection.
     */
    public static boolean[][][] masks;

    /**
     * The asteroids currently existing in the game.
     */
    public static Pool asteroids;

    private byte _angle;                  // Orientation of the asteroid.
    private byte _size;                   // Small, medium or large.
    private static byte[] _value = {5, 2, 1};   // Points earned when shot.
    private static final byte _FIELD_TOP = 0;
    private static final byte _FIELD_BOTTOM = 1;
    private static final byte _FIELD_LEFT = 2;
    private static final byte _FIELD_RIGHT = 3;


    static {
        // Create and populate the asteroid pool.
        Asteroid[] array = new Asteroid[20];
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = new Asteroid();
        }
        asteroids = new Pool(array);

        final int[] allRadii = {3, 5, 8};
        final byte[][] allAngles = {
          {3, 11, 19, 25, 3},       // small
          {2, 7, 17, 19, 26, 2},    // medium
          {1, 4, 9, 15, 21, 23, 29, 1} // large
        };
        radii = new byte[3];
        diameters = new int[3];
        xcoords = new byte[3][];
        ycoords = new byte[3][];
        masks = new boolean[3][][];
        for (int i = SIZE_SMALL; i <= SIZE_LARGE; i++) {
         // Precompute the asteroid vertices.
            radii[i] = (byte)(allRadii[i] * ratioNum / ratioDenom);
            byte[] angles = allAngles[i];
            byte[] pointx = new byte[angles.length];
            xcoords[i] = pointx;
            byte[] pointy = new byte[angles.length];
            ycoords[i] = pointy;
            for (int j = angles.length - 1; j >= 0; j--) {
                pointx[j] = (byte)((radii[i] * Mobile.cos[angles[j]]) >> 6);
                pointy[j] = (byte)((radii[i] * Mobile.sin[angles[j]]) >> 6);
            }

         // Scan convert the asteroid polygon.        
            diameters[i] = radii[i] << 1;
            masks[i] = new boolean[diameters[i]][];
            for (int j = 0; j < diameters[i]; j++) {
               masks[i][j] = new boolean[diameters[i]];
            }
            Geometry.scanConvertPolygon(masks[i], pointx, pointy);
        }
     }

    /**
     * Initializes an Asteroid instance to make it large, positionned
     * on a screen boundary and going in the direction of opposite boundary.
     */
    public static void randomInit(Asteroid asteroid) {
        int x = 0, y = 0;
        byte angle = 0;
        switch(Math.abs(Game.random.nextInt()) % 4) {
            case _FIELD_TOP:
                y = 1;
                x = Math.abs(Game.random.nextInt()) % width;
                angle = (byte)(15 + Math.abs(Game.random.nextInt()) % 15);
                break;
            case _FIELD_LEFT:
                x = 1;
                y =  Math.abs(Game.random.nextInt()) % height;
                angle = (byte)((23 + Math.abs(Game.random.nextInt()) % 15) % 32);
                break;
            case _FIELD_RIGHT:
                x = width - 1;
                y =  Math.abs(Game.random.nextInt()) % height;
                angle = (byte)(7 + Math.abs(Game.random.nextInt()) % 15);
                break;
            case _FIELD_BOTTOM:
                y = height - 1;
                x = Math.abs(Game.random.nextInt()) % width;
                angle = (byte)(Math.abs(Game.random.nextInt()) % 15);
                break;
        }
        asteroid.init(x, y, angle, SIZE_LARGE);
    }

    /**
     * Initializes a Asteroid instance by setting its position, angle
     * and size.
     */
    public final void init(int x, int y, byte angle, byte size) {
        _angle = angle;
        _size = size;
        moveTo(x, y);
        setVelocity(cos[angle] << 2, sin[angle] << 2);
    }

    public Asteroid() {
    }

    /**
     * Move the asteroid to its next position.
     */
    public static final void move() {
      for (int i = 0; i < asteroids.count; i++) {
            Asteroid a = (Asteroid)asteroids.pool[i];
           a._x += a.vx;
           a._y += a.vy;
           a.x = a._x >> 8;
           a.y = a._y >> 8;

           // If a border has been hit, wrap the trajectory around
           // the screen. The new origin is the projection of the
           // intersection point on the opposite border.
           if (a.x <= 0) {
               a.moveTo(width - 2, a.y);
           } else if (a.x >= width - 1) {
               a.moveTo(1, a.y);
           } else if (a.y <= 0) {
               a.moveTo(a.x, height - 2);
           } else if (a.y >= height - 1) {
               a.moveTo(a.x, 1);
           }
        }
    }

    /**
     * Compute collisions between the asteroids and
     * the ship and the rockets.
     */
     public static final void collisionDetection() {
      Ship ship = Ship.ship;
      Field field = Game.field;

      // Collision detection makes sense only while the game is
      // being played and the ship is alive.
        if ((field != null) && (field.getState() == Field.GAME_STATE) && (ship.isAlive)) {

         int score = field.getScore();
         int newScore = score;
         Pool rockets = Rocket.rockets;
         Pool explosions = Explosion.explosions;

         for (asteroids.current = asteroids.count - 1; asteroids.current >= 0;) {
            Asteroid a = (Asteroid)asteroids.pool[asteroids.current--];

            // Detect ship - asteroid collisions.
            int offset = ship.angle << 2;
            for (int i = 0; i < 4; i++) {
                 int sx = ship.x + Ship.xcoords[offset + i] - a.x + radii[a._size];
                 if ((sx >= 0) && (sx < diameters[a._size])) {
                  int sy = ship.y + Ship.ycoords[offset + i] - a.y + radii[a._size];
                  if ((sy >= 0) && (sy < diameters[a._size])) {

                     // Use the mask for collision detection
                     if (masks[a._size][sx][sy]) {
                              int lives = field.getLives();
                             if ((lives > 0) && (ship.isAlive)) {
                                 ship.explode();
                                 field.setLives(lives - 1);
                             }
                     }
                  }
                 }
            }


            // Detect asteroid - rocket collisions.
            for (rockets.current = rockets.count - 1; rockets.current >= 0;) {
               Rocket r = (Rocket)rockets.pool[rockets.current--];

               // Transform the r into asteroid coordinates.
                 int rx = r.x - a.x + radii[a._size];
                 if ((rx >= 0) && (rx < diameters[a._size])) {
                  int ry = r.y - a.y + radii[a._size];
                  if ((ry >= 0) && (ry < diameters[a._size])) {

                     // Use the mask for collision detection
                     if (masks[a._size][rx][ry]) {
                               asteroids.removeCurrent();
                               rockets.removeCurrent();
                               newScore += _value[a._size];
                               a.split(r);
                               Explosion explosion = (Explosion)explosions.addNewObject();
                               if (explosion != null) {
                                   explosion.init(r.x, r.y);
                               }
                               break;
                     }
                    }
                 }
                }
            }
            if (newScore != score) {
                field.setScore(newScore);
            }
        }
    }

    /**
     * Draws an asteroid of the specified size and location
     * in the specified graphic context.
     */
    public static final void draw(byte size, int xpos, int ypos, Graphics g) {
        byte[] xcoord = xcoords[size];
        byte[] ycoord = ycoords[size];
        for (int i = 0; i < xcoord.length - 1; i++) {
             g.drawLine(xcoord[i] + xpos, ycoord[i] + ypos, xcoord[i + 1] + xpos, ycoord[i + 1] + ypos);
         }
    }


    /**
     * Draws all the asteroids of the supplied object pool using the
     *  specified graphic context.
     */
    static public final void draw(Graphics g) {
      for (int i = 0; i < asteroids.count; i++) {
         Asteroid a = (Asteroid)asteroids.pool[i];
           byte[] xcoord = xcoords[a._size];
           byte[] ycoord = ycoords[a._size];
         for (int j = 0; j < xcoord.length - 1; j++) {
            g.drawLine(
               xcoord[j] + a.x,
               ycoord[j] + a.y,
               xcoord[j + 1] + a.x,
               ycoord[j + 1] + a.y);
         }
        }
    }


    /**
     * Splits the asteroid in two smaller pieces and set their
     * direction depending on the asteroid direction and the hitting
     * rocket direction. The pieces are added to the supplied collection.
     */
    public final void split(Rocket rocket) {
        if (_size >= SIZE_MEDIUM) {
            byte newSize = (byte)(_size - 1);
            int angle = (cos[_angle] * sin[rocket.angle] - cos[rocket.angle] * sin[_angle]) >> 10;
            byte angle1 = (byte)(_angle + angle + Math.abs(Game.random.nextInt()) % 5);
            if (angle1 < 0) {
                angle1 += 32;
            }
            if (angle1 >= 32) {
                angle1 -= 32;
            }
            byte angle2 = (byte)(_angle + angle - Math.abs(Game.random.nextInt()) % 5);
            if (angle2 < 0) {
                angle2 += 32;
            }
            if (angle2 >= 32) {
                angle2 -= 32;
            }
            Asteroid a = (Asteroid)asteroids.addNewObject();
            if (a != null) {
                a.init(x, y, angle1, newSize);
            }
            a = (Asteroid)asteroids.addNewObject();
            if (a != null) {
                a.init(x, y, angle2, newSize);
            }
        }
    }
}
