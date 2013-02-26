package com.le.sunriise.table;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ListTablesCmd {
    private static final Logger log = Logger.getLogger(ListTablesCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;

        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
        } else {
            Class<ListTablesCmd> clz = ListTablesCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Database db = openedDb.getDb();
            Set<String> tableNames = db.getTableNames();

//            printRowColumns(db.getSystemCatalog());
            tableNames = db.getSystemTableNames();
            for (String tableName : tableNames) {
                Table table = db.getSystemTable(tableName);
                printRowColumns(table);
            }

            tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                printRowColumns(table);
            }

        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (openedDb != null) {
                openedDb.close();
                openedDb = null;
            }
            log.info("< DONE");
        }
    }

    private static void printRowColumns(Table table) {
        StringBuilder sb = new StringBuilder();
        sb.append(table.getName());

        sb.append("\t");
        sb.append(table.getRowCount());

        sb.append("\t");
        sb.append(table.getColumnCount());

        sb.append("\t");
        sb.append(table.getIndexes().size());

        System.out.println(sb.toString());

    }

}
