package com.le.sunriise.md;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class ExportAccountsToMd {
    private static final Logger log = Logger.getLogger(ExportAccountsToMd.class);

    private void export(File dbFile, String password, File outFile) throws IOException {
        OpenedDb openedDb = null;
        PrintWriter writer = null;
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            Database db = openedDb.getDb();

            List<Account> accounts = AccountUtil.getAccounts(db);
            log.info("Accounts=" + accounts.size());

            readTransactions(db, accounts);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } finally {
                    writer = null;
                }
            }
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
        }

    }

    private void readTransactions(Database db, List<Account> accounts) throws IOException {
        int count = 0;
        int parentTransactionCount = 0;
        int splitTransactionCount = 0;
        List<Transaction> transactions = null;
        int nTransactions = 0;
        int nSplitTransactions = 0;
        for (Account account : accounts) {
            transactions = AccountUtil.getTransactions(db, account);
            account.setTransactions(transactions);

            nTransactions = transactions.size();
            nSplitTransactions = getSplitTransactionCount(transactions);

            log.info(count + ", " + account.getName() + ", " + account.isClosed() + ", " + account.getStartingBalance() + ", "
                    + calculateCurrentBalance(account) + ", " + nTransactions);

            parentTransactionCount += nTransactions;
            splitTransactionCount += nSplitTransactions;

            count++;
        }
        log.info("ParentTransactionCount: " + parentTransactionCount);
        log.info("SplitTransactionCount: " + splitTransactionCount);

        // transaction with no account
        Account account = null;
        transactions = AccountUtil.getTransactions(db, account);
        nTransactions = transactions.size();

        nSplitTransactions = getSplitTransactionCount(transactions);

        log.info("ParentTransactionCount (no account): " + nTransactions);
        log.info("SplitTransactionCount (no account): " + nSplitTransactions);

        log.info("Total Transaction: " + ((parentTransactionCount + splitTransactionCount) + (nTransactions + nSplitTransactions)));
    }

    public BigDecimal calculateCurrentBalance(Account account) {
        BigDecimal currentBalane = account.getStartingBalance();
        if (currentBalane == null) {
            log.warn("Starting balance is null. Set to 0. Account's id=" + account.getId());
            currentBalane = new BigDecimal(0.00);
        }
        for (Transaction transaction : account.getTransactions()) {
            if (transaction.isVoid()) {
                continue;
            }
            if (transaction.isRecurring()) {
                continue;
            }
            BigDecimal amount = transaction.getAmount();
            if (amount != null) {
                currentBalane = currentBalane.add(amount);
            } else {
                log.warn("Transaction with no amount, id=" + transaction.getId());
            }
        }
        return currentBalane;
    }

    private int getSplitTransactionCount(List<Transaction> transactions) {
        int count = 0;
        for (Transaction transaction : transactions) {
            List<TransactionSplit> splits = transaction.getSplits();
            if (splits == null) {
                continue;
            }
            count += splits.size();
        }
        return count;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;
        File outFile = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            outFile = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            password = args[1];
            outFile = new File(args[2]);
        } else {
            Class<ExportAccountsToMd> clz = ExportAccountsToMd.class;
            System.out.println("Usage: " + clz.getName() + " file.mny [passsword] out.xmd");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        ExportAccountsToMd exporter = new ExportAccountsToMd();
        try {
            exporter.export(dbFile, password, outFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }
}
