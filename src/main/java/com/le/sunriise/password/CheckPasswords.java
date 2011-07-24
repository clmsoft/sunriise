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
        private String testPassword;
        private File dbFile;

        public CheckPasswordWorker(File dbFile, String line) {
            this.dbFile = dbFile;
            this.testPassword = line;
        }

        public String call() throws Exception {
            int counterValue = counter.incrementAndGet();
            if ((counterValue % 10000) == 0) {
                log.info("Have checked " + counterValue);
            }
            try {
                Utils.openDbReadOnly(dbFile, testPassword);
                if (log.isDebugEnabled()) {
                    log.debug("testPassword=" + testPassword + ", YES");
                }
                return testPassword;
            } catch (java.lang.IllegalStateException e) {
                // wrong password
                if (log.isDebugEnabled()) {
                    log.warn(e);
                }
            }
            if (log.isDebugEnabled()) {
                log.debug("testPassword=" + testPassword + ", NO");
            }
            return null;
        }
    }

    public CheckPasswords(int threads) {
        super();
        pool = Executors.newFixedThreadPool(threads);
    }

    private String check(File dbFile, File path) throws IOException {
        return recursePath(dbFile, path);
    }

    private String recursePath(File dbFile, File path) throws IOException {
        if (path.isDirectory()) {
            recurseDirectory(dbFile, path);
        } else {
            return consumeFile(dbFile, path);
        }
        return null;
    }

    private void recurseDirectory(File dbFile, File directory) throws IOException {
        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                recursePath(dbFile, file);
            }
        }
    }

    private String consumeFile(File dbFile, File file) {
        log.info("> file=" + file);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            return consumeReader(dbFile, reader);
        } catch (IOException e) {
            log.error(e, e);
        }
        return null;
    }

    private String consumeReader(File dbFile, BufferedReader reader) {
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

                Callable<String> callable = new CheckPasswordWorker(dbFile, line);
                Future<String> future = pool.submit(callable);
                workingList.add(future);

                ListIterator<Future<String>> iter = workingList.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
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
            }
            while (workingList.size() > 0) {
                ListIterator<Future<String>> iter = workingList.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
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
            matchedPassword = checker.check(dbFile, path);
            log.info("Have checked " + checker.getCounter().get());
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            long millis = stopWatch.click();

            if (checker != null) {
                checker.close();
            }
            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.MILLISECONDS.toMinutes(hours);
            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(minutes);

            String durationString = String.format("%d hour, %d min, %d sec", hours, minutes, seconds);
            log.info("Took " + durationString);

            log.info("< DONE, matchedPassword=" + matchedPassword);

            // the pool might still be running. Force exit.
            System.exit(0);
        }
    }

    private void close() {
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
}
