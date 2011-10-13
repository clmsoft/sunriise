package com.le.sunriise.password;

import java.io.File;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class CheckPasswordTask implements Callable<String> {
    private static final Logger log = Logger.getLogger(CheckPasswordTask.class);

    private File dbFile;

    private List<String> passwords;

    private Integer id;

    public CheckPasswordTask(File dbFile, List<String> passwords, Integer id) {
        super();
        this.dbFile = dbFile;
        this.passwords = passwords;
        this.id = id;
    }

    
    @Override
    public String call() throws Exception {
        String rv = null;
        OpenedDb openedDb = null;
        try {
            rv = null;
            for (String password : passwords) {
                openedDb = Utils.openDbReadOnly(dbFile, password);
                if (openedDb != null) {
                    rv = password;
                    break;
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e);
            }
        } finally {
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
        }
        return rv;
    }
}
