package com.le.sunriise;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.ExportFilter;
import com.healthmarketscience.jackcess.ExportUtil;
import com.healthmarketscience.jackcess.SimpleExportFilter;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;

public class ExportToCsv {
    private static final Logger log = Logger.getLogger(ExportToCsv.class);

    private Database db = null;

    public void writeToDir(File outDir) throws IOException {
        PrintWriter writer = null;
        startExport(outDir);
        try {
            File file = new File(outDir, "db.txt");
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            // writer.println("file=" + dbFile);
            // writer.println("fileFormat: " + db.getFileFormat());
            writer.println(db.toString());

            writer.println("");
            List<Query> queries = db.getQueries();
            writer.println("getQueries, " + queries.size());
            for (Query query : queries) {
                writer.println(query.toSQLString());
            }
            Set<String> tableNames = db.getTableNames();
            // writer.println("tableNames.size: " + tableNames.size());
            int count = 0;
            startExportTables(tableNames.size());
            try {
                for (String tableName : tableNames) {
                    try {
                        log.info("tableName=" + tableName);
                        if (!exportedTable(tableName, count)) {
                            break;
                        }
                        writeTableInfo(db, tableName, outDir);
                        count++;
                    } catch (IOException e) {
                        log.warn("Cannot write table info for tableName=" + tableName);
                    }
                }
            } finally {
                endExportTables(count);
            }
        } finally {
            if (writer != null) {
                writer.close();
            }
            writer = null;
            endExport(outDir);
        }
    }

    protected void startExport(File outDir) {
    }

    protected void endExport(File outDir) {
    }

    protected void startExportTables(int size) {
    }

    protected boolean exportedTable(String tableName, int count) {
        return true;
    }

    protected void endExportTables(int count) {
    }

    private static void writeTableInfo(Database db, String tableName, File outDir) throws IOException {
        File dir = new File(outDir, tableName);
        if ((!dir.exists()) && (!dir.mkdirs())) {
            throw new IOException("Cannot create directory, dir=" + dir);
        }

        Table table = db.getTable(tableName);

        File file = new File(dir, "table.txt");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer.println(table.toString());
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }

        writeColumnsInfo(table, dir);

        writeRowsInfo(db, table, dir);
    }

    private static void writeColumnsInfo(Table table, File dir) throws IOException {
        // Table table = db.getTable(tableName);
        File columnsFile = null;
        // columnsFile = new File(dir, "columns.txt");
        if (columnsFile != null) {
            PrintWriter writer = null;
            try {
                writer = new PrintWriter(new BufferedWriter(new FileWriter(columnsFile)));
                writer.println("#name: " + table.getName());
                writer.println("#columns: " + table.getColumnCount());
                writer.println("");
                List<Column> columns = table.getColumns();
                for (Column column : columns) {
                    writer.println(column.getName() + "," + column.getType().toString());
                }
            } finally {
                if (writer != null) {
                    writer.close();
                    writer = null;
                }
            }
        }

        File columnsDir = new File(dir, "columns.d");
        if ((!columnsDir.exists()) && (!columnsDir.mkdirs())) {
            throw new IOException("Cannot create directory, dir=" + columnsDir);
        }
        List<Column> columns = table.getColumns();
        for (Column column : columns) {
            writeColumnsInfo(columnsDir, column);
        }
    }

    private static void writeColumnsInfo(File columnsDir, Column column) throws IOException {
        File file = new File(columnsDir, column.getName() + ".txt");
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
            writer.println(column.toString());
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
    }

    private static void writeRowsInfo(Database db, Table table, File dir) throws IOException {
        File file = new File(dir, table.getName() + "-" + "rows.csv");
        BufferedWriter writer = null;
        try {
            writer = new BufferedWriter(new FileWriter(file));
            ExportFilter filter = SimpleExportFilter.INSTANCE;
            ExportUtil.exportWriter(db, table.getName(), writer, true, ExportUtil.DEFAULT_DELIMITER, ExportUtil.DEFAULT_QUOTE_CHAR, filter);
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }

    }

    private void close() {
        if (db != null) {
            try {
                db.close();
            } catch (IOException e) {
                log.warn(e);
            } finally {
                db = null;
            }
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String inFileName = null;
        String outDirName = null;
        String password = null;
        if (args.length == 2) {
            inFileName = args[0];
            outDirName = args[1];
            password = null;
        } else if (args.length == 3) {
            inFileName = args[0];
            outDirName = args[1];
            password = args[2];
        } else {
            Class clz = ExportToCsv.class;
            System.out.println("Usage: " + clz.getName() + " sample.mny outDir [password]");
            System.exit(1);
        }

        File inFile = new File(inFileName);
        File outDir = new File(outDirName);

        log.info("inFile=" + inFile);
        log.info("outDir=" + outDir);

        ExportToCsv dbHelper = null;
        try {
            if (!inFile.exists()) {
                throw new IOException("File=" + inFile.getAbsoluteFile().getAbsolutePath() + " does not exist.");
            }
            dbHelper = new ExportToCsv();
            dbHelper.setDb(Utils.openDbReadOnly(inFile, password));
            if ((!outDir.exists()) && (!outDir.mkdirs())) {
                throw new IOException("Cannot create directory, outDir=" + outDir);
            }
            dbHelper.writeToDir(outDir);
        } catch (IllegalStateException e) {
            // java.lang.IllegalStateException: Incorrect password provided
            log.error(e);
        } catch (IOException e) {
            log.error(e);
        } finally {
            if (dbHelper != null) {
                dbHelper.close();
            }
        }
        log.info("< DONE");
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }
}
