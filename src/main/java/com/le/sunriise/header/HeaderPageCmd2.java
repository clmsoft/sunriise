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
package com.le.sunriise.header;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.backup.BackupFileUtils;

public class HeaderPageCmd2 {
    private static final Logger log = Logger.getLogger(HeaderPageCmd2.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;

        if (args.length == 0) {
            Class<HeaderPageCmd2> clz = HeaderPageCmd2.class;
            System.out.println("Usage: java " + clz.getName() + " sample.mny [sample2.mny ...]");
            System.exit(1);
        }

        for (String arg : args) {
            dbFile = new File(arg);
            if (dbFile.isDirectory()) {
                File[] files = dbFile.listFiles();
                for (File file : files) {
                    String name = file.getName();
                    if (BackupFileUtils.isMnyFiles(name)) {
                        print(file);
                    }
                }
            } else {
                print(dbFile);
            }
        }
    }

    private static void print(File dbFile) {
        System.out.println("###");
        System.out.println("dbFile=" + dbFile);
        HeaderPage headerPage = null;
        try {
            headerPage = new HeaderPage(dbFile);
            // System.out.println("fileSize=" +
            // dbFile.length());
            HeaderPage.printHeaderPage(headerPage);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (headerPage != null) {
                headerPage = null;
            }
        }
    }
}
