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

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class RemoveTodayQuotes {
    private static final Logger log = Logger.getLogger(RemoveTodayQuotes.class);

    private void removeTodayQuotes(File inFile, String password, int dayOffset) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);
            removeTodayQuotes(openedDb.getDb(), dayOffset);
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

    private void removeTodayQuotes(Database db, int dayOffset) throws IOException {
        Table table = db.getTable("SP");
        Cursor cursor = Cursor.createCursor(table);

        Date date = QuoteUtils.getTimestamp(dayOffset, true);

        // List<Integer> hsps = new ArrayList<Integer>();
        Integer[] srcs = {
        // manual update
        new Integer(5),
        // online update
        // new Integer(6),
        };
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        for (Integer src : srcs) {
            rowPattern.clear();
            rowPattern.put("dt", date);
            rowPattern.put("src", src);
            log.info("Looking for existing row with date=" + date + ", src=" + src);

            cursor.beforeFirst();
            while (cursor.moveToNextRow()) {
                if (cursor.currentRowMatches(rowPattern)) {
                    Map<String, Object> row = cursor.getCurrentRow();
                    Integer hsec = (Integer) row.get("hsec");
                    log.info("Found" + " hsec=" + hsec + ", symbol=" + QuoteUtils.getSymbol(hsec, db) + ", src=" + row.get("src")
                            + ", dt=" + row.get("dt"));
                    cursor.deleteCurrentRow();
                    // hsps.add((Integer) row.get("hsp"));
                }
            }
        }

        // for(Integer hsp: hsps) {
        // log.info("hsp=" + hsp);
        // rowPattern.clear();
        // rowPattern.put("hsp", hsp);
        // log.info("Looking for row hsp=" + hsp);
        // if (cursor.findFirstRow(rowPattern)) {
        // log.info("   FOUND. Deleting");
        // cursor.deleteCurrentRow();
        // } else {
        // log.info("   NOT FOUND");
        // }
        // }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String inFileName = null;
        String password = null;
        int dayOffset = 0;

        if (args.length == 1) {
            inFileName = args[0];
        } else if (args.length == 2) {
            inFileName = args[0];
            password = args[1];
        } else if (args.length == 3) {
            inFileName = args[0];
            password = args[1];
            try {
                dayOffset = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                log.warn(e);
                dayOffset = 0;
            }
        } else {
            Class clz = RemoveTodayQuotes.class;
            System.out.println("Usage: " + clz.getName() + " sample.mny [password]");
            System.exit(1);
        }

        if ((password != null) && (password.length() <= 0)) {
            password = null;
        }

        File inFile = new File(inFileName);

        log.info("inFile=" + inFile);
        if (password == null) {
            log.info("password=" + password);
        }
        log.info("dayOffset=" + dayOffset);

        if (!inFile.exists()) {
            log.error("File " + inFile + " does not exist.");
            System.exit(1);
        }

        try {
            RemoveTodayQuotes main = new RemoveTodayQuotes();
            main.removeTodayQuotes(inFile, password, dayOffset);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

}
