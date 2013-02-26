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
package com.le.sunriise.account;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.AccountType;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.viewer.OpenedDb;

public abstract class AbstractAccountVisitor {
    private static final Logger log = Logger.getLogger(AbstractAccountVisitor.class);
    private Account currentAccount;
    private Transaction currentTransaction;
    private Transaction currentFilteredTransaction;
    protected MnyContext mnyContext;

    public void visit(File dbFile, String password) throws IOException {
        OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
        this._visit(openedDb);
    }

    public abstract void preVisit(OpenedDb openedDb) throws IOException;

    public abstract void visit(OpenedDb openedDb) throws IOException;

    public abstract void postVisit(OpenedDb openedDb) throws IOException;

    public abstract void visitAccounts(List<Account> accounts) throws IOException;

    public abstract void visitAccount(Account account) throws IOException;

    public abstract void visitTransaction(Transaction transaction) throws IOException;

    public abstract void visitFilteredTransaction(Transaction transaction) throws IOException;

    protected void _visit(OpenedDb openedDb) throws IOException {
        preVisit(openedDb);
        try {
            this.mnyContext = AccountUtil.createMnyContext(openedDb);

            visit(openedDb);

            List<Account> accounts = mnyContext.getAccounts();

            visitAccounts(accounts);

            for (Account account : accounts) {
                _visitAccount(account);
            }
        } finally {
            postVisit(openedDb);
        }
    }

    private void _visitAccount(Account account) throws IOException {
        if (! acceptAccount(account)) {
            return;
        }
        this.currentAccount = account;

        Database db = mnyContext.getDb();
        AccountUtil.retrieveTransactions(db, account);
        AccountUtil.calculateCurrentBalance(account);
        if (account != null) {
            AccountType accountType = account.getAccountType();
            switch (accountType) {
            case INVESTMENT:
                Double marketValue = AccountUtil.calculateInvestmentBalance(account, mnyContext);
                account.setCurrentBalance(new BigDecimal(marketValue));
                break;
            default:
                if (log.isDebugEnabled()) {
                    log.warn("Not handle accountType=" + accountType);
                }
                break;
            }
        }

        visitAccount(account);

        List<Transaction> transactions = account.getTransactions();
        if (transactions != null) {
            for (Transaction transaction : transactions) {
                _visitTransaction(transaction);
            }
        }

        List<Transaction> filteredTransactions = account.getFilteredTransactions();
        if (transactions != null) {
            for (Transaction filteredTransaction : filteredTransactions) {
                _visitFilteredTransaction(filteredTransaction);
            }
        }

    }

    protected boolean acceptAccount(Account account) {
        return true;
    }

    private void _visitTransaction(Transaction transaction) throws IOException {
        this.currentTransaction = transaction;

        visitTransaction(transaction);
    }

    private void _visitFilteredTransaction(Transaction transaction) throws IOException {
        this.currentFilteredTransaction = transaction;

        visitFilteredTransaction(transaction);
    }
}
