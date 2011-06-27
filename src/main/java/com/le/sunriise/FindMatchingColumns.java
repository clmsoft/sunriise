package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.viewer.OpenedDb;

public class FindMatchingColumns {
    private static final Logger log = Logger.getLogger(FindMatchingColumns.class);
    private OpenedDb openedDb;

    public FindMatchingColumns(File dbFile, String password) throws IOException {
        openedDb = Utils.openDbReadOnly(dbFile, password);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Database db = null;
        String dbFileName = null;
        
        if (args.length == 1) {
            dbFileName = args[0];
        } else {
            Class clz = FindMatchingColumns.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny");
            System.exit(1);
        }
        
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);
        FindMatchingColumns finder = null;
        try {
            finder = new FindMatchingColumns(dbFile, password);
            Map<String, Map<Column, List<Table>>> results = finder.find();
            if ((results != null) && (results.size() > 0)) {
                for (String tableName : results.keySet()) {
                    Map<Column, List<Table>> matchedColumns = results.get(tableName);
                    if ((matchedColumns != null) && (matchedColumns.size() > 0)) {
                        for (Column column : matchedColumns.keySet()) {
                            List<Table> matchedTables = matchedColumns.get(column);
                            log.info("### " + tableName + ", " + column.getName() + ", " + matchedTables.size());
                            for (Table matchedTable : matchedTables) {
                                log.info(tableName + ", " + matchedTable.getName() + ", " + column.getName() + ", " + column.getType());
                            }
                        }
                        log.info("");
                    }
                }
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (finder != null) {
                try {
                    finder.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    finder = null;
                }
            }
            log.info("> DONE");
        }
    }

    public Map<String, Map<Column, List<Table>>> find() throws IOException {
        Map<String, Map<Column, List<Table>>> r = new LinkedHashMap<String, Map<Column, List<Table>>>();
        Set<String> tableNames = openedDb.getDb().getTableNames();
        for (String tableName : tableNames) {
            Map<Column, List<Table>> results = find(tableName);
            if ((results != null) && (results.size() > 0)) {
                r.put(tableName, results);
            }
            // for (Column column : results.keySet()) {
            // log.info("###");
            // List<Table> matchedTables = results.get(column);
            // for (Table matchedTable : matchedTables) {
            // log.info(tableName + ", " + matchedTable.getName() + ", " +
            // column.getName() + ", " + column.getType());
            // }
            // }
        }
        return r;
    }

    public Map<Column, List<Table>> find(String tableName) throws IOException {
        Map<Column, List<Table>> results = new LinkedHashMap<Column, List<Table>>();
        Table table = openedDb.getDb().getTable(tableName);
        if (table != null) {
            results = find(table);
        }
        return results;
    }

    private Map<Column, List<Table>> find(Table table) throws IOException {
        Map<Column, List<Table>> results = new LinkedHashMap<Column, List<Table>>();
        List<Column> columns = table.getColumns();
        for (Column column : columns) {
            List<Table> matchedTables = find(table, column);
            if ((matchedTables != null) && (matchedTables.size() > 0)) {
                results.put(column, matchedTables);
            }
        }
        return results;
    }

    private List<Table> find(Table targetTable, Column targetColumn) throws IOException {
        List<Table> matchedTables = new ArrayList<Table>();

        String targetTableName = targetTable.getName();
        if (log.isDebugEnabled()) {
            log.debug(targetTableName);
            log.debug("  " + targetColumn.getName() + ", " + targetColumn.getType());
        }
        Database db = openedDb.getDb();
        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            List<Column> matchedColumns = null;
            if (tableName.equals(targetTableName)) {
                continue;
            }
            Table table = db.getTable(tableName);
            if (! accept(table)) {
                continue;
            }
            matchedColumns = find(table, targetTable, targetColumn);
            if ((matchedColumns != null) && (matchedColumns.size() > 0)) {
                matchedTables.add(table);
            }
        }
        return matchedTables;
    }

    private boolean accept(Table table) {
        return true;
    }

    private List<Column> find(Table table, Table targetTable, Column targetColumn) {
        List<Column> matched = new ArrayList<Column>();
        List<Column> columns = table.getColumns();
        for (Column column : columns) {
            if (! accept(column)) {
                continue;
            }
            if (column.getName().compareToIgnoreCase(targetColumn.getName()) != 0) {
                continue;
            }

            if (column.getType().compareTo(targetColumn.getType()) != 0) {
                continue;
            }

            matched.add(column);
        }

        return matched;
    }

    private boolean accept(Column column) {
        return true;
    }

    public void close() throws IOException {
        if (openedDb != null) {
            openedDb.close();
        }
    }

}
