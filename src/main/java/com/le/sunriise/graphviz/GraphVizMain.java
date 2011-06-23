package com.le.sunriise.graphviz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;

public class GraphVizMain {
    private static final Logger log = Logger.getLogger(GraphVizMain.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Database db = null;
        String dbFileName = "C:/Users/Hung Le/Documents/Microsoft Money/2007/temp/My Money - Copy.mny";
        dbFileName = "./misc/2005UK/copy of sample.mny";
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);

        try {
            db = Utils.openDbReadOnly(dbFile, password);
            Set<String> tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                if (table == null) {
                    log.warn("Cannot find table=" + tableName);
                    continue;
                }
                File d = new File("target");
                d = new File(d, "graphviz");
                d.mkdirs();
                File outFile = new File(d, table.getName() + ".dot");
                GenGraphViz genGraphViz = new GenGraphViz();
                genGraphViz.gen(table, outFile);
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
