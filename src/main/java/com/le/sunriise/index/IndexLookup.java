package com.le.sunriise.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

public class IndexLookup {
    private static final Logger log = Logger.getLogger(IndexLookup.class);

    private class ColumnNameComparator implements Comparator<Column> {
        
        public int compare(Column o1, Column o2) {
            String o1Name = o1.getTable().getName() + "." + o1.getName();
            String o2Name = o2.getTable().getName() + "." + o2.getName();
            return o1Name.compareTo(o2Name);
        }
    }

    private ColumnNameComparator columnNameComparator = new ColumnNameComparator();

    public boolean isPrimaryKeyColumn(Column column) {
        Table table = column.getTable();
        for (Index index : table.getIndexes()) {
            if (index.isPrimaryKey()) {
                for (IndexData.ColumnDescriptor col : index.getColumns()) {
                    if (columnNameComparator.compare(col.getColumn(), column) == 0) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public List<Column> getReferencing(Column column) throws IOException {
        List<Column> columns = new ArrayList<Column>();
        Table table = column.getTable();
        for (Index index : table.getIndexes()) {
            if (index.isForeignKey() && index.getReference().isPrimaryTable()) {
                if (index.getColumns().size() != 1) {
                    log.warn("ForeignKey index must have exactly 1 column, index=" + index.getName());
                    continue;
                }
                if (indexHasColumn(index, column)) {
                    // load index/table from referenced table
                    Index otherIndex = index.getReferencedIndex();
                    if (otherIndex.getColumns().size() != 1) {
                        log.warn("ForeignKey index must have exactly 1 column, index=" + otherIndex.getName());
                        continue;
                    }
                    ColumnDescriptor columnDescriptor = otherIndex.getColumns().get(0);
                    columns.add(columnDescriptor.getColumn());
                    // Table otherTable = otherIndex.getTable();
                }
            }
        }
        return columns;
    }

    private boolean indexHasColumn(Index index, Column column) {
        for (ColumnDescriptor col : index.getColumns()) {
            if (col.getName().equals(column.getName())) {
                return true;
            }
        }
        return false;
    }

    public List<Column> getReferencedColumns(Column column) throws IOException {
        List<Column> columns = new ArrayList<Column>();
        Database db = column.getDatabase();
        for (String tableName : db.getTableNames()) {
            Table table = db.getTable(tableName);
            if (table == null) {
                log.warn("Cannot find table=" + tableName);
                continue;
            }
            findReferencedColumns(table, column, columns);
        }

        return columns;
    }

    private void findReferencedColumns(Table table, Column column, List<Column> columns) throws IOException {
        // Table table = column.getTable();
        for (Index index : table.getIndexes()) {
            if (index.isForeignKey() && index.getReference().isPrimaryTable()) {
                if (index.getColumns().size() != 1) {
                    log.warn("ForeignKey index must have exactly 1 column, index=" + index.getName());
                    continue;
                }
                // load index/table from referenced table
                Index otherIndex = index.getReferencedIndex();
                if (otherIndex.getColumns().size() != 1) {
                    log.warn("ForeignKey index must have exactly 1 column, index=" + otherIndex.getName());
                    continue;
                }
                ColumnDescriptor columnDescriptor = otherIndex.getColumns().get(0);
                if (columnNameComparator.compare(columnDescriptor.getColumn(), column) == 0) {
                    columns.add(index.getColumns().get(0).getColumn());
                }
                // Table otherTable = otherIndex.getTable();
            }
        }
    }

    public Long getMax(Column column) throws IOException {
        Long max = null;

        Cursor cursor = Cursor.createCursor(column.getTable());
        Collection<String> columnNames = new ArrayList<String>();
        columnNames.add(column.getName());
        Map<String, Object> row = null;
        while ((row = cursor.getNextRow(columnNames)) != null) {
            Object[] rowData = row.values().toArray();
            if (rowData == null) {
                continue;
            }
            if (rowData.length != 1) {
                continue;
            }
            Object obj = rowData[0];
            if (obj == null) {
                continue;
            }
            String value = null;
            if (obj instanceof byte[]) {
                value = ByteUtil.toHexString((byte[]) obj);
            } else {
                value = String.valueOf(obj);
            }
            Long currentValue = null;
            try {
                currentValue = Long.valueOf(value);
            } catch (NumberFormatException e) {
                if (log.isDebugEnabled()) {
                    log.warn(e);
                }
                continue;
            }
            if (currentValue == null) {
                continue;
            }
            if (max == null) {
                max = currentValue;
            } else {
                max = Math.max(max, currentValue);
            }
        }

        return max;
    }
}
