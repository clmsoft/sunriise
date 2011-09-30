package com.le.sunriise.md;

public class TransactionSplit extends MnyObject {
    private Integer parentId;

    private Integer rowId;

    private Transaction transactionId;

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    public Integer getRowId() {
        return rowId;
    }

    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    public Transaction getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(Transaction transaction) {
        this.transactionId = transaction;
    }
}
