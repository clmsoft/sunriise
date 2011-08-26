package com.le.sunriise.md;

import java.util.List;

import javax.swing.table.AbstractTableModel;

final class AccountViewerTableModel extends AbstractTableModel {
    private final Account account;

    AccountViewerTableModel(Account account) {
        this.account = account;
    }

    @Override
    public int getRowCount() {
        return account.getTransactions().size();
    }

    @Override
    public int getColumnCount() {
        return 6;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = null;
        List<Transaction> transactions = account.getTransactions();
        Transaction transaction = transactions.get(rowIndex);
        switch (columnIndex) {
        case 0:
            value = transaction.getId();
            break;
        case 1:
            value = transaction.getDate();
            break;
        case 2:
            value = transaction.getAmount();
            break;
        case 3:
            value = AccountUtil.getRunningBalance(rowIndex, account);
            break;
        case 4:
            value = transaction.isRecurring();
            break;
        case 5:
            value = transaction.isVoid();
            break;
        default:
            value = null;
            break;
        }

        return value;
    }

    @Override
    public String getColumnName(int column) {
        String value = null;
        switch (column) {
        case 0:
            value = "ID";
            break;
        case 1:
            value = "Date";
            break;
        case 2:
            value = "Amount";
            break;
        case 3:
            value = "Balance";
            break;
        case 4:
            value = "Recurring";
            break;
        case 5:
            value = "Voided";
            break;
        default:
            value = null;
            break;
        }

        return value;
    }
}