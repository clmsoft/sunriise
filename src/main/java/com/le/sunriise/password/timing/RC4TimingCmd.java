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
package com.le.sunriise.password.timing;

import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.engines.RC4Engine;

import com.le.sunriise.StopWatch;
import com.le.sunriise.password.PasswordUtils;
import com.le.sunriise.password.crypt.BouncyCastleUtils;
import com.le.sunriise.password.crypt.CryptlUtils;
import com.le.sunriise.password.crypt.JDKUtils;

public class RC4TimingCmd {
    private static final Logger log = Logger.getLogger(PasswordUtils.class);

    private static final int DEFAULT_KEY_LENGTH = 8;

    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    private static final int DEFAULT_CIPHERTEXT_LENGTH = 4;

    public interface CryptoProvider {
        public byte[] decryptUsingRC4(byte[] ciphertext, byte[] key);

        public String getName();
    };

    private static final class BouncyCastleProvider implements CryptoProvider {
        private final String name;
        private RC4Engine engine;

        public BouncyCastleProvider() {
            super();
            this.name = "BouncyCastleProvider";
            this.engine = new RC4Engine();
        }

        @Override
        public byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
            return BouncyCastleUtils.decryptUsingRC4(engine, ciphertext, key);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final class JDKProvider implements CryptoProvider {
        private final String name;

        public JDKProvider() {
            super();
            this.name = "JDKProvider";
        }

        @Override
        public byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
            return JDKUtils.decryptUsingRC4(ciphertext, key);
        }

        @Override
        public String getName() {
            return name;
        }
    }

    private static final class LocalProvider implements CryptoProvider {
        private final String name;

        public LocalProvider() {
            super();
            this.name = "LocalProvider";
        }

        @Override
        public byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
            return CryptlUtils.decryptUsingRC4(ciphertext, key);
        }

        @Override
        public String getName() {
            return name;
        }
    }

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

        log.info("keyLength=" + keyLength);
        log.info("cipherTextLength=" + cipherTextLength);
        log.info("maxIteration=" + maxIteration);

        CryptoProvider provider = null;

        provider = new LocalProvider();
        doTiming(keyLength, cipherTextLength, maxIteration, provider);

        provider = new BouncyCastleProvider();
        doTiming(keyLength, cipherTextLength, maxIteration, provider);

        // provider = new JDKProvider();
        // doTiming(keyLength, cipherTextLength, maxIteration, provider);

    }

    private static long doTiming(int keyLength, int cipherTextLength, int maxIteration, CryptoProvider provider) {
        long delta = 0L;

        Random rand = new SecureRandom();

        int maxPool = 100;

        byte[][] keys = new byte[maxPool][];
        for (int i = 0; i < maxPool; i++) {
            keys[i] = new byte[keyLength];
            rand.nextBytes(keys[i]);
        }
        byte[][] ciphertexts = new byte[maxPool][];
        for (int i = 0; i < maxPool; i++) {
            ciphertexts[i] = new byte[cipherTextLength];
            rand.nextBytes(ciphertexts[i]);
        }

        log.info("> START, provider=" + provider.getName());
        StopWatch watch = new StopWatch();
        final int max = maxIteration;
        long bytes = 0L;
        try {
            for (int i = 0; i < max; i++) {
                // int ranIndex = rand.nextInt(maxPool);
                int ranIndex = i % maxPool;
                provider.decryptUsingRC4(ciphertexts[ranIndex], keys[ranIndex]);
                bytes += ciphertexts[ranIndex].length;
            }
        } finally {
            delta = watch.click();
            log.info("delta=" + delta);
            log.info("    rate=" + (max / (delta / 1000)) + "/sec");
            log.info("    rate(bytes)=" + ((bytes / 1024) / (delta / 1000)) + "K/sec");
            log.info("< END, provider=" + provider.getName());
        }

        return delta;
    }

    public static long doTiming(int maxIteration, CryptoProvider provider) {
        return doTiming(DEFAULT_KEY_LENGTH, DEFAULT_CIPHERTEXT_LENGTH, maxIteration, provider);
    }

    public static long doTiming(int maxIteration) {
        CryptoProvider provider = new BouncyCastleProvider();
        return doTiming(maxIteration, provider);
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
