package com.le.sunriise.password;

import java.math.BigInteger;

import org.apache.log4j.Logger;

public class GenBruteForce {
    private static final Logger log = Logger.getLogger(GenBruteForce.class);

    public static char[] ALPHABET_UPPERS = genChars('A', 'Z');
    public static char[] ALPHABET_LOWERS = genChars('a', 'z');
    public static char[] ALPHABET_DIGITS = genChars('0', '9');
    public static char[] ALPHABET_SPECIAL_CHARS_1 = { '!', '@', '#' };

    private final char[] alphabets;

    private final int passwordLength;

    private char[] buffer;

    private char[] mask;

    private boolean terminate = false;

    public GenBruteForce(int passwordLength, char[] mask, char[] alphabets) {
        this.passwordLength = passwordLength;
        this.mask = mask;
        this.alphabets = alphabets;

        buffer = new char[this.passwordLength + 1];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = '\0';
        }
    }

    public static char[] genChars(char start, char end) {
        StringBuilder sb = new StringBuilder();
        for (char c = start; c <= end; c++) {
            sb.append(c);
        }
        return sb.toString().toCharArray();
    }

    public long generate() {
        return generateString(buffer, alphabets, mask);
    }

    private long generateString(char[] buffer, char[] alphabet, char[] mask) {
        long count = 0;
        int passwordLength = 0;
        int alphabetLen = alphabet.length;
        if (!terminate) {
            count += generateString(buffer, buffer.length, mask, passwordLength, alphabet, alphabetLen);
        } else {
            log.warn("Terminate early at passwordLength=" + passwordLength + ", alphabetLen=" + alphabetLen);
        }
        return count;
    }

    private long generateString(char[] buffer, int bufferLen, char[] mask, int cursor, char[] alphabets, int alphabetsLen) {
        long count = 0;

        // cursor is zero-base index
        if (cursor >= (bufferLen - 1)) {
            // done, nothing more to do
            return count;
        }

        // if the mask has
        // loop through the alphabets
        if ((mask != null) && (mask[cursor] != '*')) {
            char c = mask[cursor];
            if (log.isDebugEnabled()) {
                log.debug("Found known char=" + c  + ", cursor=" + cursor);
            }
            buffer[cursor] = c;
            notifyResult(new String(buffer, 0, cursor + 1));
            count++;
            if (!terminate) {
                long n = generateString(buffer, bufferLen, mask, cursor + 1, alphabets, alphabetsLen);
                count += n;
            } else {
                log.warn("Terminate early at cursor=" + cursor + ", alphabet=" + c + ", alphabetLen=" + alphabetsLen);
            }
        } else {
            for (int i = 0; i < alphabetsLen; i++) {
                char c = alphabets[i];
                buffer[cursor] = c;
                notifyResult(new String(buffer, 0, cursor + 1));
                count++;
                if (!terminate) {
                    long n = generateString(buffer, bufferLen, mask, cursor + 1, alphabets, alphabetsLen);
                    count += n;
                } else {
                    log.warn("Terminate early at cursor=" + cursor + ", alphabet=" + c + ", alphabetLen=" + alphabetsLen);
                    break;
                }
            }
        }

        if (cursor == 1) {
            if (log.isDebugEnabled()) {
                log.debug("count=" + count + ", cursor=" + cursor + ", alphabetLen=" + alphabetsLen);
            }
        }

        return count;
    }

    public void notifyResult(String string) {
        log.info(string);
    }

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
