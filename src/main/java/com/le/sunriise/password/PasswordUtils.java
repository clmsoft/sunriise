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
