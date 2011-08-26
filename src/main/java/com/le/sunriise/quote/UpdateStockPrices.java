package com.le.sunriise.quote;

import java.io.File;
import java.io.IOException;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.viewer.OpenedDb;

public class UpdateStockPrices {
    private static final Logger log = Logger.getLogger(UpdateStockPrices.class);

    private final class RowByDateComparator implements Comparator<Map<String, Object>> {
        @Override
        public int compare(Map<String, Object> row1, Map<String, Object> row2) {
            Date dt1 = (Date) row1.get("dt");
            Date dt2 = (Date) row2.get("dt");

            if ((dt1 == null) && (dt2 == null)) {
                return 0;
            }

            if (dt1 == null) {
                return -1;
            }

            if (dt2 == null) {
                return 1;
            }

            return dt1.compareTo(dt2);
        }
    }

    public UpdateStockPrices() {
        super();
    }

    public void update(File quotesFile, File inFile, String password) throws IOException {
        List<PriceInfo> newPrices = PriceInfo.parse(quotesFile);
        update(newPrices, inFile, password);
    }

    public void update(List<PriceInfo> newPrices, File inFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);
            update(newPrices, openedDb.getDb());
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

    private void update(List<PriceInfo> newPrices, Database db) throws IOException {
        for (PriceInfo newPrice : newPrices) {
            if (!update(newPrice, db)) {
                log.warn("SKIP update for security=" + newPrice.getStockSymbol());
            }
        }
    }

    private boolean update(PriceInfo priceInfo, Database db) throws IOException {
        Integer hsec = null;

        // First let see if we have an existing security
        log.info("");
        String stockSymbol = priceInfo.getStockSymbol();
        log.info("Looking for stockSymbol=" + stockSymbol);
        hsec = QuoteUtils.findSecId(stockSymbol, db);
        if (hsec == null) {
            // no existing security. Skip
            log.warn("Cannot find stockSymbol=" + stockSymbol);
            return false;
        }

        log.info("Found stockSymbol=" + stockSymbol + ", hsec=" + hsec);

        // Update to which date?
        Date date = priceInfo.getDate();
        if (date == null) {
            date = QuoteUtils.getTimestamp();
        }
        if (log.isDebugEnabled()) {
            log.debug("date=" + date);
        }

        Table table = db.getTable("SP");
        Cursor cursor = Cursor.createCursor(table);

        Map<String, Object> row = null;

        row = findExistingRowToUpdate(cursor, hsec, date);
        if (row == null) {
            log.info("No existing row that we can update from. Will duplicate one.");
            row = findBestRowToUpdate(cursor, hsec);
            if (row == null) {
                log.warn("Has no existing row that we can duplicate from");
                return false;
            }

            // has a row that we can duplicate
            Map<String, Object> rowPattern = new HashMap<String, Object>();
            IndexLookup indexLookup = new IndexLookup();
            Column column = table.getColumn("hsp");
            Long value = indexLookup.getMax(column);
            value++;
            row.put("hsp", value);
            row.put("dt", date);

            Integer src = new Integer(5);
            // force src == 5
            if (src != null) {
                row.put("src", src);
            }
            // TODO: clear off other columns before adding?
            table.addRow(row.values().toArray());

            // now go look for it again
            row = findExistingRowToUpdate(cursor, hsec, date);
        }
        if (row == null) {
            log.warn("Cannot find just inserted row");
            return false;
        }

        Double dPrice = null;

        dPrice = (Double) row.get("dPrice");
        log.info("Old price " + dPrice);

        dPrice = priceInfo.getPrice();
        row.put("dPrice", dPrice);

        Column column = table.getColumn("dPrice");
        cursor.setCurrentRowValue(column, dPrice);
        row = cursor.getCurrentRow();
        dPrice = (Double) row.get("dPrice");
        log.info("New price " + dPrice);

        // Date dtSerial = new Date();
        // column = tSP.getColumn("dtSerial");
        // cSP.setCurrentRowValue(column, dtSerial);

        return row != null;
    }

