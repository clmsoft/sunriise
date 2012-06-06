package com.le.sunriise.password;

import org.apache.log4j.Logger;

public class GenBruteForce2Chars {
    private static final Logger log = Logger.getLogger(GenBruteForce2Chars.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        char[] mask = "**".toCharArray();
        char[] alphabets = GenBruteForce.genChars('a', 'e');
        
        GenBruteForceContext context = new GenBruteForceContext(mask, alphabets);
        GenBruteForce gen = new GenBruteForce(context);
        gen.generate();
    }

}
