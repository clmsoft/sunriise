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

package com.le.sunriise.password.dict;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.le.sunriise.password.HeaderPage;
import com.le.sunriise.password.dict.CheckDictionary;

public class CheckDictionaryTest {
    private static final Logger log = Logger.getLogger(CheckDictionaryTest.class);

    private final class JustCountWorker extends CheckDictionary {
        @Override
        public Callable<String> createWorker(final HeaderPage headerPage, final String testPassword, final AtomicLong counter) {
            Callable<String> worker = null;

            worker = new Callable<String>() {
                @Override
                public String call() throws Exception {
                    counter.getAndIncrement();
                    if (log.isDebugEnabled()) {
                        log.debug("testPassword=" + testPassword);
                    }
                    return null;
                }
            };
            return worker;
        }
    }

    @Test
    public void testFindingMatched() throws IOException {
        String dbFileName = null;
        String pathName = null;
        String expected = null;

        dbFileName = "src/test/data/sunset-sample-pwd.mny";

        // first entry
        pathName = "src/test/data/dict/matched/matched01.txt";
        expected = "123@ABC!";
        checkPassword(dbFileName, pathName, expected);
        // last entry
        pathName = "src/test/data/dict/matched/matched02.txt";
        expected = "123@ABC!";
        checkPassword(dbFileName, pathName, expected);
        // middle
        pathName = "src/test/data/dict/matched/matched03.txt";
        expected = "123@ABC!";
        checkPassword(dbFileName, pathName, expected);
    }

    @Test
    public void testTraversingPath() throws IOException {
        String dbFileName = null;
        String pathName = null;
        long expected = -1L;

        dbFileName = "src/test/data/sunset-sample-pwd.mny";

        // 3106L
        pathName = "src/test/data/dict/john.txt";
        expected = 3106L;
        checkCounter(dbFileName, pathName, expected);

        // 306706
        pathName = "src/test/data/dict/dir01/cain.txt";
        expected = 306706L;
        checkCounter(dbFileName, pathName, expected);

        // 0
        pathName = "src/test/data/dict/dir02/empty01.txt";
        expected = 0L;
        checkCounter(dbFileName, pathName, expected);

        pathName = "src/test/data/dict/dir02/empty02.txt";
        expected = 0L;
        checkCounter(dbFileName, pathName, expected);

        pathName = "src/test/data/dict/dir02/empty03.txt";
        expected = 0L;
        checkCounter(dbFileName, pathName, expected);

        pathName = "src/test/data/dict/dir02/empty04.txt";
        expected = 0L;
        checkCounter(dbFileName, pathName, expected);

        // 306706 + 370
        pathName = "src/test/data/dict/dir01/";
        expected = 307076L;
        checkCounter(dbFileName, pathName, expected);

        // 370 + 306706 + 3106 + 500 + (3107 *3)
        pathName = "src/test/data/dict/";
        expected = 320003L;
        checkCounter(dbFileName, pathName, expected);
    }

    private void checkCounter(String dbFileName, String pathName, long expected) throws IOException {
        HeaderPage headerPage;
        File path;
        CheckDictionary checker = null;
        try {
            checker = new JustCountWorker();
            File dbFile = new File(dbFileName);
            headerPage = new HeaderPage(dbFile);
            path = new File(pathName);
            String result = checker.check(headerPage, path);
            Assert.assertNull(result);
            AtomicLong counter = checker.getCounter();
            Assert.assertEquals(counter.get(), expected);
        } finally {
            if (checker != null) {
                checker.close();
                checker = null;
            }
        }
    }

    private void checkPassword(String dbFileName, String pathName, String expected) throws IOException {
        HeaderPage headerPage;
        File path;
        CheckDictionary checker = null;
        try {
            checker = new CheckDictionary();
            File dbFile = new File(dbFileName);
            headerPage = new HeaderPage(dbFile);
            path = new File(pathName);
            String result = checker.check(headerPage, path);
            Assert.assertNotNull(result);
            Assert.assertTrue(result.compareTo(expected) == 0);
        } finally {
            if (checker != null) {
                checker.close();
                checker = null;
            }
        }
    }
}
