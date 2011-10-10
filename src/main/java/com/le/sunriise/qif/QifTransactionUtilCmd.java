package com.le.sunriise.qif;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.accountviewer.Transaction;
import com.le.sunriise.viewer.OpenedDb;

public class QifTransactionUtilCmd {
    private static final Logger log = Logger.getLogger(Logger.class);

    private static final char[] ILLEGAL_CHARACTERS = { '/', '\n', '\r', '\t', '\0', '\f', '`', '?', '*', '\\', '<', '>', '|', '\"', ':' };

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File outDir = null;
        String password = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            outDir = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            outDir = new File(args[1]);
            password = args[2];
        } else {
            Class<QifTransactionUtilCmd> clz = QifTransactionUtilCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny outDir [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("outDir=" + outDir);
        try {
            OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
            printTransactionsToDir(openedDb, outDir);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    private static void printTransactionsToDir(OpenedDb openedDb, File outDir) throws IOException {
        MnyContext mnyContext = AccountUtil.createMnyContext(openedDb);
        List<Account> accounts = mnyContext.getAccounts();
        int count = 0;
        int size = accounts.size();
        for (Account account : accounts) {
            count++;
            log.info("");
            log.info("###");
            log.info(count + "/" + size + ", account=" + account.getName());
            printTransactionsToDir(account, mnyContext, outDir);
        }
    }

    private static void printTransactionsToDir(Account account, MnyContext mnyContext, File outDir) throws IOException {
        if ((!outDir.exists()) && (!outDir.mkdirs())) {
            throw new IOException("Cannot create directory outDir=" + outDir);
        }

        String name = account.getName();
        String tempName = new String(name);
        for (char c : ILLEGAL_CHARACTERS) {
            tempName = tempName.replace(c, '_');
        }
        if (!tempName.equals(name)) {
            log.warn("Rename name from '" + name + "' to '" + tempName + "'");
            name = tempName;
        }

        File outFile = new File(outDir, name + ".qif");
        printTransactions(account, mnyContext, outFile);
    }

    private static void printTransactions(Account account, MnyContext mnyContext, File outFile) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            printTransactions(account, mnyContext, writer);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    private static void printTransactions(Account account, MnyContext mnyContext, PrintWriter writer) throws IOException {
        Database db = mnyContext.getDb();
        AccountUtil.getTransactions(db, account);
        BigDecimal currentBalance = AccountUtil.calculateCurrentBalance(account);

        List<Transaction> transactions = account.getTransactions();
        if (transactions == null) {
            log.info("transactions=" + 0);
            return;
        }
        log.info("transactions=" + transactions.size());
        log.info("currentBalance=" + currentBalance);

        try {
            printTransactionsHeader(account, mnyContext, writer);
            printStartingBalance(account, writer);
            for (Transaction transaction : transactions) {
                QifExportUtils.logQif(transaction, mnyContext, writer);
            }
        } finally {
            printTransactionsFooter(account, mnyContext, writer);
        }
    }

    private static void printTransactionsHeader(Account account, MnyContext mnyContext, PrintWriter writer) {
//        !Account
//        NCalPERS Retireme (Contributions)
//        TBank
//        ^   
        writer.println("!Account");
        writer.println("N" + account.getName());
        writer.println("T" + QifAccountUtil.toQifType(account));
        writer.println("^");

        String type = QifAccountUtil.toQifType(account);
        writer.println("!Type:" + type);

    }

    private static void printStartingBalance(Account account, PrintWriter writer) {
        BigDecimal startingBalance = account.getStartingBalance();
        if (startingBalance == null) {
            if (startingBalance == null) {
                startingBalance = new BigDecimal(0.00);
            }
        }
        Date date = null;
        List<Transaction> transactions = account.getTransactions();
        if (transactions != null) {
            if (transactions.size() > 0) {
                Transaction transaction = transactions.get(0);
                date = transaction.getDate();
            }
        }
        if (date == null) {
            // long time ago
            date = new Date(0);
        }
        // D1/ 1/95
        writer.println("D" + QifExportUtils.qifDate(date));
        // U0.00
        writer.println("U" + QifExportUtils.qifAmount(startingBalance));
        // T0.00
        writer.println("T" + QifExportUtils.qifAmount(startingBalance));
        // CX
        writer.println("CX");
        // POpening Balance
        writer.println("POpening Balance");
        // L[Checking]
        String accountName = account.getName();
        writer.println("L" + "[" + accountName + "]");
        writer.println("^");
    }

    private static void printTransactionsFooter(Account account, MnyContext mnyContext, PrintWriter writer) {

    }

}
