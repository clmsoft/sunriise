package com.le.sunriise.password.timing;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.engines.RC4Engine;

import com.le.sunriise.StopWatch;
import com.le.sunriise.password.BouncyCastleUtils;
import com.le.sunriise.password.PasswordUtils;

public class RC4TimingCmd {
    private static final Logger log = Logger.getLogger(PasswordUtils.class);

    private static final int DEFAULT_KEY_LENGTH = 8;

    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    private static final int DEFAULT_CIPHERTEXT_LENGTH = 4;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int keyLength = DEFAULT_KEY_LENGTH;
        int cipherTextLength = DEFAULT_CIPHERTEXT_LENGTH;
        int maxIteration = DEFAULT_MAX_ITERATIONS;

        if (args.length == 0) {
            // default is above
        } else if (args.length == 1) {
            keyLength = intValueOf(args[0], keyLength);
        } else if (args.length == 2) {
            keyLength = intValueOf(args[0], keyLength);
            cipherTextLength = intValueOf(args[1], cipherTextLength);
        } else if (args.length == 3) {
            keyLength = intValueOf(args[0], keyLength);
            cipherTextLength = intValueOf(args[1], cipherTextLength);
            maxIteration = intValueOf(args[2], maxIteration);
        } else {
            Class<RC4TimingCmd> clz = RC4TimingCmd.class;
            System.out.println("Usage: java " + clz.getName() + "[keyLength cipherTextLength maxIteration]");
            System.out.println("  keyLength (default=" + keyLength + ")");
            System.out.println("  cipherTextLength (default=" + cipherTextLength + ")");
            System.out.println("  maxIteration (default=" + maxIteration + ")");

            System.exit(1);
        }

        doTiming(keyLength, cipherTextLength, maxIteration);
    }

    private static void doTiming(int keyLength, int cipherTextLength, int maxIteration) {
        RC4Engine engine = new RC4Engine();

        byte[] key = new byte[keyLength];
        byte[] ciphertext = new byte[cipherTextLength];

        for (int i = 0; i < key.length; i++) {
            key[i] = Byte.valueOf("" + i);
        }

        for (int i = 0; i < ciphertext.length; i++) {
            ciphertext[i] = Byte.valueOf("" + i);
        }

        log.info("key.length=" + key.length);
        log.info("ciphertext.length=" + ciphertext.length);
        log.info("maxIteration=" + maxIteration);

        log.info("> START");
        StopWatch watch = new StopWatch();
        final int max = maxIteration;
        long bytes = 0L;
        try {
            for (int i = 0; i < max; i++) {
                BouncyCastleUtils.decryptUsingRC4(engine, ciphertext, key);
                bytes += ciphertext.length;
            }
        } finally {
            long delta = watch.click();
            log.info("delta=" + delta);
            log.info("    rate=" + (max / (delta / 1000)) + "/sec");
            log.info("    rate(bytes)=" + ((bytes / 1024) / (delta / 1000)) + "K/sec");
            log.info("< END");
        }
    }

    public static int intValueOf(String strValue, int defaultValue) {
        int value = defaultValue;
        try {
            value = Integer.valueOf(strValue);
        } catch (NumberFormatException e) {
            log.warn(e);
            value = defaultValue;
        }
        return value;
    }
}
