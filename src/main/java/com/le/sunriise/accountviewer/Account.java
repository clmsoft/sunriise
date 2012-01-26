package com.le.sunriise.accountviewer;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Account extends MnyObject implements Comparable<Account> {
    private Integer id;

    private Integer relatedToAccountId;
    private Account relatedToAccount;

    private String name;

    private Integer type;

    private Boolean closed;

    private BigDecimal startingBalance;
    private BigDecimal currentBalance;

    private List<Transaction> transactions;
    private List<Transaction> filteredTransactions;

    private AccountType accountType;

    private Integer currencyId;

    private NumberFormat amountFormatter = NumberFormat.getCurrencyInstance();

    private String currencyCode;

    // fRetirement
    private Boolean retirement;

    // uat
    private Integer investmentSubType;

    // amtLimit
    private BigDecimal amountLimit;

    private List<SecurityHolding> securityHoldings;

    private final NumberFormat securityQuantityFormatter;

    public Account() {
        super();

        securityQuantityFormatter = NumberFormat.getIntegerInstance();
        securityQuantityFormatter.setGroupingUsed(true);
        if (securityQuantityFormatter instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) securityQuantityFormatter;
            df.setMinimumFractionDigits(4);
            df.setMaximumFractionDigits(4);
        }
    }

    public String formatAmmount(BigDecimal amount) {
        return amountFormatter.format(amount);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;

        this.accountType = AccountType.toAccountType(type);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public void setClosed(Boolean closed) {
        this.closed = closed;
    }

    public BigDecimal getStartingBalance() {
        if (startingBalance == null) {
            return new BigDecimal(0.0);
        }
        return startingBalance;
    }

    public void setStartingBalance(BigDecimal startingBalance) {
        this.startingBalance = startingBalance;
    }

    public Integer getRelatedToAccountId() {
        return relatedToAccountId;
    }

    public void setRelatedToAccountId(Integer relatedToAccountId) {
        this.relatedToAccountId = relatedToAccountId;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public String toString() {
        return getName();
    }

    @Override
    public int compareTo(Account o) {
        return id.compareTo(o.getId());
    }

    public AccountType getAccountType() {
        return accountType;
    }

    public void setAccountType(AccountType accountType) {
        this.accountType = accountType;
    }

    public Integer getCurrencyId() {
        return currencyId;
    }

    public void setCurrencyId(Integer currencyId) {
        this.currencyId = currencyId;
    }

    public String getCurrencyCode() {
        return currencyCode;
    }

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

    public Boolean getRetirement() {
        return retirement;
    }

    public void setRetirement(Boolean retirement) {
        this.retirement = retirement;
    }

    public Integer getInvestmentSubType() {
        return investmentSubType;
    }

    public void setInvestmentSubType(Integer investmentSubType) {
        this.investmentSubType = investmentSubType;
    }

    public BigDecimal getAmountLimit() {
        return amountLimit;
    }

    public void setAmountLimit(BigDecimal amountLimit) {
        this.amountLimit = amountLimit;
    }

    public boolean isCreditCard() {
        return getAccountType() == AccountType.CREDIT_CARD;
    }

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

    public BigDecimal getCurrentBalance() {
        return currentBalance;
    }

    public void setCurrentBalance(BigDecimal currentBalance) {
        this.currentBalance = currentBalance;
    }

    public Boolean getClosed() {
        return closed;
    }

    public String formatSecurityQuantity(Double quantity) {
        return securityQuantityFormatter.format(quantity);
    }

    public List<SecurityHolding> getSecurityHoldings() {
        return securityHoldings;
    }

    public void setSecurityHoldings(List<SecurityHolding> securityHoldings) {
        this.securityHoldings = securityHoldings;
    }

    public Account getRelatedToAccount() {
        return relatedToAccount;
    }

    public void setRelatedToAccount(Account relatedToAccount) {
        this.relatedToAccount = relatedToAccount;
    }

    public List<Transaction> getFilteredTransactions() {
        return filteredTransactions;
    }

    public void setFilteredTransactions(List<Transaction> filteredTransactions) {
        this.filteredTransactions = filteredTransactions;
    }
}
