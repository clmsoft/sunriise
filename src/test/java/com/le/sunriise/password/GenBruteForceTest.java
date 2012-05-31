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

package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import com.healthmarketscience.jackcess.JetFormat;

public class GenBruteForceTest {
    private static final Logger log = Logger.getLogger(GenBruteForceTest.class);

    private final class Counter extends GenBruteForce {
        private int count;

        private Counter(int passwordLength, char[] mask, char[] alphabets) {
            super(passwordLength, mask, alphabets);
        }

        @Override
        public void notifyResult(String string) {
            if (log.isDebugEnabled()) {
                log.debug(string);
            }
            count++;
        }

        public int getCount() {
            return count;
        }
    }

    /**
     * Test the GenBruteForce.calculateExpected() function against hard-code
     * expected value.
     */
    @Test
    public void testCalculateExpected() {
        int passwordLength;
        char[] alphabets;
        BigInteger actual;
        BigInteger expected;

        passwordLength = 7;
        alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ".toCharArray();
        actual = BigInteger.valueOf(8353082582L);
        expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        Assert.assertEquals(expected, actual);

        passwordLength = 7;
        alphabets = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789!@#$%^&*()-_+=~`[]{}|\\:;\"'<>,.?/".toCharArray();
        actual = BigInteger.valueOf(6823331935124L);
        expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        Assert.assertEquals(expected, actual);

        passwordLength = 8;
        // 92
        alphabets = GenBruteForce.ALPHABET_US_KEYBOARD;
        actual = BigInteger.valueOf(5188586409742380L);
        expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void testForMatchingCount() {
        int passwordLength = 0;
        char[] alphabets = null;

        passwordLength = 0;
        alphabets = GenBruteForce.genChars('a', 'a');
        testCount(passwordLength, alphabets);

        passwordLength = 1;
        alphabets = GenBruteForce.genChars('a', 'a');
        testCount(passwordLength, alphabets);

        passwordLength = 1;
        alphabets = GenBruteForce.genChars('a', 'b');
        testCount(passwordLength, alphabets);

        passwordLength = 2;
        alphabets = GenBruteForce.genChars('a', 'd');
        testCount(passwordLength, alphabets);

        passwordLength = 2;
        alphabets = GenBruteForce.genChars('a', 'z');
        testCount(passwordLength, alphabets);

        passwordLength = 4;
        alphabets = GenBruteForce.genChars('a', 'z');
        testCount(passwordLength, alphabets);

        passwordLength = 6;
        alphabets = GenBruteForce.genChars('a', 'z');
        testCount(passwordLength, alphabets);
    }

    /**
     * For a given password length and alphabets, simulate running the password
     * checking and match it against the calculated expected value.
     * 
     * @param passwordLength
     * @param alphabets
     */
    private void testCount(int passwordLength, char[] alphabets) {
        log.info("Testing passwordLength=" + passwordLength + ", alphabets.length=" + alphabets.length);

        int count;
        char[] mask = null;
        Counter gen = new Counter(passwordLength, mask, alphabets);
        gen.generate();
        count = gen.getCount();
        BigInteger expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        BigInteger actual = BigInteger.valueOf(count);

        log.info("  expected=" + expected);

        Assert.assertEquals(expected, actual);
    }

    /**
     * Test brute-force method.
     * 
     * @throws IOException
     */
    @Test
    public void testBruteForce() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd-5.mny");
        String expectedPassword = "12@a!";

        char[] mask = null;
        int passwordLength = 5;
        char[] alphabets = null;
        String password = null;

        mask = new String("12@a!").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12@a*").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12@**").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12***").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("1****").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("0****").toCharArray();
        logInputs(dbFile, mask, passwordLength, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNull(password);

        boolean longRunning = false;
        if (longRunning) {
            mask = new String("*****").toCharArray();
            logInputs(dbFile, mask, passwordLength, alphabets);
            password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
            Assert.assertNotNull(password);
            Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

            mask = null;
            logInputs(dbFile, mask, passwordLength, alphabets);
            password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
            Assert.assertNotNull(password);
            Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
        }
    }

    @Test
    public void testFalsePositive() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");

        HeaderPage headerPage = new HeaderPage(dbFile);
        HeaderPageOnlyPasswordChecker checker = new HeaderPageOnlyPasswordChecker(headerPage);

        boolean matched = false;
        String testPassword = "1238.NQE";
        matched = checker.check(testPassword);
        if (matched) {
            matched = PasswordUtils.doubleCheck(headerPage, testPassword);
        }
        Assert.assertFalse(matched);
    }

    @Test
    public void testLength8Password() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        int passwordLength = -1; // don't know
        // 123@ABC!
        String expectedPassword = "123@ABC!";
        boolean skip = true;

        if (skip) {
            return;
        }

        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        mask = "123@ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@ABC*".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@AB**".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@A***".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@****".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123*****".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }

    private void logInputs(File dbFile, char[] mask, int passwordLength, char[] alphabets) {
        log.info("###");
        log.info("dbFile=" + dbFile);
        log.info("passwordLength=" + passwordLength);
        log.info("mask=" + ((mask == null) ? null : new String(mask)));
        log.info("alphabets=" + ((alphabets == null) ? alphabets : new String(alphabets)));
    }
}
