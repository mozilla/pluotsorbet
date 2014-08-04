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

/**
 * The base class for all moving objects on screen.
 * Provides pseudo-floating point capabilities.
 * @author Jean-Francois Doue
 * @version 1.3, 2001/10/26
 */
public abstract class Mobile extends Object {
    /**
     * A cosine table.
     */
   public static final int[] cos = { 64,
       62, 59, 53, 45, 35, 24, 12, 0, -12,
       -24, -35, -45, -53, -59, -62, -64,
       -62, -59, -53, -45, -35, -24, -12,
       0, 12, 24, 35, 45, 53, 59, 62 };

    /**
     * A sine table.
     */
   public static final int[] sin = { 0,
       12, 24, 35, 45, 53, 59, 62, 64, 62,
       59, 53, 45, 35, 24, 12, 0, -12,
       -24, -35, -45, -53, -59, -62, -64,
       -62, -59, -53, -45, -35, -24, -12
       };
    /**
     * Graphics are scaled to keep the same
     * aspect as on the original development platform.
     */

    public static int ratioNum;
    /**
     * The screen width of the original development platform.
     */
    public static final int ratioDenom = 96;

    /**
     * Screen width
     */
    public static int width;

    /**
     * Screen height
     */
    public static int height;

    /**
     * The screen coordinates (in pixels) of the mobile.
     */
    public int x, y;

    /**
     * The velocity of the mobile (in pseudo floating point units.)
     */
    public int vx, vy;

    /**
     * The screen coordinates (in pseudo floating point units) of the mobile.
     */
    protected int _x, _y;

    /**
     * The previous screen coordinates (in pixels) of the mobile.
     */
    public int xold, yold;

    public Mobile() {
    }

    /**
     * Move the mobile to a specific screen location.
     */
    public void moveTo(int x, int y) {
        this.x = x;
        this.y = y;
        this._x = x << 8;
        this._y = y << 8;
        this.xold = x;
        this.yold = y;
    }

    /**
     * Alters the velocity of the mobile.
     */
    public void setVelocity(int vx, int vy) {
        this.vx = vx;
        this.vy = vy;
    }
}
