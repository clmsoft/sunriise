package com.le.sunriise.qif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ExportAccountsToQif {
    private static final Logger log = Logger.getLogger(ExportAccountsToQif.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;
        File outFile = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            outFile = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            password = args[1];
            outFile = new File(args[2]);
        } else {
            Class<ExportAccountsToQif> clz = ExportAccountsToQif.class;
            System.out.println("Usage: " + clz.getName() + " file.mny [passsword] out.qif");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        ExportAccountsToQif exporter = new ExportAccountsToQif();
        try {
            exporter.export(dbFile, password, outFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    private void export(File dbFile, String password, File outFile) throws IOException {
        OpenedDb openedDb = null;
        PrintWriter writer = null;
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            export(openedDb.getDb(), writer);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } finally {
                    writer = null;
                }
            }
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
        }

    }

    private void export(Database db, PrintWriter writer) throws IOException {
        String tableName = "ACCT";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            writer.println("!Option:AutoSwitch");
            writer.println("!Account");

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();
                
                String name = (String) row.get("szFull");
                if (name == null) {
                    continue;
                }
                if (name.length() == 0) {
                    continue;
                }
                
                Integer type = (Integer) row.get("at");
                
                writer.println("N" + name);
                writer.println("T" + type);
                writer.println("^");
            }
        } finally {
            writer.println("!Clear:AutoSwitch");
        }
    }
}
