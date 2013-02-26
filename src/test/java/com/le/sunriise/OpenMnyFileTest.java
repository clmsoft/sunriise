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

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.misc.MnyTestFile;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.viewer.OpenedDb;

public class OpenMnyFileTest {
    private static final Logger log = Logger.getLogger(OpenMnyFileTest.class);

    @Test
    public void testSunset() throws IOException {
        String fileName = null;

        fileName = "sunset01.mny";
        testSunset(fileName);

        fileName = "sunset02.mny";
        testSunset(fileName);
    }

    protected void testSunset(String fileName) throws IOException {
        if (fileName == null) {
            return;
        }
        MnyTestFile testFile;
        String password;
        File dbFile;
        testFile = MnyTestFile.getSampleFile(fileName);
        Assert.assertNotNull(testFile);
        fileName = testFile.getFileName();
        log.info("fileName: " + fileName);
        dbFile = new File(testFile.getFileName());
        password = testFile.getPassword();
        log.info("password: " + password);
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
        openedDb.close();
    }
}
