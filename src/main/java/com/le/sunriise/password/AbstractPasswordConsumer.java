package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.log4j.Logger;

import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public abstract class AbstractPasswordConsumer implements Runnable {
    private static final Logger log = Logger.getLogger(AbstractPasswordConsumer.class);

    private final File dbFile;

    private final BlockingQueue<PasswordItem> passwordQueue;

    private final CyclicBarrier barrier;

    private int count;

    protected abstract void notifyFoundPassword(String password);

    protected abstract boolean stopConsuming();

    public AbstractPasswordConsumer(File dbFile, BlockingQueue<PasswordItem> passwordQueue, CyclicBarrier barrier) {
        this.dbFile = dbFile;
        this.passwordQueue = passwordQueue;
        this.barrier = barrier;
    }

    @Override
    public void run() {
        count = 0;
        log.info("> START " + Thread.currentThread().getName());
        try {
            doWork();
        } finally {
            doFinally();
        }
    }

    protected void doWork() {
        while (!stopConsuming()) {
            if (!doConsuming()) {
                break;
            }
        }
    }

    protected boolean doConsuming() {
        count++;
        if ((count % 10000) == 0) {
            if (log.isDebugEnabled()) {
                log.debug("  consumed count=" + count);
            }
        }
        OpenedDb openedDb = null;
        try {
            PasswordItem workItem = passwordQueue.take();
            if (workItem.isEndOfWork()) {
                passwordQueue.put(new PasswordItem(true));
                return false;
            }
            String password = workItem.getPassword();
            if (password != null) {
                openedDb = Utils.openDbReadOnly(dbFile, password);
                if (openedDb != null) {
                    notifyFoundPassword(password);
                    Collection<PasswordItem> leftOver = new ArrayList<PasswordItem>();
                    passwordQueue.drainTo(leftOver);
                    passwordQueue.put(new PasswordItem(true));
                    return false;
                }
            }
        } catch (IllegalStateException e) {
            // wrong password
            if (log.isDebugEnabled()) {
                log.warn(e);
            }
        } catch (InterruptedException e) {
            log.warn(e);
            return false;
        } catch (IOException e) {
            log.warn(e);
            return false;
        } finally {
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
        }
        return true;
    }

    protected void doFinally() {
        if (this.barrier != null) {
            try {
                log.info("> WAITING for barrier " + Thread.currentThread().getName() + ", count=" + count);
                barrier.await();
            } catch (InterruptedException e) {
                log.warn(e);
            } catch (BrokenBarrierException e) {
                log.warn(e);
            }
        }
    }
}
