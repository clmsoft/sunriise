package com.le.sunriise.export;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.StopWatch;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ExportToMdb {
    private static final Logger log = Logger.getLogger(ExportToMdb.class);
    /**
     * @param args
     */
    public static void main(String[] args) {
        File srcFile = null;
        String srcPassword = null;
        File destFile = null;

        if (args.length == 2) {
            srcFile = new File(args[0]);
            destFile = new File(args[1]);
        } else if (args.length == 3) {
            srcFile = new File(args[0]);
            srcPassword = args[1];
            destFile = new File(args[2]);
        } else {
            Class<ExportToMdb> clz = ExportToMdb.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password] out.mdb");
            System.exit(1);
        }

        ExportToMdb exporter = new ExportToMdb();
        try {
            log.info("srcFile=" + srcFile);
            exporter.export(srcFile, srcPassword, destFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("destFile=" + destFile.getAbsolutePath());
            log.info("< DONE");
        }
    }

    public void export(File srcFile, String srcPassword, File destFile) throws IOException {
        OpenedDb srcDb = null;
        Database destDb = null;

        try {
            srcDb = Utils.openDbReadOnly(srcFile, srcPassword);
            destDb = export(srcDb, destFile);
        } finally {
            if (srcDb != null) {
                try {
                    srcDb.close();
                } finally {
                    srcDb = null;
                }
            }
            if (destDb != null) {
                try {
                    destDb.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    destDb = null;
                }
            }
        }
    }

    public Database export(OpenedDb srcDb, File destFile) throws IOException {
        Database destDb = Utils.createEmptyDb(destFile);
        copyDb(srcDb.getDb(), destDb);
        return destDb;
    }

    private void copyDb(Database srcDb, Database destDb) throws IOException {
        Set<String> tableNames = srcDb.getTableNames();
        int tableCount = tableNames.size();
        int addedCount = 0;
        startCopyTables(tableCount);
        try {
            for (String tableName : tableNames) {
                Table table = srcDb.getTable(tableName);
                if (!copyTable(table, destDb)) {
                    break;
                }
                addedCount++;
            }
        } finally {
            endCopyTables(addedCount);
        }
    }

    protected void startCopyTables(int maxCount) {
        log.info("> Adding tables=" + maxCount);
    }

    protected void endCopyTables(int count) {
        log.info("< Added tables=" + count);
    }

    protected boolean startCopyTable(String name) {
        log.info("> startCopyTable, name=" + name);
        return true;
    }

    private boolean copyTable(Table srcTable, Database destDb) throws IOException {
        if (!startCopyTable(srcTable.getName())) {
            return false;
        }
        try {
            String tableName = srcTable.getName();
            Table destTable = destDb.getTable(tableName);
            if (destTable != null) {
                log.warn("tableName=" + tableName + " exists.");
            } else {
                try {
                    List<Column> columns = srcTable.getColumns();
                    Database db = destDb;
                    boolean useExistingTable = false;
                    importColumns(columns, db, tableName, useExistingTable);
                    destTable = destDb.getTable(tableName);
                } catch (SQLException e) {
                    throw new IOException(e);
                }

                int batchSize = 200;
                List<Object[]> rows = new ArrayList<Object[]>(batchSize);
                StopWatch stopWatch = new StopWatch();
                int rowCount = srcTable.getRowCount();
                if (!startAddingRows(rowCount)) {
                    return false;
                }
                Cursor cursor = null;
                int count = 0;
                try {
                    cursor = Cursor.createCursor(srcTable);
                    while (cursor.moveToNextRow()) {
                        Map<String, Object> row = cursor.getCurrentRow();
                        rows.add(row.values().toArray());
                        if (rows.size() >= batchSize) {
                            destTable.addRows(rows);
                            count += rows.size();
                            rows.clear();
                            if (!addedRow(count)) {
                                break;
                            }
                        }
                    }
                    if (rows.size() > 0) {
                        destTable.addRows(rows);
                        count += rows.size();
                        addedRow(count);
                        rows.clear();
                    }
                } finally {
                    long delta = stopWatch.click();
                    if (cursor != null) {
                        cursor = null;
                    }
                    endAddingRows(count, delta);
                }
            }
        } finally {
            endCopyTable(srcTable.getName());
        }

        return true;
    }

    protected boolean addedRow(int count) {
        return true;
    }

    protected void endCopyTable(String name) {
        log.info("< endCopyTable, name=" + name);
    }

    protected boolean startAddingRows(int max) {
        log.info("Adding rows=" + max + " ...");
        return true;
    }

    protected void endAddingRows(int count, long delta) {
        log.info("Added rows=" + count + ", ms=" + delta);
    }

    private static void importColumns(List<Column> srcColumns, Database db, String tableName, boolean useExistingTable) throws SQLException, IOException {
        tableName = Database.escapeIdentifier(tableName);
        Table table = null;
        if (!useExistingTable || ((table = db.getTable(tableName)) == null)) {
            List<Column> columns = new LinkedList<Column>();
            for (int i = 0; i < srcColumns.size(); i++) {
                Column srcColumn = srcColumns.get(i);
                Column column = new Column();
                column.setName(Database.escapeIdentifier(srcColumn.getName()));
                int lengthInUnits = srcColumn.getLengthInUnits();
                column.setType(srcColumn.getType());
                DataType type = column.getType();
                // we check for isTrueVariableLength here to avoid setting the
                // length
                // for a NUMERIC column, which pretends to be var-len, even
                // though it
                // isn't
                if (type.isTrueVariableLength() && !type.isLongValue()) {
                    column.setLengthInUnits((short) lengthInUnits);
                }
                if (type.getHasScalePrecision()) {
                    int scale = srcColumn.getScale();
                    int precision = srcColumn.getPrecision();
                    if (type.isValidScale(scale)) {
                        column.setScale((byte) scale);
                    }
                    if (type.isValidPrecision(precision)) {
                        column.setPrecision((byte) precision);
                    }
                }
                columns.add(column);
            }
            db.createTable(tableName, columns);
        }
    }
}
