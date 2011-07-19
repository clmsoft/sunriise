package com.le.sunriise.quote;

import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public class QuoteUtils {

    private static final String TABLE_NAME_SEC = "SEC";

    public static Integer findSecId(String stockSymbol, Database db) throws IOException {
        Table table = db.getTable(TABLE_NAME_SEC);

        Map<String, Object> row = null;
        // SEC:
        // Name: (SEC) mUID
        // Name: (SEC) szFull
        // Name: (SEC) szSymbol
        String[] columns = { "mUID", "szSymbol", "szFull" };

        Cursor cursor = Cursor.createCursor(table);

        Map<String, Object> rowPattern = new HashMap<String, Object>();

        for (String column : columns) {
            rowPattern.clear();
            rowPattern.put(column, stockSymbol);
            row = findRowFromTop(cursor, rowPattern);
            if (row != null) {
                break;
            }
        }
        if (row == null) {
            return null;
        }

        Integer hsec = null;
        hsec = (Integer) row.get("hsec");

        return hsec;
    }

    public static Map<String, Object> findRowFromTop(Cursor cursor, Map<String, Object> rowPattern) throws IOException {
        cursor.beforeFirst();
        if (!cursor.findRow(rowPattern)) {
            return null;
        }
        Map<String, Object> row = cursor.getCurrentRow();
        return row;
    }

    public static Date getTimestamp() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        now = calendar.getTime();
        return now;
    }

    static boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        if (str.length() <= 0) {
            return true;
        }
        return false;
    }

}
