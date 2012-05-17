package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

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
    public void test() {
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

    @Test
    public void testBruteForce() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd-5.mny");
        char[] mask = null;
        int passwordLength = 5;
        char[] alphabets = null;
        String password = null;

        mask = new String("12@a!").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

        mask = new String("12@a*").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

        mask = new String("12@**").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

        mask = new String("12***").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

        mask = new String("1****").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

        mask = new String("0****").toCharArray();
        password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        Assert.assertNull(password);
//        Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);
        
        boolean longRunning = false;
        if (longRunning) {
            mask = new String("*****").toCharArray();
            password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
            Assert.assertNotNull(password);
            Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);

            mask = null;
            password = SingleThreadBruteForce.checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
            Assert.assertNotNull(password);
            Assert.assertTrue(password.compareToIgnoreCase("12@a!") == 0);
        }
    }
}
