package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.concurrent.Callable;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;

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

    public String call() throws Exception {
        String rv = null;
        Database db = null;
        try {
            rv = null;
            for (String password : passwords) {
                db = Utils.openDbReadOnly(dbFile, password);
                if (db != null) {
                    rv = password;
                    break;
                }
            }
        } catch (Exception e) {
            if (log.isDebugEnabled()) {
                log.error(e);
            }
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    db = null;
                }
            }
        }
        return rv;
    }
}
