package com.le.sunriise.md;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
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

            List<Account> accounts = readAccounts(db);
            Comparator<Account> comparator = new Comparator<Account>() {
                public int compare(Account o1, Account o2) {
                    return o1.getName().compareTo(o2.getName());
                }
            };
            Collections.sort(accounts, comparator);
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
            transactions = getTransactions(db, account);
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
        transactions = getTransactions(db, account);
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

    public List<Transaction> getTransactions(Database db, Account account) throws IOException {
        List<Transaction> transactions = new ArrayList<Transaction>();
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
                        addTransaction(transactions, row);
                    }
                } else {
                    row = cursor.getCurrentRow();
                    Integer hacct = (Integer) row.get("hacct");
                    if (hacct == null) {
                        addTransaction(transactions, row);
                    }
                }
            }
            handleSplit(db, transactions);
        } finally {

        }

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
        Collections.sort(transactions, comparator);

        return transactions;
    }

    private void addTransaction(List<Transaction> transactions, Map<String, Object> row) {
        Integer htrn = (Integer) row.get("htrn");

        BigDecimal amt = (BigDecimal) row.get("amt");

        // Integer cs = (Integer) row.get("cs");

        Integer grftt = (Integer) row.get("grftt");

        Date date = (Date) row.get("dt");
        
        Integer frq = (Integer) row.get("frq");

        Transaction transaction = new Transaction();
        transaction.setId(htrn);
        transaction.setDate(date);
        transaction.setAmount(amt);
        transaction.setStatusFlag(grftt);
        transaction.setFrequency(frq);

        transactions.add(transaction);
    }

    private void handleSplit(Database db, List<Transaction> transactions) throws IOException {
        String splitTableName = "TRN_SPLIT";
        Table splitTable = db.getTable(splitTableName);
        Cursor splitCursor = Cursor.createIndexCursor(splitTable, splitTable.getIndex("htrnTrnSplit"));

        Map<Integer, List<TransactionSplit>> splits = new HashMap<Integer, List<TransactionSplit>>();
        ListIterator<Transaction> listIterator = transactions.listIterator();
        while (listIterator.hasNext()) {
            Transaction transaction = listIterator.next();
            TransactionSplit transactionSplit = null;
            transactionSplit = getTransactionSplit(splitCursor, transaction);

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

    private TransactionSplit getTransactionSplit(Cursor splitCursor, Transaction transaction) throws IOException {
        splitCursor.reset();
        TransactionSplit transactionSplit = null;
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("htrn", transaction.getId());
        if (splitCursor.findRow(rowPattern)) {
            Map<String, Object> row = splitCursor.getCurrentRow();
            Integer htrnParent = (Integer) row.get("htrnParent");
            Integer iSplit = (Integer) row.get("iSplit");

            transactionSplit = new TransactionSplit();
            transactionSplit.setTransaction(transaction);
            transactionSplit.setParentId(htrnParent);
            transactionSplit.setRowId(iSplit);
        }
        return transactionSplit;
    }

    public List<Account> readAccounts(Database db) throws IOException {
        List<Account> accounts = new ArrayList<Account>();

        String tableName = "ACCT";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                String name = (String) row.get("szFull");
                if (name == null) {
                    continue;
                }
                if (name.length() == 0) {
                    continue;
                }

                Integer type = (Integer) row.get("at");

                Integer hacct = (Integer) row.get("hacct");

                Integer hacctRel = (Integer) row.get("hacctRel");

                boolean closed = (Boolean) row.get("fClosed");

                BigDecimal amtOpen = (BigDecimal) row.get("amtOpen");

                Account account = new Account();
                account.setId(hacct);
                account.setRelatedToAccountId(hacctRel);
                account.setName(name);
                account.setType(type);
                account.setClosed(closed);
                account.setStartingBalance(amtOpen);

                accounts.add(account);
            }
        } finally {
        }

        return accounts;
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
