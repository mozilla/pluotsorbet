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
 * Class to group various geometric functions.
 * @author Jean-Francois Doue
 * @version 1.4, 2002/10/14
 */

public class Geometry extends Object {

    /**
     * Scan converts a polygon. 
     * @param pixels
     * The resulting scan-converted polygon as a 2D boolean array
     * @param xcoords
     * The X-coordinates of the polygon vertices. The vertex are ordered clockwise,
     * and the last vertex equals the first vertex.
     * @param ycoords
     * The Y-coordinates of the polygon vertices. The vertex are ordered clockwise,
     * and the last vertex equals the first vertex.
     */

    public static void scanConvertPolygon(boolean[][] pixels, byte[] xcoords, byte[] ycoords) {
      // Degenerate cases
      if (pixels.length == 0) {
         return;
      }
      if (pixels.length == 1) {
         pixels[0][0] = true;
         return;
      }

      // Index of the polygon segment being scan converted.
      int top = 0, bottom = xcoords.length - 1;

       // Parameters used to adapt the Bresenham algorithm.
       // to the 8 possible octants. (top and bottom lines)
       boolean isSteepT = false, isSteepB = false;
       int incrYT = 0, incrYB = 0;

      // Decision variable of the Bresenham algorithm.
       // If d <= 0, then the next point ought to be E
       // If d > 0, then the next point ought to be NE
       int dT = 0, dB = 0;

       // Incremental d of the Bresenham algorithm when E is chosen.
       int incrET = 0, incrEB = 0;

       // Incremental d of the Bresenham algorithm when NE is chosen.
       int incrNET = 0, incrNEB = 0;

       // The x coordinate of the current line point.
       int x = xcoords[top];

       // The y coordinate of the current line point (top and bottom lines).
       int yT = 0, yB = 0;

      // Translation
      int tx = pixels.length >> 1;


      // Scan convert the top and bottom border of the polygon.
      // The scan conversion is done once the lines cross.
      while (top <= bottom) {

         // Another segment of the top border has been reached.
         if (x == xcoords[top]) {
            while (xcoords[top + 1] == xcoords[top] && top < xcoords.length) {
               top++;
            }
            int dxT = xcoords[top + 1] - xcoords[top];
            int dyT = ycoords[top + 1] - ycoords[top];
            yT = ycoords[top];
            top++;
              incrYT = (dyT >= 0) ? 1 : -1;
              int absDxT = Math.abs(dxT);
              int absDyT = Math.abs(dyT);
              isSteepT = (absDxT <= absDyT);
              if (isSteepT) {
                  dT = (absDxT << 1) - absDyT;
                  incrET = absDxT << 1;
                  incrNET = (absDxT - absDyT ) << 1;
              } else {
                  dT = (absDyT << 1) - absDxT;
                  incrET = absDyT << 1;
                  incrNET = (absDyT - absDxT ) << 1;
              }
         } else {
            // Compute the Y coordinate of the next top border point.
              if (isSteepT) {
                  if (dT <= 0) {
                  while (dT <= 0) {
                         yT += incrYT;
                         dT += incrET;
                  }
               }
                   yT += incrYT;
                   dT += incrNET;
              } else {
                  if (dT <= 0) {
                      dT += incrET;
                  } else {
                      yT += incrYT;
                      dT += incrNET;
                  }
              }
           }

         // Another segment of the bottom border has been reached.
         if (x == xcoords[bottom]) {
            while (xcoords[bottom - 1] == xcoords[bottom] && bottom >= 0) {
               bottom--;
            }
            int dxB = xcoords[bottom - 1] - xcoords[bottom];
            int dyB = ycoords[bottom - 1] - ycoords[bottom];
            yB = ycoords[bottom];
            bottom--;
              incrYB = (dyB >= 0) ? 1 : -1;
              int absDxB = Math.abs(dxB);
              int absDyB = Math.abs(dyB);
              isSteepB = (absDxB <= absDyB);
              if (isSteepB) {
                  dB = (absDxB << 1) - absDyB;
                  incrEB = absDxB << 1;
                  incrNEB = (absDxB - absDyB ) << 1;
              } else {
                  dB = (absDyB << 1) - absDxB;
                  incrEB = absDyB << 1;
                  incrNEB = (absDyB - absDxB ) << 1;
              }
          } else {
            // Compute the Y coordinate of the next bottom border point.
              if (isSteepB) {
                  if (dB <= 0) {
                  while (dB <= 0) {
                         yB += incrYB;
                         dB += incrEB;
                  }
               }
                   yB += incrYB;
                   dB += incrNEB;
              } else {
                  if (dB <= 0) {
                      dB += incrEB;
                  } else {
                      yB += incrYB;
                      dB += incrNEB;
                  }
              }
             }

           // Fill the line between the top and the bottom border points.
            int min = (yB <= yT) ? yB : yT;
            int max = (yB > yT) ? yB : yT;
         for (int i = min; i <= max; i++) {
            pixels[pixels.length - 1 - i - tx][x + tx] = true;
         }
         x--;
      }
      /*
      for (int i = 0; i < pixels.length; i++) {
         for (int j = pixels[i].length - 1; j >= 0; j--) {
            if (pixels[i][j]) {
               System.out.print("#");
            } else {
               System.out.print(".");
            }
         }
         System.out.println();
      }
      */
    }
}
