package com.nokia.mid.media;

public interface AudioOutput {
  public int getActiveOutputMode();
  public int[] getOutputDevices();
}
