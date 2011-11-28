package com.le.sunriise.password;

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
        char [] mask = null;
        Counter gen = new Counter(passwordLength, mask, alphabets);
        gen.generate();
        count = gen.getCount();
        BigInteger expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        BigInteger actual = BigInteger.valueOf(count);

        log.info("  expected=" + expected);
        
        Assert.assertEquals(expected, actual);
    }

}
