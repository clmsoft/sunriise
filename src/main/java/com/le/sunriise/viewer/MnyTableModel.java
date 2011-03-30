package com.le.sunriise.viewer;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.table.AbstractTableModel;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

final class MnyTableModel extends AbstractTableModel {
    private static final Logger log = Logger.getLogger(MnyTableModel.class);
    private final Table table;
    private int currentRow = 0;
    private Cursor cursor;
    private Map<String, Object> data = null;
    private boolean dbReadOnly = false;

    MnyTableModel(Table table) throws IOException {
        this.table = table;
        this.cursor = Cursor.createCursor(table);
        this.cursor.reset();
        this.cursor.moveToNextRow();
    }

    public int getRowCount() {
        return table.getRowCount();
    }

    public int getColumnCount() {
        return table.getColumnCount();
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        try {
            moveCursorToRow(rowIndex);
            value = data.get(getColumnName(columnIndex));
            if (value instanceof byte[]) {
                value = ByteUtil.toHexString((byte[]) value);
            }
        } catch (IOException e) {
            log.error(e, e);
        }

        return value;
    }

    private void moveCursorToRow(int rowIndex) throws IOException {
        int delta = rowIndex - currentRow;
        currentRow = rowIndex;
        if (delta == 0) {
            if (data == null) {
                data = cursor.getCurrentRow();
            }
        } else if (delta < 0) {
            cursor.movePreviousRows(-delta);
            data = cursor.getCurrentRow();
        } else {
            cursor.moveNextRows(delta);
            data = cursor.getCurrentRow();
        }
    }

    public String getColumnName(int column) {
        List<Column> cols = table.getColumns();
        return cols.get(column).getName();
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
        Class clz = MnyViewer.getColumnJavaClass(column);
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
//            data.put(getColumnName(columnIndex), aValue);
//            cursor.updateCurrentRow(data.values().toArray());
            cursor.setCurrentRowValue(table.getColumn(getColumnName(columnIndex)), aValue);
            data = cursor.getCurrentRow();
            fireTableCellUpdated(rowIndex, columnIndex);
        } catch (IOException e) {
            log.error(e, e);
        }
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
            currentRow = 0;
            data = null;
            cursor.reset();
            cursor.moveToNextRow();
            fireTableRowsDeleted(rowIndex, rowIndex);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    public void duplicateRow(int rowIndex) {
        log.info("> duplicateRow rowIndex=" + rowIndex);
        if (dbReadOnly) {
            return;
        }
        try {
            moveCursorToRow(rowIndex);
            Table table = cursor.getTable();
            Set<Integer> uniqueColumnIndex = new HashSet<Integer>();
            List<Index> indexes = table.getIndexes();
            for(Index index: indexes) {
                if (! index.isUnique()) {
                    continue;
                }
                List<ColumnDescriptor> columns = index.getColumns();
                for(ColumnDescriptor column: columns) {
                    uniqueColumnIndex.add(column.getColumnIndex());
                }
            }
            Object[] dataArray = data.values().toArray();
            for(int i = 0; i < dataArray.length; i++) {
                if(uniqueColumnIndex.contains(i)) {
                    dataArray[i] = "";
                }
            }
            int rowCount = table.getRowCount();
            table.addRow(dataArray);
            currentRow = 0;
            data = null;
            cursor.reset();
            cursor.moveToNextRow();
            fireTableRowsInserted(rowCount, rowCount);
        } catch (IOException e) {
            log.error(e, e);
        }
    }
}