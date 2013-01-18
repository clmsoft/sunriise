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
package com.le.sunriise.accountviewer;

import java.util.List;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Category;
import com.le.sunriise.mnyobject.Currency;
import com.le.sunriise.mnyobject.Payee;
import com.le.sunriise.mnyobject.Security;

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
