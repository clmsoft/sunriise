/*******************************************************************************
 * Copyright (c) 2013 Hung Le
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

    public abstract void setAccountId(Integer accountId);
    
    public abstract Integer getAccountId();

}