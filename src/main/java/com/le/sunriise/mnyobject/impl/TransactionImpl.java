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
package com.le.sunriise.mnyobject.impl;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.le.sunriise.mnyobject.Frequency;
import com.le.sunriise.mnyobject.InvestmentActivityImpl;
import com.le.sunriise.mnyobject.InvestmentTransaction;
import com.le.sunriise.mnyobject.MnyObject;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionInfo;
import com.le.sunriise.mnyobject.TransactionSplit;
import com.le.sunriise.mnyobject.TransactionState;


public class TransactionImpl extends MnyObject implements Comparable<TransactionImpl>, Transaction {
    private static final Logger log = Logger.getLogger(TransactionImpl.class);

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

    private InvestmentActivityImpl investmentActivity;

    private InvestmentTransaction investmentTransaction;

    private Integer clearedState;

    private String memo;

    private String number;

    private TransactionState state = TransactionState.UNRECONCILED;

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getSplits()
     */
    @Override
    public List<TransactionSplit> getSplits() {
        return splits;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setSplits(java.util.List)
     */
    @Override
    public void setSplits(List<TransactionSplit> splits) {
        this.splits = splits;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getAmount()
     */
    @Override
    public BigDecimal getAmount() {
        return amount;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setAmount(java.math.BigDecimal)
     */
    @Override
    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getStatusFlag()
     */
    @Override
    public Integer getStatusFlag() {
        return statusFlag;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setStatusFlag(java.lang.Integer)
     */
    @Override
    public void setStatusFlag(Integer status) {
        this.statusFlag = status;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isVoid()
     */
    @Override
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

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getDate()
     */
    @Override
    public Date getDate() {
        return date;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setDate(java.util.Date)
     */
    @Override
    public void setDate(Date date) {
        this.date = date;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isRecurring()
     */
    @Override
    public boolean isRecurring() {
        return frequency.isRecurring();
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getRunningBalance()
     */
    @Override
    public BigDecimal getRunningBalance() {
        return runningBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setRunningBalance(java.math.BigDecimal)
     */
    @Override
    public void setRunningBalance(BigDecimal runningBalance) {
        this.runningBalance = runningBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getCategoryId()
     */
    @Override
    public Integer getCategoryId() {
        return categoryId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setCategoryId(java.lang.Integer)
     */
    @Override
    public void setCategoryId(Integer category) {
        this.categoryId = category;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getPayeeId()
     */
    @Override
    public Integer getPayeeId() {
        return payeeId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setPayeeId(java.lang.Integer)
     */
    @Override
    public void setPayeeId(Integer payee) {
        this.payeeId = payee;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getTransferredAccountId()
     */
    @Override
    public Integer getTransferredAccountId() {
        return transferredAccountId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setTransferredAccountId(java.lang.Integer)
     */
    @Override
    public void setTransferredAccountId(Integer transferredAccount) {
        this.transferredAccountId = transferredAccount;
    }

    @Override
    public int compareTo(TransactionImpl o) {
        return id.compareTo(o.getId());
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getFrequency()
     */
    @Override
    public Frequency getFrequency() {
        return frequency;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setFrequency(com.le.sunriise.mnyobject.Frequency)
     */
    @Override
    public void setFrequency(Frequency frequency) {
        this.frequency = frequency;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getTransactionInfo()
     */
    @Override
    public TransactionInfo getTransactionInfo() {
        return transactionInfo;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setTransactionInfo(com.le.sunriise.mnyobject.TransactionInfo)
     */
    @Override
    public void setTransactionInfo(TransactionInfo transactionInfo) {
        this.transactionInfo = transactionInfo;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getSecurityId()
     */
    @Override
    public Integer getSecurityId() {
        return securityId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setSecurityId(java.lang.Integer)
     */
    @Override
    public void setSecurityId(Integer securityId) {
        this.securityId = securityId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isInvestment()
     */
    @Override
    public boolean isInvestment() {
        return transactionInfo.isInvestment();
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getInvestmentActivity()
     */
    @Override
    public InvestmentActivityImpl getInvestmentActivity() {
        return investmentActivity;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setInvestmentActivity(com.le.sunriise.mnyobject.InvestmentActivityImpl)
     */
    @Override
    public void setInvestmentActivity(InvestmentActivityImpl investmentActivity) {
        this.investmentActivity = investmentActivity;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getInvestmentTransaction()
     */
    @Override
    public InvestmentTransaction getInvestmentTransaction() {
        return investmentTransaction;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setInvestmentTransaction(com.le.sunriise.mnyobject.InvestmentTransaction)
     */
    @Override
    public void setInvestmentTransaction(InvestmentTransaction investmentTransaction) {
        this.investmentTransaction = investmentTransaction;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getQuantity()
     */
    @Override
    public Double getQuantity() {
        Double quantity = null;
        InvestmentTransaction investmentTransaction = getInvestmentTransaction();
        if (investmentTransaction != null) {
            quantity = investmentTransaction.getQuantity();
        }
        return quantity;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getPrice()
     */
    @Override
    public Double getPrice() {
        Double price = null;
        InvestmentTransaction investmentTransaction = getInvestmentTransaction();
        if (investmentTransaction != null) {
            price = investmentTransaction.getPrice();
        }
        return price;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isTransfer()
     */
    @Override
    public boolean isTransfer() {
        return transferredAccountId != null;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getClearedState()
     */
    @Override
    public Integer getClearedState() {
        return clearedState;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setClearedState(java.lang.Integer)
     */
    @Override
    public void setClearedState(Integer clearedState) {
        this.clearedState = clearedState;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isCleared()
     */
    @Override
    public boolean isCleared() {
        return (clearedState != null) && (clearedState == 1);
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#isReconciled()
     */
    @Override
    public boolean isReconciled() {
        return (clearedState != null) && (clearedState == 2);
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#hasSplits()
     */
    @Override
    public boolean hasSplits() {
        if (splits == null) {
            return false;
        }
        return splits.size() > 0;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getMemo()
     */
    @Override
    public String getMemo() {
        return memo;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setMemo(java.lang.String)
     */
    @Override
    public void setMemo(String memo) {
        this.memo = memo;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getNumber()
     */
    @Override
    public String getNumber() {
        return number;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setNumber(java.lang.String)
     */
    @Override
    public void setNumber(String number) {
        this.number = number;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#getState()
     */
    @Override
    public TransactionState getState() {
        return state;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Transaction#setState(com.le.sunriise.mnyobject.TransactionState)
     */
    @Override
    public void setState(TransactionState state) {
        this.state = state;
    }
}
