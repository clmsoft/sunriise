package com.le.sunriise.json;

import java.io.File;
import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;

import com.google.common.hash.HashCode;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hashing;
import com.le.sunriise.Utils;
import com.le.sunriise.export.ExportToJSON;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.viewer.OpenedDb;

public class JsonTest {

    @Test
    public void test() throws IOException {
        String dbFilename = "src/test/data/sunset01.mny";
        String password = null;
        ExportToJSON exporter = new ExportToJSON() {
            @Override
            protected void exportMnyContext(File outDir) throws IOException {
                Assert.assertNotNull(mnyContext);

                super.exportMnyContext(outDir);

                File file1 = null;
                File file2 = null;
                HashFunction hashFunction = Hashing.sha1();

                file1 = new File(outDir, "categories.json");
                file2 = new File(outDir, "categories.json");
                HashCode hashCode1 = com.google.common.io.Files.hash(file1, hashFunction);
                HashCode hashCode2 = com.google.common.io.Files.hash(file2, hashFunction);
                Assert.assertTrue(hashCode1.equals(hashCode2));
            }

            @Override
            protected void exportAccount(Account account, File outDir) throws IOException {
                Assert.assertNotNull(account);
            }
        };
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
