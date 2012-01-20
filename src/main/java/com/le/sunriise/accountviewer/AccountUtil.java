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
            Account account = getAcccount(row);
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

    private static Account getAcccount(Map<String, Object> row) {
        Account account = null;
        String name = (String) row.get("szFull");
        if (name == null) {
            return account;
        }
        if (name.length() == 0) {
            return account;
        }

        account = new Account();

        account.setName(name);

        Integer hacct = (Integer) row.get("hacct");
        account.setId(hacct);

        Integer hacctRel = (Integer) row.get("hacctRel");
        account.setRelatedToAccountId(hacctRel);

        Integer type = (Integer) row.get("at");
        account.setType(type);

        Boolean closed = (Boolean) row.get("fClosed");
        account.setClosed(closed);

        BigDecimal amtOpen = (BigDecimal) row.get("amtOpen");
        account.setStartingBalance(amtOpen);

        Integer currencyId = (Integer) row.get("hcrnc");
        account.setCurrencyId(currencyId);

        Boolean retirement = (Boolean) row.get("fRetirement");
        account.setRetirement(retirement);

        // 0: 403(b)
        // 1: 401k
        // 2: IRA
        // 3: Keogh
        Integer investmentSubType = (Integer) row.get("uat");
        account.setInvestmentSubType(investmentSubType);

        // amtLimit
        BigDecimal amountLimit = (BigDecimal) row.get("amtLimit");
        account.setAmountLimit(amountLimit);

        return account;
    }

    public static List<Account> getAccounts(Database db) throws IOException {
        boolean sort = true;
        return getAccounts(db, sort);
    }

    public static List<Transaction> retrieveTransactions(Database db, Account account) throws IOException {
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
                        AccountUtil.addTransactionFromRow(db, transactionFilters, row, transactions);
                    }
                } else {
                    row = cursor.getCurrentRow();
                    Integer hacct = (Integer) row.get("hacct");
                    if (hacct == null) {
                        AccountUtil.addTransactionFromRow(db, transactionFilters, row, transactions);
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
                log.info("< getTransactions, delta=" + delta + ", " + (delta * 1.0) / count);
            }

        }

        account.setTransactions(transactions);

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

    private static boolean addTransactionFromRow(Database db, List<TransactionFilter> filters, Map<String, Object> row,
            List<Transaction> transactions) throws IOException {
        Transaction transaction = new Transaction();

        // transaction id
        Integer htrn = (Integer) row.get("htrn");
        transaction.setId(htrn);

        // amount
        BigDecimal amt = (BigDecimal) row.get("amt");
        transaction.setAmount(amt);

        // TableID index ColumnName comments
        // TRN 7 cs "cleared state?
        // 0 == not cleared
        // 1 == cleared
        // 2 == reconciled

        Integer cs = (Integer) row.get("cs");
        transaction.setClearedState(cs);

        // flags? we are currentl using this to figure out which transaction to
        // skip/void
        Integer grftt = (Integer) row.get("grftt");
        transaction.setStatusFlag(grftt);
        if (grftt != null) {
            TransactionInfo transactionInfo = new TransactionInfo();
            transactionInfo.setFlag(grftt);
            transaction.setTransactionInfo(transactionInfo);
        }

        // date
        Date date = (Date) row.get("dt");
        transaction.setDate(date);

        // frequency for recurring transaction?
        Integer frq = (Integer) row.get("frq");
        Double cFrqInst = (Double) row.get("cFrqInst");
        Frequency frequency = new Frequency();
        frequency.setFrq(frq);
        frequency.setcFrqInst(cFrqInst);
        transaction.setFrequency(frequency);

        // category
        Integer hcat = (Integer) row.get("hcat");
        transaction.setCategoryId(hcat);

        // payee
        Integer lhpay = (Integer) row.get("lHpay");
        transaction.setPayeeId(lhpay);

        // transfer to account
        Integer hacctLink = (Integer) row.get("hacctLink");
        transaction.setTransferredAccountId(hacctLink);

        // hsec: security
        Integer hsec = (Integer) row.get("hsec");
        transaction.setSecurityId(hsec);

        // act: Investment activity: Buy, Sell ..
        Integer act = (Integer) row.get("act");
        InvestmentActivity investmentActivity = new InvestmentActivity(act);
        transaction.setInvestmentActivity(investmentActivity);

        InvestmentTransaction investmentTransaction = null;
        if (transaction.isInvestment()) {
            investmentTransaction = getInvestmentTransaction(db, transaction.getId());
        }
        transaction.setInvestmentTransaction(investmentTransaction);

        // mMemo
        String memo = (String) row.get("mMemo");
        transaction.setMemo(memo);

        // szId
        String szId = (String) row.get("szId");
        transaction.setNumber(szId);

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
        if (cursor.findFirstRow(rowPattern)) {
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
        if (splitCursor.findFirstRow(rowPattern)) {
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

                Category category = new Category();

                Integer hcat = (Integer) row.get("hcat");
                category.setId(hcat);

                Integer hcatParent = (Integer) row.get("hcatParent");
                category.setParentId(hcatParent);

                category.setName(name);

                Integer hct = (Integer) row.get("hct");
                category.setClassificationId(hct);

                Integer nLevel = (Integer) row.get("nLevel");
                category.setLevel(nLevel);

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
        return calculateInvestmentBalance(account, date, mnyContext);
    }

    public static Double calculateInvestmentBalance(Account account, Date date, MnyContext mnyContext) {
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
            if (date != null) {
                Date transactionDate = transaction.getDate();
                if (transactionDate.compareTo(date) > 0) {
                    continue;
                }
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

        List<SecurityHolding> securityHoldings = new ArrayList<SecurityHolding>();
        for (Integer securityId : quantities.keySet()) {
            Double quantity = quantities.get(securityId);
            // TODO: skip really small holding value
            if (quantity < 0.00000001) {
                continue;
            }
            SecurityHolding securityHolding = new SecurityHolding();
            securityHolding.setId(securityId);
            securityHolding.setQuanity(quantity);

            Map<Integer, Security> securities = mnyContext.getSecurities();
            Security security = securities.get(securityId);
            String securityName = null;
            if (security != null) {
                securityName = security.getName();
            } else {
                securityName = securityId.toString();
            }
            securityHolding.setName(securityName);

            try {
                Double price = getSecurityLatestPrice(securityId, date, mnyContext);
                if (price == null) {
                    price = new Double(0.0);
                }
                securityHolding.setPrice(new BigDecimal(price));
            } catch (IOException e) {
                log.warn("Cannot find latest price for securityId=" + securityId, e);
            }
            securityHoldings.add(securityHolding);
            BigDecimal price = securityHolding.getPrice();
            securityHolding.setMarketValue(new BigDecimal(price.doubleValue() * securityHolding.getQuanity()));
        }
        Collections.sort(securityHoldings, new Comparator<SecurityHolding>() {
            @Override
            public int compare(SecurityHolding o1, SecurityHolding o2) {
                return o1.getName().compareTo(o2.getName());
            }
        });
        account.setSecurityHoldings(securityHoldings);
        for (SecurityHolding sec : securityHoldings) {
            log.info("securityName=" + sec.getName() + ", quantity=" + account.formatSecurityQuantity(sec.getQuanity())
                    + ", price=" + account.formatAmmount(sec.getPrice()) + ", value=" + account.formatAmmount(sec.getMarketValue()));
            accountMarketValue += sec.getMarketValue().doubleValue();
        }

        try {
            Double cashAccountValue = getCashAccountValue(account, mnyContext);
            log.info("cashAccountValue=" + cashAccountValue);
            if (cashAccountValue != null) {
                accountMarketValue += cashAccountValue.doubleValue();
            }
        } catch (IOException e) {
            log.warn(e);
        }

        return accountMarketValue;
    }

    private static Double getCashAccountValue(Account account, MnyContext mnyContext) throws IOException {
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
            relatedToAccount = getAcccount(row);
        }
        return relatedToAccount;
    }

    private static Double getSecurityLatestPrice(Integer securityId, Date date, MnyContext mnyContext) throws IOException {
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

        Map<Integer, Payee> payees = getPayees(db);
        mnyContext.setPayees(payees);

        Map<Integer, Category> categories = getCategories(db);
        mnyContext.setCategories(categories);

        Map<Integer, Currency> currencies = getCurrencies(db);
        mnyContext.setCurrencies(currencies);

        Map<Integer, Security> securities = getSecurities(db);
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
            Double investmentBalance = calculateInvestmentBalance(account, date, mnyContext);
            currentBalance = new BigDecimal(investmentBalance);
        } else {
            currentBalance = calculateNonInvestmentBalance(account, date);
        }
        return currentBalance;
    }
}
