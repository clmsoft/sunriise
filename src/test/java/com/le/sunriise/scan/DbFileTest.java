package com.le.sunriise.scan;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;

import com.le.sunriise.misc.MnyTestFile;
import com.le.sunriise.scan.DbFile;

public class DbFileTest {
    private static final Logger log = Logger.getLogger(DbFileTest.class);

    @Test
    public void test() throws IOException {
        for (MnyTestFile testFile : MnyTestFile.SAMPLE_FILES) {
            if (testFile.isBackup()) {
                continue;
            }

            DbFile dbFile = new DbFile(testFile.getFileName(), testFile.getPassword());
            log.info("dbFileName=" + testFile.getFileName());
            Assert.assertNotNull(dbFile);
            Assert.assertTrue(dbFile.isPasswordIsValid());
            Assert.assertTrue(dbFile.getLeftOverBytes() == 0);
        }
    }
}
