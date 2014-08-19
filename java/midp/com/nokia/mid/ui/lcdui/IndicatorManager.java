package com.nokia.mid.ui.lcdui;

public class IndicatorManager {
    private static IndicatorManager instance = null;

    public static IndicatorManager getIndicatorManager() {
        if (instance == null) {
            instance = new IndicatorManager();
        }

        return instance;
    }

    public int appendIndicator(Indicator indicator, boolean paramBoolean) {
        throw new RuntimeException("IndicatorManager.appendIndicator(L...Indicator;Z)I not implemented");
    }
}
