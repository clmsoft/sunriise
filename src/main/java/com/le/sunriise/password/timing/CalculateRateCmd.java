/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password.timing;

import java.io.File;
import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.log4j.Logger;

import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.PasswordUtils;
import com.le.sunriise.password.bruteforce.GenBruteForce;

public class CalculateRateCmd {
    private static final int DEFAULT_SECONDS = 60;
    private static final Logger log = Logger.getLogger(CalculateRateCmd.class);
    private AtomicBoolean quit;
    private char[] buffer = new char[8];
    private char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
    private Random random = new Random();
    private ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(2);
    private long counter;
    private String testPassword;
    private long rate;

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length <= 0) {
            Class<CalculateRateCmd> clz = CalculateRateCmd.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny");
            System.exit(0);
        }
        String dbFileName = args[0];
        long rate = CalculateRateCmd.calculateRate(dbFileName);
        log.info("Final rate=" + rate + "/sec");
    }

    public static long calculateRate(String dbFileName) {
        return calculateRate(dbFileName, DEFAULT_SECONDS);
    }

    public static long calculateRate(String dbFileName, int seconds) {
        long rate = 0;
        CalculateRateCmd cmd = new CalculateRateCmd();
        File dbFile = null;
        dbFile = new File(dbFileName);
        try {
            cmd.calculateRate(dbFile, seconds);
            rate = cmd.getRate();
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (cmd != null) {
                cmd.shutDown();
                cmd = null;
            }
        }
        return rate;
    }

    private void shutDown() {
        if (scheduler != null) {
            scheduler.shutdown();
        }

    }

    private void calculateRate(File dbFile, final int seconds) throws IOException {
        log.info("> calculateRate ..");
        Runnable command = new Runnable() {
            private int ticks;
            private int maxTicks = seconds;

            @Override
            public void run() {
                if (ticks++ > maxTicks) {
                    quit.getAndSet(true);
                }
                rate = counter / ticks;
                log.info("ticks=" + ticks + "/" + maxTicks + ", rate=" + rate + "/sec" + ", testPassword=" + testPassword);
            }
        };
        long initialDelay = 0L;
        TimeUnit unit = TimeUnit.SECONDS;
        long period = 1L;
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(command, initialDelay, period, unit);

        HeaderPage headerPage = new HeaderPage(dbFile);
        quit = new AtomicBoolean(false);
        counter = 0L;
        while (!quit.get()) {
            testPassword = getNextTestPassword();
            PasswordUtils.checkUsingHeaderPage(headerPage, testPassword);
            counter++;
        }
        future.cancel(true);
        log.info("< calculateRate ..");
    }

    private String getNextTestPassword() {
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = getNextRandomChar();
        }
        return new String(buffer);
    }

    private char getNextRandomChar() {
        return alphabets[random.nextInt(alphabets.length)];
    }

    public long getRate() {
        return rate;
    }
}
