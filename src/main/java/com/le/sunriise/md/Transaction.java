package com.le.sunriise.md;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

public class Transaction extends MnyObject implements Comparable<Transaction> {
    private static final Logger log = Logger.getLogger(Transaction.class);

    private Integer id;

    private BigDecimal amount;

    private BigDecimal runningBalance;

    private List<TransactionSplit> splits;

    private Integer statusFlag;

    private Date date;

    private Frequency frequency;

    private Integer categoryId;

    private Integer payeeId;

    private Integer transferredAccountId;

    private TransactionInfo transactionInfo;

    private Integer securityId;

    private InvestmentActivity investmentActivity;

    private InvestmentTransaction investmentTransaction;

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

        if (transactionInfo.isVoid()) {
            return true;
        }
        // if ((statusFlag & 256) == 256) {
        // if (!transactionInfo.isVoid()) {
        // log.warn("isVoid does not match");
        // }
        // return true;
        // }

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

    public boolean isRecurring() {
        return frequency.isRecurring();
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

    public Frequency getFrequency() {
        return frequency;
    }

    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    public Integer getSecurityId() {
        return securityId;
    }

    public void setSecurityId(Integer securityId) {
        this.securityId = securityId;
    }

    public boolean isInvestment() {
        return transactionInfo.isInvestment();
    }

    public InvestmentActivity getInvestmentActivity() {
        return investmentActivity;
    }

    public void setInvestmentActivity(InvestmentActivity investmentActivity) {
        this.investmentActivity = investmentActivity;
    }

    public InvestmentTransaction getInvestmentTransaction() {
        return investmentTransaction;
    }

    public void setInvestmentTransaction(InvestmentTransaction investmentTransaction) {
        this.investmentTransaction = investmentTransaction;
    }

    public Double getQuantity() {
        Double quantity = null;
        InvestmentTransaction investmentTransaction = getInvestmentTransaction();
        if (investmentTransaction != null) {
            quantity = investmentTransaction.getQuantity();
        }
        return quantity;
    }

    public Double getPrice() {
        Double price = null;
        InvestmentTransaction investmentTransaction = getInvestmentTransaction();
        if (investmentTransaction != null) {
            price = investmentTransaction.getPrice();
        }
        return price;
    }

}
