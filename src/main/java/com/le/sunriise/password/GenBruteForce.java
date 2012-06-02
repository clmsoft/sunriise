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

import java.io.CharArrayWriter;
import java.io.IOException;
import java.math.BigInteger;
import org.apache.log4j.Logger;

public class GenBruteForce {
    private static final Logger log = Logger.getLogger(GenBruteForce.class);

    private static final char DEFAULT_MASK_WILD_CHAR = '*';

    private static final char DEFAULT_MASK_SKIP_CHAR = '+';

    public static char[] ALPHABET_UPPERS = genChars('A', 'Z');
    public static char[] ALPHABET_LOWERS = genChars('a', 'z');
    public static char[] ALPHABET_DIGITS = genChars('0', '9');
    public static char[] ALPHABET_SPECIAL_CHARS_1 = { '!', '@', '#' };
    public static char[] ALPHABET_SPECIAL_CHARS_2 = "$%^&*()_+-=".toCharArray();
    public static char[] ALPHABET_SPECIAL_CHARS_3 = "[]\\{}|;':\"".toCharArray();
    public static char[] ALPHABET_SPECIAL_CHARS_4 = ",./<>?".toCharArray();
    public static char[] ALPHABET_US_KEYBOARD = createUSKeyboardAlphabets();
    public static char[] ALPHABET_US_KEYBOARD_MNY = createUSKeyboardMnyAlphabets();

    private boolean terminate = false;

    private char maskSkipChar = DEFAULT_MASK_SKIP_CHAR;

    private char maskWildChar = DEFAULT_MASK_WILD_CHAR;

    private GenBruteForceContext context;

    private String currentResult;

    private static char[] createUSKeyboardMnyAlphabets() {
        return appendCharArrays(ALPHABET_UPPERS, ALPHABET_DIGITS, ALPHABET_SPECIAL_CHARS_1, ALPHABET_SPECIAL_CHARS_2,
                ALPHABET_SPECIAL_CHARS_3, ALPHABET_SPECIAL_CHARS_4);
    }

    private static char[] createUSKeyboardAlphabets() {
        return appendCharArrays(ALPHABET_UPPERS, ALPHABET_LOWERS, ALPHABET_DIGITS, ALPHABET_SPECIAL_CHARS_1,
                ALPHABET_SPECIAL_CHARS_2, ALPHABET_SPECIAL_CHARS_3, ALPHABET_SPECIAL_CHARS_4);
    }

