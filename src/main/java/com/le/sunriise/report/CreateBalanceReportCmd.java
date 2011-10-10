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
import com.le.sunriise.Utils;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountType;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.MnyContext;
import com.le.sunriise.viewer.OpenedDb;

public class CreateBalanceReportCmd {
    private static final Logger log = Logger.getLogger(CreateBalanceReportCmd.class);

    private class AccountBalance {
        private Account account;
        private Date date;
        private BigDecimal balance;

        public Account getAccount() {
            return account;
        }

        public void setAccount(Account account) {
            this.account = account;
        }

        public Date getDate() {
            return date;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public BigDecimal getBalance() {
            return balance;
        }

        public void setBalance(BigDecimal balance) {
            this.balance = balance;
        }

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
        log.info("outDir=" + outFile);
        try {
            OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
            CreateBalanceReportCmd cmd = new CreateBalanceReportCmd();
            cmd.genReport(openedDb, outFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    private void genReport(OpenedDb openedDb, File outFile) throws IOException {
        Map<AccountType, List<AccountBalance>> balances = new HashMap<AccountType, List<AccountBalance>>();

        Date date = new Date();
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            MnyContext mnyContext = AccountUtil.createMnyContext(openedDb);
            List<Account> accounts = mnyContext.getAccounts();
            int count = 0;
            int size = accounts.size();
            for (Account account : accounts) {
                count++;
                log.info("");
                log.info("###");
                log.info(count + "/" + size + ", account=" + account.getName());
                Database db = mnyContext.getDb();
                AccountUtil.getTransactions(db, account);
                BigDecimal currentBalance = null;
                if (account.getAccountType() == AccountType.INVESTMENT) {
                    Double investmentBalance = AccountUtil.calculateInvestmentBalance(account, date, mnyContext);
                    currentBalance = new BigDecimal(investmentBalance);
                } else {
                    currentBalance = AccountUtil.calculateCurrentBalance(account, date);
                }
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

            for (AccountType accountType : balances.keySet()) {
                List<AccountBalance> list = balances.get(accountType);
                writer.println("");
                writer.println("### " + accountType);
                for (AccountBalance balance : list) {
                    Account account = balance.getAccount();
                    writer.println(account.getName() + ", " + account.getAccountType() + ", " + account.formatAmmount(balance.getBalance()));
                }
            }
        } finally {
            if (writer != null) {
                writer.close();
                writer = null;
            }
        }

    }

}
