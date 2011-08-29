package com.le.sunriise.md;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public class Transaction implements Comparable<Transaction>{
    private Integer id;

    private BigDecimal amount;

    private BigDecimal runningBalance;

    private List<TransactionSplit> splits;

    private Integer statusFlag;

    private Date date;

    private Integer frequency;

    private Integer categoryId;

    private Integer payeeId;

    private Integer transferredAccountId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public List<TransactionSplit> getSplits() {
        return splits;
    }

    public void setSplits(List<TransactionSplit> splits) {
        this.splits = splits;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public Integer getStatusFlag() {
        return statusFlag;
    }

    public void setStatusFlag(Integer status) {
        this.statusFlag = status;
    }

    public boolean isVoid() {
        if (statusFlag == null) {
            return false;
        }

        if ((statusFlag & 256) == 256) {
            return true;
        }

        // TODO: hack to skip unknown transactions
        if (statusFlag == 2490368) {
            return true;
        }
        if (statusFlag == 2490400) {
            return true;
        }
        if (statusFlag > 2359302) {
            return true;
        }
        if (statusFlag > 2097152) {
            return true;
        }

        return false;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public boolean isRecurring() {
        Integer frequency = getFrequency();
        if ((frequency != null) && (frequency > 0)) {
            return true;
        }
        return false;

    }

    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    public Integer getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(Integer category) {
        this.categoryId = category;
    }

    public Integer getPayeeId() {
        return payeeId;
    }

    public void setPayeeId(Integer payee) {
        this.payeeId = payee;
    }

    public Integer getTransferredAccountId() {
        return transferredAccountId;
    }

    public void setTransferredAccountId(Integer transferredAccount) {
        this.transferredAccountId = transferredAccount;
    }

    @Override
    public int compareTo(Transaction o) {
        return id.compareTo(o.getId());
    }
}
