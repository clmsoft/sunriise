package com.le.sunriise.encryption;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class PrintHeader {
    private static final Logger log = Logger.getLogger(PrintHeader.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File mdbFile = new File(args[0]);
        String password = null;
        try {
            EncryptionUtils.parseHeader(mdbFile, password);
        } catch (IOException e) {
            log.error(e);
        }
    }

}
