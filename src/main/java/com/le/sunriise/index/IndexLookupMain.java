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
import com.le.sunriise.viewer.OpenedDb;

public class IndexLookupMain {
    private static final Logger log = Logger.getLogger(IndexLookupMain.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;;

        if (args.length == 1) {
            dbFileName = args[0];
        } else {
            Class clz = IndexLookupMain.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny");
            System.exit(1);
        }
        OpenedDb openedDb = null;
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);

        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            IndexLookup indexLookup = new IndexLookup();
            Database db = openedDb.getDb();
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
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
            log.info("< DONE");
        }
    }
}
