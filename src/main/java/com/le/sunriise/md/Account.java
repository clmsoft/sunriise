package com.le.sunriise.md;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.util.List;
import java.util.Locale;

public class Account extends MnyObject implements Comparable<Account> {
    private Integer id;

    private Integer relatedToAccountId;

    private String name;

    private Integer type;

    private boolean closed;

    private BigDecimal startingBalance;

    private List<Transaction> transactions;

    private AccountType accountType;

    private Integer currencyId;

    private NumberFormat amountFormatter = NumberFormat.getCurrencyInstance();

    private String currencyCode;

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

    public boolean isClosed() {
        return closed;
    }

    public void setClosed(boolean closed) {
        this.closed = closed;
    }

    public BigDecimal getStartingBalance() {
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
//         currencyCode = "CNY";

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

//            currencyLocale = Locale.CHINA;

            if (currencyLocale != null) {
                amountFormatter = NumberFormat.getCurrencyInstance(currencyLocale);
            } else {
                amountFormatter = NumberFormat.getCurrencyInstance();
            }
            amountFormatter.setCurrency(javaCurrency);
        }
    }
}
