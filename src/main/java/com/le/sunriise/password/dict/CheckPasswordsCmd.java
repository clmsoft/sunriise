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
package com.le.sunriise.password.dict;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;
import com.le.sunriise.password.HeaderPage;
import com.le.sunriise.password.timing.Duration;

// http://essayweb.net/mathematics/permcomb.shtml
public class CheckPasswordsCmd {
    private static final Logger log = Logger.getLogger(CheckPasswordsCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File path = null;
        int threads = 1;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            path = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            path = new File(args[1]);
            try {
                threads = Integer.valueOf(args[2]);
            } catch (NumberFormatException e) {
                log.warn(e);
            }
        } else {
            Class<CheckPasswordsCmd> clz = CheckPasswordsCmd.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny {passwordsFile.txt | path} threads");
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
        if (threads <= 0) {
            threads = 1;
        }

        log.info("dbFile=" + dbFile);
        log.info("path=" + path);
        log.info("threads=" + threads);

        String matchedPassword = null;
        CheckDictionary checker = null;
        StopWatch stopWatch = new StopWatch();
        try {
            checker = new CheckDictionary(threads);
            HeaderPage headerPage = new HeaderPage(dbFile);
            matchedPassword = checker.check(headerPage, path);
            log.info("Have checked " + checker.getCounter().get());
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            long millis = stopWatch.click();

            if (checker != null) {
                checker.close();
                checker = null;
            }

            String durationString = Duration.toDurationString(millis);
            log.info("Took " + durationString);

            log.info("< DONE, matchedPassword=" + matchedPassword);

            // the pool might still be running. Force exit.
            System.exit(0);
        }
    }
}
