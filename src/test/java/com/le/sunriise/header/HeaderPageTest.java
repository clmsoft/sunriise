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

package com.le.sunriise.header;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.healthmarketscience.jackcess.JetFormat;
import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.misc.MnyTestFile;

public class HeaderPageTest {

    @Test
    public void test() throws IOException {
        File dbFile = null;

        String fileName = "sunset-sample-pwd-5.mny";
        MnyTestFile testFile = MnyTestFile.getSampleFile(fileName);
        Assert.assertNotNull(testFile);
        dbFile = new File(testFile.getFileName());
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

        String fileName = "sunset-sample.mbf";
        MnyTestFile testFile = MnyTestFile.getSampleFile(fileName);
        Assert.assertNotNull(testFile);
        dbFile = new File(testFile.getFileName());
        HeaderPage headerPage = new HeaderPage(dbFile);

        Assert.assertEquals(JetFormat.VERSION_MSISAM, headerPage.getJetFormat());
        Assert.assertTrue(headerPage.isNewEncryption());
        Assert.assertTrue(headerPage.isUseSha1());
        Assert.assertEquals("[06, FE, 53, F3, 6C, F5, E3, 40]", HeaderPage.toHexString(headerPage.getSalt()));
        Assert.assertEquals("[06, FE, 53, F3]", HeaderPage.toHexString(headerPage.getBaseSalt()));
        Assert.assertEquals("[ED, 42, B3, 47]", HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
    }
}
