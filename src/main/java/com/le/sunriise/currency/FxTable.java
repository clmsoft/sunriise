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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

public class FxTable {
    private static final Logger log = Logger.getLogger(FxTable.class);

    private List<FxTableEntry> entries = new ArrayList<FxTableEntry>();

    private NumberFormat rateFormatter;

    public FxTable() {
        super();
        this.rateFormatter = NumberFormat.getNumberInstance();
        this.rateFormatter.setGroupingUsed(false);
        this.rateFormatter.setMinimumFractionDigits(2);
        this.rateFormatter.setMaximumFractionDigits(10);
    }

    public Double getRateString(String fromCurrency, String toCurrency) {
        if (log.isDebugEnabled()) {
            log.debug("getRateString: " + fromCurrency + ", " + toCurrency);
        }

        for (FxTableEntry entry : entries) {
            String fCurr = entry.getFromCurrency();
            String toCurr = entry.getToCurrency();
            if (log.isDebugEnabled()) {
                log.debug("getRateString: fCur=" + fCurr + ", toCurr=" + toCurr);
            }

            if (fCurr.compareToIgnoreCase(fromCurrency) != 0) {
                continue;
            }
            if (toCurr.compareToIgnoreCase(toCurrency) != 0) {
                continue;
            }
            Double value = Double.valueOf(entry.getRate());
            if (log.isDebugEnabled()) {
                log.debug("getRateString: " + fromCurrency + ", " + toCurrency + ", rate=" + value);
            }
            return value;
        }

        // derived rate
        for (FxTableEntry entry : entries) {
            String fCurr = entry.getFromCurrency();
            String toCurr = entry.getToCurrency();
            if (log.isDebugEnabled()) {
                log.debug("getRateString (derived): fCur=" + fCurr + ", toCurr=" + toCurr);
            }
            if (fCurr.compareToIgnoreCase(toCurrency) != 0) {
                continue;
            }
            if (toCurr.compareToIgnoreCase(fromCurrency) != 0) {
                continue;
            }
            Double value = Double.valueOf(entry.getRate());
            value = 1.00 / value;
            if (log.isDebugEnabled()) {
                log.debug("getRateString (derived): " + fromCurrency + ", " + toCurrency + ", rate=" + value);
            }
            return value;
        }
        if (log.isDebugEnabled()) {
            log.debug("getRateString (NOT_FOUND): " + fromCurrency + ", " + toCurrency + ", rate=" + null);
        }
        return null;
    }

    public void load(File file) throws IOException {
        Reader reader = null;
        CsvReader csvReader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            csvReader = new CsvReader(reader);
            csvReader.readHeaders();
            String columnName = null;
            FxTableEntry entry = null;
            while (csvReader.readRecord()) {
                entry = new FxTableEntry();

                columnName = "FromCurrency";
                String fromCurrency = csvReader.get(columnName);
                if (isNull(fromCurrency)) {
                    log.warn("Value for column=" + columnName + " is null.");
                    continue;
                }
                if (fromCurrency != null) {
                    fromCurrency = fromCurrency.trim();
                }
                if (log.isDebugEnabled()) {
                    log.debug("fromCurrency=" + fromCurrency);
                }
                entry.setFromCurrency(fromCurrency);

                columnName = "ToCurrency";
                String toCurrency = csvReader.get(columnName);
                if (isNull(toCurrency)) {
                    log.warn("Value for column=" + columnName + " is null.");
                    continue;
                }
                if (toCurrency != null) {
                    toCurrency = toCurrency.trim();
                }
                if (log.isDebugEnabled()) {
                    log.debug("toCurrency=" + toCurrency);
                }
                entry.setToCurrency(toCurrency);

                columnName = "Rate";
                String rate = csvReader.get(columnName);
                if (isNull(rate)) {
                    log.warn("Value for column=" + columnName + " is null.");
                    continue;
                }
                if (log.isDebugEnabled()) {
                    log.debug("rate=" + rate);
                }
                if (rate != null) {
                    rate = rate.trim();
                }
                entry.setRate(rate);

                entries.add(entry);
            }
        } finally {
            if (csvReader != null) {
                csvReader.close();
                csvReader = null;
            }

            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    reader = null;
                }
            }
        }
    }

    private boolean isNull(String str) {
        if (str == null) {
            return true;
        }

        if (str.length() <= 0) {
            return true;
        }

        return false;
    }
}
