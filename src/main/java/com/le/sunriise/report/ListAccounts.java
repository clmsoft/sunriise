package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.accountviewer.Account;

public class ListAccounts extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(ListAccounts.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;

        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
        } else {
            Class<ListAccounts> clz = ListAccounts.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            ListAccounts cmd = new ListAccounts();
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    @Override
    public void visitAccount(Account account) {
        log.info(account.getName());
    }
}
