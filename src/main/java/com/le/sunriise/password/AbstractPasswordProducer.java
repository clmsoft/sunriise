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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;

public abstract class AbstractPasswordProducer implements Runnable {
    private static final Logger log = Logger.getLogger(AbstractPasswordProducer.class);

    private final BlockingQueue<ReaderItem> readerQueue;

    private final BlockingQueue<PasswordItem> passwordQueue;

    private boolean trim = true;

    private final CyclicBarrier barrier;

    protected abstract boolean stopProducing();

    public AbstractPasswordProducer(BlockingQueue<ReaderItem> readerQueue, BlockingQueue<PasswordItem> passwordQueue, CyclicBarrier barrier) {
        this.readerQueue = readerQueue;
        this.passwordQueue = passwordQueue;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        log.info("> START " + Thread.currentThread().getName());
        try {
            doWork();
        } finally {
            doFinally();
        }
    }

    protected void doWork() {
        while (!stopProducing()) {
            if (!doProducing()) {
                break;
            }
        }
    }

    protected boolean doProducing() {
        BufferedReader reader = null;
        try {
            ReaderItem item = readerQueue.take();
            reader = item.getReader();
            log.info("Got reader");
            if (item.isEndOfWork()) {
                log.info("Got reader end-of-work");
                readerQueue.put(new ReaderItem(true));
                return false;
            }
            readPasswords(reader);
        } catch (InterruptedException e) {
            log.warn(e, e);
            return false;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    reader = null;
                }
            }
        }

        return true;
    }

    protected void doFinally() {
        if (this.barrier != null) {
            try {
                log.info("> WAITING for barrier " + Thread.currentThread().getName());
                this.barrier.await();
            } catch (InterruptedException e) {
                log.warn(e);
            } catch (BrokenBarrierException e) {
                log.warn(e);
            }
        }
    }

    protected void readPasswords(BufferedReader reader) {
        String line = null;
        int lineCount = 1;
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

                if (stopProducing()) {
                    break;
                }

                lineCount++;

                try {
                    passwordQueue.put(new PasswordItem(line));
                } catch (InterruptedException e) {
                    log.warn(e);
                    break;
                }
            }
        } catch (IOException e) {
            log.error(e);
        }
    }

    public boolean isTrim() {
        return trim;
    }

    public void setTrim(boolean trim) {
        this.trim = trim;
    }
}
