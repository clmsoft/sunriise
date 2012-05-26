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
package com.le.sunriise.excel;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class Account {
    private static final Logger log = Logger.getLogger(Account.class);

    // Name Type Status Opening Balance Balance Currency
    private String name;
    private String accountType;
    private String status;
    private BigDecimal openingBalance;
    private BigDecimal balance;
    private String currency;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public BigDecimal getOpeningBalance() {
        return openingBalance;
    }

    public void setOpeningBalance(BigDecimal openingBalance) {
        this.openingBalance = openingBalance;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

    public String getCurrency() {
        return currency;
    }

    public void setCurrency(String currency) {
        this.currency = currency;
    }

    static Account toAccount(String sheetName, Row row) {
        Account account = new Account();
        for (Cell cell : row) {
            parseCell(sheetName, row, account, cell);
        }
        return account;
    }

    private static void parseCell(String sheetName, Row row, Account account, Cell cell) {
        int rowNum = row.getRowNum();
        if (rowNum == 0) {
            // header
            return;
        }
        int columnIndex = cell.getColumnIndex();
        int cellType = cell.getCellType();

        CellReference cellRef = new CellReference(rowNum, columnIndex);
        if (log.isDebugEnabled()) {
            log.info("Cell: " + cellRef.formatAsString());
        }
        // System.out.print(cellRef.formatAsString());
        // System.out.print(" - ");

        String cellLocation = sheetName + "." + rowNum + "-" + columnIndex;
        switch (columnIndex) {
        case 0:
            if (cellType != Cell.CELL_TYPE_STRING) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setName(cell.getRichStringCellValue().getString());
            break;
        case 1:
            if (cellType != Cell.CELL_TYPE_STRING) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setAccountType(cell.getRichStringCellValue().getString());
            break;
        case 2:
            if (cellType != Cell.CELL_TYPE_STRING) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setStatus(cell.getRichStringCellValue().getString());
            break;
        case 3:
            if (cellType != Cell.CELL_TYPE_NUMERIC) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            if (DateUtil.isCellDateFormatted(cell)) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setOpeningBalance(new BigDecimal(cell.getNumericCellValue()));
            break;
        case 4:
            if (cellType != Cell.CELL_TYPE_NUMERIC) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            if (DateUtil.isCellDateFormatted(cell)) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setBalance(new BigDecimal(cell.getNumericCellValue()));
            break;
        case 5:
            if (cellType != Cell.CELL_TYPE_STRING) {
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            account.setCurrency(cell.getRichStringCellValue().getString());
            break;
        }
    }
}
