package com.le.sunriise.tax;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.junit.Assert;
import org.junit.Test;

public class TaxInfoTest {
    @Test
    public void testSunset() throws IOException {
        File dbFile  = new File("src/test/data/sunset-sample-pwd.mny");
        String password = "123@abc!";
        List<TaxInfo> taxInfoList = TaxInfo.parse(dbFile, password);
        Assert.assertNotNull(taxInfoList);
        Assert.assertEquals(25, taxInfoList.size());
    }
}
