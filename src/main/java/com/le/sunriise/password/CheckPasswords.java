package com.le.sunriise.password;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;
import com.le.sunriise.Utils;

public class CheckPasswords {
    private static final Logger log = Logger.getLogger(CheckPasswords.class);

    private boolean trim = true;

    private ExecutorService pool;

    private final AtomicInteger counter = new AtomicInteger();

    private final class CheckPasswordWorker implements Callable<String> {
        private final File dbFile;
        private final HeaderPage headerPage;
        private final String testPassword;

        public CheckPasswordWorker(File dbFile, HeaderPage headerPage, String testPassword) {
            super();
            this.dbFile = dbFile;
            this.headerPage = headerPage;
            this.testPassword = testPassword;
        }

        public CheckPasswordWorker(File dbFile, String testPassword) {
            this(dbFile, null, testPassword);
        }

        public CheckPasswordWorker(HeaderPage headerPage, String testPassword) {
            this(null, headerPage, testPassword);
        }

        @Override
        public String call() throws Exception {
            int counterValue = counter.incrementAndGet();
            int max = 100000;
            if ((counterValue % max) == 0) {
                log.info("Have checked " + counterValue);
            }

            if (checkPassword(testPassword)) {
                log.info("testPassword=" + testPassword + ", YES");
                return testPassword;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("testPassword=" + testPassword + ", NO");
                }
                return null;
            }
        }

        private boolean checkPassword(String testPassword) throws IOException {
            boolean result = false;
            boolean checkUsingOpenDb = (headerPage == null);

            if (checkUsingOpenDb) {
                result = checkUsingOpenDb(dbFile, testPassword);
            } else {
                result = checkMinPasswordChecker(headerPage, testPassword);
            }

            return result;
        }

        private boolean checkMinPasswordChecker(HeaderPage headerPage, String testPassword) throws IOException {
            boolean result = false;
            try {
                if (MinPasswordChecker.checkPassword(headerPage, testPassword)) {
                    if (log.isDebugEnabled()) {
                        log.debug("OK password=" + testPassword);
                    }
                    result = true;
                } else {
                    if (log.isDebugEnabled()) {
                        log.debug("NOT OK password=" + testPassword);
                    }
                    result = false;
                }

            } finally {

            }
            return result;
        }

        private boolean checkUsingOpenDb(File dbFile, String testPassword) throws IOException {
            boolean result = false;
            try {
                Utils.openDbReadOnly(dbFile, testPassword);
                if (log.isDebugEnabled()) {
                    log.debug("testPassword=" + testPassword + ", YES");
                }
                result = true;
            } catch (java.lang.IllegalStateException e) {
                // wrong password
                if (log.isDebugEnabled()) {
                    log.warn(e);
                }
            }
            return result;
        }
    }

    public CheckPasswords(int threads) {
        super();
        pool = Executors.newFixedThreadPool(threads);
    }

    public String check(HeaderPage headerPage, File path) throws IOException {
        return recursePath(headerPage, path);
    }

    private String recursePath(HeaderPage headerPage, File path) throws IOException {
        if (path.isDirectory()) {
            return recurseDirectory(headerPage, path);
        } else {
            return consumeFile(headerPage, path);
        }
    }

    private String recurseDirectory(HeaderPage dbFile, File directory) throws IOException {
        String value = null;
        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                value = recursePath(dbFile, file);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private String consumeFile(HeaderPage headerPage, File file) {
        log.info("> file=" + file);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return consumeReader(headerPage, reader);
        } catch (IOException e) {
            log.error(e, e);
        }
        return null;
    }

    private String consumeReader(HeaderPage headerPage, BufferedReader reader) {
        String result = null;
        String line = null;
        int lineCount = 1;
        List<Future<String>> workingList = new ArrayList<Future<String>>();

        try {
            while ((line = reader.readLine()) != null) {
                if (trim) {
                    line = line.trim();
                }
                if (line.length() <= 0) {
                    continue;
                }
                if (line.charAt(0) == '#') {
                    continue;
                }

                lineCount++;

                Callable<String> callable = new CheckPasswordWorker(headerPage, line);
                Future<String> future = pool.submit(callable);
                workingList.add(future);

                ListIterator<Future<String>> iter = workingList.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
                                log.info("111 workingList.size=" + workingList.size());
                                return result;
                            }
                        } catch (ExecutionException e) {
                            log.warn(e, e);
                        } catch (InterruptedException e) {
                            log.warn(e);
                        } finally {
                            iter.remove();
                        }
                    }
                }
            }
            while (workingList.size() > 0) {
                ListIterator<Future<String>> iter = workingList.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
                                log.info("222 workingList.size=" + workingList.size());
                                return result;
                            }
                        } catch (ExecutionException e) {
                            log.warn(e);
                        } catch (InterruptedException e) {
                            log.warn(e);
                        } finally {
                            iter.remove();
                        }
                    }
                }
                try {
                    Thread.sleep(1000L);
                } catch (InterruptedException e) {
                    log.warn(e);
                }
            }
        } catch (IOException e) {
            log.error(e);
        }

        return null;
    }

    public void close() {
        if (pool != null) {
            try {
                pool.shutdownNow();
            } finally {
                pool = null;
            }
        }

    }

    public AtomicInteger getCounter() {
        return counter;
    }

    public static String toDurationString(long millis) {
        long hours = TimeUnit.MILLISECONDS.toHours(millis);
        long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.MILLISECONDS.toMinutes(hours);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);

        String durationString = String.format("%d hour, %d min, %d sec", hours, minutes, seconds);
        return durationString;
    }
}
