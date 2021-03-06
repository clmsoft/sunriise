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
package com.le.sunriise.qif;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.InvestmentActivityImpl;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionSplit;
import com.le.sunriise.mnyobject.impl.MnyObjectUtil;

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
                Integer securityId = transaction.getSecurityId();
                writer.println("Y" + AccountUtil.getSecurityName(securityId, mnyContext));

                // I Price
                Double price = transaction.getPrice();
                if (price == null) {
                    price = new Double(0.0);
                }
                writer.println("I" + formatter.format(price));

                // Q Quantity (# of shares or split ratio)
                Double quantity = transaction.getQuantity();
                if (quantity == null) {
                    quantity = new Double(0.0);
                }
                writer.println("Q" + quantity);

                printClearedStatus(transaction, writer);

                // P 1st line text for transfers/reminders
                writer.println("P" + "transfers-TODO");

                // M Memo
                printMemo(transaction, writer);

                // O Commission
                writer.println("O" + "Commission-TODO");

                // L For MiscIncX or MiscExpX actions:Category/class
                // followed by
                // |transfer/class of the transaction
                // For MiscInc or MiscExp actions:Category/class of the
                // transaction
                // For all other actions:Transfer/class of the
                // transactions
                writer.println("L" + "Category-TODO");

                // T and U
                printAmount(transaction, writer);

                // $ Amount transferred
                writer.println("$" + "Amount-TODO");
            } else {
                // D Date
                writer.println("D" + qifDate(transaction.getDate()));

                // T and U
                printAmount(transaction, writer);

                // C Cleared status
                printClearedStatus(transaction, writer);

                // N Number (check or reference)
                printNumber(transaction, writer);

                // P Payee/description
                printPayee(transaction, mnyContext, writer);

                printMemo(transaction, writer);

                // A Address (up to 5 lines; 6th line is an optional
                // message)

                // L Category/class or transfer/class
                printQifCategory("L", transaction, mnyContext, writer);

                printSplits(transaction, mnyContext, writer);
            }
        } finally {
            // ^ End of entry
            writer.println("^");
        }

    }

    private static void printSplits(Transaction transaction, MnyContext mnyContext, PrintWriter writer) {
        List<TransactionSplit> splits = transaction.getSplits();
        if (splits != null) {
            for (TransactionSplit split : splits) {
                Transaction txn = split.getTransaction();

                // S Category in split (category/class or
                // transfer/class)
                printQifCategory("S", txn, mnyContext, writer);
                // E Memo in split
                String memo = txn.getMemo();
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

    private static void printPayee(Transaction transaction, MnyContext mnyContext, PrintWriter writer) {
        Integer payeeId = transaction.getPayeeId();
        if (log.isDebugEnabled()) {
            log.debug("payeeId=" + payeeId);
        }
        String payeeName = MnyObjectUtil.getPayeeName(payeeId, mnyContext.getPayees());
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
    }

    private static void printNumber(Transaction transaction, PrintWriter writer) {
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
    }

    private static void printMemo(Transaction transaction, PrintWriter writer) {
        String memo = transaction.getMemo();
        if (memo != null) {
            writer.println("M" + memo);
        }
    }

    private static void printAmount(Transaction transaction, PrintWriter writer) {
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
    }

    private static void printClearedStatus(Transaction transaction, PrintWriter writer) {
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

        InvestmentActivityImpl investmentActivity = transaction.getInvestmentActivity();
        if (investmentActivity == null) {
            return null;
        }

        Integer flag = investmentActivity.getFlag();
        String str = "ACTIVITY_UNKNOWN";
        boolean added = true;
        switch (flag) {
        case InvestmentActivityImpl.BUY:
            str = "Buy";
            break;
        case InvestmentActivityImpl.SELL:
            str = "Sell";
            added = false;
            break;
        case InvestmentActivityImpl.DIVIDEND:
            str = "Dividend";
            break;
        case InvestmentActivityImpl.INTEREST:
            str = "Interest";
            break;
        case InvestmentActivityImpl.RETURN_OF_CAPITAL:
            str = "Return of Capital";
            break;
        case InvestmentActivityImpl.REINVEST_DIVIDEND:
            str = "Reinvest Dividend";
            break;
        case InvestmentActivityImpl.REINVEST_INTEREST:
            str = "Reinvest Interest";
            break;
        case InvestmentActivityImpl.REMOVE_SHARES:
            str = "Remove Shares";
            added = false;
            break;
        case InvestmentActivityImpl.ADD_SHARES:
            str = "Add Shares";
            break;
        case InvestmentActivityImpl.S_TERM_CAP_GAINS_DIST:
            str = "S-Term Cap Gains Dist";
            break;
        case InvestmentActivityImpl.L_TERM_CAP_GAINS_DIST:
            str = "L-Term Cap Gains Dist";
            break;
        case InvestmentActivityImpl.REINVEST_S_TERM_CG_DIST:
            str = "Reinvest S-Term CG Dist";
            break;
        case InvestmentActivityImpl.REINVEST_L_TERM_CG_DIST:
            str = "Reinvest L-Term CG Dist";
            break;
        case InvestmentActivityImpl.TRANSFER_SHARES_IN:
            str = "Transfer Shares (in)";
            break;
        case InvestmentActivityImpl.TRANSFER_SHARES_OUT:
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
                for (Account account : accounts) {
                    Integer id = account.getId();
                    if (id.equals(transferredAccountId)) {
                        writer.println(prefix + "[" + account.getName() + "]");
                        break;
                    }
                }
            }
        } else {
            Integer categoryId = transaction.getCategoryId();
            log.info("categoryId=" + categoryId);
            String categoryName = MnyObjectUtil.getCategoryName(categoryId, mnyContext.getCategories());
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
