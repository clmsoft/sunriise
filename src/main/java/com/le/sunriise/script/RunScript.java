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
package com.le.sunriise.script;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class RunScript {
    private static final Logger log = Logger.getLogger(RunScript.class);

    private String tableName;

    private List<MatchColumn> selectColumns = new ArrayList<MatchColumn>();

    private List<MatchColumn> setColumns = new ArrayList<MatchColumn>();

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public List<MatchColumn> getSelectColumns() {
        return selectColumns;
    }

    public void setSelectColumns(List<MatchColumn> matches) {
        this.selectColumns = matches;
    }

    public List<MatchColumn> getSetColumns() {
        return setColumns;
    }

    public void setSetColumns(List<MatchColumn> setValues) {
        this.setColumns = setValues;
    }

    public static void runScript(File dbFile, File scriptFile, String password) throws IOException {
        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(dbFile, password);
            List<RunScript> runScripts = RunScriptUtils.parseScriptFile(scriptFile);
            for (RunScript runScript : runScripts) {
                log.info("Applying script on table='" + runScript.getTableName() + "'");
                runScript.execScriptFile(openedDb);
            }
        } finally {
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
        }
    }

    private void execScriptFile(OpenedDb openedDb) throws IOException {
        if (tableName == null) {
            throw new IOException("tablename is null.");
        }
        Database db = openedDb.getDb();
        Table table = db.getTable(tableName);
        if (table == null) {
            throw new IOException("Cannot find tableName=" + tableName);
        }
        Column columnPattern = null;
        Object valuePattern = null;
        int count = 0;
        List<MatchColumn> matchColumns = selectColumns;
        for (MatchColumn matchColumn : matchColumns) {
            String columnName = matchColumn.getColumnName();
            Column column = null;
            Object value = matchColumn.getValue();
            try {
                column = table.getColumn(columnName);
                if (column == null) {
                    log.warn("Invalid columnName=" + columnName);
                }
                Class clz = com.le.sunriise.viewer.MynViewer.getColumnJavaClass(column);
                if (log.isDebugEnabled()) {
                    log.debug(column.getName() + ", " + clz.getName() + ", " + column.getType());
                }
                if (clz.isAssignableFrom(Integer.class)) {
                    try {
                        Number i = Integer.valueOf(value.toString());
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Long.class)) {
                    try {
                        Number i = null;
                        if (column.getType().isLongValue()) {
                            i = Long.valueOf(value.toString());
                        } else {
                            i = Integer.valueOf(value.toString());
                        }
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Double.class)) {
                    try {
                        Double i = Double.valueOf(value.toString());
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Date.class)) {
                    throw new IOException("Date converter is not implemented yet.");
                } else {
                    // assuming string
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid columnName=" + columnName);
            }
            if (count == 0) {
                columnPattern = column;
                valuePattern = matchColumn.getValue();
            }

            log.info("Select column=" + column.getName() + ", value=" + matchColumn.getValue() + ", valueClass="
                    + matchColumn.getValue().getClass().getName());

            count++;
        }

        Cursor cursor = Cursor.createCursor(table);
        if ((columnPattern != null) && (valuePattern != null)) {
            String columnName = columnPattern.getName();

            if (log.isDebugEnabled()) {
                log.debug("columnName=" + columnName);
                log.debug("valuePattern=" + valuePattern.getClass().getName());
            }

            Iterable<Map<String, Object>> rows = cursor.columnMatchIterable(columnPattern, valuePattern);
            for (Map<String, Object> row : rows) {
                if (log.isDebugEnabled()) {
                    log.debug("> row=" + row);
                }

                count = 0;
                boolean skipThisRow = false;
                for (MatchColumn match : selectColumns) {
                    if (count == 0) {
                        count++;
                        continue;
                    }

                    Object v = row.get(match.getColumnName());
                    if (v instanceof Comparable) {
                        if (((Comparable) v).compareTo(match.getValue()) != 0) {
                            skipThisRow = true;
                            break;
                        } else {
                            if (!v.equals(match.getValue())) {
                                skipThisRow = true;
                                break;
                            }
                        }
                    }
                    count++;
                }
                if (skipThisRow) {
                    continue;
                }

                updateRow(table, cursor, row);
            }
        }
    }

    private void updateRow(Table table, Cursor cursor, Map<String, Object> row) throws IOException {
        if (log.isDebugEnabled()) {
            log.debug(row);
        }

        List<MatchColumn> matchColumns = setColumns;
        for (MatchColumn matchColumn : matchColumns) {
            Column column = null;
            String columnName = matchColumn.getColumnName();
            Object value = matchColumn.getValue();
            try {
                column = table.getColumn(columnName);
                if (column == null) {
                    log.warn("Invalid columnName=" + columnName);
                }
                Class clz = com.le.sunriise.viewer.MynViewer.getColumnJavaClass(column);
                if (log.isDebugEnabled()) {
                    log.debug(column.getName() + ", " + clz.getName() + ", " + column.getType());
                }
                if (clz.isAssignableFrom(Integer.class)) {
                    try {
                        Number i = Integer.valueOf(value.toString());
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Long.class)) {
                    try {
                        Number i = null;
                        if (column.getType().isLongValue()) {
                            i = Long.valueOf(value.toString());
                        } else {
                            i = Integer.valueOf(value.toString());
                        }
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Double.class)) {
                    try {
                        Double i = Double.valueOf(value.toString());
                        matchColumn.setValue(i);
                    } catch (NumberFormatException e) {
                        log.warn(e);
                    }
                } else if (clz.isAssignableFrom(Date.class)) {
                    throw new IOException("Date converter is not implemented yet.");
                } else {
                    // assuming string - as-is
                }
            } catch (IllegalArgumentException e) {
                log.warn("Invalid columnName=" + columnName);
            }

            log.info("update column=" + column.getName());
            Object oldValue = cursor.getCurrentRowValue(column);
            Object newValue = matchColumn.getValue();
            log.info("  oldValue=" + oldValue);
            log.info("  newValue=" + newValue);
            if (newValue.equals(oldValue)) {
                log.warn("SKIP - New value is same as old value.");
            } else {
                cursor.setCurrentRowValue(column, newValue);
            }
            Object currentValue = cursor.getCurrentRowValue(column);
            log.info("  currentValue=" + currentValue);
        }
    }

}
