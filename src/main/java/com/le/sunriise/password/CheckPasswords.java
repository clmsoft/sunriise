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
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.le.sunriise.Utils;

public class CheckPasswords {
    private static final Logger log = Logger.getLogger(CheckPasswords.class);

    private boolean trim = true;

    private ExecutorService pool;

    private final AtomicLong counter = new AtomicLong();

    public CheckPasswords(int threads) {
        super();
        pool = Executors.newFixedThreadPool(threads);
    }

    public CheckPasswords() {
        this(1);
    }

    public String check(HeaderPage headerPage, File path) throws IOException {
        return recursePath(headerPage, path);
    }

    public static boolean checkUsingHeaderPage(HeaderPage headerPage, String testPassword) throws IOException {
        boolean result = false;
        try {
            if (HeaderPageOnlyPasswordChecker.checkPassword(headerPage, testPassword)) {
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

    public static boolean checkUsingOpenDb(File dbFile, String testPassword) throws IOException {
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

    private String recursePath(HeaderPage headerPage, File path) throws IOException {
        if ((path != null) && (path.isDirectory())) {
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
        if (headerPage == null) {
            return null;
        }
        if (file == null) {
            return null;
        }
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
        List<Future<String>> runningJobs = new ArrayList<Future<String>>();

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

                Callable<String> callable = createWorker(headerPage, line, counter);
                if (callable != null) {
                    Future<String> future = pool.submit(callable);
                    runningJobs.add(future);
                }

                // check running jobs to see if any has done
                ListIterator<Future<String>> iter = runningJobs.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
                                log.info("111 runningJobs.size=" + runningJobs.size());
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

            // flush the working list
            while (runningJobs.size() > 0) {
                ListIterator<Future<String>> iter = runningJobs.listIterator();
                while (iter.hasNext()) {
                    Future<String> job = iter.next();
                    if (job.isDone()) {
                        try {
                            result = job.get();
                            if (result != null) {
                                log.info("222 runningJobs.size=" + runningJobs.size());
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

    public Callable<String> createWorker(HeaderPage headerPage, String testPassword, AtomicLong counter) {
        return new CheckPasswordWorker(headerPage, testPassword, counter);
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

    public AtomicLong getCounter() {
        return counter;
    }
}
