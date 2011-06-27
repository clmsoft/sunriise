package com.le.sunriise.quote;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class UpdateQuotes {
    private static final Logger log = Logger.getLogger(UpdateQuotes.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String quotesFileName = null;
        String inFileName = null;
        String password = null;

        if (args.length == 2) {
            quotesFileName = args[0];
            inFileName = args[1];
            password = null;
        } else if (args.length == 3) {
            quotesFileName = args[0];
            inFileName = args[1];
            password = args[2];
        } else {
            Class clz = UpdateQuotes.class;
            System.out.println("Usage: " + clz.getName() + " quotes.txt sample.mny [password]");
            System.exit(1);
        }

        File quotesFile = new File(quotesFileName);
        File inFile = new File(inFileName);

        log.info("quotesFile=" + quotesFile);
        log.info("inFile=" + inFile);

        try {
            UpdateQuotes updateQuotes = new UpdateQuotes();
            updateQuotes.update(quotesFile, inFile, password);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("< DONE");
        }
    }

    private void update(File quotesFile, File inFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);
            update(openedDb.getDb());
        } catch (IllegalStateException e) {
            // java.lang.IllegalStateException: Incorrect password provided
            throw new IOException(e);
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

    private void update(Database db) throws IOException {
        Table tSP = db.getTable("SP");

        Table tSEC = db.getTable("SEC");

        // SEC:
        // Name: (SEC) szFull
        // Name: (SEC) szSymbol

        Cursor cSEC = Cursor.createCursor(tSEC);
        Map<String, Object> rowPattern = new HashMap<String, Object>();

        rowPattern.clear();
        rowPattern.put("szSymbol", "MSFT");
        while (cSEC.moveToNextRow()) {
            if (cSEC.currentRowMatches(rowPattern)) {
                Map<String, Object> row = cSEC.getCurrentRow();
                log.info(row);
            }
        }
    }

}
