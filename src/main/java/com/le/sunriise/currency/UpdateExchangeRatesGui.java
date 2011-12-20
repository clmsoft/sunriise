package com.le.sunriise.currency;

import java.util.prefs.Preferences;


public class UpdateExchangeRatesGui {
    private static final Preferences prefs = Preferences.userNodeForPackage(UpdateExchangeRatesGui.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        UpdateFxAction.updateFx(prefs);
    }

}
