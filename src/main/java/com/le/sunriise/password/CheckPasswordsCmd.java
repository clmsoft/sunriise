package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;

// http://essayweb.net/mathematics/permcomb.shtml
public class CheckPasswordsCmd {
    private static final Logger log = Logger.getLogger(CheckPasswordsCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File path = null;
        int threads = 1;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            path = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            path = new File(args[1]);
            try {
                threads = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                log.warn(e);
            }
        } else {
            Class<CheckPasswords> clz = CheckPasswords.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny {passwordsFile.txt | path} threads");
            System.exit(1);
        }

        if (!dbFile.exists()) {
            log.error("dbFile does not exist, dbFile=" + dbFile);
            System.exit(1);
        }

        if (!path.exists()) {
            log.error("path does not exist, path=" + path);
            System.exit(1);
        }
        if (threads <= 0) {
            threads = 1;
        }

        log.info("dbFile=" + dbFile);
        log.info("path=" + path);
        log.info("threads=" + threads);

        String matchedPassword = null;
        CheckPasswords checker = null;
        StopWatch stopWatch = new StopWatch();
        try {
            checker = new CheckPasswords(threads);
            HeaderPage headerPage = new HeaderPage(dbFile);
            matchedPassword = checker.check(headerPage, path);
            log.info("Have checked " + checker.getCounter().get());
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            long millis = stopWatch.click();

            if (checker != null) {
                checker.close();
                checker = null;
            }

            String durationString = Duration.toDurationString(millis);
            log.info("Took " + durationString);

            log.info("< DONE, matchedPassword=" + matchedPassword);

            // the pool might still be running. Force exit.
            System.exit(0);
        }
    }
}
