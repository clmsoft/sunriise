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
package com.le.sunriise.mnyobject;

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

    // column: grftt
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

    private Integer clearedState;

    private String memo;

    private String number;

    private TransactionState state = TransactionState.UNRECONCILED;

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

    public boolean isTransfer() {
        return transferredAccountId != null;
    }

    public Integer getClearedState() {
        return clearedState;
    }

    public void setClearedState(Integer clearedState) {
        this.clearedState = clearedState;
    }

    public boolean isCleared() {
        return (clearedState != null) && (clearedState == 1);
    }

    public boolean isReconciled() {
        return (clearedState != null) && (clearedState == 2);
    }

    public boolean hasSplits() {
        if (splits == null) {
            return false;
        }
        return splits.size() > 0;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }

    public String getNumber() {
        return number;
    }

    public void setNumber(String number) {
        this.number = number;
    }

    public TransactionState getState() {
        return state;
    }

    public void setState(TransactionState state) {
        this.state = state;
    }
}
