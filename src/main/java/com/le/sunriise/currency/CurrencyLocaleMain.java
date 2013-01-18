package com.le.sunriise.currency;

import java.text.NumberFormat;
import java.util.Locale;

import org.apache.log4j.Logger;

public class CurrencyLocaleMain {
    private static final Logger log = Logger.getLogger(CurrencyLocaleMain.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Double amount = new Double(80.700000);

        NumberFormat numberFormat = null;

        Locale[] locales = Locale.getAvailableLocales();
        for (Locale locale : locales) {
            numberFormat = NumberFormat.getNumberInstance(locale);
            log.info(locale + ", " + numberFormat.format(amount));
        }
    }

}
