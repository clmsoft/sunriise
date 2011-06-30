package com.le.sunriise.model.bean;

import com.healthmarketscience.jackcess.Table;

public class TableListItem {
    private Table table;

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String toString() {
        return table.getName() + " (" + table.getRowCount() + ")";
    }
}
