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
package com.le.sunriise.password.dict;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.AbstractHeaderPagePasswordChecker;

/**
 * Check for matching password against a set of dictionary. Support
 * multi-threads.
 * 
 * @author hle
 * 
 */
public class CheckDictionary {
    private static final Logger log = Logger.getLogger(CheckDictionary.class);

    private boolean trim = true;

    private ExecutorService pool;

    // per run that might need to be reset if this instance is being re-use
    private final AtomicLong counter = new AtomicLong(0L);

    private final AtomicBoolean quit = new AtomicBoolean(false);

    private String result;

    /**
     * 
     * @param nThreads
     *            is number of threads to run
     */
    public CheckDictionary(int nThreads) {
        super();

        if (nThreads < 1) {
            nThreads = 1;
        }

        // like newFixedThreadPool but with an ArrayBlockingQueue
        int corePoolSize = nThreads;
        int maximumPoolsize = nThreads;
        long keepAliveTime = 0L;
        TimeUnit unit = TimeUnit.MINUTES;
        BlockingQueue<Runnable> workQueue = new ArrayBlockingQueue<Runnable>(100, true);
        RejectedExecutionHandler handler = new ThreadPoolExecutor.CallerRunsPolicy();
        this.pool = new ThreadPoolExecutor(corePoolSize, maximumPoolsize, keepAliveTime, unit, workQueue, handler);
    }

    public void reset() {
        counter.getAndSet(0L);
        quit.getAndSet(false);
        setResult(null);
    }
    /**
     * Default number of threads is 1.
     */
    public CheckDictionary() {
        this(1);
    }

    /**
     * Check the given headerPage against a set of dictionary. The set of
     * dictionary is formed by parsing the give file: one word per line, skip
     * '#' comment line. If the given path is a directory, traverse the
     * directory, skip 'hidden' file (with prefix dot '.').
     * 
     * @param headerPage
     * @param candidatesPath
     * @return a String which is a matched password. If null, no password found.
     * @throws IOException
     */
    public String check(HeaderPage headerPage, File candidatesPath) throws IOException {
        if (headerPage.isNewEncryption()) {
            // short-cut: if password is blank, quit immediately
            byte[] encrypted4BytesCheck = headerPage.getEncrypted4BytesCheck();
            if (AbstractHeaderPagePasswordChecker.isBlankKey(encrypted4BytesCheck)) {
                // no password?
                log.warn("Found blank encrypted4BytesCheck=" + AbstractHeaderPagePasswordChecker.toHexString(encrypted4BytesCheck));
                return null;
            }
            recursePath(headerPage, candidatesPath);
            pool.shutdown();

            log.info("> Waiting for all background tasks to complete ...");
            long timeout = 3L;
            TimeUnit unit = TimeUnit.MINUTES;
            String result = null;
            try {
                pool.awaitTermination(timeout, unit);
            } catch (InterruptedException e) {
                log.warn("Interrupted while waiting for background to complete. Result might not be completed.");
            } finally {
                result = getResult();
                log.info("< DONE waiting, result=" + result);
            }

            return result;
        } else {
            log.info("Found embeddedDatabasePassword");
            return headerPage.getEmbeddedDatabasePassword();
        }
    }

    public void stop() {
        log.info("> STOP");
        quit.getAndSet(true);
    }

    private void recursePath(HeaderPage headerPage, File candidatesPath) throws IOException {
        if (acceptPathAsDirectory(candidatesPath)) {
            recurseDirectory(headerPage, candidatesPath);
        } else {
            if (acceptPathAsFile(candidatesPath)) {
                consumeFile(headerPage, candidatesPath);
            }
        }
    }

    protected boolean acceptPathAsFile(File path) {
        if (path == null) {
            return false;
        }
        if (path.getName().startsWith(".")) {
            return false;
        }
        return path.isFile();
    }

