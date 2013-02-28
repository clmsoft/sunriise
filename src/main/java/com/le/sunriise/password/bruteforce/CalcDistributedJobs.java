/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password.bruteforce;

import java.math.BigInteger;

import org.apache.log4j.Logger;

public class CalcDistributedJobs {
    private static final Logger log = Logger.getLogger(CalcDistributedJobs.class);

    private static final class GenDistributedJobs extends GenBruteForce {
        private int maxJobs;
        private int count = 0;
        final private char lastAlphabetsChar;
        private int maxPasswordLength;

        public GenDistributedJobs(GenBruteForceContext context, int maxJobs, int maxPasswordLength) {
            super(context);
            this.maxJobs = maxJobs;
            this.lastAlphabetsChar = getContext().getAlphabets()[getContext().getAlphabets().length - 1];
            this.maxPasswordLength = maxPasswordLength;
        }

        @Override
        public void notifyResult(String string) {
            log.info("" + count + ", " + toMaskString(string));
            count++;
            if (count >= maxJobs) {
                char c = string.charAt(string.length() - 1);
                if (c == lastAlphabetsChar) {
                    // setTerminate(true);
                }
            }
        }

        private String toMaskString(String string) {
            StringBuilder sb = new StringBuilder();
            sb.append(string);
            while (sb.length() < maxPasswordLength) {
                sb.append("*");
            }
            return sb.toString();
        }
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        char[] mask = "***".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;

        log.info("> START CalcDistributedJobs");
        int maxJobs = 1000;
        final int alphabetsLenghth = alphabets.length;
        final int maxPasswordLength = 8;
        int passwordLength = 0;
        for (int i = 0; i < maxPasswordLength; i++) {
            passwordLength = i;
            BigInteger count = GenBruteForce.calculateExpected(passwordLength, alphabetsLenghth);
            log.info("passwordLength=" + passwordLength + ", alphabetsLenghth=" + alphabetsLenghth + ", count=" + count);

            if (count.longValue() > maxJobs) {
                break;
            }
        }

        char[] shortMask = new char[passwordLength];
        for (int i = 0; i < shortMask.length; i++) {
            shortMask[i] = '*';
        }
        GenBruteForceContext context = new GenBruteForceContext(mask, alphabets);
        GenBruteForce gen = new GenDistributedJobs(context, maxJobs, maxPasswordLength);
        gen.generate();
    }

}
