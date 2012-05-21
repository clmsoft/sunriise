package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

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
                    if (name.endsWith(".mny") || name.endsWith(".mbf")) {
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
            printHeaderPage(headerPage);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (headerPage != null) {
                headerPage = null;
            }
        }
    }

    private static void printHeaderPage(HeaderPage headerPage) {
        System.out.println("getJetFormat=" + headerPage.getJetFormat());
        System.out.println("getJetFormat.PAGE_SIZE=" + headerPage.getJetFormat().PAGE_SIZE);
        System.out.println("getCharset=" + headerPage.getCharset());

        System.out.println("isNewEncryption=" + headerPage.isNewEncryption());

        System.out.println("isUseSha1=" + headerPage.isUseSha1());
        System.out.println("getSalt=" + HeaderPage.toHexString(headerPage.getSalt()));
        System.out.println("getBaseSalt=" + HeaderPage.toHexString(headerPage.getBaseSalt()));
        System.out.println("encrypted4BytesCheck=" + HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
    }
}
