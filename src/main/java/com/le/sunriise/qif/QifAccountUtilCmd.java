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
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Currency;
import com.le.sunriise.mnyobject.impl.MnyObjectUtil;
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
        Map<Integer, Currency> currencies = MnyObjectUtil.getCurrencies(db);

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
