package benchmark;

import com.sun.cldchi.jvm.JVM;

import javax.microedition.lcdui.*;
import javax.microedition.lcdui.game.Sprite;
import javax.microedition.midlet.*;

public class GraphicsBench extends MIDlet {
  interface TestInfo {
    String getDescription();
    void executeFrame(Graphics g, long numOps);
  }

  class TestCanvas extends Canvas implements Runnable {
    TestCanvas(Display d) {
      display = d;

      Image img = Image.createImage(getWidth(), getHeight());
      offscreenBuffer = img.getGraphics();

      // NOTE: When adding new tests, append them to the end
      //       of this array
      tests = new TestInfo[2];
      tests[0] = new DrawRegionTransNoneCenter(getWidth(), getHeight());
      tests[1] = new DrawStringBaselineCenter(getWidth(), getHeight());

      currentTest = tests[0];
    }

    protected void paint(Graphics screenG) {
      if (currentTest != null) {
        if (isOffscreen) {
          long frameStartTime = JVM.monotonicTimeMillis();
          currentTest.executeFrame(offscreenBuffer, numOpsPerFrame);
          lastFrameTime = JVM.monotonicTimeMillis() - frameStartTime;
        } else {
          long frameStartTime = JVM.monotonicTimeMillis();
          currentTest.executeFrame(screenG, numOpsPerFrame);
          lastFrameTime = JVM.monotonicTimeMillis() - frameStartTime;
        }
      }
    }

    void startNextTest() {
      if (isOffscreen) {
        isOffscreen = false;
        if (nextTestIndex < tests.length) {
          currentTest = tests[nextTestIndex];
          System.out.println(currentTest.getDescription());
          nextTestIndex++;
        } else {
          System.out.println("DONE");
          return;
        }
      } else {
        isOffscreen = true;
        System.out.println("Offscreen " + currentTest.getDescription());
      }

      repaint();
      display.callSerially(this);
    }

    protected void showNotify() {
      startNextTest();
    }

    public void run() {
      totalFrameTime += lastFrameTime;
      numSamples++;
      if (numSamples < targetSamples) {
        repaint();
        display.callSerially(this);
        return;
      }

      avgFrameTime = (totalFrameTime * 1000) / numSamples;
      System.out.println("\tOps per second: " + ((numOpsPerFrame * 1000000) / avgFrameTime));
      numSamples = 0;
      totalFrameTime = 0;

      startNextTest();
    }

    private final long targetSamples = 240;
    private final long numOpsPerFrame = 500;
    private long lastFrameTime = 0;
    private long totalFrameTime = 0;
    private long avgFrameTime = 0;
    private long numSamples = 0;
    private TestInfo[] tests;
    private TestInfo currentTest;
    private int nextTestIndex = 0;
    private Display display;
    private Graphics offscreenBuffer;
    private boolean isOffscreen = true;
  }

  public void startApp() {
    Display d = Display.getDisplay(this);
    TestCanvas test = new TestCanvas(d);
    test.setFullScreenMode(true);
    d.setCurrent(test);
  }



  //
  // Implement actual tests below
  //

  class DrawRegionTransNoneCenter implements TestInfo {
    DrawRegionTransNoneCenter(int w, int h) {
      width = w;
      height = h;

      int rectWidth = width / 2;
      int rectHeight = height / 2;

      srcImages = new Image[2];

      srcImages[0] = Image.createImage(width, height);
      Graphics srcG = srcImages[0].getGraphics();
      srcG.setColor(255, 0, 0);
      srcG.fillRect(0, 0, rectWidth, rectHeight);
      srcG.setColor(0, 255, 0);
      srcG.fillRect(rectWidth, 0, rectWidth, rectHeight);
      srcG.setColor(0, 0, 255);
      srcG.fillRect(rectWidth, rectHeight, rectWidth, rectHeight);
      srcG.setColor(255, 255, 0);
      srcG.fillRect(0, rectHeight, rectWidth, rectHeight);


      srcImages[1] = Image.createImage(width, height);
      Graphics src2G = srcImages[1].getGraphics();
      src2G.setColor(0, 255, 255);
      src2G.fillRect(0, 0, rectWidth, rectHeight);
      src2G.setColor(255, 255, 255);
      src2G.fillRect(rectWidth, 0, rectWidth, rectHeight);
      src2G.setColor(0, 0, 0);
      src2G.fillRect(rectWidth, rectHeight, rectWidth, rectHeight);
      src2G.setColor(255, 0, 255);
      src2G.fillRect(0, rectHeight, rectWidth, rectHeight);
    }

    public void executeFrame(Graphics g, long numOps) {
      for (long j = 0; j < numOps; j++) {
        g.drawRegion(srcImages[curImageIndex], 0, 0, width, height, Sprite.TRANS_NONE, width / 2, height / 2, Graphics.VCENTER|Graphics.HCENTER);
        curImageIndex = (curImageIndex + 1) % srcImages.length;
      }
      curImageIndex = (curImageIndex + 1) % srcImages.length;
    }

    public String getDescription() {
      return description;
    }

    private int i = 0;
    private int width;
    private int height;
    private Image[] srcImages;
    private int curImageIndex = 0;
    private final String description = "Graphics.drawRegion TRANS_NONE centered";
  }

  class DrawStringBaselineCenter implements TestInfo {
    DrawStringBaselineCenter(int w, int h) {
      width = w;
      height = h;
    }

    public void executeFrame(Graphics g, long numOps) {
      for (int j = 0; j < numOps; j++) {
        g.drawString(string, j%width, j%height, Graphics.BASELINE|Graphics.HCENTER);
      }
    }

    public String getDescription() {
      return description;
    }

    private int width;
    private int height;
    private final String description = "Graphics.drawString baseline centered";
    private final String string = "String string = new String(\"string\");";
  }

  //
  // End impl of actual tests
  //

  public void pauseApp() {
  }

  public void destroyApp(boolean unconditional) {
  }

  // Future tests to consider:
  //
  // ImageDataFactory.createImmutableImageDecodeImage
  // ImageDataFactory.createImmutableImageDataRegion
  // ImageDataFactory.createImmutableImageDataCopy
  // ImageDataFactory.createMutableImageData
  // ImageDataFactory.createImmutableImageDecodeRGBImage
  // ImageData.getRGB
  // DirectUtils.setPixels
  // DirectGraphicsImp.getPixels
  // DirectGraphicsImp.drawPixels
  // Graphics.render
  // Graphics.renderRegion
  // Graphics.drawSubstring
  // Graphics.drawChars
  // Graphics.drawChar
  // Graphics.fillTriangle
  // Graphics.drawRect
  // Graphics.fillRect
  // Graphics.drawRoundRect
  // Graphics.fillRoundRect
  // Graphics.drawArc
  // Graphics.fillArc
  // Graphics.drawLine
  // Graphics.drawRGB
  //
  // Font.stringWidth
  // Font.charWidth
  // Font.charsWidth
  // Font.substringWidth
  //
  // Graphics.resetGC
  // Graphics.reset
  // Graphics.copyArea
  // Graphics.setDimensions
  // Graphics.setClip
  // Graphics.clipRect
  // Graphics.drawRegion
  // Graphics.drawImage
}

