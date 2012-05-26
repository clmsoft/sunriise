/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
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