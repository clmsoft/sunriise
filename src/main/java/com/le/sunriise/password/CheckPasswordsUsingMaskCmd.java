package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class CheckPasswordsUsingMaskCmd {
    private static final Logger log = Logger.getLogger(CheckPasswordsUsingMaskCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        int passwordLength = -1;
        char[] mask = null;
        char[] alphabets = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            try {
                passwordLength = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                passwordLength = -1;
            }
            if (passwordLength < 0) {
                mask = toMask(args[1]);
                passwordLength = mask.length;
            } else {
                mask = null;
            }
            alphabets = toAlphabets("us-keyboard-mny");
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            try {
                passwordLength = Integer.valueOf(args[1]);
            } catch (NumberFormatException e) {
                passwordLength = -1;
            }
            if (passwordLength < 0) {
                mask = toMask(args[1]);
                passwordLength = mask.length;
            } else {
                mask = null;
            }
            alphabets = toAlphabets(args[2]);
        } else {
            Class<CheckPasswordsUsingMaskCmd> clz = CheckPasswordsUsingMaskCmd.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny {mask | passwordLength} [alphabets]");
            System.exit(1);
        }

        String password = null;
        try {
            password = SingleThreadBruteForce.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("password=" + password);
        }
    }

    private static char[] toMask(String string) {
        return string.toCharArray();
    }

    private static char[] toAlphabets(String string) {
        char[] alphabets = null;
        if (string.compareToIgnoreCase("upper-only") == 0) {
            alphabets = GenBruteForce.ALPHABET_UPPERS;
        } else if (string.compareToIgnoreCase("lower-only") == 0) {
            alphabets = GenBruteForce.ALPHABET_LOWERS;
        } else if (string.compareToIgnoreCase("upper-lower") == 0) {
            alphabets = GenBruteForce.appendCharArrays(GenBruteForce.ALPHABET_UPPERS, GenBruteForce.ALPHABET_LOWERS);
        } else if (string.compareToIgnoreCase("upper-lower-digit") == 0) {
            alphabets = GenBruteForce.appendCharArrays(GenBruteForce.ALPHABET_UPPERS, GenBruteForce.ALPHABET_LOWERS,
                    GenBruteForce.ALPHABET_DIGITS);
        } else if (string.compareToIgnoreCase("us-keyboard") == 0) {
            alphabets = GenBruteForce.ALPHABET_US_KEYBOARD;
        } else if (string.compareToIgnoreCase("us-keyboard-mny") == 0) {
            alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        } else {
            alphabets = string.toCharArray();
        }

        return alphabets;
    }

}
