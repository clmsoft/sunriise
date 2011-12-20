package com.le.sunriise.password;

import java.math.BigInteger;
import org.apache.log4j.Logger;

public class BruteForceTime {

    private static final Logger log = Logger.getLogger(BruteForceTime.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        int alphabetsLenghth = 26;
        String message = null;
        int ratePerSecond = 150000;

        log.info("ratePerSecond=" + ratePerSecond);
        
        alphabetsLenghth = 26;
        message = "Just lower OR upper case ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

        alphabetsLenghth = 26 + 10;
        message = "Just lower OR upper case + digits ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

        alphabetsLenghth = 26 + 10 + 3;
        message = "Just lower OR upper case + digits + 3 special chars ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

        alphabetsLenghth = 26 * 2;
        message = "Both lower AND upper case ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

        alphabetsLenghth = (26 * 2) + 10;
        message = "Both lower AND upper case + digits ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

        alphabetsLenghth = (26 * 2) + 10 + 3;
        message = "Both lower AND upper case + digits + 3 special chars ... alphabetsLenghth=" + alphabetsLenghth;
        calc(alphabetsLenghth, message, ratePerSecond);

    }

    private static void calc(int alphabetsLenghth, String message, int ratePerSecond) {
        log.info("#");
        log.info(message);
        int passwordLength;

        int start = 4;
        int max = 8;
        for (int i = start; i <= max; i++) {
            passwordLength = i;
            BigInteger count = GenBruteForce.calculateExpected(passwordLength, alphabetsLenghth);
            log.info(passwordLength + ", " + alphabetsLenghth + ", " + count + ", " + calcHowLong(count, ratePerSecond));
        }
    }

    private static String calcHowLong(BigInteger count, int ratePerSecond) {
        count = count.divide(BigInteger.valueOf(ratePerSecond));
        BigInteger millis = count.multiply(BigInteger.valueOf(1000));
        Duration duration = new Duration(millis.longValue());
        return duration.toString();
    }

}
