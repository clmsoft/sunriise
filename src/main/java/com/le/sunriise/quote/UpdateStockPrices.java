package com.le.sunriise.quote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexCursor;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.viewer.OpenedDb;

public class UpdateStockPrices {
    private static final Logger log = Logger.getLogger(UpdateStockPrices.class);

    private NumberFormat priceFormatter;

    private SimpleDateFormat csvDateFormatter = new SimpleDateFormat("yyyy/MM/dd");

    public UpdateStockPrices() {
        super();
        this.priceFormatter = NumberFormat.getNumberInstance();
        this.priceFormatter.setGroupingUsed(false);
        this.priceFormatter.setMinimumFractionDigits(2);
        this.priceFormatter.setMaximumFractionDigits(10);
    }

    private void update(File quotesFile, File inFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);

            List<PriceInfo> newPrices = getNewPrices(quotesFile);

            update(openedDb.getDb(), newPrices);
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

    private List<PriceInfo> getNewPrices(File quotesFile) throws IOException {
        List<PriceInfo> newPrices = new ArrayList<PriceInfo>();

        CsvReader csvReader = null;
        try {
            csvReader = new CsvReader(new BufferedReader(new FileReader(quotesFile)));
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                String symbol = csvReader.get("symbol");
                if (isBlank(symbol)) {
                    log.warn("Bad format, 'symbol' column is blank");
                    continue;
                }

                String priceStr = csvReader.get("price");
                if (isBlank(priceStr)) {
                    log.warn("Bad format, 'price' column is blank. " + csvReader.getRawRecord());
                    continue;
                }
                Number price = null;
                try {
                    price = priceFormatter.parse(priceStr);
                } catch (ParseException e) {
                    log.warn(e);
                }
                if (price == null) {
                    continue;
                }

                String dateString = csvReader.get("date");
                Date date = null;
                if (isBlank(priceStr)) {
                    log.warn("No date, will use today");
                } else {
                    try {
                        date = csvDateFormatter.parse(dateString);
                    } catch (ParseException e) {
                        log.warn(e);
                    }
                }
                if (date == null) {
                    date = getTimestamp();
                }

                PriceInfo priceInfo = new PriceInfo(symbol, price.doubleValue());
                if (date != null) {
                    priceInfo.setDate(date);
                }

                newPrices.add(priceInfo);
            }
        } finally {
            if (csvReader != null) {
                try {
                    csvReader.close();
                } finally {
                    csvReader = null;
                }
            }
        }
        return newPrices;
    }

    private boolean isBlank(String str) {
        if (str == null) {
            return true;
        }
        if (str.length() <= 0) {
            return true;
        }
        return false;
    }

    private void update(Database db, List<PriceInfo> newPrices) throws IOException {
        for (PriceInfo newPrice : newPrices) {
            update(db, newPrice);
        }
    }

    private boolean update(Database db, PriceInfo priceInfo) throws IOException {
        Integer hsec = null;

        log.info("");
        String stockSymbol = priceInfo.getStockSymbol();
        log.info("Looking for stockSymbol=" + stockSymbol);
        hsec = findSecurity(stockSymbol, db);
        if (hsec == null) {
            log.warn("Cannot find stockSymbol=" + stockSymbol);
            return false;
        } else {
            log.info("Found stockSymbol=" + stockSymbol + ", hsec=" + hsec);
        }

        Date date = priceInfo.getDate();
        if (date == null) {
            date = getTimestamp();
        }
        if (log.isDebugEnabled()) {
            log.debug("date=" + date);
        }

        Table tSP = db.getTable("SP");
        Cursor cSP = Cursor.createCursor(tSP);
        // Index index = tSP.getIndex("HsecDateSrcSp");
        // index = null;
        // if (index != null) {
        // log.info("Has index=" + "HsecDateSrcSp");
        // cSP = IndexCursor.createCursor(tSP, index);
        // }

        Map<String, Object> rowPattern = new HashMap<String, Object>();
        Map<String, Object> row = null;

        cSP.beforeFirst();
        rowPattern.clear();
        rowPattern.put("dt", date);
        rowPattern.put("hsec", hsec);
        // src == 6 is online update
        Integer src = new Integer(6);
        rowPattern.put("src", src);
        log.info("Looking for existing row with date=" + date + ", hsec=" + hsec + ", src=" + src);
        if (cSP.findRow(rowPattern)) {
            row = cSP.getCurrentRow();
            log.info("Found one");
        } else {
            log.info("Found none");
            log.info("Will duplicate one");
            row = getLastRow(cSP, hsec);
            log.info("Last price date " + row.get("dt") + ", src=" + row.get("src"));

            IndexLookup indexLookup = new IndexLookup();
            Column column = tSP.getColumn("hsp");
            Long value = indexLookup.getMax(column);
            value++;
            row.put("hsp", value);
            row.put("dt", date);
            // force src == 6
            row.put("src", src);
            tSP.addRow(row.values().toArray());
            cSP.reset();

            rowPattern.clear();
            rowPattern.put("dt", date);
            rowPattern.put("hsec", hsec);
            rowPattern.put("src", src);
            if (cSP.findRow(rowPattern)) {
                row = cSP.getCurrentRow();
                log.info("Just inserted, row=" + row);
            } else {
                row = null;
                log.warn("Cannot find just inserted row");
                return false;
            }
        }

        if (row != null) {
            Double dPrice = null;

            dPrice = (Double) row.get("dPrice");
            log.info("Old price " + dPrice);

            dPrice = priceInfo.getPrice();
            row.put("dPrice", dPrice);

            Column column = tSP.getColumn("dPrice");
            cSP.setCurrentRowValue(column, dPrice);
            row = cSP.getCurrentRow();
            dPrice = (Double) row.get("dPrice");
            log.info("New price " + dPrice);

//            Date dtSerial = new Date();
//            column = tSP.getColumn("dtSerial");
//            cSP.setCurrentRowValue(column, dtSerial);
        }

        if (log.isDebugEnabled()) {
            log.debug(row);
        }

        return row != null;
    }

    private Date getTimestamp() {
        Date now = new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        now = calendar.getTime();
        return now;
    }

    private Map<String, Object> getLastRow(Cursor cSP, Integer hsec) throws IOException {
        Map<String, Object> row = null;

        Map<String, Object> rowPattern = new HashMap<String, Object>();

        List<Map<String, Object>> rows = new ArrayList<Map<String, Object>>();

        rows.clear();
        cSP.beforeFirst();
        rowPattern.clear();
        rowPattern.put("hsec", hsec);
        rowPattern.put("src", new Integer(6));
        while (cSP.moveToNextRow()) {
            if (cSP.currentRowMatches(rowPattern)) {
                row = cSP.getCurrentRow();
                rows.add(row);
            }
        }
        int size = rows.size();
        if (size > 0) {
            log.info("Found row with src == 6 that we can duplicated");
        } else {
            rows.clear();
            cSP.beforeFirst();
            rowPattern.clear();
            rowPattern.put("hsec", hsec);
            while (cSP.moveToNextRow()) {
                if (cSP.currentRowMatches(rowPattern)) {
                    row = cSP.getCurrentRow();
                    rows.add(row);
                }
            }
            size = rows.size();
            if (size <= 0) {
                return null;
            } else {
                log.info("Found row that we can duplicated");
            }
        }

        Comparator<Map<String, Object>> comparator = new Comparator<Map<String, Object>>() {
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
        };
        Collections.sort(rows, comparator);
        row = rows.get(rows.size() - 1);

        return row;
    }

    private Integer findSecurity(String stockSymbol, Database db) throws IOException {
        Table tSEC = db.getTable("SEC");

        Map<String, Object> row = null;
        // SEC:
        // Name: (SEC) mUID
        // Name: (SEC) szFull
        // Name: (SEC) szSymbol

        Cursor cSEC = Cursor.createCursor(tSEC);
        Map<String, Object> rowPattern = new HashMap<String, Object>();

        cSEC.beforeFirst();
        rowPattern.clear();
        rowPattern.put("mUID", stockSymbol);
        while (cSEC.moveToNextRow()) {
            if (cSEC.currentRowMatches(rowPattern)) {
                row = cSEC.getCurrentRow();
                break;
            }
        }

        if (row == null) {
            cSEC.beforeFirst();
            rowPattern.clear();
            rowPattern.put("szSymbol", stockSymbol);
            while (cSEC.moveToNextRow()) {
                if (cSEC.currentRowMatches(rowPattern)) {
                    row = cSEC.getCurrentRow();
                    break;
                }
            }
        }

        if (row == null) {
            cSEC.beforeFirst();
            rowPattern.clear();
            rowPattern.put("szFull", stockSymbol);
            while (cSEC.moveToNextRow()) {
                if (cSEC.currentRowMatches(rowPattern)) {
                    row = cSEC.getCurrentRow();
                }
            }
        }

        Integer hsec = null;
        if (row != null) {
            hsec = (Integer) row.get("hsec");
        }
        return hsec;

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
