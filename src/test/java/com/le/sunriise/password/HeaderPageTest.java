package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.healthmarketscience.jackcess.JetFormat;

public class HeaderPageTest {

    @Test
    public void test() throws IOException {
        File dbFile = null;

        dbFile = new File("src/test/data/sunset-sample-pwd-5.mny");
        HeaderPage headerPage = new HeaderPage(dbFile);

        Assert.assertEquals(JetFormat.VERSION_MSISAM, headerPage.getJetFormat());
        Assert.assertTrue(headerPage.isNewEncryption());
        Assert.assertTrue(headerPage.isUseSha1());
        Assert.assertEquals("[42, A0, 5B, E7, 28, F5, E3, 40]", HeaderPage.toHexString(headerPage.getSalt()));
        Assert.assertEquals("[42, A0, 5B, E7]", HeaderPage.toHexString(headerPage.getBaseSalt()));
        Assert.assertEquals("[D6, BB, F8, 4B]", HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
    }
    
    @Test
    public void testBackupFile() throws IOException {
        File dbFile = null;

        dbFile = new File("src/test/data/sunset-sample.mbf");
        HeaderPage headerPage = new HeaderPage(dbFile);

        Assert.assertEquals(JetFormat.VERSION_MSISAM, headerPage.getJetFormat());
        Assert.assertTrue(headerPage.isNewEncryption());
        Assert.assertTrue(headerPage.isUseSha1());
        Assert.assertEquals("[06, FE, 53, F3, 6C, F5, E3, 40]", HeaderPage.toHexString(headerPage.getSalt()));
        Assert.assertEquals("[06, FE, 53, F3]", HeaderPage.toHexString(headerPage.getBaseSalt()));
        Assert.assertEquals("[ED, 42, B3, 47]", HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
    }
}
