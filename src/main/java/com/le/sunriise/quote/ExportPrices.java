package com.le.sunriise.quote;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ExportPrices {
    private static final Logger log = Logger.getLogger(ExportPrices.class);

    public void export(File inFile, File outFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDbReadOnly(inFile, password);
            export(openedDb.getDb(), outFile);
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

    private void export(Database db, File outFile) throws IOException {
        Table table = db.getTable("SP");
        Cursor cursor = Cursor.createCursor(table);
        while (cursor.moveToNextRow()) {
            Map<String, Object> row = cursor.getCurrentRow();
            Date date = (Date) row.get("dt");
            Integer hsec = (Integer) row.get("hsec");
            String symbol = QuoteUtils.getSymbol(hsec, db);
            Double price = (Double) row.get("dPrice");
            log.info(date + ", " + symbol + ", " + price);
        }
    }

}
