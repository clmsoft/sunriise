package com.le.sunriise.report;

import java.math.BigDecimal;
import java.util.Date;

import com.le.sunriise.accountviewer.Account;

class AccountBalance {
    private Account account;
    private Date date;
    private BigDecimal balance;

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public void setBalance(BigDecimal balance) {
        this.balance = balance;
    }

}