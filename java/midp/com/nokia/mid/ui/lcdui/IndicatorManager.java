package com.nokia.mid.ui.lcdui;

public class IndicatorManager {
  private static IndicatorManager instance = null;

  public static IndicatorManager getIndicatorManager() {
    if (instance == null) {
      instance = new IndicatorManager();
    }

    return instance;
  }
}
