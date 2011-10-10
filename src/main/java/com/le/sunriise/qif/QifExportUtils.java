package com.le.sunriise.qif;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.Category;
import com.le.sunriise.accountviewer.InvestmentActivity;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.accountviewer.Payee;
import com.le.sunriise.accountviewer.Transaction;
import com.le.sunriise.accountviewer.TransactionSplit;

public class QifExportUtils {
    private static final Logger log = Logger.getLogger(QifExportUtils.class);

    private static Calendar calendar = Calendar.getInstance();
    private static NumberFormat formatter = DecimalFormat.getInstance();
    static {
        if (formatter instanceof DecimalFormat) {
            DecimalFormat df = (DecimalFormat) formatter;
            df.setMaximumFractionDigits(2);
            df.setMinimumFractionDigits(2);
        }
    }

    public static void logQif(Transaction transaction, MnyContext mnyContext, PrintWriter writer) {
        try {
            if (transaction.isInvestment()) {
                // Letter What it means
                // D Date (optional)
                writer.println("D" + qifDate(transaction.getDate()));
                // N Action
                String qifInvestmentAction = qifInvestmentAction(transaction);
                if (qifInvestmentAction != null) {
                    writer.println("N" + qifInvestmentAction);
                }
                // Y Security
                // I Price
                // Q Quantity (# of shares or split ratio)
                // C Cleared status
                // P 1st line text for transfers/reminders
                // M Memo
                // O Commission
                // L For MiscIncX or MiscExpX actions:Category/class
                // followed by
                // |transfer/class of the transaction
                // For MiscInc or MiscExp actions:Category/class of the
                // transaction
                // For all other actions:Transfer/class of the
                // transactions
                // T Amount of transaction
                // U Amount of transaction (higher possible value than
                // T)
                // $ Amount transferred
            } else {
                // D Date
                writer.println("D" + qifDate(transaction.getDate()));
                // T Amount of transaction
                if (transaction.isVoid()) {
                    writer.println("T" + "0.00");
                } else {
                    writer.println("T" + qifAmount(transaction.getAmount()));
                }
                // U Amount of transaction (higher possible value than T)
                if (transaction.isVoid()) {
                    writer.println("U" + "0.00");
                } else {
                    writer.println("U" + qifAmount(transaction.getAmount()));
                }

                // C Cleared status
                if (transaction.isVoid()) {
                    // clear: *
                    // reconcile: X
                    writer.println("C*");
                } else {
                    if (transaction.isReconciled()) {
                        writer.println("CX");
                    } else if (transaction.isCleared()) {
                        writer.println("C*");
                    }
                }
                // N Number (check or reference)
                String number = transaction.getNumber();
                if (number != null) {
                    log.info("number=" + number);

                    String leftSide = null;
                    String rightSide = null;
                    if (number.length() == 0) {
                        leftSide = null;
                        rightSide = null;
                    } else if (number.length() == 1) {
                        leftSide = null;
                        rightSide = number;
                    } else {
                        leftSide = number.substring(0, 1);
                        rightSide = number.substring(1);
                    }
                    if (leftSide != null) {
                        leftSide = leftSide.trim();
                    }
                    if (rightSide != null) {
                        rightSide = rightSide.trim();
                    }
                    String value = rightSide;
                    if (value != null) {
                        value = value.trim();
                        writer.println("N" + value);
                    }
                }
                // P Payee/description
                Integer payeeId = transaction.getPayeeId();
                if (log.isDebugEnabled()) {
                    log.debug("payeeId=" + payeeId);
                }
                String payeeName = Payee.getPayeeName(payeeId, mnyContext.getPayees());
                if (transaction.isVoid()) {
                    // money way
                    // writer.println("PVOID " + Payee.getPayeeName(payeeId,
                    // mnyContext.getPayees()));
                    // quicken way
                    if (payeeName != null) {
                        writer.println("P**VOID**" + payeeName);
                    } else {
                        writer.println("P**VOID**");
                    }
                } else {
                    if (payeeName != null) {
                        writer.println("P" + payeeName);
                    }
                }
                // M Memo
                String memo = transaction.getMemo();
                if (memo != null) {
                    writer.println("M" + memo);
                }

                // A Address (up to 5 lines; 6th line is an optional
                // message)

                // L Category/class or transfer/class
                printQifCategory("L", transaction, mnyContext, writer);
                List<TransactionSplit> splits = transaction.getSplits();
                if (splits != null) {
                    for (TransactionSplit split : splits) {
                        Transaction txn = split.getTransaction();

                        // S Category in split (category/class or
                        // transfer/class)
                        printQifCategory("S", txn, mnyContext, writer);
                        // E Memo in split
                        memo = txn.getMemo();
                        if (memo != null) {
                            writer.println("M" + memo);
                        }
                        // $ Dollar amount of split
                        if (txn.isVoid()) {
                            writer.println("$" + "0.00");
                        } else {
                            writer.println("$" + qifAmount(txn.getAmount()));
                        }
                        // % Percentage of split if percentages are used
                        // F Reimbursable business expense flag
                    }
                }
            }
        } finally {
            // ^ End of entry
            writer.println("^");
        }

    }

