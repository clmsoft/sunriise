package com.le.sunriise.password.timing;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;
import com.le.sunriise.password.HeaderPage;
import com.le.sunriise.password.HeaderPagePasswordChecker;

public class PasswordCheckerTimingCmd {
    private static final Logger log = Logger.getLogger(PasswordCheckerTimingCmd.class);

    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    private static final String DEFAULT_MNY_FILENAME = "src/test/data/sunset-sample-pwd.mny";

    private static final String DEFAULT_PASSWORD = "123qwe!@";

    /**
     * 
     * @param args
     */
    public static void main(String[] args) {
        String mnyFileName = "src/test/data/sunset-sample-pwd.mny";
        int maxCount = 10000000;
        String password = "123qwe!@";

        doTiming(mnyFileName, password, maxCount);
    }

    public static long doTiming(int maxIteration) {
        return doTiming(DEFAULT_MNY_FILENAME, DEFAULT_PASSWORD, maxIteration);
    }

    public static long doTiming(String mnyFileName, String password, int maxIteration) {
        long delta = 0L;
        HeaderPagePasswordChecker checker = null;
        try {
            log.info("> START");
            File dbFile = new File(mnyFileName);
            HeaderPage headerPage = new HeaderPage(dbFile);
            checker = new HeaderPagePasswordChecker(headerPage);
            int max = maxIteration;
            StopWatch stopWatch = new StopWatch();
            try {
                for (int i = 0; i < max; i++) {
                    checker.check(password);
                }
            } finally {
                delta = stopWatch.click();
                log.info("delta=" + delta);
                log.info("    rate=" + (max / (delta / 1000)) + "/sec");
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (checker != null) {
                checker = null;
            }
            log.info("< END");
        }
        return delta;
    }

}
