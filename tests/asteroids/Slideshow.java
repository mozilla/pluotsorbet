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

import java.util.*;
import javax.microedition.lcdui.*;

/**
 * Class to manage the game presentation screens.
 * @author Jean-Francois Doue
 * @version 1.2, 2001/10/24
 */
public class Slideshow extends TimerTask {
    private byte _nextState;
    private static int _smallFontHeight;
    private static int _bigFontHeight;
    private static final String _asteroid = "ASTEROID";
    private static final String _field = "ZONE";
    private static final String _byjfd = "by J.F. Doue";
    private static final String _version = "v1.4 - 13 Oct 2002";
    private static final String _gameControls = "Game Controls";
    private static final String _points = "Points";
    private static final String _hiScores = "High Scores";
    private static final String _gameOver = "Game Over";
    private static final String[] _commands = {"Rotate:", "Shoot:", "Thrust:", "Teleport:"};
    private static final String[] _keys = {"Left/Right", "Fire", "Up or A", "B"};
    static {
        _smallFontHeight = Field.smallFont.getHeight();;
        _bigFontHeight = Field.bigFont.getHeight();
    }

    public Slideshow(byte nextState) {
        _nextState = nextState;
    }

    /**
     * Overriden from TimerTask. Triggers the transition to the next
     * game state.
     */
    public void run() {
        Game.field.setState(_nextState);
    }


    /**
     * Draws the title screen.
     */
    public static void drawTitleScreen(Graphics g) {
        g.setColor(0x00000000);
        g.setFont(Field.bigFont);
        int x = (Mobile.width - Field.bigFont.stringWidth(_asteroid)) >> 1;
        int y = ((Mobile.height - ((_bigFontHeight + _smallFontHeight) << 1)) >> 1) + _bigFontHeight;
        g.drawString(_asteroid, x, y, Graphics.BOTTOM|Graphics.LEFT);
        x = (Mobile.width - Field.bigFont.stringWidth(_field)) >> 1;
        g.drawString(_field, x, y + _bigFontHeight, Graphics.BOTTOM|Graphics.LEFT);

        g.setColor(0x008080FF);
        x = ((Mobile.width - Field.bigFont.stringWidth(_asteroid)) >> 1) - 1;
        y += 1;
        g.drawString(_asteroid, x, y, Graphics.BOTTOM|Graphics.LEFT);
        x = ((Mobile.width - Field.bigFont.stringWidth(_field)) >> 1) - 1;
        y += _bigFontHeight;
        g.drawString(_field, x, y, Graphics.BOTTOM|Graphics.LEFT);

        y += _smallFontHeight;
        g.setColor(0x00000000);
        g.setFont(Field.smallFont);
        x = (Mobile.width - Field.smallFont.stringWidth(_byjfd)) >> 1;
        g.drawString(_byjfd, x, y, Graphics.BOTTOM|Graphics.LEFT);
        y += _smallFontHeight;
        x = (Mobile.width - Field.smallFont.stringWidth(_version)) >> 1;
        g.drawString(_version, x, y, Graphics.BOTTOM|Graphics.LEFT);
    }

    /**
     * Draws the control screen.
     */
    public static void drawControlScreen(Graphics g) {
        // Draw the "Game control" string
        g.setFont(Field.smallFont);
        g.setColor(0x00000000);
        int x = (Mobile.width - Field.smallFont.stringWidth(_gameControls)) >> 1;
        int y0 = ((Mobile.height - _smallFontHeight * 5) >> 1) + _smallFontHeight;
        int y = y0;
        g.drawString(_gameControls, x, y, Graphics.BOTTOM|Graphics.LEFT);

        // Draw the command names
        x = (Mobile.width >> 1) - 2;
        for (int i = 0; i < 4; i++) {
            y += _smallFontHeight;
            g.drawString(_commands[i], x, y, Graphics.BOTTOM|Graphics.RIGHT);
        }

        // Draw the command keys
        g.setColor(0x00FF0000);
        y = y0;
        x = (Mobile.width >> 1) + 2;
        for (int i = 0; i < 4; i++) {
            y += _smallFontHeight;
            g.drawString(_keys[i], x, y, Graphics.BOTTOM|Graphics.LEFT);
        }
    }

    /**
     * Draws the points screen.
     */
    public static void drawPointsScreen(Graphics g) {
        g.setFont(Field.smallFont);
        g.setColor(0x00000000);
        int x = (Mobile.width - Field.smallFont.stringWidth(_points)) >> 1;
        int y = ((Mobile.height - 6 * Asteroid.radii[Asteroid.SIZE_LARGE] - _smallFontHeight) >> 1) + _smallFontHeight;
        g.drawString(_points, x, y, Graphics.BOTTOM|Graphics.LEFT);
        x = Mobile.width >> 2;
        y += Asteroid.radii[Asteroid.SIZE_LARGE];
        Asteroid.draw(Asteroid.SIZE_SMALL, x, y, g);
        int textx = Mobile.width >> 1;
        int texty = y + (_smallFontHeight >> 1);
        g.drawString("5 pts", textx, texty, Graphics.BOTTOM|Graphics.LEFT);
        y += (Asteroid.radii[Asteroid.SIZE_LARGE] << 1);
        Asteroid.draw(Asteroid.SIZE_MEDIUM, x, y, g);
        texty = y + (_smallFontHeight >> 1);
        g.drawString("2 pts", textx, texty, Graphics.BOTTOM|Graphics.LEFT);
        y += (Asteroid.radii[Asteroid.SIZE_LARGE] << 1);
        Asteroid.draw(Asteroid.SIZE_LARGE, x, y, g);
        texty = y + (_smallFontHeight >> 1);
        g.drawString("1 pt", textx, texty, Graphics.BOTTOM|Graphics.LEFT);

    }

    /**
     * Draws the game over screen.
     */
    public static void drawGameOverScreen(Graphics g) {
        // Draw the "Game over" string
        g.setFont(Field.smallFont);
        g.setColor(0x00000000);
        int x = (Mobile.width - Field.smallFont.stringWidth(_gameOver)) >> 1;
        int y = (Mobile.height + _smallFontHeight) >> 1;
        g.drawString(_gameOver, x, y, Graphics.BOTTOM|Graphics.LEFT);
    }

    /**
     * Draws the high scores screen.
     */
    public static void drawHighScoresScreen(Graphics g) {
        // Draw the "High score" string
        g.setFont(Field.smallFont);
        g.setColor(0x00000000);
        int x = (Mobile.width - Field.smallFont.stringWidth(_hiScores)) >> 1;
        int y0 = ((Mobile.height - (_smallFontHeight << 2)) >> 1) + _smallFontHeight;
        int y = y0;
        g.drawString(_hiScores, x, y, Graphics.BOTTOM|Graphics.LEFT);

        // Draw the best player names
        x = (Mobile.width >> 1) - 2;
        for (int i = 0; i < 3; i++) {
            y += _smallFontHeight;
            g.drawString(Game.scores.names[i], x, y, Graphics.BOTTOM|Graphics.RIGHT);
        }

        // Draw the best scores.
        g.setColor(0x00FF0000);
        y = y0;
        x = (Mobile.width >> 1) + 2;
        char[] scoreString = new char[4];
        for (int i = 0; i < 3; i++) {
            y += _smallFontHeight;
            Scores.toCharArray(Game.scores.values[i], scoreString);
            g.drawChars(scoreString, 0, 4, x, y, Graphics.BOTTOM|Graphics.LEFT);
        }
    }
}
