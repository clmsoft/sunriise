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
        
        this.passwordQueue = new ArrayBlockingQueue<PasswordItem>(passwordQueueCapacity);
        this.threadCount = threadCount;
        this.dbFile = dbFile;

        this.readerQueue = new ArrayBlockingQueue<ReaderItem>(readerQueueCapacity);
        
        this.mainBarrier = createMainBarrier(dbFile);

        this.producerBarrier = createProducerBarrier();
        this.producerThread = createProducerThread();

        this.consumerBarrier = createConsumerBarrier();
        
        this.consumerThreads = createConsumerThreads(dbFile);

        startThreads();
    }

    private ArrayList<Thread> createConsumerThreads(final File dbFile) {
        ArrayList<Thread> threads = new ArrayList<Thread>();
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
            Thread t = new Thread(consumer, "ConsumerThread-" + i);
            t.setDaemon(true);
            threads.add(t);
        }
        return threads;
    }

    private CyclicBarrier createConsumerBarrier() {
        int parties = this.threadCount;
        Runnable barrierAction = new Runnable() {
            @Override
            public void run() {
                log.info("All consumers are DONE");

                if (mainBarrier != null) {
                    try {
                        log.info("Waiting for mainBarrier ...");
                        // from consumer
                        mainBarrier.await();
                    } catch (InterruptedException e) {
                        log.warn(e);
                    } catch (BrokenBarrierException e) {
                        log.warn(e);
                    }
                }
            }
        };
        
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
        
        return barrier;
    }

    private Thread createProducerThread() {
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
        Thread t = new Thread(producerTask, "ProducerThread");
        t.setDaemon(true);
        return t;
    }

    private CyclicBarrier createProducerBarrier() {
        int parties = 1;
        Runnable barrierAction = new Runnable() {
            @Override
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
                            // from producer
                            mainBarrier.await();
                        } catch (InterruptedException e) {
                            log.warn(e);
                        } catch (BrokenBarrierException e) {
                            log.warn(e);
                        }
                    }
                }
            }
        };
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
        return barrier;
    }

    private CyclicBarrier createMainBarrier(final File dbFile) {
        // parties: main + producer + consumer
        int parties = 3;
        Runnable barrierAction = new Runnable() {
            @Override
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
        };
        CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
        return barrier;
    }

    private void startThreads() {
        if (this.producerThread != null) {
            this.producerThread.start();
        }

        for (Thread t : consumerThreads) {
            t.start();
        }
    }

    private void recursePath(File path) throws IOException {
        if (path.isDirectory()) {
            recurseDirectory(path);
        } else {
            consumeFile(path);
        }
    }

    private void consumeFile(File file) {
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

    private void recurseDirectory(File directory) throws IOException {
        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                recursePath(file);
            }
        }
    }

    private boolean hasResult() {
        return getPassword() != null;
    }

    private void startCheck(File path) throws IOException {
        try {
            recursePath(path);
        } finally {
            try {
                log.info("Notifying producers to stop ...");
                readerQueue.put(new ReaderItem(true));

                log.info("> WAITING for mainBarrier ...");
                // from main
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