    private static String qifInvestmentAction(Transaction transaction) {
        // Investment Actions track common investment activities such as
        // Dividends, Reinvestment of income, and Capital Gains (CG).
        // Codes that end in X indicate that the transaction was generated by
        // the account, but the cash is transferred to a different
        // account (in which case the Category field is the destination account
        // name).
        // Action codes include: CGLong (Capital Gains Long Term), CGLongX,
        // CGMid, CGMidX, CGShort, CGShortX, Div, DivX,
        // IntInc, IntIncX, MargInt, MargIntX, RtnCap, RtnCapX, XIn, XOut,
        // Added, Removed, StkSplit

        // Buy (X)
        // Div
        // IntInc
        // ReinvDiv
        // ReinvLg
        // ReinvSh
        // Sell (X)

        InvestmentActivity investmentActivity = transaction.getInvestmentActivity();
        if (investmentActivity == null) {
            return null;
        }

        Integer flag = investmentActivity.getFlag();
        String str = "ACTIVITY_UNKNOWN";
        boolean added = true;
        switch (flag) {
        case InvestmentActivity.BUY:
            str = "Buy";
            break;
        case InvestmentActivity.SELL:
            str = "Sell";
            added = false;
            break;
        case InvestmentActivity.DIVIDEND:
            str = "Dividend";
            break;
        case InvestmentActivity.INTEREST:
            str = "Interest";
            break;
        case InvestmentActivity.RETURN_OF_CAPITAL:
            str = "Return of Capital";
            break;
        case InvestmentActivity.REINVEST_DIVIDEND:
            str = "Reinvest Dividend";
            break;
        case InvestmentActivity.REINVEST_INTEREST:
            str = "Reinvest Interest";
            break;
        case InvestmentActivity.REMOVE_SHARES:
            str = "Remove Shares";
            added = false;
            break;
        case InvestmentActivity.ADD_SHARES:
            str = "Add Shares";
            break;
        case InvestmentActivity.S_TERM_CAP_GAINS_DIST:
            str = "S-Term Cap Gains Dist";
            break;
        case InvestmentActivity.L_TERM_CAP_GAINS_DIST:
            str = "L-Term Cap Gains Dist";
            break;
        case InvestmentActivity.REINVEST_S_TERM_CG_DIST:
            str = "Reinvest S-Term CG Dist";
            break;
        case InvestmentActivity.REINVEST_L_TERM_CG_DIST:
            str = "Reinvest L-Term CG Dist";
            break;
        case InvestmentActivity.TRANSFER_SHARES_IN:
            str = "Transfer Shares (in)";
            break;
        case InvestmentActivity.TRANSFER_SHARES_OUT:
            str = "Transfer Shares (out)";
            added = false;
            break;
        default:
            str = "ACTIVITY_UNKNOWN";
            break;
        }

        return str;
    }

    private static void printQifCategory(String prefix, Transaction transaction, MnyContext mnyContext, PrintWriter writer) {

        // String prefix = "L";
        if (transaction.isTransfer()) {
            Integer transferredAccountId = transaction.getTransferredAccountId();
            if (log.isDebugEnabled()) {
                log.debug("transferredAccountId=" + transferredAccountId);
            }
            List<Account> accounts = mnyContext.getAccounts();
            if (accounts != null) {
                for (Account a : accounts) {
                    Integer id = a.getId();
                    if (id.equals(transferredAccountId)) {
                        writer.println(prefix + "[" + a.getName() + "]");
                        break;
                    }
                }
            }
        } else {
            Integer categoryId = transaction.getCategoryId();
            log.info("categoryId=" + categoryId);
            String categoryName = Category.getCategoryName(categoryId, mnyContext.getCategories());
            if (categoryName != null) {
                writer.println(prefix + categoryName);
            }

            if ((categoryName == null) && (transaction.hasSplits())) {
                List<TransactionSplit> splits = transaction.getSplits();
                TransactionSplit split = splits.get(0);
                Transaction txn = split.getTransaction();
                printQifCategory(prefix, txn, mnyContext, writer);
            }
        }
    }

    public static String qifAmount(BigDecimal amount) {
        return formatter.format(amount);
    }

    public static String qifDate(Date date) {
        StringBuilder sb = new StringBuilder();

        calendar.setTime(date);
        int dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH);
        int month = calendar.get(Calendar.MONTH);
        int year = calendar.get(Calendar.YEAR);
        if (year < 2000) {
            // 12/31/1999
            sb.append(month + 1);
            sb.append("/");
            sb.append(dayOfMonth);
            sb.append("/");
            sb.append(year);
        } else {
            // 10/7'2002
            sb.append(month + 1);
            sb.append("/");
            sb.append(dayOfMonth);
            sb.append("'");
            sb.append(year);
        }
        return sb.toString();
    }

}
