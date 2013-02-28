package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.mnyobject.Currency;

public class CurrencyImplUtil {

    public static Map<Integer, Currency> getCurrencies(Database db) throws IOException {
        Map<Integer, Currency> currencies = new HashMap<Integer, Currency>();

        String tableName = "CRNC";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                String name = (String) row.get("szName");
                if (name == null) {
                    continue;
                }
                if (name.length() == 0) {
                    continue;
                }

                Integer hcrnc = (Integer) row.get("hcrnc");
                String isoCode = (String) row.get("szIsoCode");

                CurrencyImpl currency = new CurrencyImpl();
                currency.setId(hcrnc);
                currency.setName(name);
                currency.setIsoCode(isoCode);

                currencies.put(hcrnc, currency);
            }
        } finally {

        }

        return currencies;
    }

}
