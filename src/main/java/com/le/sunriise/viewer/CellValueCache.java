package com.le.sunriise.viewer;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CellValueCache {
    private static final Logger log = Logger.getLogger(CellValueCache.class);

    private final Cache<String, Object> cellsCache;
    private final Cache<Integer, Map<String, Object>> rowsCache;

    public CellValueCache() {
        super();

        this.cellsCache = CacheBuilder.newBuilder().maximumSize(50000).build();

        this.rowsCache = CacheBuilder.newBuilder().maximumSize(50000).build();
    }

    public Object get(String cachedKey) {
        if (log.isDebugEnabled()) {
            log.debug("> get, cachedKey=" + cachedKey);
        }
        return cellsCache.getIfPresent(cachedKey);
    }

    public void put(String cachedKey, Object aValue) {
        cellsCache.put(cachedKey, aValue);
    }

    public void close() {
        invalidateAll();
    }

    void invalidateAll() {
        if (cellsCache != null) {
            cellsCache.invalidateAll();
        }
        if (rowsCache != null) {
            rowsCache.invalidateAll();
        }
    }

    public Map<String, Object> getRowsCache(int rowIndex) {
        if (log.isDebugEnabled()) {
            log.debug("> getRowsCache, rowIndex=" + rowIndex);
        }

        return rowsCache.getIfPresent(rowIndex);
    }

    public void putRowsCache(int rowIndex, Map<String, Object> rowData) {
        rowsCache.put(rowIndex, rowData);
    }
}
