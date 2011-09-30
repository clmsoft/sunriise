package com.le.sunriise.md;

import java.util.List;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;

public class MnyContext {
    private List<Account> accounts;

    private Map<Integer, Payee> payees;

    private Map<Integer, Category> categories;

    private Map<Integer, Currency> currencies;

    private Map<Integer, Security> securities;

    private Database db;
    
    public List<Account> getAccounts() {
        return accounts;
    }

    public void setAccounts(List<Account> accounts) {
        this.accounts = accounts;
    }

    public Map<Integer, Payee> getPayees() {
        return payees;
    }

    public void setPayees(Map<Integer, Payee> payees) {
        this.payees = payees;
    }

    public Map<Integer, Category> getCategories() {
        return categories;
    }

    public void setCategories(Map<Integer, Category> categories) {
        this.categories = categories;
    }

    public Map<Integer, Currency> getCurrencies() {
        return currencies;
    }

    public void setCurrencies(Map<Integer, Currency> currencies) {
        this.currencies = currencies;
    }

    public Map<Integer, Security> getSecurities() {
        return securities;
    }

    public void setSecurities(Map<Integer, Security> securities) {
        this.securities = securities;
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }
}
