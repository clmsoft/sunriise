package com.le.sunriise;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.junit.Test;

import com.le.sunriise.password.HeaderPage;
import com.le.sunriise.password.MinPasswordChecker;

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
        testPassword(headerPage);

        dbFile = new File("src/test/data/money2005-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(false, headerPage.isUseSha1());
        testPassword(headerPage);

        dbFile = new File("src/test/data/money2008-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(true, headerPage.isUseSha1());
        password = "Test12345";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);
        password = "TEST12345";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);
        
        dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        headerPage = new HeaderPage(dbFile);
        Assert.assertEquals(true, headerPage.isNewEncryption());
        Assert.assertEquals(true, headerPage.isUseSha1());
        testPassword(headerPage);
    }

    private void testPassword(HeaderPage headerPage) throws IOException {
        String password = null;

        boolean matched = false;

        password = null;
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "1";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "12345";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "123456";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);

        password = "123@abc!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@Abc!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@aBc!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@abC!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@ABC!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(true, matched);

        password = "123@abcd!";
        matched = MinPasswordChecker.checkPassword(headerPage, password);
        Assert.assertEquals(false, matched);
    }
}
