package midlets;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.*;
import java.io.*;

class Game extends Canvas {
        public void paint(Graphics g) {
                try {
                        Image img = Image.createImage("test.png");
                        g.drawImage(img, 10, 10, 0);
                        g.drawRegion(img, 10, 5, 20, 30, Sprite.TRANS_MIRROR_ROT270, 100, 100, 0);
                        g.drawRegion(img, 10, 10, 30, 20, Sprite.TRANS_ROT90, 150, 100, 0);
                        g.drawRegion(img, 5, 10, 20, 20, Sprite.TRANS_MIRROR_ROT90, 100, 150, 0);
                        g.drawRegion(img, 10, 0, 20, 10, Sprite.TRANS_ROT180, 150, 150, 0);
                        g.drawRegion(img, 10, 10, 20, 20, Sprite.TRANS_MIRROR, 120, 120, 0);
                } catch (Exception e) {
                }
                g.drawLine(0, 0, 100, 100);
                g.drawLine(20, 20, 20, 150);
                g.setColor(0, 255, 0);
                g.drawRect(30, 20, 20, 150);
                g.setColor(255, 255, 0);
                //g.fillRect(130, 20, 20, 150);
                g.drawLine(50, 50, 40, 120);
                g.setClip(50, 50, 10, 10);
                g.fillRect(0, 0, 100, 150);
        }
}
public class TestCanvas extends TestMidlet {
        
        public void startApp() {
                Display d = Display.getDisplay(this);
                Game g = new Game();
                d.setCurrent(g);
                finish();
        }
        
}
