package com.le.sunriise.mnyobject;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

public interface Transaction {

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract List<TransactionSplit> getSplits();

    public abstract void setSplits(List<TransactionSplit> splits);

    public abstract BigDecimal getAmount();

    public abstract void setAmount(BigDecimal amount);

    public abstract Integer getStatusFlag();

    public abstract void setStatusFlag(Integer status);

    public abstract boolean isVoid();

    public abstract Date getDate();

    public abstract void setDate(Date date);

    public abstract boolean isRecurring();

    public abstract BigDecimal getRunningBalance();

    public abstract void setRunningBalance(BigDecimal runningBalance);

    public abstract Integer getCategoryId();

    public abstract void setCategoryId(Integer category);

    public abstract Integer getPayeeId();

    public abstract void setPayeeId(Integer payee);

    public abstract Integer getTransferredAccountId();

    public abstract void setTransferredAccountId(Integer transferredAccount);

    public abstract Frequency getFrequency();

    public abstract void setFrequency(Frequency frequency);

    public abstract TransactionInfo getTransactionInfo();

    public abstract void setTransactionInfo(TransactionInfo transactionInfo);

    public abstract Integer getSecurityId();

    public abstract void setSecurityId(Integer securityId);

    public abstract boolean isInvestment();

    public abstract InvestmentActivityImpl getInvestmentActivity();

    public abstract void setInvestmentActivity(InvestmentActivityImpl investmentActivity);

    public abstract InvestmentTransaction getInvestmentTransaction();

    public abstract void setInvestmentTransaction(InvestmentTransaction investmentTransaction);

    public abstract Double getQuantity();

    public abstract Double getPrice();

    public abstract boolean isTransfer();

    public abstract Integer getClearedState();

    public abstract void setClearedState(Integer clearedState);

    public abstract boolean isCleared();

    public abstract boolean isReconciled();

    public abstract boolean hasSplits();

    public abstract String getMemo();

    public abstract void setMemo(String memo);

    public abstract String getNumber();

    public abstract void setNumber(String number);

    public abstract TransactionState getState();

    public abstract void setState(TransactionState state);

}