package com.le.sunriise.json;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class JsonTest {

    @Test
    @Ignore
    public void test() throws IOException {
        File dbFile = new File("src/test/data/sunset01.mny");
        String password = null;
        OpenedDb openedDb = null;
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Assert.assertNotNull(openedDb);
            
        } finally {
            openedDb.close();
            openedDb = null;
        }
    }

}
