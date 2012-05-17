package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;

public class SingleThreadBruteForce {
    private static final Logger log = Logger.getLogger(SingleThreadBruteForce.class);

    private static final class CheckBruteForce extends GenBruteForce {
        private final HeaderPage hp;
        private int count = 0;
        private String password = null;
        private BigInteger maxCount;
        private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);
        private static final BigInteger ONE_THOUSAND = BigInteger.valueOf(1000);
        private StopWatch stopWatch;
        private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
        private ScheduledFuture<?> periodicStatusFuture;

        private CheckBruteForce(int passwordLength, char[] alphabets, char[] mask, HeaderPage hp) {
            super(passwordLength, mask, alphabets);
            this.hp = hp;
            maxCount = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
        }

        @Override
        public void notifyResult(String string) {
            count++;
            String testPassword = string;
            if (checkPassword(testPassword)) {
                log.info("!!! Found password=" + testPassword);
                setTerminate(true);
                password = testPassword;
            }
        }

        public boolean checkPassword(String testPassword) {
            boolean matched = false;
            matched = HeaderPageOnlyPasswordChecker.checkPassword(hp, testPassword);
            return matched;
        }

        @Override
        public long generate() {
            long rv = 0;
            count = 0;
            password = null;
            stopWatch = new StopWatch();
            if (periodicStatusFuture != null) {
                periodicStatusFuture.cancel(true);
            }
            Runnable command = new Runnable() {
                @Override
                public void run() {
                    long delta = stopWatch.click(false);
                    Duration duration = new Duration(delta);
                    BigInteger aCount = BigInteger.valueOf(count);
                    BigInteger percentage = aCount.multiply(ONE_HUNDRED).divide(maxCount);
                    log.info("Tested " + count + " strings" + " (" + percentage + "% completed" + ", elapsed=" + duration.toString() + ")");
                    if (delta > 0) {
                        BigInteger seconds = BigInteger.valueOf(delta).divide(ONE_THOUSAND);
                        if (seconds.longValue() > 0) {
                            log.info("  Rate=" + aCount.divide(seconds) + "/sec");
                        }
                    }

                    if (isTerminate()) {
                        if (periodicStatusFuture != null) {
                            periodicStatusFuture.cancel(true);
                        }
                    }
                }
            };
            long initialDelay = 0;
            long period = 30;
            TimeUnit unit = TimeUnit.SECONDS;
            this.periodicStatusFuture = scheduledExecutorService.scheduleAtFixedRate(command, initialDelay, period, unit);
            try {
                rv = super.generate();
            } finally {
                command.run();
                if (this.periodicStatusFuture != null) {
                    this.periodicStatusFuture.cancel(true);
                }
            }
            return rv;
        }

        public String getPassword() {
            return password;
        }

        public void shutdown() {
            if (this.scheduledExecutorService != null) {
                this.scheduledExecutorService.shutdown();
            }
        }
    }

    private static char[] createAlphabets() {
        List<char[]> arrays = new ArrayList<char[]>();
        arrays.add(GenBruteForce.ALPHABET_UPPERS);
        arrays.add(GenBruteForce.ALPHABET_DIGITS);
        arrays.add(GenBruteForce.ALPHABET_SPECIAL_CHARS_1);
        return createAlphabets(arrays);
    }

    private static char[] createAlphabets(List<char[]> arrays) {
        int size = 0;
        for (char[] array : arrays) {
            size += array.length;
        }
        char[] alphabets = new char[size];

        char[] src = null;
        int srcPos = 0;
        char[] dest = alphabets;
        int destPos = 0;
        int length = 0;
        for (char[] array : arrays) {
            src = array;
            destPos += length;
            length = src.length;
            System.arraycopy(src, srcPos, dest, destPos, length);
        }
        return alphabets;
    }

    public static String checkUsingBruteForce(File dbFile, int passwordLength, char[] mask, char[] alphabets) throws IOException {
        String password = null;
        HeaderPage headerPage = new HeaderPage(dbFile);

        if (alphabets == null) {
            alphabets = createAlphabets();
        }
        log.info("");
        log.info("alphabets.length=" + alphabets.length);
        if (log.isDebugEnabled()) {
            for (int i = 0; i < alphabets.length; i++) {
                log.debug(alphabets[i]);
            }
        }
        final HeaderPage hp = headerPage;
        CheckBruteForce checker = null;
        try {
            checker = new CheckBruteForce(passwordLength, alphabets, mask, hp);
            checker.generate();
            password = checker.getPassword();
            log.info("password=" + password);
        } finally {
            if (checker != null) {
                try {
                    checker.shutdown();
                } finally {
                    checker = null;
                }
            }
        }
        return password;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        int passwordLength = 5;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            passwordLength = Integer.valueOf(args[1]);
        } else {
            Class<SingleThreadBruteForce> clz = SingleThreadBruteForce.class;
            System.out.println("Usage: java " + clz.getName() + " sample.mny passwordLength");
            System.exit(1);
        }
        char[] mask = null;
         mask = new String("*****!").toCharArray();

        char[] alphabets = createAlphabets();

        log.info("dbFile=" + dbFile);
        log.info("passwordLength=" + passwordLength);
        log.info("mask=" + ((mask == null) ? null : new String(mask)));
        log.info("alphabets=" + new String(alphabets));

        try {
            checkUsingBruteForce(dbFile, passwordLength, mask, alphabets);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

}
