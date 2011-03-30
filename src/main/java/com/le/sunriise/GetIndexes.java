package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

public class GetIndexes {

    private static final Logger log = Logger.getLogger(GetRelationships.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Database db = null;
        String dbFileName = "C:/Users/Hung Le/Documents/Microsoft Money/2007/temp/My Money - Copy.mny";
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);
        try {
            db = Utils.openDbReadOnly(dbFile, password);
            Set<String> tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                if (table == null) {
                    continue;
                }
                List<Index> indexes = table.getIndexes();
                for (Index index : indexes) {
                    String name = index.getName();
//                    if (! name.equals("PrimaryKey")) {
//                        continue;
//                    }
                    if (! index.isUnique()) {
                        continue;
                    }
                    List<ColumnDescriptor> columns = index.getColumns();
                    log.info(table.getName() + ", " + name + " (" + columns.size() + ")");
                    log.info("  " + index.isUnique());
                    for(ColumnDescriptor column: columns) {
                        log.info("  " + column.getName());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    db = null;
                }
            }
            log.info("< DONE");
        }
    }
}