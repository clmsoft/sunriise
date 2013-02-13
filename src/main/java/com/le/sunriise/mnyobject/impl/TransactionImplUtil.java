package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.accountviewer.TransactionFilter;
import com.le.sunriise.mnyobject.Frequency;
import com.le.sunriise.mnyobject.InvestmentActivityImpl;
import com.le.sunriise.mnyobject.InvestmentTransaction;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionInfo;

public class TransactionImplUtil {

    private static final String COL_FI_STMT_ID = "mFiStmtId";
    private static final String COL_DATE = "dt";
    private static final String COL_GRFTT = "grftt";
    private static final String COL_CLEARED_STATE = "cs";
    private static final String COL_AMOUNT = "amt";
    private static final String COL_ID = "htrn";

    public static boolean addTransactionFromRow(Database db, Integer accountId, List<TransactionFilter> filters,
            Map<String, Object> row, List<Transaction> transactions, List<Transaction> filteredTransactions) throws IOException {
        Transaction transaction = new TransactionImpl();
        transaction.setAccountId(accountId);

        // transaction id
        Integer htrn = (Integer) row.get(COL_ID);
        transaction.setId(htrn);

        // fiTransactionId
        String fiTransactionId = (String) row.get(COL_FI_STMT_ID);
        transaction.setFiTransactionId(fiTransactionId);

        // amount
        BigDecimal amt = (BigDecimal) row.get(COL_AMOUNT);
        transaction.setAmount(amt);

        // TableID index ColumnName comments
        // TRN 7 cs "cleared state?
        // 0 == not cleared
        // 1 == cleared
        // 2 == reconciled

        Integer cs = (Integer) row.get(COL_CLEARED_STATE);
        transaction.setClearedState(cs);

        // flags? we are currently using this to figure out which transaction to
        // skip/void
        Integer grftt = (Integer) row.get(COL_GRFTT);
        transaction.setStatusFlag(grftt);
        if (grftt != null) {
            TransactionInfo transactionInfo = new TransactionInfoImpl();
            transactionInfo.setFlag(grftt);
            transaction.setTransactionInfo(transactionInfo);
        }

        // date
        Date date = (Date) row.get(COL_DATE);
        transaction.setDate(date);

        // frequency for recurring transaction?
        Double cFrqInst = (Double) row.get("cFrqInst");
        Frequency frequency = new FrequencyImpl();
        Integer frq = (Integer) row.get("frq");
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
            investmentTransaction = InvestmentTransactionImplUtil.getInvestmentTransaction(db, transaction.getId());
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

}
