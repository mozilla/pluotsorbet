package com.nokia.mid.media;

public interface AudioOutputControl extends javax.microedition.media.Control {
  public static final int DEFAULT = 0;
  public static final int ALL = 1;
  public static final int NONE = 2;
  public static final int PRIVATE = 3;
  public static final int PUBLIC = 4;

  public int[] getAvailableOutputModes();
  public int getOutputMode();
  public AudioOutput getCurrent();
  public int setOutputMode(int mode);
}
