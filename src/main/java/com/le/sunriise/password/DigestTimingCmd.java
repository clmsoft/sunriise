package com.le.sunriise.password;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.engines.RC4Engine;

import com.le.sunriise.StopWatch;

public class DigestTimingCmd {
    private static final Logger log = Logger.getLogger(PasswordUtils.class);
    private static final int PASSWORD_LENGTH = 0x28;

    /**
     * @param args
     */
    public static void main(String[] args) {
        byte[] byteArray = new byte[PASSWORD_LENGTH];
        final boolean useSha1 = true;

        for (int i = 0; i < byteArray.length; i++) {
            byteArray[i] = Byte.valueOf("" + i);
        }
        log.info("byteArray.length=" + byteArray.length);
        log.info("useSha1=" + useSha1);
        log.info("> START");
        StopWatch watch = new StopWatch();
        final int max = 10000000;
        long bytes = 0L;
        try {
            for (int i = 0; i < max; i++) {
                BouncyCastleUtils.createDigestBytes(byteArray, useSha1);
                bytes += byteArray.length;
            }
        } finally {
            long delta = watch.click();
            log.info("delta=" + delta);
            log.info("    rate=" + (max / (delta / 1000)) + "/sec");
            log.info("    rate(bytes)=" + ((bytes / 1024) / (delta / 1000)) + "K/sec");
            log.info("< END");
        }
    }
}
