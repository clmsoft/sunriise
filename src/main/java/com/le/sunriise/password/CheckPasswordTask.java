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
