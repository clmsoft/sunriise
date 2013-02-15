package com.le.sunriise.password.dict;

import java.io.File;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import com.le.sunriise.header.HeaderPage;

public class PasswordWorkerContext {
    private final File dbFile;

    private final HeaderPage headerPage;
    
    private final AtomicLong counter;

    private final AtomicBoolean quit;
    
    private final ResultCollector resultCollector;
    
    public PasswordWorkerContext(File dbFile, HeaderPage headerPage, AtomicLong counter, AtomicBoolean quit, ResultCollector resultCollector) {
        super();
        this.dbFile = dbFile;
        this.headerPage = headerPage;
        this.counter = counter;
        this.quit = quit;
        this.resultCollector= resultCollector; 
    }

    public File getDbFile() {
        return dbFile;
    }

    public HeaderPage getHeaderPage() {
        return headerPage;
    }

    public AtomicLong getCounter() {
        return counter;
    }

    public AtomicBoolean getQuit() {
        return quit;
    }

    public ResultCollector getResultCollector() {
        return resultCollector;
    }

}
