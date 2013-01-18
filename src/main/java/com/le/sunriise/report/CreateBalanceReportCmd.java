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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.AccountType;
import com.le.sunriise.viewer.OpenedDb;

public class CreateBalanceReportCmd extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(CreateBalanceReportCmd.class);

    private Date date;
    private Map<AccountType, List<AccountBalance>> balances;
    private File outFile;

    public File getOutFile() {
        return outFile;
    }

    public void setOutFile(File outFile) {
        this.outFile = outFile;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File outFile = null;
        String password = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            outFile = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            outFile = new File(args[1]);
            password = args[2];
        } else {
            Class<CreateBalanceReportCmd> clz = CreateBalanceReportCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny outFile [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("outFile=" + outFile);
        try {
            CreateBalanceReportCmd cmd = new CreateBalanceReportCmd();
            cmd.setOutFile(outFile);
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    @Override
    public void preVisit(OpenedDb openedDb) throws IOException {
        this.date = new Date();
        this.balances = new HashMap<AccountType, List<AccountBalance>>();
    }

    @Override
    public void postVisit(OpenedDb openedDb) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));

            for (AccountType accountType : balances.keySet()) {
                List<AccountBalance> list = balances.get(accountType);
                writer.println("");
                writer.println("### " + accountType);
                for (AccountBalance balance : list) {
                    Account account = balance.getAccount();
                    writer.println(account.getName() + ", " + account.getAccountType() + ", "
                            + account.formatAmmount(balance.getBalance()));
                }
            }
        } finally {
            log.info("outFile=" + outFile);
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }
    }

    @Override
    public void visitAccount(Account account) throws IOException {
        log.info("account=" + account.getName());

        Database db = mnyContext.getDb();
        AccountUtil.retrieveTransactions(db, account);
        BigDecimal currentBalance = AccountUtil.calculateBalance(account, date, mnyContext);
        AccountBalance accountBalance = new AccountBalance();
        accountBalance.setAccount(account);
        accountBalance.setDate(date);
        accountBalance.setBalance(currentBalance);

        AccountType accountType = account.getAccountType();
        List<AccountBalance> list = balances.get(accountType);
        if (list == null) {
            list = new ArrayList<AccountBalance>();
            balances.put(accountType, list);
        }
        list.add(accountBalance);

    }

}
