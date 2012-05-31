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

    private boolean doubleCheck = true;

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
            result = PasswordUtils.checkUsingOpenDb(dbFile, testPassword);
        } else {
            result = PasswordUtils.checkUsingHeaderPage(headerPage, testPassword);
            if (result) {
                if (isDoubleCheck()) {
                    result = PasswordUtils.doubleCheck(headerPage, testPassword);
                }
            }
        }

        return result;
    }

    public AtomicLong getCounter() {
        return counter;
    }

    public boolean isDoubleCheck() {
        return doubleCheck;
    }

    public void setDoubleCheck(boolean doubleCheck) {
        this.doubleCheck = doubleCheck;
    }

}