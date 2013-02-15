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

package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.le.sunriise.header.HeaderPage;

public class CheckPasswordTest {

    @Test
    public void test() throws IOException {
        File dbFile = null;
        HeaderPage headerPage = null;
        String password = null;
        boolean matched = false;
        
        dbFile = new File("src/test/data/money2001-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(false, headerPage.isNewEncryption());
        Assert.assertEquals(false, headerPage.isUseSha1());
        Assert.assertEquals("TEST12345", headerPage.getEmbeddedDatabasePassword());

        dbFile = new File("src/test/data/money2004-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(false, headerPage.isUseSha1());
        testSunsetSamplePassword(headerPage);

        dbFile = new File("src/test/data/money2005-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(false, headerPage.isUseSha1());
        testSunsetSamplePassword(headerPage);

        dbFile = new File("src/test/data/money2008-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(true, headerPage.isUseSha1());
        password = "Test12345";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);
        password = "TEST12345";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);
        
        dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(true, headerPage.isUseSha1());
        testSunsetSamplePassword(headerPage);
    }

    private void testSunsetSamplePassword(HeaderPage headerPage) throws IOException {
        String password = null;

        boolean matched = false;

        password = null;
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "1";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "12345";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "123456";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "123@abc!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@Abc!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@aBc!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@abC!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@ABC!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@abcd!";
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);
    }
}
