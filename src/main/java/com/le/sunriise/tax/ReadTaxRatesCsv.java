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
package com.le.sunriise.tax;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.apache.log4j.Logger;

import com.csvreader.CsvReader;

public class ReadTaxRatesCsv {
    private static final Logger log = Logger.getLogger(ReadTaxRatesCsv.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        ReadTaxRatesCsv helper = new ReadTaxRatesCsv();
        File file = new File("src/main/resources/taxRates.csv");

        log.info("> loading file=" + file);
        try {
            helper.load(file);
        } catch (IOException e) {
            log.warn(e);
        } finally {
            log.info("< DONE");
        }
    }

    public void load(File file) throws IOException {
        Reader reader = null;
        CsvReader csvReader = null;

        try {
            reader = new BufferedReader(new FileReader(file));
            csvReader = new CsvReader(reader);
            // csvReader.readHeaders();
            while (csvReader.readRecord()) {
                int columnCount = csvReader.getColumnCount();
                if (columnCount == 4) {
                    readOtherRate(csvReader);
                } else if (columnCount == 6) {
                    readIncomeRate(csvReader);
                } else {
                    log.warn("Bad format: " + csvReader.getRawRecord());
                }
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

    private void readIncomeRate(CsvReader csvReader) throws IOException {
        // "2005","TRSingle","Income_1","0.0","7300.0","10.0"
        String year = csvReader.get(0);
        String type = csvReader.get(1);
        String desc = csvReader.get(2);
        String startAmount = csvReader.get(3);
        String endAmount = csvReader.get(4);
        String rate = csvReader.get(5);
    }

    private void readOtherRate(CsvReader csvReader) throws IOException {
        // "2005","TRSingle","Long-Term Capital Gains","15.0"
        String year = csvReader.get(0);
        String type = csvReader.get(1);
        String desc = csvReader.get(2);
        String rate = csvReader.get(3);

    }

}
