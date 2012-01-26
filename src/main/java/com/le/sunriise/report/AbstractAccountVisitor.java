package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.accountviewer.Transaction;
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

    private void _visit(OpenedDb openedDb) throws IOException {
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
        this.currentAccount = account;

        Database db = mnyContext.getDb();
        AccountUtil.retrieveTransactions(db, account);

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

    private void _visitTransaction(Transaction transaction) throws IOException {
        this.currentTransaction = transaction;

        visitTransaction(transaction);
    }

    private void _visitFilteredTransaction(Transaction transaction) throws IOException {
        this.currentFilteredTransaction = transaction;

        visitFilteredTransaction(transaction);
    }
}
