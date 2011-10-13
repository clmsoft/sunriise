package com.le.sunriise.graphviz;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class GraphVizMain {
    private static final Logger log = Logger.getLogger(GraphVizMain.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        OpenedDb openedDb = null;
        String dbFileName = null;
        File outDir = new File("target/graphviz");

        if (args.length == 1) {
            dbFileName = args[0];
        } else if (args.length == 2) {
            dbFileName = args[0];
            outDir = new File(args[1]);
        } else {
            Class<GraphVizMain> clz = GraphVizMain.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny [outDir]");
            System.exit(1);
        }
        outDir.mkdirs();
        log.info("outDir=" + outDir);
        
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);

        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Database db = openedDb.getDb();
            Set<String> tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                if (table == null) {
                    log.warn("Cannot find table=" + tableName);
                    continue;
                }
                File outFile = new File(outDir, table.getName() + ".dot");
                GenGraphViz genGraphViz = new GenGraphViz();
                genGraphViz.gen(table, outFile);
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
            log.info("outDir=" + outDir);
            log.info("< DONE");
        }
    }

}
