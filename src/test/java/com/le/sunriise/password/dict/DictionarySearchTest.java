package com.le.sunriise.password.dict;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

import com.le.sunriise.password.ui.DictionarySearch;

public class DictionarySearchTest {
    private static final Logger log = Logger.getLogger(DictionarySearchTest.class);

    private final class MyDictionarySearch extends DictionarySearch {
        private String result = null;

        public String getResult() {
            return result;
        }

        private MyDictionarySearch() {
            super();
        }

        @Override
        protected void notifyResult(String matchedPassword) {
            super.notifyResult(matchedPassword);
            this.result = matchedPassword;
        }
    }

    @Test
    public void testNoReUse() throws IOException {
        boolean reUseChecker = false;
        test01(reUseChecker);
    }

    @Ignore
    @Test
    public void testReUse() throws IOException {
        boolean reUseChecker = true;
        test01(reUseChecker);
    }

    private void test01(boolean reUseChecker) throws IOException {
        String dbFileName = null;
        String pathName = null;
        String expected = null;

        MyDictionarySearch searcher = new MyDictionarySearch();

        dbFileName = "src/test/data/money2001-pwd.mny";
        pathName = "src/test/data/dict/";
        expected = "TEST12345";
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/money2002.mny";
        pathName = "src/test/data/dict/";
        expected = null;
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/money2004-pwd.mny";
        pathName = "src/test/data/dict/";
        expected = "123@ABC!";
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/money2005-pwd.mny";
        pathName = "src/test/data/dict/";
        expected = "123@ABC!";
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/money2008-pwd.mny";
        pathName = "src/test/data/dict/";
        expected = "Test12345";
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/sunset-sample-pwd.mny";
        pathName = "src/test/data/dict/";
        expected = "123@ABC!";
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/sunset-sample-pwd-5.mny";
        pathName = "src/test/data/dict/";
        expected = "12@a!";
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);

        dbFileName = "src/test/data/sunset-sample-pwd-6.mny";
        pathName = "src/test/data/dict/";
        expected = null;
        if (!reUseChecker) {
            searcher = new MyDictionarySearch();
        }
        checkPassword(dbFileName, pathName, expected, searcher);
    }

    private void checkPassword(String dbFileName, String pathName, String expected, MyDictionarySearch searcher) throws IOException {
        log.info("> dbFileName=" + dbFileName);
        try {
            File dbFile = new File(dbFileName);
            File path = new File(pathName);
            searcher.startCheck(dbFile, path);

            String result = searcher.getResult();
            log.info("result=" + result);
            if (expected != null) {
                Assert.assertNotNull(result);
                Assert.assertTrue(result.compareTo(expected) == 0);
            } else {
                Assert.assertNull(result);
            }
        } finally {
        }
    }
}
