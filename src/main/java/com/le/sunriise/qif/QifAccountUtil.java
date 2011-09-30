package com.le.sunriise.qif;

import java.io.PrintWriter;
import java.util.List;

import org.apache.log4j.Logger;

import com.le.sunriise.md.Account;
import com.le.sunriise.md.AccountType;

public class QifAccountUtil {
    private static final Logger log = Logger.getLogger(QifAccountUtil.class);

    /*
     * !Option:AutoSwitch !Account N401(k) (Contributions) TBank ^ NAlexian
     * Brothers (Contributions) TBank ^ !Clear:AutoSwitch
     */
    public static void print(Account account, PrintWriter writer) {
        try {
            writer.println("N" + account.getName());
            AccountType accountType = account.getAccountType();
            writer.println("T" + toQifType(accountType));
        } finally {
            writer.println("^");
        }
    }

    public static void print(List<Account> accounts, PrintWriter writer) {
        writer.println("!Option:AutoSwitch");
        writer.println("!Account");
        try {
            for (Account account : accounts) {
                print(account, writer);
            }
        } finally {
            writer.println("!Clear:AutoSwitch");
        }
    }

    private static String toQifType(AccountType accountType) {
        String qifType = null;

        /*
Export Header Type of account
!Type:Bank Bank account
!Type:Cash Cash account
!Type:CCard Credit card account
!Type:Invst Investment account
!Type:Oth A Asset account
!Type:Oth L Liability account
!Type:Invoice Invoice account (business subtype of Oth A)
!Type:Tax Tax account (business subtype of Oth L)
!Type:Bill Bill account (business subtype of Oth L)
         */
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
