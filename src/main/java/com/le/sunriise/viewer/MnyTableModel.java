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
package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.ClipboardOwner;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.google.common.collect.MapMaker;
import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.index.IndexLookup;

public class MnyTableModel extends AbstractTableModel {
    private static final Logger log = Logger.getLogger(MnyTableModel.class);

    private final Table table;
    private int currentRow = 0;
    private final Cursor cursor;
    // private Map<String, Object> rowData = null;
    private boolean dbReadOnly = false;
    private final IndexLookup indexLookup = new IndexLookup();
    private final Map<String, Object> cellsCache;

    private final List<Column> columns;

    private final Column[] columnsArray;

    private final Map<Integer, Map<String, Object>> rowsCache;

    public MnyTableModel(Table table) throws IOException {
        this.table = table;
        this.columns = table.getColumns();
        this.columnsArray = new Column[this.columns.size()];
        this.columns.toArray(this.columnsArray);
        this.cursor = Cursor.createCursor(table);
        this.cursor.reset();
        this.cursor.moveToNextRow();
        // this.cellsCache = new HashMap<String, Object>();
        this.cellsCache = new MapMaker().softValues().maximumSize(50000).makeMap();
        // this.rowsCache = new HashMap<Integer, Map<String, Object>>();
        this.rowsCache = new MapMaker().softValues().maximumSize(50000).makeMap();
    }

    @Override
    public int getRowCount() {
        return table.getRowCount();
    }

