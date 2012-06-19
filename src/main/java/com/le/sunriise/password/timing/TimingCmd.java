package com.le.sunriise.password.timing;

import org.apache.log4j.Logger;

public class TimingCmd {
    private static final Logger log = Logger.getLogger(TimingCmd.class);

    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int maxIteration = DEFAULT_MAX_ITERATIONS;

        log.info("#");
        long digestDelta = DigestTimingCmd.doTiming(maxIteration);

        log.info("#");
        long rc4Delta = RC4TimingCmd.doTiming(maxIteration);

        log.info("#");
        long checkerDelta = PasswordCheckerTimingCmd.doTiming(maxIteration);

        log.info("#");
        log.info("digestDelta=" + digestDelta);
        log.info("rc4Delta=" + rc4Delta);
        long sum = (digestDelta + rc4Delta);
        log.info("sum=" + sum);
        
        long rate1 = maxIteration/(sum/1000L);
        log.info("  rate=" + rate1 + "/sec");

        log.info("checkerDelta=" + checkerDelta);
        long rate2 = maxIteration/(checkerDelta/1000L);
        log.info("  rate=" + rate2 + "/sec");
        
        long percentage = ((rate1 - rate2) * 100) / rate1;
        log.info("  percentage=" + percentage + "%");
    }

}