    protected boolean acceptPathAsDirectory(File path) {
        if (path == null) {
            return false;
        }
        if (path.getName().startsWith(".")) {
            return false;
        }
        return path.isDirectory();
    }

    private void recurseDirectory(HeaderPage dbFile, File directory) throws IOException {
        if (quit.get()) {
            notifyQuitEarly("Quit early in recurseDirectory");
            return;
        }

        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                recursePath(dbFile, file);
            }
        }
    }

    private void consumeFile(HeaderPage headerPage, File candidatesFile) {
        if (quit.get()) {
            notifyQuitEarly("Quit early in consumeFile");
            return;
        }

        if (headerPage == null) {
            return;
        }
        if (candidatesFile == null) {
            return;
        }

        log.info("> candidatesFile=" + candidatesFile);
        BufferedReader candidatesReader = null;
        int lines = 0;
        try {
            candidatesReader = new BufferedReader(new FileReader(candidatesFile));
            lines = consumeReader(headerPage, candidatesReader);
        } catch (Exception e) {
            log.error(e, e);
        } finally {
            log.info("> candidatesFile=" + candidatesFile  + ", lines=" + lines);
            if (candidatesReader != null) {
                try {
                    candidatesReader.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    candidatesReader = null;
                }
            }
        }
        return;
    }

    private int consumeReader(HeaderPage headerPage, BufferedReader reader) throws IOException {
        String line = null;
        int lines = 0;
        
        // LOOP
        while ((line = reader.readLine()) != null) {
            if (quit.get()) {
                notifyQuitEarly("Quit early in consumeReader");
                break;
            }

            if (trim) {
                line = line.trim();
            }

            if (skipLine(line)) {
                continue;
            }

            lines++;
            Callable<String> callable = createWorker(headerPage, line);
            if (callable != null) {
                pool.submit(callable);
            }
        }
        
        return lines;
    }

    private boolean skipLine(String line) {
        if (line.length() <= 0) {
            return true;
        }
        if (line.charAt(0) == '#') {
            return true;
        }

        return false;
    }

    private String checkJobsForResult(List<Future<String>> runningJobs) {
        ListIterator<Future<String>> iter = runningJobs.listIterator();
        String result = null;

        // log.info("> checkJobsForResult");
        // LOOP
        while (iter.hasNext()) {
            if (quit.get()) {
                notifyQuitEarly("Quit early in checkJobsForResult");
                break;
            }

            Future<String> job = iter.next();
            if (job.isDone()) {
                try {
                    result = job.get();
                    if (result != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("111 runningJobs.size=" + runningJobs.size());
                        }
                        break;
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

        // log.info("< checkJobsForResult");
        return result;
    }

    private void notifyQuitEarly(String reason) {
        log.warn(reason);
    }

    private String flushJobs(List<Future<String>> runningJobs) {
        String result = null;
        long sleepMs = 1000L;

        log.info("runningJobs.size=" + runningJobs.size());

        // LOOP
        while (runningJobs.size() > 0) {
            if (quit.get()) {
                notifyQuitEarly("Quit early in flushJobs");
                break;
            }

            result = checkJobsForResult(runningJobs);

            if (result != null) {
                if (quit.get()) {
                    notifyQuitEarly("Quit early in flushJobs");
                    break;
                }
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    log.warn(e);
                }
            }
        }

        log.info("In waitForEmptyWorkingList - ready to return, has result=" + result);
        return result;
    }

    private Callable<String> createWorker(HeaderPage headerPage, String testPassword) {
        ResultCollector resultCollector = new ResultCollector() {
            @Override
            public void setResult(String result) {
                CheckDictionary.this.setResult(result);
            }
        };
        PasswordWorkerContext context = new PasswordWorkerContext(null, headerPage, counter, quit, resultCollector);
        return createWorker(context, testPassword);
    }

    protected Callable<String> createWorker(PasswordWorkerContext context, String testPassword) {
        return new CheckPasswordWorker(context, testPassword);
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

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
