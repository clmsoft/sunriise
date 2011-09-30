package com.le.sunriise.md;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexCursor;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.StopWatch;

public class AccountUtil {
    private static final Logger log = Logger.getLogger(AccountUtil.class);

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

                Integer currencyId = (Integer) row.get("hcrnc");

                Account account = new Account();
                account.setId(hacct);
                account.setRelatedToAccountId(hacctRel);
                account.setName(name);
                account.setType(type);
                account.setClosed(closed);
                account.setStartingBalance(amtOpen);
                account.setCurrencyId(currencyId);

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

    public static List<Transaction> getTransactionsXXX(Database db, Account account) throws IOException {
        if (account == null) {
            return getTransactions(db, account);
        }

        StopWatch stopWatch = new StopWatch();

        log.info("> getTransactions, account=" + account.getName());
        List<Transaction> transactions = new ArrayList<Transaction>();
        String tableName = "TRN";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            Index index = null;
            index = table.getIndex("hacctTrn");
            IndexCursor indexCursor = IndexCursor.createCursor(table, index);
            for (Map<String, Object> row : indexCursor.entryIterable(Arrays.asList("hacct"), account.getId())) {
                if (log.isDebugEnabled()) {
                    log.info(row);
                }
                List<TransactionFilter> transactionFilters = null;

                AccountUtil.addTransaction(db, transactions, row, transactionFilters);
            }
            AccountUtil.handleSplit(db, transactions);

            boolean filterRecurring = true;
            if (filterRecurring) {
                filterRecurring(transactions);
            }
        } finally {
            long delta = stopWatch.click();
            log.info("< getTransactions, delta=" + delta);
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

    public static List<Transaction> getTransactions(Database db, Account account) throws IOException {
        StopWatch stopWatch = new StopWatch();

        log.info("> getTransactions, account=" + account.getName());
        List<TransactionFilter> transactionFilters = null;

        transactionFilters = getTransactionFilters();

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
                        AccountUtil.addTransaction(db, transactions, row, transactionFilters);
                    }
                } else {
                    row = cursor.getCurrentRow();
                    Integer hacct = (Integer) row.get("hacct");
                    if (hacct == null) {
                        AccountUtil.addTransaction(db, transactions, row, transactionFilters);
                    }
                }
            }
            AccountUtil.handleSplit(db, transactions);

            // boolean filterRecurring = true;
            // if (filterRecurring) {
            // filterRecurring(transactions);
            // }

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
            log.info("< getTransactions, delta=" + delta);
        }

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

    private static void filterRecurring(List<Transaction> transactions) {
        ListIterator<Transaction> listIterator = transactions.listIterator();
        while (listIterator.hasNext()) {
            Transaction transaction = listIterator.next();
            if (transaction.isRecurring()) {
                listIterator.remove();
            }
        }
    }

    private static boolean addTransaction(Database db, List<Transaction> transactions, Map<String, Object> row, List<TransactionFilter> filters)
            throws IOException {
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
        Double cFrqInst = (Double) row.get("cFrqInst");

        // category
        Integer hcat = (Integer) row.get("hcat");

        // payee
        Integer lhpay = (Integer) row.get("lHpay");

        // transfer to account
        Integer hacctLink = (Integer) row.get("hacctLink");

        // hsec: security
        Integer hsec = (Integer) row.get("hsec");

        // act: Investment activity: Buy, Sell ..
        Integer act = (Integer) row.get("act");

        Transaction transaction = new Transaction();
        transaction.setId(htrn);

        transaction.setDate(date);

        transaction.setAmount(amt);

        transaction.setStatusFlag(grftt);

        if (grftt != null) {
            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.setFlag(grftt);
            transaction.setTransactionInfo(transactionInfo);
        }

        Frequency frequency = new Frequency();
        frequency.setFrq(frq);
        frequency.setcFrqInst(cFrqInst);
        transaction.setFrequency(frequency);

        transaction.setCategoryId(hcat);

        transaction.setPayeeId(lhpay);

        transaction.setTransferredAccountId(hacctLink);

        transaction.setSecurityId(hsec);

        InvestmentActivity investmentActivity = new InvestmentActivity(act);
        transaction.setInvestmentActivity(investmentActivity);

        InvestmentTransaction investmentTransaction = null;
        if (transaction.isInvestment()) {
            investmentTransaction = getInvestmentTransaction(db, transaction.getId());
        }
        transaction.setInvestmentTransaction(investmentTransaction);

        boolean accept = true;
        if (filters != null) {
            for (TransactionFilter filter : filters) {
                if (!filter.accept(transaction, row)) {
                    accept = false;
                    break;
                }
            }
        }

        if (accept) {
            transactions.add(transaction);
        }
        return accept;
    }

    private static InvestmentTransaction getInvestmentTransaction(Database db, Integer id) throws IOException {
        InvestmentTransaction investmentTransaction = new InvestmentTransaction();

        String splitTableName = "TRN_INV";
        Table splitTable = db.getTable(splitTableName);
        Cursor cursor = Cursor.createCursor(splitTable);
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("htrn", id);
        if (cursor.findRow(rowPattern)) {
            Map<String, Object> row = cursor.getCurrentRow();
            Double price = (Double) row.get("dPrice");
            investmentTransaction.setPrice(price);

            Double quantity = (Double) row.get("qty");
            investmentTransaction.setQuantity(quantity);
        }

        return investmentTransaction;
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
            transactionSplit = AccountUtil.getTransactionSplit(cursor, transaction);

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
            transactionSplit.setTransactionId(transaction);
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

    public static Map<Integer, Security> getSecurities(Database db) throws IOException {
        Map<Integer, Security> securities = new HashMap<Integer, Security>();

        String tableName = "SEC";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                Integer hsec = (Integer) row.get("hsec");

                String szFull = (String) row.get("szFull");
                String szSymbol = (String) row.get("szSymbol");

                Security security = new Security();
                security.setId(hsec);
                security.setName(szFull);
                security.setSymbol(szSymbol);

                securities.put(hsec, security);
            }
        } finally {

        }

        return securities;
    }

    public static Map<Integer, Currency> getCurrencies(Database db) throws IOException {
        Map<Integer, Currency> currencies = new HashMap<Integer, Currency>();

        String tableName = "CRNC";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                String name = (String) row.get("szName");
                if (name == null) {
                    continue;
                }
                if (name.length() == 0) {
                    continue;
                }

                Integer hcrnc = (Integer) row.get("hcrnc");
                String isoCode = (String) row.get("szIsoCode");

                Currency currency = new Currency();
                currency.setId(hcrnc);
                currency.setName(name);
                currency.setIsoCode(isoCode);

                currencies.put(hcrnc, currency);
            }
        } finally {

        }

        return currencies;
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
                Integer hct = (Integer) row.get("hct");

                Category category = new Category();
                category.setId(hcat);
                category.setParentId(hcatParent);
                category.setName(name);
                category.setClassificationId(hct);

                categories.put(hcat, category);
            }
        } finally {

        }

        return categories;
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

    public static BigDecimal calculateCurrentBalance(Account account) {
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

    public static Double calculateInvestmentBalance(Account account, MnyContext mnyContext) {
        Map<Integer, Double> quantities = new HashMap<Integer, Double>();
        Double accountMarketValue = new Double(0.0);

        for (Transaction transaction : account.getTransactions()) {
            if (transaction.isVoid()) {
                continue;
            }
            if (transaction.isRecurring()) {
                continue;
            }
            if (!transaction.isInvestment()) {
                continue;
            }
            InvestmentTransaction investmentTransaction = transaction.getInvestmentTransaction();
            if (investmentTransaction == null) {
                log.warn("Transaction is an investment transaction but investmentTransaction is null");
                continue;
            }
            Integer securityId = transaction.getSecurityId();
            InvestmentActivity investmentActivity = transaction.getInvestmentActivity();
            Double quantity = quantities.get(securityId);
            if (quantity == null) {
                quantity = new Double(0.0);
                quantities.put(securityId, quantity);
            }
            Double q = investmentTransaction.getQuantity();
            if (q == null) {
                q = new Double(0.0);
            }
            if (investmentActivity.isAdded()) {
                quantity += q;
            } else {
                quantity -= q;
            }
            quantities.put(securityId, quantity);
        }

        TreeMap<String, Double> sortedByName = new TreeMap<String, Double>(new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                return o1.compareTo(o2);
            }
        });
        Map<String, Double> prices = new HashMap<String, Double>();

        for (Integer securityId : quantities.keySet()) {
            Double quantity = quantities.get(securityId);
            Map<Integer, Security> securities = mnyContext.getSecurities();
            Security security = securities.get(securityId);
            String securityName = null;
            if (security != null) {
                securityName = security.getName();
            } else {
                securityName = securityId.toString();
            }
            sortedByName.put(securityName, quantity);

            try {
                Double price = getSecurityLatestPrice(securityId, mnyContext);
                if (price == null) {
                    price = new Double(0.0);
                }
                prices.put(securityName, price);
            } catch (IOException e) {
                log.warn("Cannot find latest price for securityId=" + securityId, e);
            }
        }
        for (String name : sortedByName.keySet()) {
            Double quantity = sortedByName.get(name);
            if (quantity > 0.00000001) {
                Double price = prices.get(name);
                if (price == null) {
                    price = new Double(0.0);
                }
                Double value = price * quantity;
                accountMarketValue += value;
                log.info("securityName=" + name + ", quantity=" + quantity + ", price=" + price + ", value=" + value);
            }
        }
        
        return accountMarketValue;
    }

    private static Double getSecurityLatestPrice(Integer securityId, MnyContext mnyContext) throws IOException {
        Double price = null;
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
            price = (Double) row.get("dPrice");
            return price;
        }
        return price;
    }
}
