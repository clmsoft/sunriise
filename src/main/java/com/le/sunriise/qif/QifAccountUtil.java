package com.le.sunriise.qif;

import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.List;

import org.apache.log4j.Logger;

import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountType;

public class QifAccountUtil {
    private static final Logger log = Logger.getLogger(QifAccountUtil.class);

    public static void print(Account account, PrintWriter writer) {
        try {
            writer.println("N" + account.getName());

            writer.println("T" + toQifType(account));

            if (account.isCreditCard()) {
                BigDecimal amountLimit = account.getAmountLimit();
                if (amountLimit != null) {
                    double value = amountLimit.doubleValue();
                    writer.println("L" + Math.abs(value));
                }
            }
        } finally {
            writer.println("^");
        }
    }

    public static void print(List<Account> accounts, PrintWriter writer) {
        printHeader(writer);
        try {
            for (Account account : accounts) {
                print(account, writer);
            }
        } finally {
            printFooter(writer);
        }
    }

    private static void printFooter(PrintWriter writer) {
        writer.println("!Clear:AutoSwitch");
    }

    private static void printHeader(PrintWriter writer) {
        writer.println("!Option:AutoSwitch");
        writer.println("!Account");
    }

    private static String toQifType(Account account) {
        AccountType accountType = account.getAccountType();
        Boolean retirement = account.getRetirement();
        
        String qifType = null;

        // Export Header Type of account
        // !Type:Bank Bank account
        // !Type:Cash Cash account
        // !Type:CCard Credit card account
        // !Type:Invst Investment account
        // !Type:Oth A Asset account
        // !Type:Oth L Liability account
        // !Type:Invoice Invoice account (business subtype of Oth A)
        // !Type:Tax Tax account (business subtype of Oth L)
        // !Type:Bill Bill account (business subtype of Oth L)
        switch (accountType) {
        case CASH:
            qifType = "Cash";
            break;
        case ASSET:
            qifType = "Oth A";
            break;
        case LOAN:
            qifType = "Oth L";
            break;
        case CREDIT_CARD:
            qifType = "CCard";
            break;
        case BANKING:
            qifType = "Bank";
            break;
        case INVESTMENT:
            qifType = "Port";
            if (account.is401k403b()) {
                qifType = "401(k)/403(b)";
            }
            break;
        case LIABILITY:
            qifType = "Oth L";
            break;
        default:
            log.warn("Cannot map to QIF type: " + accountType);
            qifType = accountType.toString();
            break;
        }

        return qifType;
    }
}
