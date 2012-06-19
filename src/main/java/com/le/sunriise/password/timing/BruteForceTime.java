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
package com.le.sunriise.password.timing;

import java.math.BigInteger;

import org.apache.log4j.Logger;

import com.le.sunriise.password.bruteforce.GenBruteForce;

public class BruteForceTime {

    private static final Logger log = Logger.getLogger(BruteForceTime.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        int alphabetsLen = 26;
        String message = null;
        long ratePerSecond = 140000;

        if (args.length > 0) {
            String dbFileName = args[0];
            ratePerSecond = CalculateRateCmd.calculateRate(dbFileName);
        }
        ratePerSecond = 240000;
        
        log.info("ratePerSecond=" + ratePerSecond);

        alphabetsLen = 26;
        message = "Just lower OR upper case ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = 26 + 10;
        message = "Just lower OR upper case + digits ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = 26 + 10 + 3;
        message = "Just lower OR upper case + digits + 3 special chars ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = 26 * 2;
        message = "Both lower AND upper case ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = GenBruteForce.ALPHABET_US_KEYBOARD_MNY.length;
        message = "Just lower OR upper case + the rest of US keyboard ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);
        
        alphabetsLen = (26 * 2) + 10;
        message = "Both lower AND upper case + digits ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = (26 * 2) + 10 + 3;
        message = "Both lower AND upper case + digits + 3 special chars ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

        alphabetsLen = 92;
        message = "us-keyboard ... alphabetsLen=" + alphabetsLen;
        calc(alphabetsLen, message, ratePerSecond);

    }

    private static void calc(int alphabetsLen, String message, long ratePerSecond) {
        log.info("#");
        log.info(message);
        int passwordLength;

        int start = 4;
        int max = 8;
        for (int i = start; i <= max; i++) {
            passwordLength = i;
            BigInteger count = GenBruteForce.calculateExpected(passwordLength, alphabetsLen);
            log.info(passwordLength + ", " + alphabetsLen + ", " + count + ", " + calcHowLong(count, ratePerSecond));
        }
    }

    private static String calcHowLong(BigInteger count, long ratePerSecond) {
        count = count.divide(BigInteger.valueOf(ratePerSecond));
        BigInteger millis = count.multiply(BigInteger.valueOf(1000));
        Duration duration = new Duration(millis.longValue());
        return duration.toString();
    }

}
