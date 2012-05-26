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

        log.info("dbFile=" + dbFile.getAbsolutePath());
        log.info("passwordLength=" + passwordLength);
        log.info("mask=" + ((mask == null)? mask : new String(mask)));
        log.info("alphabets=" + ((alphabets == null)? alphabets : new String(alphabets)));
        
        String password = null;
        try {
            password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
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
