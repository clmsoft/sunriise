package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class HeaderPageCmd {
    private static final Logger log = Logger.getLogger(HeaderPageCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else {
            Class<HeaderPageCmd> clz = HeaderPageCmd.class;
            System.out.println("Usage: java " + clz.getName() + " sample.mny");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        HeaderPage headerPage = null;
        try {
            headerPage = new HeaderPage(dbFile);
            log.info("fileSize=" + dbFile.length());
            
            logHeaderPage(headerPage);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    private static void logHeaderPage(HeaderPage headerPage) {
        log.info("getJetFormat=" + headerPage.getJetFormat());
        log.info("getJetFormat.PAGE_SIZE=" + headerPage.getJetFormat().PAGE_SIZE);
        log.info("getCharset=" + headerPage.getCharset());

        log.info("isNewEncryption=" + headerPage.isNewEncryption());
        log.info("getEmbeddedDatabasePassword=" + headerPage.getEmbeddedDatabasePassword());

        log.info("isUseSha1=" + headerPage.isUseSha1());
        log.info("getSalt=" + HeaderPage.toHexString(headerPage.getSalt()));
        log.info("getBaseSalt=" + HeaderPage.toHexString(headerPage.getBaseSalt()));
        log.info("encrypted4BytesCheck=" + HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
//        log.info("decrypted4BytesCheck=" + HeaderPage.toHexString(headerPage.getBaseSalt()));
    }
}
