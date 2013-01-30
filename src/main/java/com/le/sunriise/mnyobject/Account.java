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
import java.util.List;

public interface Account {
    /**
     * Get the internal MsMoney id for this account.
     * 
     * @return an non-negative Integer which is the internal MsMoney id for this
     *         account.
     */
    public abstract Integer getId();

    /**
     * Set the internal MsMoney id for this account.
     * 
     * @param id
     *            an non-negative Integer which is the internal MsMoney id for
     *            this account.
     */
    public abstract void setId(Integer id);

    /**
     * Get the account name.
     * 
     * @return
     */
    public abstract String getName();

    /**
     * Set the account name.
     * 
     * @param name
     */
    public abstract void setName(String name);

    /**
     * Get the account type.
     * 
     * @return
     */
    public abstract Integer getType();

    /**
     * Set the account type.
     * 
     * @param type
     */
    public abstract void setType(Integer type);

    public abstract AccountType getAccountType();

    public abstract void setAccountType(AccountType accountType);

    public abstract Integer getCurrencyId();

    public abstract void setCurrencyId(Integer currencyId);

    public abstract String getCurrencyCode();

    public abstract void setCurrencyCode(String currencyCode);

    public abstract BigDecimal getStartingBalance();

    public abstract void setStartingBalance(BigDecimal startingBalance);

    public abstract BigDecimal getCurrentBalance();

    public abstract void setCurrentBalance(BigDecimal currentBalance);

    public abstract Integer getRelatedToAccountId();

    public abstract void setRelatedToAccountId(Integer relatedToAccountId);

    public abstract Account getRelatedToAccount();

    public abstract void setRelatedToAccount(Account relatedToAccount);

    public abstract List<Transaction> getTransactions();

    public abstract void setTransactions(List<Transaction> transactions);

    public abstract String formatAmmount(BigDecimal amount);

    public abstract void setClosed(Boolean closed);

    public abstract Boolean getRetirement();

    public abstract void setRetirement(Boolean retirement);

    public abstract Integer getInvestmentSubType();

    public abstract void setInvestmentSubType(Integer investmentSubType);

    public abstract BigDecimal getAmountLimit();

    public abstract void setAmountLimit(BigDecimal amountLimit);

    public abstract List<SecurityHolding> getSecurityHoldings();

    public abstract void setSecurityHoldings(List<SecurityHolding> securityHoldings);

    public abstract List<Transaction> getFilteredTransactions();

    public abstract void setFilteredTransactions(List<Transaction> filteredTransactions);

    public abstract boolean isCreditCard();

    public abstract boolean is401k403b();

    public abstract Boolean getClosed();

    public abstract String formatSecurityQuantity(Double quantity);
}