package com.nokia.mid.ui.lcdui;

/*
 * This class is undocumented.
 *
 * We think this class is needed when multiple background MIDlets
 * need to append an Indicator to the list of the system indicators.
 * We don't really need to do anything in appendIndicator, since
 * we're using the Notifications API to implement indicators. When
 * a notification is created, it is automatically shown to the user.
 *
 */

public class IndicatorManager {
    private static IndicatorManager instance = null;

    public static IndicatorManager getIndicatorManager() {
        if (instance == null) {
            instance = new IndicatorManager();
        }

        return instance;
    }

    public int appendIndicator(Indicator indicator, boolean paramBoolean) {
        if (paramBoolean) {
            System.out.println("IndicatorManager.appendIndicator(L...Indicator;Z)I unexpected value");
        }
        return 0;
    }
}
