package com.le.sunriise.index;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.Index.ForeignKeyReference;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

public class MyWalkIndexes extends WalkIndexes {
    private static final Logger log = Logger.getLogger(MyWalkIndexes.class);

    private Set<Integer> primaryKeyIndexes = new HashSet<Integer>();
    private Set<Integer> foreignKeyIndexes = new HashSet<Integer>();
    private Set<Integer> uniqueIndexes = new HashSet<Integer>();

    private List<String> danglingReferences = new ArrayList<String>();

    public MyWalkIndexes(File dbFile, String password) throws IOException {
        super(dbFile, password);
    }

    // 
    // protected boolean accept(Table table) {
    // String tableName = table.getName();
    // if (tableName.compareToIgnoreCase("SP") == 0) {
    // return true;
    // }
    // return false;
    // }

    // 
    // protected boolean accept(Index index, Table table) {
    // String name = index.getName();
    // if (name.compareToIgnoreCase("PrimaryKey") != 0) {
    // return false;
    // }
    //
    // return true;
    // }

    
    @Override
    protected void walk(Index idx, Table table) throws IOException {
//        String indexName = idx.getName();
//        log.info("");
//        log.info("indexName=" + indexName);
//        log.info("  isPrimaryKey=" + idx.isPrimaryKey());
//        log.info("  isForeignKey=" + idx.isForeignKey());
//        log.info("  isUnique=" + idx.isUnique());

//        todo(index, table);
        // https://sourceforge.net/projects/jackcess/forums/forum/456474/topic/4563641
        /*
code]
Index idx = ...;  // get index from current table
IndexCursor otherCursor = null;

if(idx.isForeignKey() && idx.getReference().isPrimaryTable()) {
 // load index/table from referenced table
 Index otherIdx = idx.getReferencedIndex();
 Table otherTable = otherIdx.getTable();
 otherCursor = IndexCursor.createCursor(otherTable, otherIdx);
}

Map<String,Object> row = ...; // row from current table

// create an entry to lookup a joined row in the other table
Object[] entryKey = new Object[index.getColumns().size()];
for(int i = 0; i < entryKey.length; ++i) {
 entryKey[i] = row.get(index.getColumns().get(i).getName()];
}

Map<String,Object> otherRow = null;
if(othercursor.findFirstRowByEntry(entryKey)) {
 otherRow = otherCursor.getCurrentRow();
}
[/code]
         */
//        IndexCursor otherCursor = null;

        if (idx.isForeignKey() && idx.getReference().isPrimaryTable()) {
            // load index/table from referenced table
            Index otherIdx = idx.getReferencedIndex();
            Table otherTable = otherIdx.getTable();
            if (otherTable == null) {
                log.warn("reference.tableName=" + null);
            } else {
//                log.info("reference.tableName=" + otherTable.getName());
//                log.info("  " + idx.getName() + ", " + otherIdx.getName());
                // otherCursor = IndexCursor.createCursor(otherTable, otherIdx);
                int max = Math.max(idx.getColumns().size(), otherIdx.getColumns().size());
                for(int i = 0; i < max; i++) {
                    String columnName = null;
                    if (i < idx.getColumns().size()) {
                        columnName = idx.getColumns().get(i).getName();
                    }
                    String leftStr = table.getName() + "." + columnName;
                     columnName = null;
                    if (i < otherIdx.getColumns().size()) {
                        columnName = otherIdx.getColumns().get(i).getName();
                    }
                    String rightStr = otherTable.getName() + "." + columnName;
                    log.info("  " + leftStr + " -> " + rightStr);
                }
            }
        }
    }

    private void todo(Index index, Table table) throws IOException {
        if (index.isForeignKey()) {
            ForeignKeyReference reference = index.getReference();
            int otherTablePageNumber = reference.getOtherTablePageNumber();
            Table otherTable = null;
            Set<String> tableNames = getDb().getTableNames();
            for (String tableName : tableNames) {
                Table t = getDb().getTable(tableName);
                int pageNumber = /* t.getTableDefPageNumber() */ 0;
                if (pageNumber == otherTablePageNumber) {
                    otherTable = t;
                    break;
                }
            }
            if (otherTable == null) {
                log.info("reference.tableName=" + null);
            } else {
                log.info("reference.tableName=" + otherTable.getName());
                String otherColumnName = index.getColumns().get(0).getName();
                try {
                    Column otherColumn = otherTable.getColumn(otherColumnName);
                    if (otherColumn == null) {
                        log.error("Cannot find otherTable's column=" + otherTable.getName() + "." + otherColumnName);
                        danglingReferences.add(table.getName() + "." + otherColumnName + "(" + index.getName() + ")" + " -/->" + otherTable.getName() + "."
                                + otherColumnName);
                    }
                } catch (IllegalArgumentException e) {
                    log.error("Cannot find otherTable's column=" + otherTable.getName() + "." + otherColumnName);
                    danglingReferences.add(table.getName() + "." + otherColumnName + "(" + index.getName() + ")" + " -/->" + otherTable.getName() + "."
                            + otherColumnName);
                }
            }
        }
        List<ColumnDescriptor> columnDescriptors = index.getColumns();
        log.info("  columns#=" + columnDescriptors.size());

        for (ColumnDescriptor columnDescriptor : columnDescriptors) {
            Column column = columnDescriptor.getColumn();
            log.info("  column=" + column.getTable().getName() + "." + column.getName());

            if (index.isPrimaryKey()) {
                primaryKeyIndexes.add(column.getColumnIndex());
            }

            if (index.isForeignKey()) {
                foreignKeyIndexes.add(column.getColumnIndex());
            }

            if (index.isUnique()) {
                uniqueIndexes.add(column.getColumnIndex());
            }
        }
    }

    public Set<Integer> getPrimaryKeyIndexes() {
        return primaryKeyIndexes;
    }

    public Set<Integer> getForeignKeyIndexes() {
        return foreignKeyIndexes;
    }

    public Set<Integer> getUniqueIndexes() {
        return uniqueIndexes;
    }

    
    @Override
    protected void walk(Table table) throws IOException {
        primaryKeyIndexes = new HashSet<Integer>();
        foreignKeyIndexes = new HashSet<Integer>();
        uniqueIndexes = new HashSet<Integer>();

        if (log.isDebugEnabled()) {
            log.debug("");
            log.debug("table=" + table.getName());
        }
        try {
            super.walk(table);
        } finally {
//            Set<Integer> indexes = null;
//
//            log.info("");
//            indexes = getPrimaryKeyIndexes();
//            for (Integer index : indexes) {
//                log.info("primaryKey: " + index);
//            }
//            indexes = getForeignKeyIndexes();
//            for (Integer index : indexes) {
//                log.info("foreignKey: " + index);
//            }
//
//            indexes = getUniqueIndexes();
//            for (Integer index : indexes) {
//                log.info("unique: " + index);
//            }

        }
    }

    public List<String> getDanglingReferences() {
        return danglingReferences;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Database db = null;
        String dbFileName = "C:/Users/Hung Le/Documents/Microsoft Money/2007/temp/My Money - Copy.mny";
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);

        MyWalkIndexes getIndexes = null;
        try {
            getIndexes = new MyWalkIndexes(dbFile, password);
            getIndexes.walk();
            for (String danglingReference : getIndexes.getDanglingReferences()) {
                log.warn(danglingReference);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (getIndexes != null) {
                getIndexes.close();
            }
            log.info("< DONE");
        }
    }
}