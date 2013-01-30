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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.StopWatch;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.AccountType;
import com.le.sunriise.mnyobject.Category;
import com.le.sunriise.mnyobject.Currency;
import com.le.sunriise.mnyobject.Payee;
import com.le.sunriise.mnyobject.Security;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionSplit;
import com.le.sunriise.mnyobject.impl.AccountImplUtil;
import com.le.sunriise.mnyobject.impl.CategoryImplUtil;
import com.le.sunriise.mnyobject.impl.CurrencyImplUtil;
import com.le.sunriise.mnyobject.impl.MnyObjectUtil;
import com.le.sunriise.mnyobject.impl.PayeeImplUtil;
import com.le.sunriise.mnyobject.impl.SecurityImplUtil;
import com.le.sunriise.mnyobject.impl.TransactionImplUtil;
import com.le.sunriise.mnyobject.impl.TransactionSplitImplUtil;
import com.le.sunriise.viewer.OpenedDb;

public class AccountUtil {
    private static final Logger log = Logger.getLogger(AccountUtil.class);

    /**
     * Get a list of accounts.
     * 
     * @param db
     * @param sort
     * @return list of account
     * @throws IOException
     */
    public static List<Account> getAccounts(Database db, boolean sort) throws IOException {
        List<Account> accounts = new ArrayList<Account>();

        String tableName = "ACCT";
        Table table = db.getTable(tableName);
        Cursor cursor = Cursor.createCursor(table);
        while (cursor.moveToNextRow()) {
            Map<String, Object> row = cursor.getCurrentRow();
            Account account = AccountImplUtil.getAcccount(row);
            if (account != null) {
                accounts.add(account);
            }
        }

        if (sort) {
            Comparator<Account> comparator = new Comparator<Account>() {
                @Override
                public int compare(Account o1, Account o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            Collections.sort(accounts, comparator);
        }

        return accounts;
    }

    /**
     * 
     * @param db
     * @return
     * @throws IOException
     */
    public static List<Account> getAccounts(Database db) throws IOException {
        boolean sort = true;
        return getAccounts(db, sort);
    }

    public static List<Transaction> retrieveTransactions(Database db, Account account) throws IOException {
        List<TransactionFilter> transactionFilters = null;
        transactionFilters = getTransactionFilters();

        return retrieveTransactions(db, account, transactionFilters);
    }

    public static List<Transaction> retrieveTransactions(final Database db, final Account account, final List<TransactionFilter> transactionFilters)
            throws IOException {
        log.info("> getTransactions, account=" + account.getName());

        StopWatch stopWatch = new StopWatch();
        List<Transaction> transactions = new ArrayList<Transaction>();
        List<Transaction> filteredTransactions = new ArrayList<Transaction>();

        String tableName = "TRN";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);
            Map<String, Object> rowPattern = new HashMap<String, Object>();
            if (account != null) {
                rowPattern.put("hacct", account.getId());
            }
            Map<String, Object> row = null;
            while (cursor.moveToNextRow()) {
                if (account != null) {
                    if (cursor.currentRowMatches(rowPattern)) {
                        row = cursor.getCurrentRow();
                        TransactionImplUtil.addTransactionFromRow(db, account.getId(), transactionFilters, row, transactions, filteredTransactions);
                    }
                } else {
                    row = cursor.getCurrentRow();
                    Integer hacct = (Integer) row.get("hacct");
                    if (hacct == null) {
                        TransactionImplUtil.addTransactionFromRow(db, hacct, transactionFilters, row, transactions, filteredTransactions);
                    }
                }
            }
            AccountUtil.handleSplit(db, transactions);

            boolean sort = true;
            Comparator<Transaction> comparator = new Comparator<Transaction>() {
                @Override
                public int compare(Transaction o1, Transaction o2) {
                    Date d1 = o1.getDate();
                    Date d2 = o2.getDate();

                    if ((d1 == null) && (d2 == null)) {
                        return 0;
                    }

                    if (d1 == null) {
                        return 1;
                    }

                    if (d2 == null) {
                        return -1;
                    }

                    return d1.compareTo(d2);
                }

            };
            if (sort) {
                if (log.isDebugEnabled()) {
                    log.debug("> sort");
                }
                try {
                    Collections.sort(transactions, comparator);
                } finally {
                    if (log.isDebugEnabled()) {
                        log.debug("< sort");
                    }
                }
            }
        } finally {
            long delta = stopWatch.click();
            int count = 0;
            if (transactions != null) {
                count = transactions.size();
            }
            if (count <= 0) {
                log.info("< getTransactions, delta=" + delta);
            } else {
                log.info("< getTransactions, delta=" + delta + ", " + ((delta * 1.0) / count) + " per txn");
            }

        }

        account.setTransactions(transactions);
        account.setFilteredTransactions(filteredTransactions);

        return transactions;
    }

