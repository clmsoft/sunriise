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

import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Category;
import com.le.sunriise.mnyobject.InvestmentActivityImpl;
import com.le.sunriise.mnyobject.InvestmentTransaction;
import com.le.sunriise.mnyobject.Payee;
import com.le.sunriise.mnyobject.Security;
import com.le.sunriise.mnyobject.SecurityHolding;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionSplit;

public class MnyObjectUtil {
    private static final Logger log = Logger.getLogger(MnyObjectUtil.class);
    
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
                log.warn("SKIP security with very small holding, id=" + securityId + ", quantity=" + quantity);
                continue;
            }
            SecurityHolding securityHolding = new SecurityHoldingImpl();
//            securityHolding.setId(securityId);
            securityHolding.setQuantity(quantity);
    
            Map<Integer, Security> securities = mnyContext.getSecurities();
            Security security = securities.get(securityId);
//            String securityName = null;
//            if (security != null) {
//                securityName = security.getName();
//            } else {
//                securityName = securityId.toString();
//            }
//            securityHolding.setName(securityName);
            securityHolding.setSecurity(security);
            
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
            securityHolding.setMarketValue(new BigDecimal(price.doubleValue() * securityHolding.getQuantity()));
        }
        Collections.sort(securityHoldings, new Comparator<SecurityHolding>() {
            @Override
            public int compare(SecurityHolding o1, SecurityHolding o2) {
                return o1.getSecurity().getName().compareTo(o2.getSecurity().getName());
            }
        });
        account.setSecurityHoldings(securityHoldings);
        for (SecurityHolding sec : securityHoldings) {
            log.info("securityName=" + sec.getSecurity().getName() + ", quantity=" + account.formatSecurityQuantity(sec.getQuantity())
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

}
