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
 * Class to represent an explosion.
 * @author Jean-Francois Doue
 * @version 1.0, 2001/07/26
 */
public class Explosion extends Object {
    public static byte[] xcoords;
    public static byte[] ycoords;
    private int _frame, _x, _y;
    private static final int MAX_FRAME = 6;
    public static Pool explosions;

    static {
      // Create and populate the explosion pool.
        Explosion[] array = new Explosion[10];
        for (int i = array.length - 1; i >= 0; i--) {
            array[i] = new Explosion();
        }
        explosions = new Pool(array);

      // Pre-compute the explosion images
        int[] Xcoords = new int[8];
        int[] Ycoords = new int[8];
        for (int i = 0; i < 8; i++) {
            Xcoords[i] = Mobile.cos[i << 2] * (Math.abs(Game.random.nextInt()) % 12);
            Ycoords[i] = Mobile.sin[i << 2] * (Math.abs(Game.random.nextInt()) % 12);
        }
        xcoords = new byte[48];
        ycoords = new byte[48];
        for (int i = 0; i < Explosion.MAX_FRAME; i++) {
            int offset = i << 3;
            for (int j = 0; j < 8; j++) {
                xcoords[offset + j] = (byte)(Xcoords[j] >> 8);
                ycoords[offset + j] = (byte)(Ycoords[j] >> 8);
                Xcoords[j] += Mobile.cos[j << 2] << 2;
                Ycoords[j] += Mobile.sin[j << 2] << 2;
            }
        }
    }
    public Explosion() {
    }

    public final void init(int x, int y) {
        _x = x;
        _y = y;
        _frame = 0;
    }

   /**
    * Animate the explosion and remove those which are done.
    */
   public static final void explode() {
      for (explosions.current = explosions.count - 1; explosions.current >= 0;) {
         Explosion e = (Explosion)explosions.pool[explosions.current--];
         e._frame++;
         if (e._frame >= Explosion.MAX_FRAME) {
            explosions.removeCurrent();
            }
        }
      }

   /**
    * Draw all the explosions using the supplied context.
    */
    public static final void draw(Graphics g) {
      for (int i = 0; i < explosions.count; i++) {
         Explosion e = (Explosion)explosions.pool[i];
           int offset = e._frame << 3;
           for (int j = 0; j < 8; j++) {
               g.drawLine(
                  e._x + xcoords[j + offset],
                  e._y + ycoords[j + offset],
                  e._x + xcoords[j + offset],
                  e._y + ycoords[j + offset]);
           }
       }
    }

}
