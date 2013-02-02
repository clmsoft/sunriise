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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.AccountType;
import com.le.sunriise.mnyobject.MnyObject;
import com.le.sunriise.mnyobject.SecurityHolding;
import com.le.sunriise.mnyobject.Transaction;

public class AccountImpl extends MnyObject implements Comparable<AccountImpl>, Account {
    /*
     * Internal MsMoney id
     */
    private Integer id;

    private String name;
    
    private Integer relatedToAccountId;
    private Account relatedToAccount;
    
    private Integer type;
    private AccountType accountType;
    
    private Boolean closed;

    private BigDecimal startingBalance;
    private BigDecimal currentBalance;

    private Integer currencyId;
    private String currencyCode;

    // fRetirement
    private Boolean retirement;

    // uat
    private Integer investmentSubType;

    private List<SecurityHolding> securityHoldings;

    // amtLimit
    private BigDecimal amountLimit;

    @JsonIgnore
    private final NumberFormat securityQuantityFormatter;

    @JsonIgnore
    private NumberFormat amountFormatter = NumberFormat.getCurrencyInstance();

    @JsonIgnore
    private List<Transaction> transactions;

    @JsonIgnore
    private List<Transaction> filteredTransactions;

    public AccountImpl() {
        super();

        securityQuantityFormatter = NumberFormat.getIntegerInstance();
        securityQuantityFormatter.setGroupingUsed(true);
        if (securityQuantityFormatter instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) securityQuantityFormatter;
            df.setMinimumFractionDigits(4);
            df.setMaximumFractionDigits(4);
        }
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#formatAmmount(java.math.BigDecimal)
     */
    @Override
    public String formatAmmount(BigDecimal amount) {
        return amountFormatter.format(amount);
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getType()
     */
    @Override
    public Integer getType() {
        return type;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setType(java.lang.Integer)
     */
    @Override
    public void setType(Integer type) {
        this.type = type;

        this.accountType = AccountType.toAccountType(type);
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setClosed(java.lang.Boolean)
     */
    @Override
    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getStartingBalance()
     */
    @Override
    public BigDecimal getStartingBalance() {
        if (startingBalance == null) {
            return new BigDecimal(0.0);
        }
        return startingBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setStartingBalance(java.math.BigDecimal)
     */
    @Override
    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getRelatedToAccountId()
     */
    @Override
    public Integer getRelatedToAccountId() {
        return relatedToAccountId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setRelatedToAccountId(java.lang.Integer)
     */
    @Override
    public void setRelatedToAccountId(Integer relatedToAccountId) {
        this.relatedToAccountId = relatedToAccountId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getTransactions()
     */
    @Override
    public List<Transaction> getTransactions() {
        return transactions;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setTransactions(java.util.List)
     */
    @Override
    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#toString()
     */
    @Override
    public String toString() {
        return getName();
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#compareTo(com.le.sunriise.mnyobject.impl.AccountImpl)
     */
    @Override
    public int compareTo(AccountImpl o) {
        return id.compareTo(o.getId());
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getAccountType()
     */
    @Override
    public AccountType getAccountType() {
        return accountType;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setAccountType(com.le.sunriise.mnyobject.AccountType)
     */
    @Override
    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getCurrencyId()
     */
    @Override
    public Integer getCurrencyId() {
        return currencyId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setCurrencyId(java.lang.Integer)
     */
    @Override
    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getCurrencyCode()
     */
    @Override
    public String getCurrencyCode() {
        return currencyCode;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setCurrencyCode(java.lang.String)
     */
    @Override
    public void setCurrencyCode(String currencyCode) {
        // currencyCode = "CNY";

        this.currencyCode = currencyCode;
        java.util.Currency javaCurrency = java.util.Currency.getInstance(currencyCode);
        if (javaCurrency != null) {
            Locale currencyLocale = null;
            if (currencyCode.equals("USD")) {
                currencyLocale = Locale.US;
            } else if (currencyCode.equals("GBP")) {
                currencyLocale = Locale.UK;
            } else if (currencyCode.equals("CAD")) {
                currencyLocale = Locale.CANADA;
            } else if (currencyCode.equals("JPY")) {
                currencyLocale = Locale.JAPAN;
            } else if (currencyCode.equals("CNY")) {
                currencyLocale = Locale.CHINA;
            } else {
                currencyLocale = null;
            }

            // currencyLocale = Locale.CHINA;

            if (currencyLocale != null) {
                amountFormatter = NumberFormat.getCurrencyInstance(currencyLocale);
            } else {
                amountFormatter = NumberFormat.getCurrencyInstance();
            }
            amountFormatter.setCurrency(javaCurrency);
        }
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getRetirement()
     */
    @Override
    public Boolean getRetirement() {
        return retirement;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setRetirement(java.lang.Boolean)
     */
    @Override
    public void setRetirement(Boolean retirement) {
        this.retirement = retirement;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getInvestmentSubType()
     */
    @Override
    public Integer getInvestmentSubType() {
        return investmentSubType;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setInvestmentSubType(java.lang.Integer)
     */
    @Override
    public void setInvestmentSubType(Integer investmentSubType) {
        this.investmentSubType = investmentSubType;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getAmountLimit()
     */
    @Override
    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setAmountLimit(java.math.BigDecimal)
     */
    @Override
    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#isCreditCard()
     */
    @Override
    public boolean isCreditCard() {
        return getAccountType() == AccountType.CREDIT_CARD;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#is401k403b()
     */
    @Override
    public boolean is401k403b() {
        if (!retirement) {
            return false;
        }

        if (investmentSubType == null) {
            return false;
        }

        // 403(b)
        if (investmentSubType == 0) {
            return true;
        }
        if (investmentSubType == 1) {
            return true;
        }

        return false;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getCurrentBalance()
     */
    @Override
    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setCurrentBalance(java.math.BigDecimal)
     */
    @Override
    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getClosed()
     */
    @Override
    public Boolean getClosed() {
        return closed;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#formatSecurityQuantity(java.lang.Double)
     */
    @Override
    public String formatSecurityQuantity(Double quantity) {
        return securityQuantityFormatter.format(quantity);
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getSecurityHoldings()
     */
    @Override
    public List<SecurityHolding> getSecurityHoldings() {
        return securityHoldings;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setSecurityHoldings(java.util.List)
     */
    @Override
    public void setSecurityHoldings(List<SecurityHolding> securityHoldings) {
        this.securityHoldings = securityHoldings;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getRelatedToAccount()
     */
    @Override
    public Account getRelatedToAccount() {
        return relatedToAccount;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setRelatedToAccount(com.le.sunriise.mnyobject.impl.Account)
     */
    @Override
    public void setRelatedToAccount(Account relatedToAccount) {
        this.relatedToAccount = relatedToAccount;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#getFilteredTransactions()
     */
    @Override
    public List<Transaction> getFilteredTransactions() {
        return filteredTransactions;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.impl.Account#setFilteredTransactions(java.util.List)
     */
    @Override
    public void setFilteredTransactions(List<Transaction> filteredTransactions) {
        this.filteredTransactions = filteredTransactions;
    }
}
