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

/**
 * Class to implement a rocket shot by the player's spaceship.
 * @author Jean-Francois Doue
 * @version 1.4, 2002/10/14
 */
public class Rocket extends Mobile {
    /**
     * The orientation of the rocket.
     */
    public byte angle;

   /**
    * The rockets currently existing in the game
    */
    public static Pool rockets;

    private int _rangex, _rangey;   // The distance travelled by the rocket.

   static {
        // Create and populate the rocket pool.
        Rocket[] array = new Rocket[10];
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = new Rocket();
        }
        rockets = new Pool(array);
   }

    public Rocket() {
    }

    /**
     * Initializes a Rocket instance by setting its position
     * and angle.
     */
    public final void init(int x, int y, byte angle, int vx, int vy) {
        moveTo(x, y);
        setVelocity((cos[angle] << 4) + vx, (sin[angle] << 4) + vy);
        this.angle = angle;
        _rangex = 0;
        _rangey = 0;
    }

    /**
     * Move the rockets and remove those which have expired.
     */
     public static final void move() {
      for (rockets.current = rockets.count - 1; rockets.current >= 0;) {
         Rocket r = (Rocket)rockets.pool[rockets.current--];

           int rangex = r._rangex >> 8;
           int rangey = r._rangey >> 8;
           int maxRange = (((width <= height) ? width : height) * 8) / 10;

          // Determines if the rocket has travelled its maximum distance
          // (80% of the screens smallest dimension).
         if ((rangex * rangex + rangey * rangey) > maxRange * maxRange) {
            rockets.removeCurrent();
            } else {

              r.xold = r.x;
              r.yold = r.y;
              r._x += r.vx;
              r._y += r.vy;
              r.x = r._x >> 8;
              r.y = r._y >> 8;

              // If a border has been hit, wrap the trajectory around
              // the screen. The new origin is the projection of the
              // intersection point on the opposite border.
              if (r.x <= 0) {
                  r.moveTo(width - 2, r.y);
              } else if (r.x >= width - 1) {
                  r.moveTo(1, r.y);
              } else if (r.y <= 0) {
                  r.moveTo(r.x, height - 2);
              } else if (r.y >= height - 1) {
                  r.moveTo(r.x, 1);
              }

              // Upgrade the range travelled.         
              r._rangex += r.vx;
              r._rangey += r.vy;

            }
        }
   }


    /**
     * Draws all the rockets of the supplied object pool using the
     * specified graphic context.
     */
    public static final void draw(Graphics g) {
      for (int i = 0; i < rockets.count; i++) {
         Rocket r = (Rocket)rockets.pool[i];
           g.drawLine(r.x, r.y, r.x, r.y);
       }
    }
}
