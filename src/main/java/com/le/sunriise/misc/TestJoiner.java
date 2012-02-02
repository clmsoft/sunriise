package com.le.sunriise.misc;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Joiner;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class TestJoiner {
    private static final Logger log = Logger.getLogger(TestJoiner.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;
        String fromTableName = null;
        String toTableName = null;

        if (args.length == 3) {
            dbFile = new File(args[0]);
            fromTableName = args[1];
            toTableName = args[2];
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
            fromTableName = args[2];
            toTableName = args[3];
        } else {
            Class<TestJoiner> clz = TestJoiner.class;

            System.out.println("Usage: java " + clz.getName() + " file.mny [password] fromTable toTable");

            System.exit(1);
        }

        printJoinRows(dbFile, password, fromTableName, toTableName);
    }

    private static void printJoinRows(File dbFile, String password, String fromTableName, String toTableName) {
        OpenedDb openedDb = null;

        log.info("dbFile=" + dbFile);
        log.info("fromTableName=" + fromTableName);
        log.info("toTableName=" + toTableName);

        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);

            Database db = openedDb.getDb();

            printJoinRows(db, fromTableName, toTableName);
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
        }
    }

    private static void printJoinRows(Database db, String fromTableName, String toTableName) throws IOException {
        Table fromTable = db.getTable(fromTableName);
        if (fromTable == null) {
            log.warn("Cannot find fromTable=" + fromTableName);
            return;
        }

        Table toTable = db.getTable(toTableName);
        if (toTable == null) {
            log.warn("Cannot find toTableName=" + toTableName);
            return;
        }
        
        Joiner join = Joiner.create(fromTable, toTable);
        printJoinInfo(join);

        for (Map<String, Object> fromRow : join.getFromTable()) {
            log.info("fromRow=" + fromRow);
            int count = 0;
            for (Map<String, Object> toRow : join.findRowsIterable(fromRow)) {
                log.info("toRow=" + toRow);
                count++;
            }
            log.info("  count=" + count);
            log.info("");
        }
    }

    private static void printJoinInfo(Joiner join) {
        log.info("fromTable=" + join.getFromTable().getName());
        log.info("fromIndex=" + join.getFromIndex().getName());

        log.info("toTable=" + join.getToTable().getName());
        log.info("toIndex=" + join.getToIndex().getName());

        log.info("");
    }
}
