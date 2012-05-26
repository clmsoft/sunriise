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
package com.le.sunriise.quote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

public class PriceInfo {
    private static final Logger log = Logger.getLogger(PriceInfo.class);

    private String stockSymbol;

    private Double price;

    private Date date;

    private static NumberFormat priceFormatter;

    private static SimpleDateFormat csvDateFormatter = new SimpleDateFormat("yyyy/MM/dd");
    static {
        priceFormatter = NumberFormat.getNumberInstance();
        priceFormatter.setGroupingUsed(false);
        priceFormatter.setMinimumFractionDigits(2);
        priceFormatter.setMaximumFractionDigits(10);
    }

    public PriceInfo(String stockSymbol, Double price) {
        super();
        this.stockSymbol = stockSymbol;
        this.price = price;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    static List<PriceInfo> parse(File inFile) throws IOException {
        List<PriceInfo> prices = new ArrayList<PriceInfo>();

        CsvReader csvReader = null;
        try {
            csvReader = new CsvReader(new BufferedReader(new FileReader(inFile)));
            csvReader.readHeaders();
            while (csvReader.readRecord()) {
                PriceInfo priceInfo = parse(csvReader);
                if (priceInfo != null) {
                    prices.add(priceInfo);
                }
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
        return prices;
    }

    private static PriceInfo parse(CsvReader csvReader) throws IOException {
        PriceInfo priceInfo = null;

        String symbol = csvReader.get("symbol");
        if (QuoteUtils.isBlank(symbol)) {
            log.warn("Bad format, 'symbol' column is blank. " + csvReader.getRawRecord());
            return priceInfo;
        }

        String priceStr = csvReader.get("price");
        if (QuoteUtils.isBlank(priceStr)) {
            log.warn("Bad format, 'price' column is blank. " + csvReader.getRawRecord());
            return priceInfo;
        }
        Number price = null;
        try {
            price = priceFormatter.parse(priceStr);
        } catch (ParseException e) {
            log.warn(e);
        }
        if (price == null) {
            return priceInfo;
        }

        String dateString = csvReader.get("date");
        Date date = null;
        if (QuoteUtils.isBlank(priceStr)) {
            log.warn("No date, will use today");
        } else {
            try {
                date = csvDateFormatter.parse(dateString);
            } catch (ParseException e) {
                log.warn(e);
            }
        }
        if (date == null) {
            date = QuoteUtils.getTimestamp();
        }

        priceInfo = new PriceInfo(symbol, price.doubleValue());
        if (date != null) {
            priceInfo.setDate(date);
        }

        return priceInfo;
    }
}
