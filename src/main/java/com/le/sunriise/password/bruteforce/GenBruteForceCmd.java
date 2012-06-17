package com.le.sunriise.password.bruteforce;

import java.math.BigInteger;

import org.apache.log4j.Logger;

public class GenBruteForceCmd {
    private static final Logger log = Logger.getLogger(GenBruteForceCmd.class);

    public static void main(String[] args) {
        int passwordLength = 5;
        char[] mask = null;
        mask = new String("*****").toCharArray();
        char[] alphabets = GenBruteForce.genChars('a', 'c');
        GenBruteForce gen = new GenBruteForce(passwordLength, mask, alphabets);
    
        long actual = gen.generate();
    
        BigInteger expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
    
        log.info("actual=" + actual + ", expected=" + expected);
    
    }

}
