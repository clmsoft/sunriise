package com.le.sunriise.viewer;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;

public class OpenedDb {
    private static final Logger log = Logger.getLogger(OpenedDb.class);

    private File dbFile;
    private File dbLockFile;
    private Database db;

    public File getDbFile() {
        return dbFile;
    }

    public void setDbFile(File dbFile) {
        this.dbFile = dbFile;
    }

    public File getDbLockFile() {
        return dbLockFile;
    }

    public void setDbLockFile(File dbLockFile) {
        this.dbLockFile = dbLockFile;
    }

    public Database getDb() {
        return db;
    }

    public void setDb(Database db) {
        this.db = db;
    }

    public void close() {
        if (db != null) {
            try {
                db.close();
            } catch (IOException e) {
                log.warn(e);
            } finally {
                db = null;
                if (dbLockFile != null) {
                    if (!dbLockFile.delete()) {
                        log.warn("Could NOT delete db lock file=" + dbLockFile);
                    } else {
                        log.info("Deleted db lock file=" + dbLockFile);
                    }
                }
            }
        }
    }
}
