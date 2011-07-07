package com.le.sunriise.export;

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
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ExportToSql {
    private static final Logger log = Logger.getLogger(ExportToSql.class);

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
            Class clz = ExportToSql.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password] out.sql");
            System.exit(1);
        }

        ExportToSql exporter = new ExportToSql();
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

    private void export(File srcFile, String srcPassword, File destFile) throws IOException {
        OpenedDb srcDb = null;

        try {
            srcDb = Utils.openDbReadOnly(srcFile, srcPassword);
            export(srcDb, destFile);
        } finally {
            if (srcDb != null) {
                try {
                    srcDb.close();
                } finally {
                    srcDb = null;
                }
            }
        }
    }

    private void export(OpenedDb srcDb, File destFile) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(destFile)));
            export(srcDb, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private void export(OpenedDb srcDb, PrintWriter writer) throws IOException {
        createTables(srcDb.getDb(), writer);
        populateTables(srcDb.getDb(), writer);
    }

    private void populateTables(Database srcDb, PrintWriter writer) throws IOException {
        Set<String> tableNames = srcDb.getTableNames();
        for (String tableName : tableNames) {
            Table table = srcDb.getTable(tableName);
            populateTable(table, writer);
        }
    }

    private void createTables(Database srcDb, PrintWriter writer) throws IOException {
        Set<String> tableNames = srcDb.getTableNames();
        for (String tableName : tableNames) {
            Table table = srcDb.getTable(tableName);
            createTable(table, writer);
        }
    }

    private void createTable(Table table, PrintWriter writer) throws IOException {
        StringBuilder stmtBuilder = new StringBuilder();
        stmtBuilder.append("CREATE TABLE " + escapeIdentifier(table.getName()) + " (");
        final List<Column> columns = table.getColumns();
        try {
            final int columnCount = columns.size();
            for (int i = 0; i < columnCount; i++) {
                final Column column = columns.get(i);

                stmtBuilder.append(escapeIdentifier(column.getName()));
                stmtBuilder.append(" ");
                switch (column.getType()) {
                /* Blob */
                case BINARY:
                case OLE:
                    stmtBuilder.append("BLOB");
                    break;

                /* Integers */
                case BOOLEAN:
                case BYTE:
                case INT:
                case LONG:
                    stmtBuilder.append("INTEGER");
                    break;

                /* Timestamp */
                case SHORT_DATE_TIME:
                    stmtBuilder.append("DATETIME");
                    break;

                /* Floating point */
                case DOUBLE:
                case FLOAT:
                case NUMERIC:
                    stmtBuilder.append("DOUBLE");
                    break;

                /* Strings */
                case TEXT:
                case GUID:
                case MEMO:
                    stmtBuilder.append("TEXT");
                    break;

                /*
                 * Money -- This can't be floating point, so let's be safe with
                 * strings
                 */
                case MONEY:
                    stmtBuilder.append("TEXT");
                    break;

                default:
                    throw new IOException("Unhandled MS Acess datatype: " + column.getType());
                }

                if (i + 1 < columnCount)
                    stmtBuilder.append(", ");
            }
        } finally {
            stmtBuilder.append(")");
            if (writer != null) {
                writer.println(stmtBuilder.toString());
            }
        }
    }

    private String escapeIdentifier(final String identifier) {
        return "'" + identifier.replace("'", "''") + "'";
    }

    private void populateTable(Table table, PrintWriter writer) throws IOException {
        final List<Column> columns = table.getColumns();
        final StringBuilder stmtBuilder = new StringBuilder();
        final StringBuilder valueStmtBuilder = new StringBuilder();

        /* Record the column count */
        final int columnCount = columns.size();

        /* Build the INSERT statement (in two pieces simultaneously) */
        stmtBuilder.append("INSERT INTO " + escapeIdentifier(table.getName()) + " (");
        valueStmtBuilder.append("(");

        for (int i = 0; i < columnCount; i++) {
            final Column column = columns.get(i);

            /* The column name and the VALUE binding */
            stmtBuilder.append(escapeIdentifier(column.getName()));
            valueStmtBuilder.append("?");

            if (i + 1 < columnCount) {
                stmtBuilder.append(", ");
                valueStmtBuilder.append(", ");
            }
        }

        /* Now append the VALUES piece */
        stmtBuilder.append(") VALUES ");
        stmtBuilder.append(valueStmtBuilder);
        stmtBuilder.append(")");

    }

}
