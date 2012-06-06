package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;

public class CheckBruteForceTest {
    private static final Logger log = Logger.getLogger(CheckBruteForceTest.class);

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
        char[] alphabets = null;
        String password = null;

        mask = new String("12@a!").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12@a*").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12@**").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("12***").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("1****").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = new String("0****").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNull(password);
    }

    private void logInputs(File dbFile, char[] mask, char[] alphabets) {
        log.info("###");
        log.info("dbFile=" + dbFile);
        // log.info("passwordLength=" + passwordLength);
        log.info("mask=" + ((mask == null) ? null : new String(mask)));
        log.info("alphabets=" + ((alphabets == null) ? alphabets : new String(alphabets)));
    }

    @Ignore
    @Test
    public void testBruteForce2() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd-5.mny");
        String expectedPassword = "12@a!";
        char[] alphabets = null;

        char[] mask;
        String password;

        mask = new String("*****").toCharArray();
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = null;
        logInputs(dbFile, mask, alphabets);
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }

    @Test
    public void testLength8Password() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        // 123@ABC!
        String expectedPassword = "123@ABC!";

        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        mask = "123@ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@ABC*".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@AB**".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@A***".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "123@****".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "*23@ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "**3@ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "***@ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

        mask = "****ABC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);

    }

    @Ignore
    @Test
    public void testLength8Password1() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        // 123@ABC!
        String expectedPassword = "123@ABC!";

        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        mask = "123*****".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }

    @Ignore
    @Test
    public void testLength8Password2() throws IOException {
        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        // 123@ABC!
        String expectedPassword = "123@ABC!";

        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        mask = "*****BC!".toCharArray();
        password = CheckBruteForceUtils.checkUsingMask(dbFile, mask, alphabets);
        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }

}
