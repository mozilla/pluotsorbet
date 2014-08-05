package com.nokia.mid.ui.lcdui;

public class IndicatorManager {
  private IndicatorManager instance = null;

  public IndicatorManager getIndicatorManager() {
    if (instance == null) {
      instance = new IndicatorManager();
    }

    return instance;
  }
}
