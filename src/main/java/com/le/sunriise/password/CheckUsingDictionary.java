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
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import org.apache.log4j.Logger;


/**
 * Check for matching password against a set of dictionary. Support multi-threads.
 * 
 * @author hle
 *
 */
public class CheckUsingDictionary {
    private static final Logger log = Logger.getLogger(CheckUsingDictionary.class);

    private boolean trim = true;

    private ExecutorService pool;

    private final AtomicLong counter = new AtomicLong(0L);

    private final AtomicBoolean quit = new AtomicBoolean(false);

    private File currentCandidatesFile;
    
    /**
     * 
     * @param threads is number of threads to run
     */
    public CheckUsingDictionary(int threads) {
        super();
        pool = Executors.newFixedThreadPool(threads);
    }

    /**
     * Default number of threads is 1.
     */
    public CheckUsingDictionary() {
        this(1);
    }

    /**
     * Check the given headerPage against a set of dictionary. The set of dictionary is formed
     * by parsing the give file: one word per line, skip '#' comment line. If the given path is
     * a directory, traverse the directory, skip 'hidden' file (with prefix dot '.').
     * 
     * @param headerPage
     * @param candidatesPath
     * @return a String which is a matched password. If null, no password found.
     * @throws IOException
     */
    public String check(HeaderPage headerPage, File candidatesPath) throws IOException {
        return recursePath(headerPage, candidatesPath);
    }

    public void stop() {
        quit.getAndSet(true);
    }
    
    private String recursePath(HeaderPage headerPage, File candidatesPath) throws IOException {
        if (acceptPathAsDirectory(candidatesPath)) {
            return recurseDirectory(headerPage, candidatesPath);
        } else {
            if (acceptPathAsFile(candidatesPath)) {
                return consumeFile(headerPage, candidatesPath);
            } else {
                return null;
            }
        }
    }

    private boolean acceptPathAsFile(File path) {
        if (path == null) {
            return false;
        }
        if (path.getName().startsWith(".")) {
            return false;
        }
        return path.isFile();
    }

    private boolean acceptPathAsDirectory(File path) {
        if (path == null) {
            return false;
        }
        if (path.getName().startsWith(".")) {
            return false;
        }
        return path.isDirectory();
    }

    private String recurseDirectory(HeaderPage dbFile, File directory) throws IOException {
        if (quit.get()) {
            return null;
        }
        
        String value = null;
        log.info("> dir=" + directory);
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                value = recursePath(dbFile, file);
                if (value != null) {
                    return value;
                }
            }
        }
        return null;
    }

    private String consumeFile(HeaderPage headerPage, File candidatesFile) {
        if (quit.get()) {
            return null;
        }
        
        if (headerPage == null) {
            return null;
        }
        if (candidatesFile == null) {
            return null;
        }
        // single-thread here
        this.currentCandidatesFile = candidatesFile;
        log.info("> candidatesFile=" + candidatesFile);
        BufferedReader candidatesReader = null;
        try {
            candidatesReader = new BufferedReader(new FileReader(candidatesFile));
            return consumeReader(headerPage, candidatesReader);
        } catch (IOException e) {
            log.error(e, e);
        }
        return null;
    }

    private String consumeReader(HeaderPage headerPage, BufferedReader reader) {
        String result = null;
        String line = null;
        int lineCount = 1;
        List<Future<String>> runningJobs = new ArrayList<Future<String>>();

        try {
            // LOOP
            while ((line = reader.readLine()) != null) {
                if (quit.get()) {
                    break;
                }
                
                if (trim) {
                    line = line.trim();
                }
                if (skipLine(line)) {
                    continue;
                }
                lineCount++;

                Callable<String> callable = createWorker(headerPage, line, counter);
                if (callable != null) {
                    Future<String> future = pool.submit(callable);
                    runningJobs.add(future);
                }

                // check running jobs to see if any has done
                result = checkJobsForResult(runningJobs);
                if (result != null) {
                    // got result, time to quit
                    break;
                }
            }

            // flush the working list
            if (result == null) {
                result = waitForEmptyWorkingList(runningJobs);
            }
        } catch (IOException e) {
            log.error(e);
        }

        return result;
    }

    private boolean skipLine(String line) {
        if (line.length() <= 0) {
            return true;
        }
        if (line.charAt(0) == '#') {
            return true;
        }

        return false;
    }

    private String checkJobsForResult(List<Future<String>> runningJobs) {
        ListIterator<Future<String>> iter = runningJobs.listIterator();
        String result = null;
        
        // LOOP
        while (iter.hasNext()) {
            if (quit.get()) {
                break;
            }
            
            Future<String> job = iter.next();
            if (job.isDone()) {
                try {
                    result = job.get();
                    if (result != null) {
                        if (log.isDebugEnabled()) {
                            log.debug("111 runningJobs.size=" + runningJobs.size());
                        }
                        break;
                    }
                } catch (ExecutionException e) {
                    log.warn(e, e);
                } catch (InterruptedException e) {
                    log.warn(e);
                } finally {
                    iter.remove();
                }
            }
        }
        return result;
    }

    private String waitForEmptyWorkingList(List<Future<String>> runningJobs) {
        String result = null;
        long sleepMs = 1000L;

        // LOOP
        while (runningJobs.size() > 0) {
            if (quit.get()) {
                break;
            }
            
            result = checkJobsForResult(runningJobs);

            if (result != null) {
                if (quit.get()) {
                    break;
                }
                try {
                    Thread.sleep(sleepMs);
                } catch (InterruptedException e) {
                    log.warn(e);
                }
            }
        }

        return result;
    }

    protected Callable<String> createWorker(HeaderPage headerPage, String testPassword, AtomicLong counter) {
        return new CheckPasswordWorker(headerPage, testPassword, counter);
    }

    public void close() {
        if (pool != null) {
            try {
                pool.shutdownNow();
            } finally {
                pool = null;
            }
        }

    }

    public AtomicLong getCounter() {
        return counter;
    }

    public File getCurrentCandidatesFile() {
        return currentCandidatesFile;
    }    
}
