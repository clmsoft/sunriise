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

        long digestDelta = DigestTimingCmd.doTiming(maxIteration);

        long rc4Delta = RC4TimingCmd.doTiming(maxIteration);

        long checkerDelta = PasswordCheckerTimingCmd.doTiming(maxIteration);

        log.info("digestDelta=" + digestDelta);
        log.info("rc4Delta=" + rc4Delta);
        long sum = (digestDelta + rc4Delta);
        log.info("sum=" + sum);
        log.info("  rate=" + (maxIteration/(sum/1000L)));

        log.info("checkerDelta=" + checkerDelta);
        log.info("  rate=" + (maxIteration/(checkerDelta/1000L)));
        
        long diff = checkerDelta - sum;
        log.info("diff=" + diff);
    }

}
