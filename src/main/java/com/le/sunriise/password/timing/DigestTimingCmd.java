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
package com.le.sunriise.password.timing;

import org.apache.log4j.Logger;
import com.le.sunriise.StopWatch;
import com.le.sunriise.password.BouncyCastleUtils;
import com.le.sunriise.password.PasswordUtils;

public class DigestTimingCmd {
    private static final Logger log = Logger.getLogger(PasswordUtils.class);
    private static final int PASSWORD_LENGTH = 0x28;
    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int passwordLength = PASSWORD_LENGTH;
        int maxIteration = DEFAULT_MAX_ITERATIONS;
        boolean useSha1 = true;

        if (args.length == 0) {
            // default is above
        } else if (args.length == 1) {
            passwordLength = RC4TimingCmd.intValueOf(args[0], passwordLength);
        } else if (args.length == 2) {
            passwordLength = RC4TimingCmd.intValueOf(args[0], passwordLength);
            maxIteration = RC4TimingCmd.intValueOf(args[1], maxIteration);
        } else if (args.length == 3) {
            passwordLength = RC4TimingCmd.intValueOf(args[0], passwordLength);
            maxIteration = RC4TimingCmd.intValueOf(args[1], maxIteration);
            useSha1 = DigestTimingCmd.boolValueOf(args[2], useSha1);
        } else {
            Class<DigestTimingCmd> clz = DigestTimingCmd.class;
            System.out.println("Usage: java " + clz.getName() + "[passwordLength maxIteration useSha1]");
            System.out.println("  passwordLength (default=" + passwordLength + ")");
            System.out.println("  maxIteration (default=" + maxIteration + ")");
            System.out.println("  useSha1 (default=" + useSha1 + ")");
            System.exit(1);
        }
        doTiming(passwordLength, maxIteration, useSha1);
    }

    private static void doTiming(int passwordLength, int maxIteration, boolean useSha1) {
        log.info("passwordLength=" + passwordLength);
        log.info("maxIteration=" + maxIteration);
        log.info("useSha1=" + useSha1);

        byte[] byteArray = new byte[passwordLength];

        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = Byte.valueOf("" + i);
        }

        log.info("> START");
        StopWatch watch = new StopWatch();
        final int max = maxIteration;
        long bytes = 0L;
        try {
            for (int i = 0; i < max; i++) {
                BouncyCastleUtils.createDigestBytes(byteArray, useSha1);
                bytes += byteArray.length;
            }
        } finally {
            long delta = watch.click();
            log.info("delta=" + delta);
            log.info("    rate=" + (max / (delta / 1000)) + "/sec");
            log.info("    rate(bytes)=" + ((bytes / 1024) / (delta / 1000)) + "K/sec");
            log.info("< END");
        }
    }

    public static boolean boolValueOf(String strValue, boolean defaultValue) {
        boolean value = defaultValue;
        try {
            value = Boolean.valueOf(strValue);
        } catch (NumberFormatException e) {
            log.warn(e);
            value = defaultValue;
        }
        return value;
    }
}