    /*
     * Find a row from which we can update
     */
    private Map<String, Object> findExistingRowToUpdate(Cursor cursor, Integer hsec, Date date) throws IOException {
        Map<String, Object> row = null;

        // Index index = tSP.getIndex("HsecDateSrcSp");
        // index = null;
        // if (index != null) {
        // log.info("Has index=" + "HsecDateSrcSp");
        // cSP = IndexCursor.createCursor(tSP, index);
        // }

        Integer[] srcs = {
                // manual update
                new Integer(5),
                // online update
                new Integer(6), };
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        for (Integer src : srcs) {
            rowPattern.clear();
            rowPattern.put("dt", date);
            rowPattern.put("hsec", hsec);
            rowPattern.put("src", src);
            log.info("Looking for existing row with date=" + date + ", hsec=" + hsec + ", src=" + src);
            row = QuoteUtils.findRowFromTop(cursor, rowPattern);
            if (row != null) {
                log.info("Found one");
                break;
            }
        }

        return row;
    }

    private Map<String, Object> findBestRowToUpdate(Cursor cursor, Integer hsec) throws IOException {
        Map<String, Object> latestMatchedRow = null;

        Map<String, Object> row = null;

        Map<String, Object> rowPattern = new HashMap<String, Object>();

        Comparator<Map<String, Object>> dateComparator = new RowByDateComparator();

        // find one with src == 5, then src == 6
        Integer[] srcs = {
                // manual update
                new Integer(5),
                // online update
                new Integer(6), };
        for (Integer src : srcs) {
            cursor.beforeFirst();
            rowPattern.clear();
            rowPattern.put("hsec", hsec);
            if (src != null) {
                rowPattern.put("src", src);
            }
            while (cursor.moveToNextRow()) {
                if (cursor.currentRowMatches(rowPattern)) {
                    row = cursor.getCurrentRow();
                    if (row == null) {
                        log.warn("currentRow is null. SKIP");
                        continue;
                    }

                    if (latestMatchedRow == null) {
                        latestMatchedRow = row;
                    } else {
                        if (dateComparator.compare(row, latestMatchedRow) > 0) {
                            latestMatchedRow = row;
                        }
                    }
                }
            }

            if (latestMatchedRow != null) {
                if (log.isDebugEnabled()) {
                    log.debug("Found row with src == " + src + " that we can duplicate.");
                }
                break;
            }
        }

        if (latestMatchedRow == null) {
            Integer src = null;
            cursor.beforeFirst();
            rowPattern.clear();
            rowPattern.put("hsec", hsec);
            if (src != null) {
                rowPattern.put("src", src);
            }
            while (cursor.moveToNextRow()) {
                if (cursor.currentRowMatches(rowPattern)) {
                    row = cursor.getCurrentRow();
                    if (row == null) {
                        log.warn("currentRow is null. SKIP");
                        continue;
                    }

                    if (latestMatchedRow == null) {
                        latestMatchedRow = row;
                    } else {
                        if (dateComparator.compare(row, latestMatchedRow) > 0) {
                            latestMatchedRow = row;
                        }
                    }
                }
            }
        }
        if (latestMatchedRow == null) {
            log.info("Cannot find any row that we can duplicate from");
            return null;
        }

        log.info("Found row that we can duplicate from, dt=" + row.get("dt") + ", hsec=" + row.get("hsec") + ", src=" + row.get("src"));

        return latestMatchedRow;
    }

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
            Class clz = UpdateStockPrices.class;
            System.out.println("Usage: " + clz.getName() + " quotes.csv sample.mny [password]");
            System.exit(1);
        }

        File quotesFile = new File(quotesFileName);
        File inFile = new File(inFileName);

        log.info("quotesFile=" + quotesFile);
        log.info("inFile=" + inFile);

        if (!quotesFile.exists()) {
            log.error("File " + quotesFile + " does not exist.");
            System.exit(1);
        }

        if (!inFile.exists()) {
            log.error("File " + inFile + " does not exist.");
            System.exit(1);
        }

        try {
            UpdateStockPrices updateQuotes = new UpdateStockPrices();
            updateQuotes.update(quotesFile, inFile, password);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("< DONE");
        }
    }
}
