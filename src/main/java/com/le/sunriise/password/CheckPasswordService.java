package com.le.sunriise.password;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import org.apache.log4j.Logger;

public class CheckPasswordService {
    private static final Logger log = Logger.getLogger(CheckPasswordService.class);

    private static final int DEFAULT_THREAD_COUNT = 4;

    private static final int DEFAULT_PASSWORD_QUEUE_CAPACITY = 100;

    private String password = null;

    private final BlockingQueue<PasswordItem> passwordQueue;

    private final BlockingQueue<ReaderItem> readerQueue;

    private int threadCount;

    private Thread producerThread;

    private ArrayList<Thread> consumerThreads;

    private File dbFile;

    private final int readerQueueCapacity = 2;

    private CyclicBarrier mainBarrier;

    private CyclicBarrier producerBarrier;

    private CyclicBarrier consumerBarrier;

    public CheckPasswordService(final File dbFile) {
        this(DEFAULT_PASSWORD_QUEUE_CAPACITY, DEFAULT_THREAD_COUNT, dbFile);
    }

    public CheckPasswordService(int passwordQueueCapacity, int threadCount, final File dbFile) {
        super();
        this.threadCount = threadCount;
        this.passwordQueue = new ArrayBlockingQueue<PasswordItem>(passwordQueueCapacity);
        this.readerQueue = new ArrayBlockingQueue<ReaderItem>(readerQueueCapacity);
        this.dbFile = dbFile;

        // main + producer + consumer
        this.mainBarrier = new CyclicBarrier(3, new Runnable() {
            public void run() {
                log.info("All producers and consumers are DONE");
                if (password == null) {
                    log.info("Found no working password");
                    log.info("  for dbFile=" + dbFile);
                } else {
                    log.info("Found working password");
                    log.info("  password=" + password);
                    log.info("  for dbFile=" + dbFile);
                }
            }
        });

        this.producerBarrier = new CyclicBarrier(1, new Runnable() {
            public void run() {
                log.info("All producers are DONE");
                try {
                    log.info("Notifying consummer to stop ...");
                    passwordQueue.put(new PasswordItem(true));
                } catch (InterruptedException e) {
                    log.warn(e);
                } finally {
                    if (mainBarrier != null) {
                        try {
                            log.info("Waiting for mainBarrier ...");
                            mainBarrier.await();
                        } catch (InterruptedException e) {
                            log.warn(e);
                        } catch (BrokenBarrierException e) {
                            log.warn(e);
                        }
                    }
                }
            }
        });

        Runnable producerTask = new AbstractPasswordProducer(readerQueue, passwordQueue, this.producerBarrier) {
            @Override
            protected boolean stopProducing() {
                if (hasResult()) {
                    return true;
                } else {
                    return false;
                }
            }
        };
        this.producerThread = new Thread(producerTask, "ProducerThread");
        this.producerThread.setDaemon(true);

        this.consumerBarrier = new CyclicBarrier(this.threadCount, new Runnable() {
            public void run() {
                log.info("All consumers are DONE");

                if (mainBarrier != null) {
                    try {
                        log.info("Waiting for mainBarrier ...");
                        mainBarrier.await();
                    } catch (InterruptedException e) {
                        log.warn(e);
                    } catch (BrokenBarrierException e) {
                        log.warn(e);
                    }
                }
            }
        });
        this.consumerThreads = new ArrayList<Thread>();
        for (int i = 0; i < this.threadCount; i++) {
            Runnable consumer = new AbstractPasswordConsumer(dbFile, passwordQueue, this.consumerBarrier) {
                @Override
                protected void notifyFoundPassword(String password) {
                    log.info("Found password=" + password);
                    setPassword(password);
                }

                @Override
                protected boolean stopConsuming() {
                    if (hasResult()) {
                        return true;
                    } else {
                        return false;
                    }
                }
            };
            Thread consumerThread = new Thread(consumer, "ConsumerThread-" + i);
            consumerThread.setDaemon(true);
            consumerThreads.add(consumerThread);
        }

        startThreads();
    }

    private void startThreads() {
        if (this.producerThread != null) {
            this.producerThread.start();
        }

        for (Thread t : consumerThreads) {
            t.start();
        }
    }

    private void walkPath(File path) throws IOException {
        if (path.isDirectory()) {
            walkDirectory(path);
        } else {
            checkFile(path);
        }
    }

    private void checkFile(File file) {
        log.info("> file=" + file);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            log.info("Adding to reader queue, file=" + file);
            readerQueue.put(new ReaderItem(reader));
        } catch (IOException e) {
            log.error(e, e);
        } catch (InterruptedException e) {
            log.warn(e);
        }
    }

    private void walkDirectory(File directory) throws IOException {
        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                walkPath(file);
            }
        }
    }

    private boolean hasResult() {
        return getPassword() != null;
    }

    private void startCheck(File path) throws IOException {
        try {
            walkPath(path);
        } finally {
            try {
                log.info("Notifying producers to stop ...");
                readerQueue.put(new ReaderItem(true));

                log.info("> WAITING for mainBarrier ...");
                mainBarrier.await();
            } catch (InterruptedException e) {
                log.warn(e);
            } catch (BrokenBarrierException e) {
                log.warn(e);
            }
        }
    }

    public synchronized String getPassword() {
        return password;
    }

    public synchronized void setPassword(String password) {
        this.password = password;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File path = null;
        if (args.length == 2) {
            dbFile = new File(args[0]);
            path = new File(args[1]);
        } else {
            Class<CheckPasswordService> clz = CheckPasswordService.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny [passwordsFile.txt | path]");
            System.exit(1);
        }

        if (!dbFile.exists()) {
            log.error("dbFile does not exist, dbFile=" + dbFile);
            System.exit(1);
        }

        if (!path.exists()) {
            log.error("path does not exist, path=" + path);
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("path=" + path);
        try {
            CheckPasswordService checker = new CheckPasswordService(dbFile);
            checker.startCheck(path);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }
}
