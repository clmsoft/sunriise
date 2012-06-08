/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
import java.io.IOException;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.JetFormat;
import com.le.sunriise.Utils;

public class PasswordUtils {
    private static final Logger log = Logger.getLogger(PasswordUtils.class);

    public static boolean checkUsingHeaderPage(HeaderPage headerPage, String testPassword) throws IOException {
        boolean result = false;
        try {
            if (AbstractHeaderPagePasswordChecker.checkPassword(headerPage, testPassword)) {
                if (log.isDebugEnabled()) {
                    log.debug("OK password=" + testPassword);
                }
                result = true;
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("NOT OK password=" + testPassword);
                }
                result = false;
            }

        } finally {

        }
        return result;
    }

    public static boolean checkUsingOpenDb(File dbFile, String testPassword) throws IOException {
        boolean result = false;
        try {
            Utils.openDbReadOnly(dbFile, testPassword);
            if (log.isDebugEnabled()) {
                log.debug("testPassword=" + testPassword + ", YES");
            }
            result = true;
        } catch (java.lang.IllegalStateException e) {
            // wrong password
            if (log.isDebugEnabled()) {
                log.warn(e);
            }
        }
        return result;
    }

    public static boolean doubleCheck(HeaderPage headerPage, String testPassword) {
        boolean matched = false;
        File file = headerPage.getDbFile();
        if ((file != null) && (file.length() > (JetFormat.VERSION_MSISAM.PAGE_SIZE * 2))) {
            // double check using openDb
            try {
                matched = checkUsingOpenDb(file, testPassword);
            } catch (Exception e) {
                if (log.isDebugEnabled()) {
                    log.error(e, e);
                }
                matched = false;
            }
            if (matched == false) {
                log.warn("False positive, testPassword=" + testPassword);
            }
        }
        return matched;
    }

}
