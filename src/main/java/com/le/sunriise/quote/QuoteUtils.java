/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
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

    public static String getSymbol(Integer hsec, Database db) throws IOException {
        String[] columns = { "szSymbol", "mUID"/* , "szFull" */};
        String symbol = null;
        Table table = db.getTable(TABLE_NAME_SEC);
        Cursor cursor = Cursor.createCursor(table);
        cursor.reset();
        while (cursor.moveToNextRow()) {
            Map<String, Object> rowPattern = new HashMap<String, Object>();
            rowPattern.put("hsec", hsec);
            if (cursor.currentRowMatches(rowPattern)) {
                Map<String, Object> row = cursor.getCurrentRow();
                for (String column : columns) {
                    Object value = row.get(column);
                    if (value != null) {
                        symbol = (String) value;
                        if (symbol.length() > 0) {
                            break;
                        }
                        symbol = null;
                    }
                }
            }
        }
        return symbol;
    }

    public static Map<String, Object> findRowFromTop(Cursor cursor, Map<String, Object> rowPattern) throws IOException {
        cursor.beforeFirst();
        if (!cursor.findFirstRow(rowPattern)) {
            return null;
        }
        Map<String, Object> row = cursor.getCurrentRow();
        return row;
    }

    public static Date getTimestamp() {
        return getTimestamp(0);
    }

    public static Date getTimestamp(int dayOffset) {
        return getTimestamp(dayOffset, false);
    }

    public static Date getTimestamp(int dayOffset, boolean weekDayOnly) {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        calendar.roll(Calendar.DAY_OF_YEAR, dayOffset);
        if (weekDayOnly) {
            int dateOfWeek = calendar.get(Calendar.DAY_OF_WEEK);
            if (dateOfWeek == Calendar.SATURDAY) {
                calendar.roll(Calendar.DAY_OF_YEAR, -1);
            } else if (dateOfWeek == Calendar.SUNDAY) {
                calendar.roll(Calendar.DAY_OF_YEAR, -2);
            }
        }
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
