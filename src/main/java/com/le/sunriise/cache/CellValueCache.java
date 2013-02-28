package com.le.sunriise.cache;

import java.util.Map;

import org.apache.log4j.Logger;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

public class CellValueCache {
    private static final Logger log = Logger.getLogger(CellValueCache.class);

    private final Cache<String, Object> cellsCache;

    private final Cache<Integer, Map<String, Object>> rowsCache;

    private class NullCellValue {
        private String cachedKey;

        public String getCachedKey() {
            return cachedKey;
        }

        public void setCachedKey(String cachedKey) {
            this.cachedKey = cachedKey;
        }

        public NullCellValue(String cachedKey) {
            super();
            this.cachedKey = cachedKey;
        }
    }

    private final int defaultCellCacheMaximumSize = 100000;

    private final int defaultRowCacheMaximumSize = 50000;

    public CellValueCache() {
        super();

        this.cellsCache = CacheBuilder.newBuilder().maximumSize(defaultCellCacheMaximumSize).build();

        this.rowsCache = CacheBuilder.newBuilder().maximumSize(defaultRowCacheMaximumSize).build();
    }

    public void put(String cachedKey, Object aValue) {
        if (aValue == null) {
            aValue = new NullCellValue(cachedKey);
            if (log.isDebugEnabled()) {
                log.debug("put NullCellValue, cachedKey=" + cachedKey);
            }
        }
        cellsCache.put(cachedKey, aValue);
    }

    public Object getIfPresent(String cachedKey) {
        return cellsCache.getIfPresent(cachedKey);
    }

    public Object get(String cachedKey) {
        Object value = null;

        if (log.isDebugEnabled()) {
            log.debug("> get, cachedKey=" + cachedKey);
        }
        value = cellsCache.getIfPresent(cachedKey);
        if (value == null) {
            return null;
        }

        if (value instanceof NullCellValue) {
            if (log.isDebugEnabled()) {
                log.debug("get NullCellValue, cachedKey=" + cachedKey);
            }
            value = null;
        }
        return value;
    }

    public void putRowsCache(int rowIndex, Map<String, Object> rowData) {
        rowsCache.put(rowIndex, rowData);
    }

    public Map<String, Object> getRowsCache(int rowIndex) {
        if (log.isDebugEnabled()) {
            log.debug("> getRowsCache, rowIndex=" + rowIndex);
        }

        return rowsCache.getIfPresent(rowIndex);
    }

    public void invalidateAll() {
        log.info("> invalidateAll");

        if (cellsCache != null) {
            cellsCache.invalidateAll();
        }
        if (rowsCache != null) {
            rowsCache.invalidateAll();
        }
    }

    public void close() {
        invalidateAll();
    }
}
