package com.le.sunriise.report;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountType;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.viewer.OpenedDb;
import com.le.sunriise.viewer.TableUtils;

public class SanityCheck extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(SanityCheck.class);

    @Override
    public void visit(OpenedDb openedDb) throws IOException {
        checkTables(openedDb);

        super.visit(openedDb);
    }

    private void checkTables(OpenedDb openedDb) throws IOException {
        Database db = openedDb.getDb();
        if (db == null) {
            return;
        }
        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            Table table = db.getTable(tableName);
            if (table == null) {
                continue;
            }
            log.info("> table=" + tableName);

            log.info("  parseHeaderInfo");
            TableUtils.parseHeaderInfo(table, openedDb);

            log.info("  parseTableMetaData");
            TableUtils.parseTableMetaData(table);

            log.info("  parseIndexInfo");
            TableUtils.parseIndexInfo(table);

            log.info("  parseKeyInfo");
            TableUtils.parseKeyInfo(table);

            log.info("  visitAllRows");
            TableUtils.visitAllRows(table);
        }
    }

    @Override
    public void visitAccount(Account account) throws IOException {
        String name = account.getName();

        log.info("> Account name=" + name);

        super.visitAccount(account);

        AccountUtil.calculateCurrentBalance(account);
        if (account != null) {
            AccountType accountType = account.getAccountType();
            switch (accountType) {
            case INVESTMENT:
                Double marketValue = AccountUtil.calculateInvestmentBalance(account, mnyContext);
                account.setCurrentBalance(new BigDecimal(marketValue));
                break;
            }
        }
        log.info("  currentBalance=" + account.getCurrentBalance());
    }

}
