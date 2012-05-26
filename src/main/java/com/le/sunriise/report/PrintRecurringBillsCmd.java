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
package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.Transaction;

public class PrintRecurringBillsCmd extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(PrintRecurringBillsCmd.class);

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
            Class<PrintRecurringBillsCmd> clz = PrintRecurringBillsCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            PrintRecurringBillsCmd cmd = new PrintRecurringBillsCmd();
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    @Override
    public void visitAccount(Account account) throws IOException {
        log.info("> account=" + account.getName());
        super.visitAccount(account);
    }

    @Override
    public void visitFilteredTransaction(Transaction transaction) throws IOException {
        if (transaction.isRecurring()) {
            printRecurring(transaction);
        }
        super.visitFilteredTransaction(transaction);
    }

    private void printRecurring(Transaction transaction) {
        System.out.println("> RECURRING");
        try {
            System.out.println(transaction.getId());
            
            System.out.println(transaction.getFrequency().getFrequencyString());
        } finally {
            System.out.println("< RECURRING");
        }
    }

}
