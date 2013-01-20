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
package com.le.sunriise.password.bruteforce;

import java.io.File;
import java.io.IOException;
import java.math.BigInteger;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.le.sunriise.StopWatch;
import com.le.sunriise.password.AbstractHeaderPagePasswordChecker;
import com.le.sunriise.password.HeaderPage;
import com.le.sunriise.password.timing.Duration;

public class CheckBruteForce extends GenBruteForce {
    private static final Logger log = Logger.getLogger(CheckBruteForce.class);

    private static final BigInteger ONE_HUNDRED = BigInteger.valueOf(100);

    private static final BigInteger ONE_THOUSAND = BigInteger.valueOf(1000);

    private final HeaderPage headerPage;
    private AtomicLong count = new AtomicLong(0L);
    private String password = null;
    private BigInteger maxCount;
    private StopWatch stopWatch;

    private ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(3);
    private ScheduledFuture<?> periodicStatusFuture;

    private BruteForceStat stat = new BruteForceStat();

    private boolean writeContextFile = false;

    private final class StatusTask implements Runnable {
        @Override
        public void run() {
            long delta = stopWatch.click(false);
            Duration duration = new Duration(delta);
            BigInteger aCount = BigInteger.valueOf(count.get());
            BigInteger percentage = null;

            if (maxCount.longValue() > 0L) {
                percentage = aCount.multiply(ONE_HUNDRED).divide(maxCount);
            } else {
                percentage = BigInteger.ZERO;
            }
            stat.setCount(aCount);
            stat.setPercentage(percentage);
            if (delta > 0) {
                BigInteger seconds = BigInteger.valueOf(delta).divide(ONE_THOUSAND);
                stat.setSeconds(seconds);
                stat.setCurrentResult(getCurrentResult());
                stat.setCurrentCursorIndex(getCurrentCursorIndex());

                if ((seconds.longValue() % 30) == 0) {
                    log.info("Tested " + count + " strings" + " (" + percentage + "% completed" + ", elapsed="
                            + duration.toString() + ")");
                    logStat(stat);
                }
            }

            if (writeContextFile) {
                GenBruteForceContext context = getContext();
                if (context != null) {
                    int cursor = context.getCursor();

                    File file = new File("bruteForceContext.json");
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        mapper.writeValue(file, context);
                    } catch (JsonGenerationException e) {
                        log.warn(e);
                    } catch (JsonMappingException e) {
                        log.warn(e);
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        log.info("Wrote context file=" + file + ", cursor=" + cursor);
                    }
                }
            }

            if (isTerminate()) {
                if (periodicStatusFuture != null) {
                    periodicStatusFuture.cancel(true);
                }
            }
        }
    }

    protected void logStat(BruteForceStat stat) {
        if (stat.getSeconds().longValue() > 0) {
            log.info("  Rate=" + GenBruteForce.calcRate(stat) + "/sec");
        } else {
            log.info("  Rate=" + "N/A" + ", count=" + stat.getCount());
        }
        log.info("    currentResult=" + stat.getCurrentResult());
        char[] alphabets = null;
        if (getContext() != null) {
            alphabets = getContext().getAlphabets();
        }
        log.info("    currentCursorIndex=" + GenBruteForce.printIntArray(stat.getCurrentCursorIndex(), alphabets));
    }

    /**
     * For a given headerPage, passwordLength, mask, and alphabets, run
     * brute-force checker to see if we can find a matching password.
     * 
     * @param headerPage
     *            is a header page for a *.mny file
     * @param passwordLength
     *            how long is a password to check?
     * @param mask
     *            password mask
     * @param alphabets
     *            is an array of char representing the alphabets
     */
    public CheckBruteForce(HeaderPage headerPage, int passwordLength, char[] mask, char[] alphabets) {
        super(passwordLength, mask, alphabets);
        this.headerPage = headerPage;
        maxCount = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
    }

    public CheckBruteForce(HeaderPage headerPage, GenBruteForceContext context) {
        super(context);
        this.headerPage = headerPage;
        maxCount = GenBruteForce.calculateExpected(context.getMask().length, context.getAlphabets().length);
    }

    /**
     * Run the brute-force check. Use getPassword() to see if there was a
     * matching password or not.
     * 
     * @return how many items were checked.
     */
    public long check() {
        if (getResumeContext() != null) {
            writeContextFile = false;
        }
        return generate();
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

    protected boolean accept(String string) {
        return true;
    }

    private boolean checkPassword(String testPassword) {
        boolean matched = false;
        matched = AbstractHeaderPagePasswordChecker.checkPassword(headerPage, testPassword);
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

        Runnable statusCommand = createScheduleStatusCmd();
        try {
            rv = super.generate();
        } finally {
            if (this.periodicStatusFuture != null) {
                this.periodicStatusFuture.cancel(true);
            }
            log.info("pre final statusCommand.run()");
            statusCommand.run();
        }

        return rv;
    }

    private Runnable createScheduleStatusCmd() {
        Runnable statusCommand = new StatusTask();
        long initialDelay = 0;
        long period = 1;
        TimeUnit unit = TimeUnit.SECONDS;
        this.periodicStatusFuture = scheduledExecutorService.scheduleAtFixedRate(statusCommand, initialDelay, period, unit);
        return statusCommand;
    }

    public String getPassword() {
        return password;
    }

    public void shutdown() {
        if (this.scheduledExecutorService != null) {
            this.scheduledExecutorService.shutdown();
        }
    }

    public BruteForceStat getStat() {
        return stat;
    }

    public char[] getAlphabets() {
        if (getContext() == null) {
            return null;
        }
        char[] alphabets = getContext().getAlphabets();
        if (alphabets == null) {
            return null;
        }
        return alphabets;
    }
}