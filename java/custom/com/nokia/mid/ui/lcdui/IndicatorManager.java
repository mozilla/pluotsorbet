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
        System.out.println("IndicatorManager.appendIndicator(L...Indicator;Z)I not implemented");
        return 0;
    }
}