    @Override
    public int getColumnCount() {
        return table.getColumnCount();
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (log.isDebugEnabled()) {
            log.debug("> getValueAt: rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
        }

        Object value = null;
        String cachedKey = createCachedKey(rowIndex, columnIndex);
        Object cachedValue = null;
        if (cellsCache != null) {
            cachedValue = cellsCache.get(cachedKey);
        }
        if (cachedValue != null) {
            if (log.isDebugEnabled()) {
                log.debug("cached HIT");
            }
            return cachedValue;
        }
        try {
            Map<String, Object> rowData = null;
            if (rowsCache != null) {
                rowData = rowsCache.get(rowIndex);
            }
            if (rowData == null) {
                moveCursorToRow(rowIndex);
                rowData = cursor.getCurrentRow();
                if (rowsCache != null) {
                    rowsCache.put(rowIndex, rowData);
                }
            }
            String columnName = getColumnName(columnIndex);
            value = rowData.get(columnName);
            if (value instanceof byte[]) {
                value = ByteUtil.toHexString((byte[]) value);
            }
        } catch (IOException e) {
            log.error(e, e);
        }
        if (cellsCache != null) {
            if (log.isDebugEnabled()) {
                log.debug("cached MISSED");
            }
            if (value != null) {
                cellsCache.put(cachedKey, value);
            }
        }
        return value;
    }

    private String createCachedKey(int rowIndex, int columnIndex) {
        return rowIndex + "*" + columnIndex;
    }

    private void moveCursorToRow(int rowIndex) throws IOException {
        int delta = rowIndex - currentRow;
        currentRow = rowIndex;
        if (delta == 0) {
            // if (rowData == null) {
            // rowData = cursor.getCurrentRow();
            // }
        } else if (delta < 0) {
            cursor.movePreviousRows(-delta);
            // rowData = cursor.getCurrentRow();
        } else {
            cursor.moveNextRows(delta);
            // rowData = cursor.getCurrentRow();
        }
    }

    @Override
    public String getColumnName(int column) {
        return columnsArray[column].getName();
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return !dbReadOnly;
    }

    @Override
    public Class<?> getColumnClass(int columnIndex) {
        if (table == null) {
            log.info("getColumnClass, t=" + table + ", " + columnIndex);
            return super.getColumnClass(columnIndex);
        }
        List<Column> cols = table.getColumns();
        Column column = cols.get(columnIndex);
        Class clz = MynViewer.getColumnJavaClass(column);
        if (log.isDebugEnabled()) {
            log.debug("getColumnClass, " + columnIndex + ", " + clz);
        }
        return clz;
    }

    @Override
    public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        if (dbReadOnly) {
            return;
        }
        try {
            moveCursorToRow(rowIndex);
            // data.put(getColumnName(columnIndex), aValue);
            // cursor.updateCurrentRow(data.values().toArray());
            Column column = table.getColumn(getColumnName(columnIndex));
            Object oldValue = cursor.getCurrentRowValue(column);
            cursor.setCurrentRowValue(column, aValue);

            log.info("setValueAt: oldValue=" + oldValue + ", newValue=" + aValue);

            // Map<String, Object> rowData = cursor.getCurrentRow();

            if (cellsCache != null) {
                String cachedKey = createCachedKey(rowIndex, columnIndex);
                cellsCache.put(cachedKey, aValue);
            }

            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    public Map<String, Object> getRowValues(int rowIndex) throws IOException {
        moveCursorToRow(rowIndex);
        Map<String, Object> rowData = cursor.getCurrentRow();
        return rowData;
    }

    public boolean isDbReadOnly() {
        return dbReadOnly;
    }

    public void setDbReadOnly(boolean dbReadOnly) {
        this.dbReadOnly = dbReadOnly;
    }

    public void deleteRow(int rowIndex) {
        log.info("> deleteRow rowIndex=" + rowIndex);
        if (dbReadOnly) {
            return;
        }
        try {
            moveCursorToRow(rowIndex);
            cursor.deleteCurrentRow();
            resetCursor();
            if (cellsCache != null) {
                cellsCache.clear();
            }
            if (rowsCache != null) {
                rowsCache.clear();
            }
            fireTableRowsDeleted(rowIndex, rowIndex);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    private void resetCursor() throws IOException {
        currentRow = 0;
        // rowData = null;
        cursor.reset();
        cursor.moveToNextRow();
    }

    public void copyColumn(int rowIndex, int columnIndex) {
        if (log.isDebugEnabled()) {
            log.debug("> copyColumn rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
        }
        try {
            Object value = getValueAt(rowIndex, columnIndex);

            String columnName = getColumnName(columnIndex);
            log.info("columnName=" + columnName + ", value=" + value + ", className="
                    + ((value == null) ? null : value.getClass().getName()));
            if (valueIsFlag(columnName)) {
                int num = Integer.valueOf(value.toString());
                StringBuffer sb = new StringBuffer();
                int maxBits = Integer.SIZE;
                for (int i = 0; i < maxBits; i++) {
                    sb.append(((num & 1) == 1) ? '1' : '0');
                    num >>= 1;
                }

                String binaryString = sb.reverse().toString();
                log.info("    value (binary)=" + binaryString);
            }
            StringSelection stringSelection = new StringSelection(value.toString());
            Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
            ClipboardOwner owner = new ClipboardOwner() {
                @Override
                public void lostOwnership(Clipboard clipboard, Transferable contents) {
                }
            };
            clipboard.setContents(stringSelection, owner);
        } finally {
            // TODO: not needed?
        }

    }

    private boolean valueIsFlag(String columnName) {
        String[] flagColumns = { "grftt", };

        if (columnName == null) {
            return false;
        }

        for (String flagColumn : flagColumns) {
            if (columnName.compareToIgnoreCase(flagColumn) == 0) {
                return true;
            }
        }
        return false;
    }

    public void duplicateRow(int rowIndex, Component locationRealativeTo) {
        log.info("> duplicateRow rowIndex=" + rowIndex);
        if (dbReadOnly) {
            return;
        }
        try {
            moveCursorToRow(rowIndex);
            Map<String, Object> rowData = cursor.getCurrentRow();
            Table table = cursor.getTable();
            IndexLookup indexLooker = new IndexLookup();
            List<Column> columns = table.getColumns();
            Object[] dataArray = rowData.values().toArray();
            for (int i = 0; i < dataArray.length; i++) {
                Column column = columns.get(i);
                if (indexLooker.isPrimaryKeyColumn(column)) {
                    Long max = indexLooker.getMax(column);
                    max = max + 1;
                    dataArray[i] = max.toString();
                }
            }

            int rowCount = table.getRowCount();
            table.addRow(dataArray);
            resetCursor();
            if (cellsCache != null) {
                cellsCache.clear();
            }
            if (rowsCache != null) {
                rowsCache.clear();
            }
            fireTableRowsInserted(rowCount, rowCount);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    public boolean columnIsDateType(int i) {
        List<Column> columns = table.getColumns();
        if (columns == null) {
            return false;
        }
        if (i >= columns.size()) {
            return false;
        }
        Column column = columns.get(i);
        DataType dataType = column.getType();
        if (dataType == DataType.SHORT_DATE_TIME) {
            return true;
        } else {
            return false;
        }
    }

    public boolean isPrimaryKeyColumn(int i) {
        List<Column> columns = table.getColumns();
        if (columns == null) {
            return false;
        }
        if (i >= columns.size()) {
            return false;
        }
        Column column = columns.get(i);
        return indexLookup.isPrimaryKeyColumn(column);
    }

    public boolean isForeignKeyColumn(int i) {
        List<Column> columns = table.getColumns();
        if (columns == null) {
            return false;
        }
        if (i >= columns.size()) {
            return false;
        }
        Column column = columns.get(i);
        List<Column> referenced = null;

        try {
            referenced = indexLookup.getReferencedColumns(column);
        } catch (IOException e) {
            log.warn(e);
        }
        if (referenced == null) {
            return false;
        }
        return referenced.size() > 0;
    }

    public void close() {
        if (cellsCache != null) {
            cellsCache.clear();
        }
        if (rowsCache != null) {
            rowsCache.clear();
        }

    }
}