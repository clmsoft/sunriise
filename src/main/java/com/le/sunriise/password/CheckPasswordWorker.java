package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

public final class CheckPasswordWorker implements Callable<String> {
    private static final Logger log = Logger.getLogger(CheckPasswordWorker.class);

    private final File dbFile;
    private final HeaderPage headerPage;
    private final String testPassword;
    private AtomicLong counter;

    public CheckPasswordWorker(File dbFile, HeaderPage headerPage, String testPassword, AtomicLong counter) {
        super();
        this.dbFile = dbFile;
        this.headerPage = headerPage;
        this.testPassword = testPassword;
        this.counter = counter;
    }

    public CheckPasswordWorker(File dbFile, String testPassword, AtomicLong counter) {
        this(dbFile, null, testPassword, counter);
    }

    public CheckPasswordWorker(HeaderPage headerPage, String testPassword, AtomicLong counter) {
        this(null, headerPage, testPassword, counter);
    }

    @Override
    public String call() throws Exception {
        long counterValue = this.counter.incrementAndGet();
        int max = 100000;
        if ((max > 0) && ((counterValue % max) == 0)) {
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
            result = CheckPasswords.checkUsingOpenDb(dbFile, testPassword);
        } else {
            result = CheckPasswords.checkUsingHeaderPage(headerPage, testPassword);
        }

        return result;
    }

    public AtomicLong getCounter() {
        return counter;
    }

}