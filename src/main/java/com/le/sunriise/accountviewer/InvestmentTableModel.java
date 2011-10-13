package com.le.sunriise.accountviewer;

import java.util.List;

public class InvestmentTableModel extends DefaultAccountViewerTableModel {
    public InvestmentTableModel(Account account) {
        super(account);
    }

    @Override
    public int getColumnCount() {
        return super.getColumnCount() + 1;
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        Object value = super.getValueAt(rowIndex, columnIndex);

        List<Transaction> transactions = getAccount().getTransactions();
        Transaction transaction = transactions.get(rowIndex);

        switch (columnIndex) {
        case 5:
            value = transaction.getQuantity();
            break;
        case 6:
            value = transaction.getPrice();
            break;
        case 7:
            value = transaction.isVoid();
            break;
        }
        return value;
    }

    @Override
    public String getColumnName(int column) {
        String columnName = super.getColumnName(column);

        switch (column) {
        case 2:
            columnName = "Activity";
            break;
        case 3:
            columnName = "Investment";
            break;
        case 5:
            columnName = "Quantity";
            break;
        case 6:
            columnName = "Price";
            break;
        case 7:
            columnName = "Voided";
            break;
        }
        return columnName;
    }
}