    private static List<TransactionFilter> getTransactionFilters() {
        List<TransactionFilter> transactionFilters;
        transactionFilters = new ArrayList<TransactionFilter>();
        transactionFilters.add(new RecurringTransactionFilter());

        // http://jythonpodcast.hostjava.net/jythonbook/chapter10.html
        // transactionFilters =
        // JythonTransactionFilters.getFilters("com.le.sunriise.python");

        return transactionFilters;
    }

    private static void handleSplit(Database db, List<Transaction> transactions) throws IOException {
        String splitTableName = "TRN_SPLIT";
        Table splitTable = db.getTable(splitTableName);
        Cursor cursor = Cursor.createIndexCursor(splitTable, splitTable.getIndex("htrnTrnSplit"));
        // Cursor cursor = Cursor.createCursor(splitTable);

        Map<Integer, List<TransactionSplit>> splits = new HashMap<Integer, List<TransactionSplit>>();
        ListIterator<Transaction> listIterator = transactions.listIterator();
        while (listIterator.hasNext()) {
            Transaction transaction = listIterator.next();
            TransactionSplit transactionSplit = null;
            transactionSplit = TransactionSplitImplUtil.getTransactionSplit(cursor, transaction);

            if (transactionSplit != null) {
                List<TransactionSplit> list = splits.get(transactionSplit.getParentId());
                if (list == null) {
                    list = new ArrayList<TransactionSplit>();
                    splits.put(transactionSplit.getParentId(), list);
                }
                list.add(transactionSplit);

                listIterator.remove();
            }
        }

        for (Transaction transaction : transactions) {
            List<TransactionSplit> list = splits.get(transaction.getId());
            if (list == null) {
                continue;
            }
            if (list.size() <= 0) {
                continue;
            }

            transaction.setSplits(list);
        }
    }

    public static BigDecimal getRunningBalance(int rowIndex, Account account) {
        List<Transaction> transactions = account.getTransactions();
        Transaction transaction = transactions.get(rowIndex);
        BigDecimal runningBalance = transaction.getRunningBalance();
        if (runningBalance != null) {
            return runningBalance;
        }

        BigDecimal previousBalance = null;
        if (rowIndex == 0) {
            previousBalance = account.getStartingBalance();
        } else {
            int previousRowIndex = rowIndex - 1;
            previousBalance = getRunningBalance(previousRowIndex, account);
        }
        if (previousBalance == null) {
            previousBalance = new BigDecimal(0);
        }

        BigDecimal currentBalance = null;
        if (transaction.isVoid() || transaction.isRecurring()) {
            currentBalance = new BigDecimal(0);
        } else {
            currentBalance = transaction.getAmount();
        }
        if (currentBalance == null) {
            currentBalance = new BigDecimal(0);
        }

        runningBalance = previousBalance.add(currentBalance);
        transaction.setRunningBalance(runningBalance);

        return runningBalance;
    }

    public static void setCurrencies(List<Account> accounts, Map<Integer, Currency> currencies) {
        for (Account account : accounts) {
            Integer currencyId = account.getCurrencyId();
            if (currencyId == null) {
                continue;
            }
            Currency currency = currencies.get(currencyId);
            if (currency == null) {
                continue;
            }
            String currencyCode = currency.getIsoCode();
            if (currencyCode == null) {
                continue;
            }
            account.setCurrencyCode(currencyCode);
        }
    }

    public static BigDecimal calculateCurrentBalance(Account relatedToAccount) {
        Date date = null;
        return calculateNonInvestmentBalance(relatedToAccount, date);
    }

