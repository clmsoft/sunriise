package com.le.sunriise.password.ui;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.dict.CheckDictionary;

public class DictionarySearch {
    private static final Logger log = Logger.getLogger(DictionarySearch.class);

    private int lastCheckerThreads = 0;

    private AtomicBoolean running = new AtomicBoolean(false);
    private CheckDictionary checker = null;
    // XXX: re-use is currently NOT working
    private boolean reUseChecker = false;

    public DictionarySearch() {
        super();
    }

    public void startCheck(final File headerPageFile, final File candidatesPath) {
        startCheck(1, headerPageFile, candidatesPath);
    }

    public void startCheck(final int nThreads, final File headerPageFile, final File candidatesPath) {
        if (!validateInputs()) {
            return;
        }

        preStart();

        this.getRunning().getAndSet(true);

        if (checker != null) {
            boolean closeChecker = true;
            if (reUseChecker) {
                closeChecker = false;
                if (nThreads > lastCheckerThreads) {
                    closeChecker = true;
                }

            } else {
                closeChecker = true;
            }
            if (closeChecker) {
                try {
                    checker.close();
                } finally {
                    checker = null;
                }
            }
        }
        lastCheckerThreads = nThreads;
        if (checker == null) {
            log.info("Created new checker, threads=" + lastCheckerThreads);
            checker = new CheckDictionary(lastCheckerThreads);
        } else {
            log.info("Re-using existing checker, threads=" + lastCheckerThreads + ", previous threads=" + lastCheckerThreads);
            checker.reset();
        }
        AtomicLong counter = checker.getCounter();
        logStatus("Running ... searched " + counter.get());

        Runnable command = new Runnable() {
            @Override
            public void run() {
                String matchedPassword = null;
                try {
                    HeaderPage headerPage = new HeaderPage(headerPageFile);
                    matchedPassword = checker.check(headerPage, candidatesPath);
                    notifyResult(matchedPassword);
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    if (!reUseChecker) {
                        if (checker != null) {
                            try {
                                checker.close();
                            } finally {
                                checker = null;
                            }
                        }
                    }
                    getRunning().getAndSet(false);

                    postStart(matchedPassword);
                }
            }
        };
        runCommand(command);
    }

    public void stopCheck() {
        if (this.checker != null) {
            this.checker.stop();
        }
    }

    protected void runCommand(Runnable command) {
        command.run();
    }

    protected void logStatus(String message) {
        log.info(message);
    }

    protected AtomicLong getCounter() {
        AtomicLong counter = null;
        if (checker != null) {
            counter = checker.getCounter();
        } else {
            counter = new AtomicLong(0L);
        }
        return counter;
    }

    protected boolean validateInputs() {
        log.info("> validateInputs");

        return true;
    }

    protected void preStart() {
        log.info("> preStart");
    }

    protected void postStart(String matchedPassword) {
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public void setRunning(AtomicBoolean running) {
        this.running = running;
    }

    protected void notifyResult(final String matchedPassword) {
        log.info("matchedPassword=" + matchedPassword);
    }

    public boolean isReUseChecker() {
        return reUseChecker;
    }

    public void setReUseChecker(boolean reUseChecker) {
        this.reUseChecker = reUseChecker;
    }

}