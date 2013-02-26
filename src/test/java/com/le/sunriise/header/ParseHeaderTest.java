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
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.misc.MnyTestFile;
import com.le.sunriise.viewer.OpenedDb;
import com.le.sunriise.viewer.TableUtils;

public class ParseHeaderTest {

    @Test
    public void test() throws IOException {
        String fileName = "sunset01.mny";
        MnyTestFile testFile = MnyTestFile.getSampleFile(fileName);
        Assert.assertNotNull(testFile);
        File dbFile = new File(testFile.getFileName());
        String password = testFile.getPassword();

        OpenedDb openedDb = null;
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
