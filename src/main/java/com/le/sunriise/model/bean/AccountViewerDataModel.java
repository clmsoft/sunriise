package com.le.sunriise.model.bean;

import java.util.List;

import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;

import com.le.sunriise.md.Account;

public class AccountViewerDataModel {
    private List<Account> accounts;

    private TableModel tableModel = new DefaultTableModel();
    
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public TableModel getTableModel() {
        return tableModel;
    }

    public void setTableModel(TableModel tableModel) {
        this.tableModel = tableModel;
    }
}
