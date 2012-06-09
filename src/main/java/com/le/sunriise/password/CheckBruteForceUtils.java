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
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class CheckBruteForceUtils {
    private static final Logger log = Logger.getLogger(CheckBruteForceUtils.class);

    static char[] createAlphabets() {
        List<char[]> arrays = new ArrayList<char[]>();
        arrays.add(GenBruteForce.ALPHABET_UPPERS);
        arrays.add(GenBruteForce.ALPHABET_DIGITS);
        arrays.add(GenBruteForce.ALPHABET_SPECIAL_CHARS_1);
        return createAlphabets(arrays);
    }

    private static char[] createAlphabets(List<char[]> arrays) {
        int size = 0;
        for (char[] array : arrays) {
            size += array.length;
        }
        char[] alphabets = new char[size];

        char[] src = null;
        int srcPos = 0;
        char[] dest = alphabets;
        int destPos = 0;
        int length = 0;
        for (char[] array : arrays) {
            src = array;
            destPos += length;
            length = src.length;
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
        return alphabets;
    }

    public static String checkUsingMask(File dbFile, int passwordLength, char[] mask, char[] alphabets) throws IOException {
        log.info("");
        
        log.info("dbFile=" + dbFile);
        HeaderPage headerPage = new HeaderPage(dbFile);

        log.info("passwordLength=" + passwordLength);

        if (mask != null) {
            log.info("mask=" + new String(mask) + ", mask.length=" + mask.length);
        } else  {
            log.info("mask=" + mask);
        }
        if (alphabets == null) {
            alphabets = createAlphabets();
        }
        
        log.info("alphabets.length=" + alphabets.length);
        if (log.isDebugEnabled()) {
            for (int i = 0; i < alphabets.length; i++) {
                log.debug(alphabets[i]);
            }
        }

        return checkUsingMask(headerPage, passwordLength, mask, alphabets);
    }

    public static String checkUsingMask(final HeaderPage headerPage, int passwordLength, char[] mask, char[] alphabets) {
        String password;
        CheckBruteForce checker = null;
        try {
            checker = new CheckBruteForce(headerPage, passwordLength, mask, alphabets);
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
        }
        return password;
    }

    public static String checkUsingMask(File dbFile, char[] mask, char[] alphabets) throws IOException {
        int passwordLength = -1;
        return checkUsingMask(dbFile, passwordLength, mask, alphabets);
    }

}
