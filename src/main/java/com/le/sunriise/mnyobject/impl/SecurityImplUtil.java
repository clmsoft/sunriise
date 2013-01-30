package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.mnyobject.Security;

public class SecurityImplUtil {

    private static final String COL_SYMBOL = "szSymbol";
    private static final String COL_NAME = "szFull";
    private static final String COL_ID = "hsec";
    private static final String TABLE_NAME = "SEC";

    public static Map<Integer, Security> getSecurities(Database db) throws IOException {
        Map<Integer, Security> securities = new HashMap<Integer, Security>();

        String tableName = TABLE_NAME;
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                addSecurity(row, securities);
            }
        } finally {

        }

        return securities;
    }

    private static void addSecurity(Map<String, Object> row, Map<Integer, Security> securities) {
        SecurityImpl security = new SecurityImpl();

        Integer hsec = (Integer) row.get(COL_ID);
        security.setId(hsec);

        String szFull = (String) row.get(COL_NAME);
        security.setName(szFull);

        String szSymbol = (String) row.get(COL_SYMBOL);
        security.setSymbol(szSymbol);

        securities.put(hsec, security);
    }

}
