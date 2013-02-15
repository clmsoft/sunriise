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
    private String password;
    private ROMemoryMappedFileChannel memoryMappedFileChannel;
    
    public JackcessFileChannelAdapter getMemoryMappedFileChannel() {
        return memoryMappedFileChannel;
    }

    public void setMemoryMappedFileChannel(ROMemoryMappedFileChannel memoryMappedFileChannel) {
        this.memoryMappedFileChannel = memoryMappedFileChannel;
    }

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
            log.info("Closing dbFile=" + dbFile);
            try {
                db.close();

                // since we specify the channel ourselves. We will need to close
                // it ourselves.
                if (memoryMappedFileChannel != null) {
                    try {
                        log.info("Closing memoryMappedFileChannel for dbFile=" + memoryMappedFileChannel.getDbFile());
                        memoryMappedFileChannel.close();
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        memoryMappedFileChannel = null;
                    }
                }
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

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public boolean isMemoryMapped() {
        return getMemoryMappedFileChannel() != null;
    }
}