    public static BigDecimal calculateNonInvestmentBalance(Account account, Date date) {
        BigDecimal currentBalance = account.getStartingBalance();
        if (currentBalance == null) {
            log.warn("Starting balance is null. Set to 0. Account's id=" + account.getId());
            currentBalance = new BigDecimal(0.00);
        }
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.isVoid()) {
                continue;
            }
            if (transaction.isRecurring()) {
                continue;
            }
            if (date != null) {
                Date transactionDate = transaction.getDate();
                if (transactionDate.compareTo(date) > 0) {
                    continue;
                }
            }
            BigDecimal amount = transaction.getAmount();
            if (amount != null) {
                currentBalance = currentBalance.add(amount);
            } else {
                log.warn("Transaction with no amount, id=" + transaction.getId());
            }
        }
        account.setCurrentBalance(currentBalance);
        return currentBalance;
    }

    public static Double calculateInvestmentBalance(Account account, MnyContext mnyContext) {
        Date date = null;
        return MnyObjectUtil.calculateInvestmentBalance(account, date, mnyContext);
    }

    public static Double getCashAccountValue(Account account, MnyContext mnyContext) throws IOException {
        Double cashAccountValue = null;
        Integer relatedToAccountId = account.getRelatedToAccountId();
        if (relatedToAccountId != null) {
            Account relatedToAccount = getAccount(relatedToAccountId, mnyContext);
            account.setRelatedToAccount(relatedToAccount);
            retrieveTransactions(mnyContext.getDb(), relatedToAccount);
            if (relatedToAccount != null) {
                BigDecimal currentBalance = calculateCurrentBalance(relatedToAccount);
                if (currentBalance != null) {
                    cashAccountValue = currentBalance.doubleValue();
                } else {
                    cashAccountValue = new Double(0.0);
                }
            }
        }
        return cashAccountValue;
    }

    private static Account getAccount(Integer relatedToAccountId, MnyContext mnyContext) throws IOException {
        Account relatedToAccount = null;
        Database db = mnyContext.getDb();

        String tableName = "ACCT";
        Table table = db.getTable(tableName);
        Cursor cursor = Cursor.createCursor(table);
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("hacct", relatedToAccountId);
        if (cursor.findFirstRow(rowPattern)) {
            Map<String, Object> row = cursor.getCurrentRow();
            relatedToAccount = AccountImplUtil.getAcccount(row);
        }
        return relatedToAccount;
    }

    public static Double getSecurityLatestPrice(Integer securityId, Date date, MnyContext mnyContext) throws IOException {
        Double price = null;

        StopWatch stopWatch = new StopWatch();
        try {
            Database db = mnyContext.getDb();
            Table table = db.getTable("SP");
            Cursor cursor = Cursor.createCursor(table);
            // XXX: assuming that row is already sorted in increasing date order
            // we will start from end
            cursor.afterLast();
            while (cursor.moveToPreviousRow()) {
                Map<String, Object> row = cursor.getCurrentRow();
                Integer hsec = (Integer) row.get("hsec");
                if (hsec.compareTo(securityId) != 0) {
                    continue;
                }
                if (date != null) {
                    Date priceDate = (Date) row.get("dt");
                    if (priceDate != null) {
                        if (priceDate.compareTo(date) > 0) {
                            continue;
                        }
                    }
                }
                price = (Double) row.get("dPrice");
                break;
            }
        } finally {
            long delta = stopWatch.click();

            log.info("< getSecurityLatestPrice, securityId=" + securityId + ", delta=" + delta);
        }
        return price;
    }

    public static MnyContext createMnyContext(OpenedDb openedDb) throws IOException {
        MnyContext mnyContext = new MnyContext();
        initMnyContext(openedDb, mnyContext);
        return mnyContext;
    }

    public static List<Account> initMnyContext(OpenedDb openedDb, MnyContext mnyContext) throws IOException {
        Database db = openedDb.getDb();
        mnyContext.setDb(db);

        Map<Integer, Payee> payees = PayeeImplUtil.getPayees(db);
        mnyContext.setPayees(payees);

        Map<Integer, Category> categories = CategoryImplUtil.getCategories(db);
        mnyContext.setCategories(categories);

        Map<Integer, Currency> currencies = CurrencyImplUtil.getCurrencies(db);
        mnyContext.setCurrencies(currencies);

        Map<Integer, Security> securities = SecurityImplUtil.getSecurities(db);
        mnyContext.setSecurities(securities);

        List<Account> accounts = getAccounts(db);
        mnyContext.setAccounts(accounts);

        setCurrencies(accounts, currencies);

        return accounts;
    }

    public static String getSecurityName(Integer securityId, MnyContext mnyContext) {
        String securityName = null;
        if (securityId != null) {
            Map<Integer, Security> securities = mnyContext.getSecurities();
            Security security = securities.get(securityId);
            if (security != null) {
                securityName = security.getName();
            } else {
                securityName = securityId.toString();
            }
        }
        return securityName;
    }

    public static BigDecimal calculateBalance(Account account, Date date, MnyContext mnyContext) {
        BigDecimal currentBalance = null;
        if (account.getAccountType() == AccountType.INVESTMENT) {
            Double investmentBalance = MnyObjectUtil.calculateInvestmentBalance(account, date, mnyContext);
            currentBalance = new BigDecimal(investmentBalance);
        } else {
            currentBalance = calculateNonInvestmentBalance(account, date);
        }
        return currentBalance;
    }

    public static String getCurrencyName(Integer currencyId, Map<Integer, Currency> currencies) {
        Currency currency = currencies.get(currencyId);
        if (currency != null) {
            return currency.getIsoCode();
        } else {
            return null;
        }
    }
}
