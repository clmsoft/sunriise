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

import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;

public class CheckBruteForce extends GenBruteForce {
    private static final Logger log = Logger.getLogger(CheckBruteForce.class);

    private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);

    private static final BigInteger ONE_THOUSAND = BigInteger.valueOf(1000);

    private final HeaderPage headerPage;
    private AtomicLong count = new AtomicLong(0L);
    private String password = null;
    private BigInteger maxCount;
    private StopWatch stopWatch;
    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(2);
    private ScheduledFuture<?> periodicStatusFuture;

    CheckBruteForce(int passwordLength, char[] alphabets, char[] mask, HeaderPage headerPage) {
        super(passwordLength, mask, alphabets);
        this.headerPage = headerPage;
        maxCount = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
    }

    @Override
    public void notifyResult(String string) {
        if (!accept(string)) {
            return;
        }
        count.getAndIncrement();
        final String testPassword = string;
        if (checkPassword(testPassword)) {
            log.info("!!! Found password=" + testPassword);
            setTerminate(true);
            password = testPassword;
        }
    }

    public boolean accept(String string) {
        return true;
    }

    public boolean checkPassword(String testPassword) {
        boolean matched = false;
        matched = AbstractHeaderPageOnlyPasswordChecker.checkPassword(headerPage, testPassword);
        return matched;
    }

    @Override
    public long generate() {
        long rv = 0;
        count = new AtomicLong(0L);
        password = null;
        stopWatch = new StopWatch();
        if (periodicStatusFuture != null) {
            periodicStatusFuture.cancel(true);
        }
        
        Runnable statusCommand = scheduleStatusCmd();
        
        try {
            rv = super.generate();
        } finally {
            statusCommand.run();
            if (this.periodicStatusFuture != null) {
                this.periodicStatusFuture.cancel(true);
            }
        }
        return rv;
    }

    private Runnable scheduleStatusCmd() {
        Runnable statusCommand = new Runnable() {
            @Override
            public void run() {
                long delta = stopWatch.click(false);
                Duration duration = new Duration(delta);
                BigInteger aCount = BigInteger.valueOf(count.get());
                BigInteger percentage = aCount.multiply(ONE_HUNDRED).divide(maxCount);
                log.info("Tested " + count + " strings" + " (" + percentage + "% completed" + ", elapsed=" + duration.toString()
                        + ")");
                if (delta > 0) {
                    BigInteger seconds = BigInteger.valueOf(delta).divide(ONE_THOUSAND);
                    if (seconds.longValue() > 0) {
                        logStatus(aCount, seconds);
                    }
                }

                if (isTerminate()) {
                    if (periodicStatusFuture != null) {
                        periodicStatusFuture.cancel(true);
                    }
                }
            }

            private void logStatus(BigInteger aCount, BigInteger seconds) {
                log.info("  Rate=" + aCount.divide(seconds) + "/sec");
                log.info("    currentResult=" + getCurrentResult());
                log.info("    currentCursorIndex=" + printIntArray(getCurrentCursorIndex()));
            }

        };
        long initialDelay = 0;
        long period = 30;
        TimeUnit unit = TimeUnit.SECONDS;
        this.periodicStatusFuture = scheduledExecutorService.scheduleAtFixedRate(statusCommand, initialDelay, period, unit);
        return statusCommand;
    }

    private String printIntArray(int[] currentCursorIndex) {
        if (currentCursorIndex == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        sb.append("[");
        try {
            for (int i = 0; i < currentCursorIndex.length; i++) {
                if (i > 0) {
                    sb.append(", ");
                }
                sb.append("" + currentCursorIndex[i]);
            }
        } finally {
            sb.append("]");
        }
        return sb.toString();
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