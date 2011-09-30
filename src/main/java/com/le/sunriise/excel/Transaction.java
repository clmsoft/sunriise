package com.le.sunriise.excel;

import java.math.BigDecimal;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.Row;

public class Transaction {
    private static final Logger log = Logger.getLogger(Transaction.class);

    // Number Date Account Payee Cleared Amount Category Subcategory Memo
    private String number;
    private Date date;
    private String accountName;
    private String payee;
    private String cleared;
    private BigDecimal amount;
    private String category;
    private String subCategory;
    private String meno;

    public static Transaction toTransaction(String sheetName, Row row) {
        Transaction txn = new Transaction();
        for (Cell cell : row) {
            parseCell(sheetName, row, txn, cell);
        }
        return txn;
    }

    private static void parseCell(String sheetName, Row row, Transaction txn, Cell cell) {
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
            switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                txn.setNumber(cell.getStringCellValue());
                break;
            case Cell.CELL_TYPE_NUMERIC:
                txn.setNumber("" + cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                txn.setNumber(cell.getRichStringCellValue().getString());
                break;
            default:
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }

            break;

        case 1:
            switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_NUMERIC:
                if (!DateUtil.isCellDateFormatted(cell)) {
                    log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                    return;
                }
                txn.setDate(DateUtil.getJavaDate(cell.getNumericCellValue()));
                break;
            default:
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            break;

        case 2:
            switch (cellType) {
            case Cell.CELL_TYPE_BLANK:
                cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_STRING:
                txn.setAccountName(cell.getStringCellValue());
                break;
            default:
                log.error("Wrong type:" + cellLocation + ", cellType=" + cellType);
                return;
            }
            break;
        }
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public String getPayee() {
        return payee;
    }

    public void setPayee(String payee) {
        this.payee = payee;
    }

    public String getCleared() {
        return cleared;
    }

    public void setCleared(String cleared) {
        this.cleared = cleared;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getSubCategory() {
        return subCategory;
    }

    public void setSubCategory(String subCategory) {
        this.subCategory = subCategory;
    }

    public String getMeno() {
        return meno;
    }

    public void setMeno(String meno) {
        this.meno = meno;
    }

}
