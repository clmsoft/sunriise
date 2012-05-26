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



package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.Transaction;
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

        List<Transaction> transactions = AccountUtil.retrieveTransactions(openedDb.getDb(), account);
        Assert.assertNotNull(transactions);
        Assert.assertEquals(3, transactions.size());
    }
}
