/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

public class ResumeWithContextTest {
    private static final Logger log = Logger.getLogger(ResumeWithContextTest.class);

    private final class AlarmTask implements Runnable {
        private CheckBruteForce checker;
        private long millis;

        public AlarmTask(CheckBruteForce checker, long millis) {
            this.checker = checker;
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
                if (checker != null) {
                    checker.setTerminate(true);
                    log.info("Terminate checker.");
                }
            } catch (InterruptedException e) {
                log.warn(e);
            }
        }
    }

    @Test
    public void testCompareCursorIndexes() {
        int indexMax = GenBruteForce.ALPHABET_US_KEYBOARD_MNY.length;
        log.info("indexMax=" + indexMax);

        int index1[] = new int[8];
        int index2[] = new int[8];

        long value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(0, value1);

        long value2 = PasswordUtils.intArrayToLong(index2, indexMax);
        Assert.assertEquals(0, value2);

        Assert.assertEquals(value1, value2);

        index1[0] = 0;
        index1[1] = 0;
        index1[2] = 0;
        index1[3] = 0;
        index1[4] = 0;
        index1[5] = 0;
        index1[6] = 0;
        index1[7] = 1;
        value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(1, value1);

        index1[7] = indexMax - 1;
        value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(indexMax - 1, value1);

        index1[6] = 1;
        value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(131, value1);

        index1[6] = 2;
        value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(197, value1);

        index1[0] = 1;
        index1[1] = 1;
        index1[2] = 1;
        index1[3] = 1;
        index1[4] = 1;
        index1[5] = 1;
        index1[6] = 1;
        index1[7] = 1;
        value1 = PasswordUtils.intArrayToLong(index1, indexMax);
        Assert.assertEquals(5539086250303L, value1);

        index2[0] = 2;
        index2[1] = 2;
        index2[2] = 2;
        index2[3] = 2;
        index2[4] = 2;
        index2[5] = 2;
        index2[6] = 2;
        index2[7] = 2;
        value2 = PasswordUtils.intArrayToLong(index2, indexMax);
        Assert.assertEquals(11078172500606L, value2);

        Assert.assertTrue(PasswordUtils.compareCursorIndexes(index1, index2, indexMax) < 0);

        index1[0] = 1;
        index1[1] = 1;
        index1[2] = 1;
        index1[3] = 1;
        index1[4] = 1;
        index1[5] = 1;
        index1[6] = 1;
        index1[7] = 1;

        index2[0] = 2;
        index2[1] = 2;
        index2[2] = 2;
        index2[3] = 2;
        index2[4] = 2;
        index2[5] = 2;
        index2[6] = 2;
        index2[7] = 2;

        for (int i = 1; i < index1.length; i++) {
            index1[i] = indexMax - 1;
            Assert.assertTrue(PasswordUtils.compareCursorIndexes(index1, index2, indexMax) < 0);
        }

        index1[0] = 3;
        index1[1] = 1;
        index1[2] = 1;
        index1[3] = 1;
        index1[4] = 1;
        index1[5] = 1;
        index1[6] = 1;
        index1[7] = 1;

        index2[0] = 2;
        index2[1] = 2;
        index2[2] = 2;
        index2[3] = 2;
        index2[4] = 2;
        index2[5] = 2;
        index2[6] = 2;
        index2[7] = 2;

        Assert.assertTrue(PasswordUtils.compareCursorIndexes(index1, index2, indexMax) > 0);
    }

    @Ignore
    @Test
    public void testResumeWithContext() throws IOException {
        log.info("###");
        log.info("> START - testResumeWithContext");

        File contextFile = new File("bruteForceContext.json");
        contextFile.exists();
        if (!contextFile.delete()) {
            String message = "Cannot delete contextFile=" + contextFile;
            log.error(message);
            throw new IOException(message);
        }

        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        // 123@ABC!
        String expectedPassword = "123@ABC!";
        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        int loops = 0;
        mask = "****ABC!".toCharArray();
        while (password == null) {
            CheckBruteForce checker = null;
            try {
                HeaderPage headerPage = new HeaderPage(dbFile);

                int passwordLength = mask.length;
                checker = new CheckBruteForce(headerPage, passwordLength, mask, alphabets);
                if (contextFile.exists()) {
                    ObjectMapper mapper = new ObjectMapper();
                    GenBruteForceContext resumeContext = mapper.readValue(contextFile, GenBruteForceContext.class);
                    Assert.assertNotNull(resumeContext);
                    log.info("### RESUME from context, " + GenBruteForce.printCursorsIndex(resumeContext));
                    log.info("### RESUME from context, cursor=" + resumeContext.getCursor());
                    checker.setResumeContext(resumeContext);
                } else {
                    log.info("### RESUME-NO");
                }

                AlarmTask cmd = new AlarmTask(checker, 60 * 1000L);
                Thread t = new Thread(cmd);
                t.setDaemon(true);
                t.start();

                checker.check();
                password = checker.getPassword();
                log.info("password=" + password);
            } finally {
                if (checker != null) {
                    try {
                        checker.shutdown();
                    } finally {
                        checker = null;
                    }
                }
                loops++;
                log.info("LOOP-ITERATION DONE, loops=" + loops);
            }
        }

        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }
}