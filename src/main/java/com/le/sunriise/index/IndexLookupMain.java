package com.le.sunriise.index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;

public class IndexLookupMain {
    private static final Logger log = Logger.getLogger(IndexLookupMain.class);

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
            IndexLookup indexLookup = new IndexLookup();
            Set<String> tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                if (table == null) {
                    log.warn("Cannot find table=" + tableName);
                    continue;
                }
                log.info("### table=" + table.getName());
                for (Column column : table.getColumns()) {
                    if (indexLookup.isPrimaryKeyColumn(column)) {
                        log.info("(PK) " + table.getName() + "." + column.getName() + ", " + indexLookup.getMax(column));
                    }
                    List<Column> referencing = indexLookup.getReferencing(column);
                    for (Column col : referencing) {
                        log.info("(referencing-FK) " + col.getTable().getName() + "." + col.getName());
                    }

                    List<Column> referenced = indexLookup.getReferencedColumns(column);
                    for (Column col : referenced) {
                        log.info("(FK) " + table.getName() + "." + column.getName() + " -> " + col.getTable().getName() + "." + col.getName());
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
                    log.warn(e, e);
                } finally {
                    db = null;
                }
            }
            log.info("< DONE");
        }
    }
}
