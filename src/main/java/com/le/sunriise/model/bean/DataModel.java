package com.le.sunriise.model.bean;

import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.healthmarketscience.jackcess.Table;

public class DataModel {
    private List<Table> tables;

    private Table table;

    private TableModel tableModel = new DefaultTableModel();

    private String tableName;

    private String tableMetaData;

    private String headerInfo;

    public List<Table> getTables() {
        return tables;
    }

    public void setTables(List<Table> tables) {
        this.tables = tables;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Table getTable() {
        return table;
    }

    public void setTable(Table table) {
        this.table = table;
    }

    public String getTableMetaData() {
        return tableMetaData;
    }

    public void setTableMetaData(String tableMetaData) {
        this.tableMetaData = tableMetaData;
    }

    public String getHeaderInfo() {
        return headerInfo;
    }

    public void setHeaderInfo(String headerInfo) {
        this.headerInfo = headerInfo;
    }
}
