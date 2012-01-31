package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.viewer.OpenedDb;
import com.le.sunriise.viewer.TableUtils;

public class ParseHeaderTest {

    @Test
    public void test() throws IOException {
        File dbFile = new File("src/test/data/sunset01.mny");
        OpenedDb openedDb = null;
        String password = null;
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Assert.assertNotNull(openedDb);
            Database db = openedDb.getDb();
            Assert.assertNotNull(openedDb);

            int count = 0;
            Set<String> names = db.getTableNames();
            Assert.assertNotNull(names);
            for (String name : names) {
                Table table = db.getTable(name);
                Assert.assertNotNull(table);
                TableUtils.parseHeaderInfo(table, openedDb);
                TableUtils.parseIndexInfo(table);
                TableUtils.parseKeyInfo(table);
                TableUtils.parseTableMetaData(table);
                count++;
            }
            Assert.assertEquals(83, count);
        } finally {
            if (openedDb != null) {
                openedDb.close();
            }
        }
    }

}
