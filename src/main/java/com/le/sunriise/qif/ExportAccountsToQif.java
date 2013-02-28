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
        File inFile = null;
        String password = null;
        File outFile = null;

        if (args.length == 2) {
            inFile = new File(args[0]);
            outFile = new File(args[1]);
        } else if (args.length == 3) {
            inFile = new File(args[0]);
            password = args[1];
            outFile = new File(args[2]);
        } else {
            Class<ExportAccountsToQif> clz = ExportAccountsToQif.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny [password] out.qif");
            System.exit(1);
        }

        log.info("inFile=" + inFile);
        log.info("outFile=" + outFile);

        ExportAccountsToQif exporter = new ExportAccountsToQif();
        try {
            exporter.export(inFile, password, outFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    private void export(File dbFile, String password, File outFile) throws IOException {
        OpenedDb openDb = null;
        PrintWriter writer = null;
        try {
            openDb = Utils.openDbReadOnly(dbFile, password);
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            export(openDb.getDb(), writer);
        } finally {
            if (openDb != null) {
                try {
                    openDb.close();
                } finally {
                    openDb = null;
                }
            }
            if (writer != null) {
                try {
                    writer.close();
                } finally {
                    writer = null;
                }
            }
        }
    }

    private void export(Database db, PrintWriter writer) throws IOException {
        String tableName = null;
        Table table = null;
        Cursor cursor = null;

        tableName = "ACCT";
        table = db.getTable(tableName);
        log.info("> traversing table=" + tableName);
        cursor = Cursor.createCursor(table);
        while (cursor.moveToNextRow()) {
            Map<String, Object> row = cursor.getCurrentRow();
            Integer hacct = (Integer) row.get("hacct");

            String name = (String) row.get("szFull");
            log.info("hacct=" + hacct + ", name=" + name);

            Integer type = (Integer) row.get("at");
            log.info("    type=" + type);
        }

        tableName = "TRN";
        table = db.getTable(tableName);
        log.info("> traversing table=" + tableName);
        cursor = Cursor.createCursor(table);
        while (cursor.moveToNextRow()) {
            Map<String, Object> row = cursor.getCurrentRow();
            Integer hacct = (Integer) row.get("hacct");
            // hacctLink
            Integer hacctLink = (Integer) row.get("hacctLink");
            log.info("hacct=" + hacct + ", hacctLink=" + hacctLink);
        }

    }

}
