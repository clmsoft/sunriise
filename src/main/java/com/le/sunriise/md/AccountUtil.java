package com.le.sunriise.md;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;

public class AccountUtil {

    public static List<Account> getAccounts(Database db, boolean sort) throws IOException {
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
            if (sort) {
                Comparator<Account> comparator = new Comparator<Account>() {
                    @Override
                    public int compare(Account o1, Account o2) {
                        return o1.getName().compareTo(o2.getName());
                    }
                };
                Collections.sort(accounts, comparator);
            }
        } finally {
        }

        return accounts;
    }

    public static List<Account> getAccounts(Database db) throws IOException {
        boolean sort = true;
        return getAccounts(db, sort);
    }

    public static List<Transaction> getTransactions(Database db, Account account) throws IOException {
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
                        AccountUtil.addTransaction(transactions, row);
                    }
                } else {
                    row = cursor.getCurrentRow();
                    Integer hacct = (Integer) row.get("hacct");
                    if (hacct == null) {
                        AccountUtil.addTransaction(transactions, row);
                    }
                }
            }
            AccountUtil.handleSplit(db, transactions);

            boolean filterRecurring = true;
            if (filterRecurring) {
                filterRecurring(transactions);
            }
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

    private static void filterRecurring(List<Transaction> transactions) {
        ListIterator<Transaction> listIterator = transactions.listIterator();
        while (listIterator.hasNext()) {
            Transaction transaction = listIterator.next();
            if (transaction.isRecurring()) {
                listIterator.remove();
            }
        }
    }

    private static void addTransaction(List<Transaction> transactions, Map<String, Object> row) {
        // transaction id
        Integer htrn = (Integer) row.get("htrn");

        // amount
        BigDecimal amt = (BigDecimal) row.get("amt");

        // Integer cs = (Integer) row.get("cs");

        // flags? we are currentl using this to figure out which transaction to
        // skip/void
        Integer grftt = (Integer) row.get("grftt");

        // date
        Date date = (Date) row.get("dt");

        // frequency for recurring transaction?
        Integer frq = (Integer) row.get("frq");

        // category
        Integer hcat = (Integer) row.get("hcat");

        // payee
        Integer lhpay = (Integer) row.get("lHpay");

        // transfer to account
        Integer hacctLink = (Integer) row.get("hacctLink");

        Transaction transaction = new Transaction();
        transaction.setId(htrn);

        transaction.setDate(date);

        transaction.setAmount(amt);

        transaction.setStatusFlag(grftt);

        transaction.setFrequency(frq);

        transaction.setCategory(hcat);

        transaction.setPayee(lhpay);

        transaction.setTransferredAccount(hacctLink);

        transactions.add(transaction);
    }

    private static void handleSplit(Database db, List<Transaction> transactions) throws IOException {
        String splitTableName = "TRN_SPLIT";
        Table splitTable = db.getTable(splitTableName);
        Cursor splitCursor = Cursor.createIndexCursor(splitTable, splitTable.getIndex("htrnTrnSplit"));

        Map<Integer, List<TransactionSplit>> splits = new HashMap<Integer, List<TransactionSplit>>();
        ListIterator<Transaction> listIterator = transactions.listIterator();
        while (listIterator.hasNext()) {
            Transaction transaction = listIterator.next();
            TransactionSplit transactionSplit = null;
            transactionSplit = AccountUtil.getTransactionSplit(splitCursor, transaction);

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

    private static TransactionSplit getTransactionSplit(Cursor splitCursor, Transaction transaction) throws IOException {
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

    public static Map<Integer, Payee> getPayees(Database db) throws IOException {
        Map<Integer, Payee> payees = new HashMap<Integer, Payee>();

        String tableName = "PAY";
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

                Integer hpay = (Integer) row.get("hpay");
                Integer hpayParent = (Integer) row.get("hpayParent");

                Payee payee = new Payee();
                payee.setId(hpay);
                payee.setParent(hpayParent);
                payee.setName(name);

                payees.put(hpay, payee);
            }
        } finally {

        }
        return payees;
    }

    public static Map<Integer, Category> getCategories(Database db) throws IOException {
        Map<Integer, Category> categories = new HashMap<Integer, Category>();

        String tableName = "CAT";
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

                Integer hcat = (Integer) row.get("hcat");
                Integer hcatParent = (Integer) row.get("hcatParent");

                Category category = new Category();
                category.setId(hcat);
                category.setParent(hcatParent);
                category.setName(name);

                categories.put(hcat, category);
            }
        } finally {

        }

        return categories;
    }
}
