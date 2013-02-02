package com.le.sunriise.json;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.le.sunriise.Utils;
import com.le.sunriise.export.ExportToJSON;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.viewer.OpenedDb;

public class JsonTest {
    private static final Logger log = Logger.getLogger(JsonTest.class);

    private static final String DEFAULT_EXPECT_DIR = "src/test/data/json";

    private final class ExporterTester extends ExportToJSON {
        private final File expectedDir;
    
        private ExporterTester(File expectedDir) {
            this.expectedDir = expectedDir;
        }
    
        @Override
        protected void exportMnyContext(File outDir) throws IOException {
            Assert.assertNotNull(mnyContext);
    
            super.exportMnyContext(outDir);
    
            String fileName = null;
    
            fileName = "categories.json";
            checkFile(expectedDir, outDir, fileName);
    
            fileName = "currencies.json";
            checkFile(expectedDir, outDir, fileName);
    
            fileName = "payees.json";
            checkFile(expectedDir, outDir, fileName);
    
            fileName = "securities.json";
            checkFile(expectedDir, outDir, fileName);
        }
    
        protected void checkFile(final File expectedDir, File outDir, String fileName) throws IOException {
            File file = null;
            File expectedFile = null;
            HashFunction hashFunction = Hashing.sha1();
    
            file = new File(outDir, fileName);
            expectedFile = new File(expectedDir, fileName);
            log.info("Check file=" + file);
            log.info("      expectedFile=" + expectedFile);
            HashCode hashCode = com.google.common.io.Files.hash(file, hashFunction);
            HashCode expectedHashCode = com.google.common.io.Files.hash(expectedFile, hashFunction);
            
            Assert.assertTrue(hashCode.equals(expectedHashCode));
        }
    
        @Override
        protected void exportAccount(Account account, File outDir) throws IOException {
            Assert.assertNotNull(account);
            
            super.exportAccount(account, outDir);
        }
    }

    @Ignore
    @Test
    public void test() throws IOException {
        for (MnyTestFile file : MnyTestFile.sampleFiles) {
            String dbFilename = file.getFileName();
            String password = file.getPassword();
            testJsonExport(dbFilename, password);
        }
    }

    private void testJsonExport(String dbFilename, String password) throws IOException {
        final File expectedDir = new File(new File(DEFAULT_EXPECT_DIR), new File(dbFilename).getName());

        ExportToJSON exporter = new ExporterTester(expectedDir);
        testJsonExport(dbFilename, password, exporter);
    }

    private void testJsonExport(String dbFilename, String password, ExportToJSON exporter) throws IOException {
        File dbFile = new File(dbFilename);
        OpenedDb openedDb = null;
        File outDir = new File("target/t_json");
        outDir.mkdirs();
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Assert.assertNotNull(openedDb);

            exporter.export(openedDb, outDir);
        } finally {
            openedDb.close();
            openedDb = null;

            if (outDir != null) {
            }
        }
    }

}