    public static char[] appendCharArrays(char[]... alphabets) {
        CharArrayWriter writer = null;
        char[] results = null;
        try {
            writer = new CharArrayWriter();
            for (int i = 0; i < alphabets.length; i++) {
                char[] alphabet = alphabets[i];
                writer.write(alphabet);
            }
            writer.flush();
            results = writer.toCharArray();
        } catch (IOException e) {
            log.warn(e);
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
        return results;
    }

    public static char[] genChars(char start, char end) {
        StringBuilder sb = new StringBuilder();
        for (char c = start; c <= end; c++) {
            sb.append(c);
        }
        return sb.toString().toCharArray();
    }

    public GenBruteForce(int passwordLength, char[] mask, char[] alphabets) {
        this.context = new GenBruteForceContext(passwordLength, mask, alphabets);
    }

    public long generate() {
        return generateString(context.getBuffer(), context.getAlphabets(), context.getMask());
    }

    private long generateString(char[] buffer, char[] alphabets, char[] mask) {
        long count = 0;
        int passwordLength = 0;
        int alphabetsLen = alphabets.length;
        if (!terminate) {
            GenBruteForceContext newContext = new GenBruteForceContext(buffer, buffer.length, mask, passwordLength, alphabets,
                    alphabetsLen);
            newContext.setCurrentCursorIndex(context.getCurrentCursorIndex());
            count += generateString(newContext);
        } else {
            log.warn("Terminate early at passwordLength=" + passwordLength + ", alphabetsLen=" + alphabetsLen);
        }
        return count;
    }

    private long generateString(GenBruteForceContext context) {
        this.setContext(context);

        long count = 0;

        // cursor is zero-base index
        if (context.getCursor() >= (context.getBufferLen() - 1)) {
            // done, nothing more to do
            return count;
        }

        char maskChar = maskWildChar;
        if (context.getMask() == null) {
            maskChar = maskWildChar;
        } else {
            maskChar = context.getMask()[context.getCursor()];
        }

        if (isWildChar(maskChar) || isSkipChar(maskChar)) {
            count = handleSpecialCharInMask(context, maskChar, count);
        } else {
            count = handleKnownCharInMask(context, maskChar, count);
        }

        // for debug - want to see the start of the loop
        if (context.getCursor() == 1) {
            if (log.isDebugEnabled()) {
                log.debug("count=" + count + ", cursor=" + context.getCursor() + ", alphabetLen=" + context.getAlphabetsLen());
            }
        }

        return count;
    }

    private long handleSpecialCharInMask(GenBruteForceContext context, char maskChar, long count) {
        // loop through the alphabets
        for (int i = 0; i < context.getAlphabetsLen(); i++) {
            char c = context.getAlphabets()[i];

            context.getBuffer()[context.getCursor()] = c;
            context.getCurrentCursorIndex()[context.getCursor()] = i;

            if (isSkipChar(maskChar)) {
                // TODO: no need to check
            } else {
                currentResult = new String(context.getBuffer(), 0, (context.getCursor() + 1));
                notifyResult(currentResult);
            }

            count++;

            if (!terminate) {
                GenBruteForceContext newContext = new GenBruteForceContext(context.getBuffer(), context.getBufferLen(),
                        context.getMask(), context.getCursor() + 1, context.getAlphabets(), context.getAlphabetsLen());
                newContext.setCurrentCursorIndex(context.getCurrentCursorIndex());
                long n = generateString(newContext);
                count += n;
            } else {
                log.warn("Terminate early at cursor=" + context.getCursor() + ", alphabet=" + c + ", alphabetLen="
                        + context.getAlphabetsLen());
                break;
            }
        }
        return count;
    }

    private long handleKnownCharInMask(GenBruteForceContext context, char maskChar, long count) {
        if (log.isDebugEnabled()) {
            log.debug("Found known char=" + maskChar + ", cursor=" + context.getCursor());
        }

        context.getCurrentCursorIndex()[context.getCursor()] = context.getAlphabetsLen() - 1;
        context.getBuffer()[context.getCursor()] = maskChar;

        currentResult = new String(context.getBuffer(), 0, (context.getCursor() + 1));
        notifyResult(currentResult);
        count++;

        if (!terminate) {
            GenBruteForceContext newContext = new GenBruteForceContext(context.getBuffer(), context.getBufferLen(),
                    context.getMask(), context.getCursor() + 1, context.getAlphabets(), context.getAlphabetsLen());
            newContext.setCurrentCursorIndex(context.getCurrentCursorIndex());
            long n = generateString(newContext);
            count += n;
        } else {
            log.warn("Terminate early at cursor=" + context.getCursor() + ", alphabet=" + maskChar + ", alphabetsLen="
                    + context.getAlphabetsLen());
        }

        return count;
    }

    private boolean isSkipChar(char maskChar) {
        boolean rv = false;

        // TODO - not implement yet

        rv = (maskChar == maskSkipChar);

        return rv;
    }

    private boolean isWildChar(char maskChar) {
        return maskChar == maskWildChar;
    }

    public void notifyResult(String string) {
        log.info(string);
    }

/**
     * {@literal
     * KS = L^(m) + L^(m+1) + L^(m+2) + ........ + L^(M)
     * where
     * L = character set length
     * m = min length of the key
     * M = max length of the key 
     * @}
     * 
     * @param passwordLength
     * @param alphabetsLenghth
     * @return
     */
    public static BigInteger calculateExpected(int passwordLength, int alphabetsLenghth) {
        // (62 ^ 1) + (62 ^ 2) + (62 ^ 3) + (62 ^ 4) +
        // (62 ^ 5) + (62 ^ 6) + (62 ^ 7) + (62 ^ 8)
        // 4^2 = 16
        // 4^1 = 4
        BigInteger expected = BigInteger.valueOf(0);
        if (passwordLength <= 0) {
            expected = BigInteger.ZERO;
        } else {
            expected = BigInteger.valueOf((long) Math.pow(alphabetsLenghth, passwordLength));
            expected = expected.add(calculateExpected(passwordLength - 1, alphabetsLenghth));
        }
        return expected;
    }

    public boolean isTerminate() {
        return terminate;
    }

    public void setTerminate(boolean terminate) {
        this.terminate = terminate;
    }

    public int[] getCurrentCursorIndex() {
        return context.getCurrentCursorIndex();
    }

    public char getMaskSkipChar() {
        return maskSkipChar;
    }

    public void setMaskSkipChar(char maskSkipChar) {
        this.maskSkipChar = maskSkipChar;
    }

    public char getMaskWildChar() {
        return maskWildChar;
    }

    public void setMaskWildChar(char maskWildChar) {
        this.maskWildChar = maskWildChar;
    }

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

    public GenBruteForceContext getContext() {
        return context;
    }

    public void setContext(GenBruteForceContext context) {
        this.context = context;
    }

    public String getCurrentResult() {
        return currentResult;
    }

}
