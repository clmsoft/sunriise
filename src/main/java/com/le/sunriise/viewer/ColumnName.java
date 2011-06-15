package com.le.sunriise.viewer;

import java.util.ArrayList;
import java.util.List;

public class ColumnName {
    private String tableName;

    private String columnName;

    private ColumnName parent;
    
    private final List<ColumnName> children = new ArrayList<ColumnName>();

    public ColumnName(String tableName, String columnName) {
        super();
        this.tableName = tableName;
        this.columnName = columnName;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String columnName) {
        this.columnName = columnName;
    }

    public ColumnName getParent() {
        return parent;
    }

    public void setParent(ColumnName parent) {
        this.parent = parent;
    }

    public List<ColumnName> getChildren() {
        return children;
    }
}
