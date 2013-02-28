/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.currency;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class UpdateExchangeRates {
    private static final Logger log = Logger.getLogger(UpdateExchangeRates.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String exchangeRatesFileName = null;
        String inFileName = null;
        String password = null;

        if (args.length == 2) {
            exchangeRatesFileName = args[0];
            inFileName = args[1];
            password = null;
        } else if (args.length == 3) {
            exchangeRatesFileName = args[0];
            inFileName = args[1];
            password = args[2];
        } else {
            Class<UpdateExchangeRates> clz = UpdateExchangeRates.class;
            System.out.println("Usage: " + clz.getName() + " fx.csv sample.mny [password]");
            System.exit(1);
        }

        File fxFile = new File(exchangeRatesFileName);
        File inFile = new File(inFileName);

        log.info("fxFile=" + fxFile);
        log.info("inFile=" + inFile);

        try {
            UpdateExchangeRates updater = new UpdateExchangeRates();
            updater.update(fxFile, inFile, password);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("< DONE");
        }
    }

    private void update(File fxFile, File inFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);
            FxTable fxTable = new FxTable();
            fxTable.load(fxFile);
            update(fxTable, openedDb.getDb());
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

    public void update(FxTable fxTable, Database db) throws IOException {
        // CRNC
        // (PK) CRNC.hcrnc
        //
        // CRNC_EXCHG
        // (FK) CRNC_EXCHG.hcrncFrom -> CRNC.hcrnc
        // (FK) CRNC_EXCHG.hcrncTo -> CRNC.hcrnc
        Table tCRNC = db.getTable("CRNC");
        Table tCRNC_EXCHG = db.getTable("CRNC_EXCHG");
        Cursor cCRNC_EXCHG = null;
        Cursor cCRNC = null;
        try {
            cCRNC_EXCHG = Cursor.createCursor(tCRNC_EXCHG);
            cCRNC = Cursor.createCursor(tCRNC);

            // {hcrncFrom=1, hcrncTo=25, rate=1.00969, dt=Mon Feb 28 00:00:00
            // PST 10000, fReversed=false, fThroughEuro=false, exchgid=-1,
            // fHist=false, szSymbol=null}
            Map<String, Object> rCRNC_EXCHG = null;
            while ((rCRNC_EXCHG = cCRNC_EXCHG.getNextRow()) != null) {
                Integer hcrncFrom = (Integer) rCRNC_EXCHG.get("hcrncFrom");
                Integer hcrncTo = (Integer) rCRNC_EXCHG.get("hcrncTo");
                Double rate = (Double) rCRNC_EXCHG.get("rate");

                Map<String, Object> rowPattern = new HashMap<String, Object>();

                Map<String, Object> rCRNC = null;

                String hcrncFromStr = null;
                String hcrncFromName = null;
                rowPattern.clear();
                rowPattern.put("hcrnc", hcrncFrom);
                if (cCRNC.findRow(rowPattern)) {
                    rCRNC = cCRNC.getCurrentRow();
                    hcrncFromStr = (String) rCRNC.get("szIsoCode");
                    hcrncFromName = (String) rCRNC.get("szName");
                }
                // {hcrnc=1, szName=Australian dollar, lcid=3081,
                // rgbFormat=[B@111a3ac, szIsoCode=AUD, szSymbol=/AUDUS,
                // fOnline=true, dtSerial=Thu Jun 16 07:17:39 PDT 2011,
                // fHidden=false, sguid={225903A4-8DD6-4C0C-8D97-E52593DED354},
                // fUpdated=true}
                // log.info(crncRow);

                String hcrncToStr = null;
                String hcrncToName = null;
                rowPattern.clear();
                rowPattern.put("hcrnc", hcrncTo);
                if (cCRNC.findRow(rowPattern)) {
                    rCRNC = cCRNC.getCurrentRow();
                    hcrncToStr = (String) rCRNC.get("szIsoCode");
                    hcrncToName = (String) rCRNC.get("szName");
                }
                // log.info(crncRow);
                Double newRate = fxTable.getRateString(hcrncFromStr, hcrncToStr);

                if (newRate == null) {
                    notifyNoExistingRate(rate, hcrncFromStr, hcrncFromName, hcrncToStr, hcrncToName, newRate);
                } else {
                    Column column = tCRNC_EXCHG.getColumn("rate");
                    if (column != null) {
                        cCRNC_EXCHG.setCurrentRowValue(column, newRate);
                    } else {
                        log.warn("Cannot find column=CRNC.rate");
                    }
                    notifyUpdateExistingRate(rate, hcrncFromStr, hcrncFromName, hcrncToStr, hcrncToName, newRate);
                }
            }
        } finally {
            if (cCRNC_EXCHG != null) {
                cCRNC_EXCHG = null;
            }

            if (cCRNC != null) {
                cCRNC = null;
            }
        }
    }

    protected void notifyUpdateExistingRate(Double rate, String hcrncFromStr, String hcrncFromName, String hcrncToStr,
            String hcrncToName, Double newRate) {
        log.info("");
        log.info("# YES NEW FX RATE");
        log.info("  CURRENT: " + hcrncFromStr + " -> " + hcrncToStr + ", " + rate + ", (" + hcrncFromName + " -> " + hcrncToName
                + ")");
        log.info("  NEW: " + hcrncFromStr + " -> " + hcrncToStr + ", " + newRate);
    }

    protected void notifyNoExistingRate(Double rate, String hcrncFromStr, String hcrncFromName, String hcrncToStr,
            String hcrncToName, Double newRate) {
        if (log.isDebugEnabled()) {
            log.debug("");
            log.debug("# NO NEW FX RATE");
            log.debug("  CURRENT: " + hcrncFromStr + " -> " + hcrncToStr + ", " + rate + ", (" + hcrncFromName + " -> "
                    + hcrncToName + ")");
            log.debug("  NEW: " + hcrncFromStr + " -> " + hcrncToStr + ", " + newRate);
        }
    }
}
