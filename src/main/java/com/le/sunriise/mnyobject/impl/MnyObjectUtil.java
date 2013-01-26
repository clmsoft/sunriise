package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.accountviewer.TransactionFilter;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Category;
import com.le.sunriise.mnyobject.Currency;
import com.le.sunriise.mnyobject.Frequency;
import com.le.sunriise.mnyobject.InvestmentActivityImpl;
import com.le.sunriise.mnyobject.InvestmentTransaction;
import com.le.sunriise.mnyobject.Payee;
import com.le.sunriise.mnyobject.Security;
import com.le.sunriise.mnyobject.SecurityHolding;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionInfo;
import com.le.sunriise.mnyobject.TransactionSplit;

public class MnyObjectUtil {
    private static final Logger log = Logger.getLogger(MnyObjectUtil.class);
    
    public static Account getAcccount(Map<String, Object> row) {
        Account account = null;
        String name = (String) row.get("szFull");
        if (name == null) {
            return account;
        }
        if (name.length() == 0) {
            return account;
        }
    
        account = new AccountImpl();
    
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
    
                CategoryImpl category = new CategoryImpl();
    
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

    public static String getCategoryName(Integer categoryId, Map<Integer, Category> categories) {
        String sep = ":";
        int depth = 0;
        int maxDepth = 2;
        return CategoryImpl.getCategoryName(categoryId, categories, sep, depth, maxDepth);
    }

    public static String toCategoryString(MnyContext context, Transaction transaction) {
        String value = null;
        Integer transferredAccountId = transaction.getTransferredAccountId();
        if (transferredAccountId != null) {
            List<Account> accounts = context.getAccounts();
            if (accounts != null) {
                for (Account account : accounts) {
                    Integer id = account.getId();
                    if (id.equals(transferredAccountId)) {
                        value = "Transfer to " + account.getName();
                        break;
                    }
                }
            }
        } else {
            Integer categoryId = transaction.getCategoryId();
            Map<Integer, Category> categories = context.getCategories();
            String categoryName = getCategoryName(categoryId, categories);
            value = categoryName;
        }
    
        if (value == null) {
            if (transaction.hasSplits()) {
                List<TransactionSplit> splits = transaction.getSplits();
                value = "(" + splits.size() + ") Split Transaction";
            }
        }
    
        if (transaction.isInvestment()) {
            Integer securityId = transaction.getSecurityId();
            String securityName = AccountUtil.getSecurityName(securityId, context);
            value = securityName;
        }
        return value;
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
    
                CurrencyImpl currency = new CurrencyImpl();
                currency.setId(hcrnc);
                currency.setName(name);
                currency.setIsoCode(isoCode);
    
                currencies.put(hcrnc, currency);
            }
        } finally {
    
        }
    
        return currencies;
    }

    public static boolean addTransactionFromRow(Database db, List<TransactionFilter> filters, Map<String, Object> row,
            List<Transaction> transactions, List<Transaction> filteredTransactions) throws IOException {
        TransactionImpl transaction = new TransactionImpl();
    
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
    
        // flags? we are currently using this to figure out which transaction to
        // skip/void
        Integer grftt = (Integer) row.get("grftt");
        transaction.setStatusFlag(grftt);
        if (grftt != null) {
            TransactionInfo transactionInfo = new TransactionInfoImpl();
            transactionInfo.setFlag(grftt);
            transaction.setTransactionInfo(transactionInfo);
        }
    
        // date
        Date date = (Date) row.get("dt");
        transaction.setDate(date);
    
        // frequency for recurring transaction?
        Integer frq = (Integer) row.get("frq");
        Double cFrqInst = (Double) row.get("cFrqInst");
        Frequency frequency = new FrequencyImpl();
        frequency.setFrq(frq);
        frequency.setcFrqInst(cFrqInst);
        frequency.setGrftt(grftt);
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
        InvestmentActivityImpl investmentActivity = new InvestmentActivityImpl(act);
        transaction.setInvestmentActivity(investmentActivity);
    
        InvestmentTransaction investmentTransaction = null;
        if (transaction.isInvestment()) {
            investmentTransaction = MnyObjectUtil.getInvestmentTransaction(db, transaction.getId());
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
        } else {
            filteredTransactions.add(transaction);
        }
    
        return accept;
    }

    public static InvestmentTransaction getInvestmentTransaction(Database db, Integer id) throws IOException {
        InvestmentTransaction investmentTransaction = new InvestmentTransactionImpl();
    
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
    
                PayeeImpl payee = new PayeeImpl();
                payee.setId(hpay);
                payee.setParent(hpayParent);
                payee.setName(name);
    
                payees.put(hpay, payee);
            }
        } finally {
    
        }
        return payees;
    }

    public static String getPayeeName(Integer payeeId, Map<Integer, Payee> payees) {
        if (payeeId == null) {
            return null;
        }
        if (payeeId < 0) {
            return null;
        }
        Payee payee = payees.get(payeeId);
        if (payee == null) {
            return null;
        }
        return payee.getName();
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
            InvestmentActivityImpl investmentActivity = transaction.getInvestmentActivity();
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
            SecurityHoldingImpl securityHolding = new SecurityHoldingImpl();
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
                Double price = AccountUtil.getSecurityLatestPrice(securityId, date, mnyContext);
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
            Double cashAccountValue = AccountUtil.getCashAccountValue(account, mnyContext);
            log.info("cashAccountValue=" + cashAccountValue);
            if (cashAccountValue != null) {
                accountMarketValue += cashAccountValue.doubleValue();
            }
        } catch (IOException e) {
            log.warn(e);
        }
    
        return accountMarketValue;
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
    
                SecurityImpl security = new SecurityImpl();
                security.setId(hsec);
                security.setName(szFull);
                security.setSymbol(szSymbol);
    
                securities.put(hsec, security);
            }
        } finally {
    
        }
    
        return securities;
    }

    public static TransactionSplit getTransactionSplit(Cursor splitCursor, Transaction transaction) throws IOException {
        splitCursor.reset();
        TransactionSplit transactionSplit = null;
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("htrn", transaction.getId());
        if (splitCursor.findFirstRow(rowPattern)) {
            Map<String, Object> row = splitCursor.getCurrentRow();
            Integer htrnParent = (Integer) row.get("htrnParent");
            Integer iSplit = (Integer) row.get("iSplit");
    
            transactionSplit = new TransactionSplitImpl();
            transactionSplit.setTransaction(transaction);
            transactionSplit.setParentId(htrnParent);
            transactionSplit.setRowId(iSplit);
        }
        return transactionSplit;
    }

}
