package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.le.sunriise.md.Account;
import com.le.sunriise.md.AccountUtil;
import com.le.sunriise.md.Transaction;
import com.le.sunriise.viewer.OpenedDb;

public class OpenMnyFileTest {
    @Test
    public void test1() throws IOException {
        File dbFile = new File("src/test/data/sunset01.mny");
        String password = null;
        openDB(dbFile, password);

        dbFile = new File("src/test/data/sunset02.mny");
        password = "12345678";
        openDB(dbFile, password);

    }

    private void openDB(File dbFile, String password) throws IOException {
        OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
        Assert.assertNotNull(openedDb);
        openedDb.close();

        openedDb = Utils.openDb(dbFile, password);
        Assert.assertNotNull(openedDb);
        openedDb.close();

        openedDb = Utils.openDbReadOnly(dbFile, password);
        Assert.assertNotNull(openedDb);
        List<Account> accounts = AccountUtil.getAccounts(openedDb.getDb());
        Assert.assertNotNull(accounts);
        Assert.assertEquals(1, accounts.size());

        Account account = accounts.get(0);
        Assert.assertNotNull(account);
        Assert.assertEquals("Investments to Watch", account.getName());

        List<Transaction> transactions = AccountUtil.getTransactions(openedDb.getDb(), account);
        Assert.assertNotNull(transactions);
        Assert.assertEquals(3, transactions.size());
    }
}
