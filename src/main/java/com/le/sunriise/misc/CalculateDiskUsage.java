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
package com.le.sunriise.misc;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class CalculateDiskUsage {
    private static final Logger log = Logger.getLogger(CalculateDiskUsage.class);

    /** Name of the table that contains system access control entries */
    private static final String TABLE_SYSTEM_ACES = "MSysACEs";
    /** Name of the table that contains table relationships */
    private static final String TABLE_SYSTEM_RELATIONSHIPS = "MSysRelationships";
    /** Name of the table that contains queries */
    private static final String TABLE_SYSTEM_QUERIES = "MSysQueries";
    /** Name of the main database properties object */
    private static final String OBJECT_NAME_DB_PROPS = "MSysDb";

    public CalculateDiskUsage() {
        super();
    }

    private void calculate(File inFile, String password) throws IOException {
        if (!inFile.exists()) {
            throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
        }

        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDb(inFile, password);
            calculate(openedDb);
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

    public static String humanReadableByteCount(long bytes, boolean si) {
        int unit = si ? 1000 : 1024;
        if (bytes < unit)
            return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(unit));
        String pre = (si ? "kMGTPE" : "KMGTPE").charAt(exp - 1) + (si ? "" : "i");
        return String.format("%.1f %sB", bytes / Math.pow(unit, exp), pre);
    }

    private void calculate(OpenedDb openedDb) throws IOException {
        File dbFile = openedDb.getDbFile();

        System.out.println(dbFile + ", " + dbFile.length() + ", " + humanReadableByteCount(dbFile.length(), true));

        Database db = openedDb.getDb();
        JetFormat format = db.getFormat();
        int pageSize = format.PAGE_SIZE;

        long runningBytes = 0L;
        int tablesCount = 0;

        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            Table table = db.getTable(tableName);
            if (table != null) {
                int pageCount = table.getApproximateOwnedPageCount();
                if (log.isDebugEnabled()) {
                    log.debug("  pageCount=" + pageCount);
                }
                int otherPageCount = getOtherPageCount(table);
                int bytes = (pageCount + otherPageCount) * pageSize;

                System.out.println(tableName + ", " + table.getRowCount() + ", " + bytes + ", "
                        + humanReadableByteCount(bytes, true));

                runningBytes += bytes;
                tablesCount++;
            } else {
                log.warn("Cannot find table=" + tableName);
            }
        }

        // tableNames.clear();
        // tableNames.add(TABLE_SYSTEM_ACES);
        // tableNames.add(TABLE_SYSTEM_RELATIONSHIPS);
        // tableNames.add(TABLE_SYSTEM_QUERIES);
        // tableNames.add(OBJECT_NAME_DB_PROPS);
        // for (String tableName : tableNames) {
        // Table table = db.getSystemTable(tableName);
        // if (table != null) {
        // int pageCount = table.getApproximateOwnedPageCount();
        // if (log.isDebugEnabled()) {
        // log.debug("  pageCount=" + pageCount);
        // }
        // int otherPageCount = getOtherPageCount(table);
        // int bytes = (pageCount + otherPageCount) * pageSize;
        //
        // System.out.println(tableName + ", " + table.getRowCount() + ", " +
        // bytes + ", " + humanReadableByteCount(bytes, true));
        //
        // runningBytes += bytes;
        // tablesCount++;
        // } else {
        // log.warn("Cannot find table=" + tableName);
        // }
        // }

        tableNames = db.getSystemTableNames();
        for (String tableName : tableNames) {
            Table table = db.getSystemTable(tableName);
            if (table != null) {
                int pageCount = table.getApproximateOwnedPageCount();
                if (log.isDebugEnabled()) {
                    log.debug("  pageCount=" + pageCount);
                }
                int otherPageCount = getOtherPageCount(table);
                int bytes = (pageCount + otherPageCount) * pageSize;

                System.out.println(tableName + ", " + table.getRowCount() + ", " + bytes + ", "
                        + humanReadableByteCount(bytes, true));

                runningBytes += bytes;
                tablesCount++;
            } else {
                log.warn("Cannot find table=" + tableName);
            }
        }

        System.out.println("Total: " + tablesCount + ", " + humanReadableByteCount(runningBytes, true));
    }

    private int getOtherPageCount(Table table) {
        if (log.isDebugEnabled()) {
            log.debug("> getOtherPageCount, table=" + table.getName());
        }

        List<Column> matchingColumns = new ArrayList<Column>();
        List<Column> columns = table.getColumns();
        for (Column column : columns) {
            DataType type = column.getType();
            if ((type == DataType.OLE) || (type == DataType.MEMO)) {
                matchingColumns.add(column);
                if (log.isDebugEnabled()) {
                    log.debug("  found OLE/MEMO column=" + column.getName());
                }
            }
        }
        if (matchingColumns.size() <= 0) {
            if (log.isDebugEnabled()) {
                log.debug("< getOtherPageCount, has no OLE/MEMO column");
            }
            return 0;
        }

        Set<Integer> pageNums = new HashSet<Integer>();
        Cursor cursor = Cursor.createCursor(table);
        try {
            while (cursor.moveToNextRow()) {
                for (Column column : matchingColumns) {
                    // this is a hack. Not in upstream yet.
                    // cursor.getCurrentRowValue(column, pageNums);
                }
            }
        } catch (IOException e) {
            log.warn(e);
        }

        int count = pageNums.size();

        if (log.isDebugEnabled()) {
            log.debug("< getOtherPageCount, count=" + count);
        }

        return count;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String inFileName = null;
        String password = null;

        if (args.length == 1) {
            inFileName = args[0];
            password = null;
        } else if (args.length == 2) {
            inFileName = args[0];
            password = args[1];
        } else {
            Class<CalculateDiskUsage> clz = CalculateDiskUsage.class;
            System.out.println("Usage: " + clz.getName() + " sample.mny [password]");
            System.exit(1);
        }

        File inFile = new File(inFileName);

        log.info("inFile=" + inFile);

        try {
            CalculateDiskUsage updateQuotes = new CalculateDiskUsage();
            updateQuotes.calculate(inFile, password);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("< DONE");
        }
    }
}
