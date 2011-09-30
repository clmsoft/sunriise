package com.le.sunriise.qif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.md.Account;
import com.le.sunriise.md.AccountUtil;
import com.le.sunriise.md.Currency;
import com.le.sunriise.viewer.OpenedDb;

public class QifAccountUtilCmd {
    private static final Logger log = Logger.getLogger(QifAccountUtilCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File qifFile = null;
        String password = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            qifFile = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            qifFile = new File(args[1]);
            password = args[2];
        } else {
            Class<QifAccountUtilCmd> clz = QifAccountUtilCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny out.qif [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("qifFile=" + qifFile);
        try {
            OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
            printAccounts(openedDb, qifFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    private static void printAccounts(OpenedDb openedDb, File qifFile) throws IOException {
        Database db = openedDb.getDb();
        Map<Integer, Currency> currencies = AccountUtil.getCurrencies(db);

        List<Account> accounts = AccountUtil.getAccounts(db);
        AccountUtil.setCurrencies(accounts, currencies);

        printAccounts(accounts, qifFile);
    }

    private static void printAccounts(List<Account> accounts, File qifFile) throws IOException {
        PrintWriter writer = null;

        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(qifFile)));
            QifAccountUtil.print(accounts, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
            writer = null;
        }

    }
}
