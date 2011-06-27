package com.le.sunriise.index;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class WalkIndexes {
    protected static final Logger log = Logger.getLogger(WalkIndexes.class);

    private OpenedDb openedDb;

    public WalkIndexes(File dbFile, String password) throws IOException {
        this.openedDb = Utils.openDbReadOnly(dbFile, password);
    }

    protected void walk() throws IOException {
        if (openedDb != null) {
            walk(openedDb.getDb());
        }
    }

    protected void walk(Database db) throws IOException {
        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            Table table = db.getTable(tableName);
            if (table != null) {
                if (accept(table)) {
                    walk(table);
                }
            }
        }
    }

    protected boolean accept(Table table) {
        return true;
    }

    protected void walk(Table table) throws IOException {
        List<Index> indexes = table.getIndexes();
        if (log.isDebugEnabled()) {
            log.debug("");
            log.debug(table.getName() + ", " + " (" + indexes.size() + ")");
        }
        for (Index index : indexes) {
            if (accept(index, table)) {
                walk(index, table);
            }
        }
    }

    protected void walk(Index index, Table table) throws IOException {
        String indexName = index.getName();
        List<ColumnDescriptor> columns = index.getColumns();

        log.info(table.getName() + ", " + indexName + " (" + columns.size() + ")");
        log.info("  isPrimaryKey=" + index.isPrimaryKey() + ",  isForeignKey=" + index.isForeignKey() + ",  isUnique=" + index.isUnique());
        for (ColumnDescriptor column : columns) {
            Column col = column.getColumn();
            log.info("  column=" + col.getName() + ", isAutoNumber=" + col.isAutoNumber());
        }

    }

    protected boolean accept(Index index, Table table) {
        return true;
    }

    public void close() {
        if (openedDb != null) {
            try {
                openedDb.close();
            } finally {
                openedDb = null;
            }
        }

    }

    public Database getDb() {
        return openedDb.getDb();
    }
}