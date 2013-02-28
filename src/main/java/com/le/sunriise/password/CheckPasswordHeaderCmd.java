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
import java.io.IOException;
import java.util.Arrays;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.JetFormat;
import com.le.sunriise.backup.BackupFileUtils;
import com.le.sunriise.header.HeaderPage;

public class CheckPasswordHeaderCmd {
    private static final Logger log = Logger.getLogger(CheckPasswordHeaderCmd.class);

    private File srcFile;

    private HeaderPage srcHeaderPage;

    public CheckPasswordHeaderCmd(File file) throws IOException {
        this.srcFile = file;
        this.srcHeaderPage = new HeaderPage(file);
    }

    private boolean checkPaths(String[] paths) throws IOException {
        boolean rv = true;
        System.out.println("srcFile=" + srcFile);

        File destFile;
        for (int i = 1; i < paths.length; i++) {
            destFile = new File(paths[i]);
            if (destFile.isDirectory()) {
                if (!checkDir(srcHeaderPage, destFile)) {
                    rv = false;
                }
            } else {
                HeaderPage destHeaderPage = new HeaderPage(destFile);
                if (!check(srcHeaderPage, destHeaderPage)) {
                    notifyMismatch(destFile, destHeaderPage);
                    rv = false;
                } else {
                    notifyMatch(destFile, destHeaderPage);
                }
            }
        }
        return rv;
    }

    private boolean checkDir(HeaderPage srcHeaderPage, File path) throws IOException {
        boolean rv = true;
        File[] files = path.listFiles();
        for (File destFile : files) {
            if (destFile.isFile()) {
                String name = destFile.getName();
                if (BackupFileUtils.isMnyFiles(name)) {
                    try {
                        // System.out.println("destFile=" + destFile);
                        HeaderPage destHeaderPage = new HeaderPage(destFile);
                        if (!check(srcHeaderPage, destHeaderPage)) {
                            notifyMismatch(destFile, destHeaderPage);
                            rv = false;
                        } else {
                            notifyMatch(destFile, destHeaderPage);
                        }
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.warn(e);
                        }
                        System.out.println("SKIPPED: " + destFile);
                    }
                }
            } else {
                if (!checkDir(srcHeaderPage, destFile)) {
                    rv = false;
                }

            }
        }
        return rv;
    }

    private void notifyMatch(File destFile, HeaderPage destHeaderPage) {
        System.out.println("MATCHED " + destFile);
    }

    private void notifyMismatch(File destFile, HeaderPage destHeaderPage) {
        System.out.println("MISMATCHED " + destFile);
        System.out.println("# srcFile=" + srcFile);
        HeaderPage.printHeaderPage(srcHeaderPage, "     ");

        System.out.println("# destFile=" + destFile);
        HeaderPage.printHeaderPage(destHeaderPage, "     ");
    }

    private boolean check(HeaderPage srcHeaderPage, HeaderPage destHeaderPage) {
        boolean rv = true;

        if (!checkJetFormat(srcHeaderPage.getJetFormat(), destHeaderPage.getJetFormat())) {
            return false;
        }

        if (srcHeaderPage.getJetFormat().PAGE_SIZE != destHeaderPage.getJetFormat().PAGE_SIZE) {
            return false;
        }

        if (srcHeaderPage.getCharset().compareTo(destHeaderPage.getCharset()) != 0) {
            return false;
        }

        if (srcHeaderPage.isNewEncryption() != destHeaderPage.isNewEncryption()) {
            return false;
        }

        if (srcHeaderPage.isUseSha1() != destHeaderPage.isUseSha1()) {
            return false;
        }

        if (!compareByteArrays(srcHeaderPage.getSalt(), destHeaderPage.getSalt())) {
            return false;
        }

        if (!compareByteArrays(srcHeaderPage.getBaseSalt(), destHeaderPage.getBaseSalt())) {
            return false;
        }

        if (!compareByteArrays(srcHeaderPage.getEncrypted4BytesCheck(), destHeaderPage.getEncrypted4BytesCheck())) {
            return false;
        }

        return rv;
    }

    private boolean compareByteArrays(byte[] data, byte[] data2) {
        return Arrays.equals(data, data2);
    }

    private boolean checkJetFormat(JetFormat jetFormat, JetFormat jetFormat2) {
        boolean rv = true;

        rv = jetFormat.toString().compareToIgnoreCase(jetFormat2.toString()) == 0;

        return rv;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        if (args.length < 2) {
            Class<CheckPasswordHeaderCmd> clz = CheckPasswordHeaderCmd.class;
            System.out.println("Usage: java " + clz.getName() + " file1.mny file1.mny ...");
            System.exit(1);
        }

        try {
            CheckPasswordHeaderCmd checkPasswordHeader = new CheckPasswordHeaderCmd(new File(args[0]));
            checkPasswordHeader.checkPaths(args);
        } catch (IOException e) {
            log.error(e, e);
        }
    }

}
