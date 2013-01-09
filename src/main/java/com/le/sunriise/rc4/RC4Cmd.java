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
package com.le.sunriise.rc4;

import java.nio.ByteBuffer;
import java.security.SecureRandom;
import java.util.Random;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;
import com.sun.jna.NativeLong;

public class RC4Cmd {
    private static final Logger log = Logger.getLogger(RC4Cmd.class);

    /*
     * Must use Sun JDK.
     */

    /**
     * @param args
     */
    public static void main(String[] args) {
        int keyLength = 20;
        final byte[] keyData = genRandomKey(keyLength);

        final String clearTextString = "The quick brown fox jumps over the lazy dog";
        final byte[] clearTextData = clearTextString.getBytes();
        final NativeLong dataLen = new NativeLong(clearTextData.length);
        final ByteBuffer buffer = ByteBuffer.allocate(clearTextData.length);
        final byte[] cipherData = new byte[clearTextData.length];
        final byte[] result = new byte[clearTextData.length];

        log.info(" key.len=" + keyData.length);
        log.info("  dataLen=" + clearTextData.length);

        log.info("> START");

        RC4_KEY key = null;

        key = setKey(keyData);
        ByteBuffer cipherText = encrypt(key, dataLen, clearTextData, buffer);

        key = setKey(keyData);
        cipherText.get(cipherData);
        ByteBuffer clearText = decrypt(key, dataLen, cipherData, buffer);

        clearText.get(result);
        String resultString = new String(result);
        log.info("resultString=" + resultString);
        log.info("  compare=" + resultString.compareTo(clearTextString));

        int max = 100000;
        StopWatch stopWatch = new StopWatch();
        try {
            for (int i = 0; i < max; i++) {
                key = setKey(keyData);
                cipherText = encrypt(key, dataLen, clearTextData, buffer);
            }
        } finally {
            long delta = stopWatch.click();
            log.info("delta=" + delta);

            long secs = delta / 1000L;
            if (secs > 0) {
                log.info("rate=" + (max / secs) + "/sec");
            } else {
                log.info("rate=N/A");
            }
            log.info("< DONE");
        }
    }

    private static byte[] genRandomKey(int keyLength) {
        final byte[] keyData = new byte[keyLength];
        Random random = new SecureRandom();
        random.nextBytes(keyData);
        return keyData;
    }

    private static final RC4_KEY setKey(byte[] keyData) {
        RC4_KEY key = null;
        final int keyLength = keyData.length;
        key = new RC4_KEY();
        Rc4Library.INSTANCE.RC4_set_key(key, keyLength, keyData);
        return key;
    }

    private static final ByteBuffer encrypt(final RC4_KEY key, final NativeLong len, final byte[] data, ByteBuffer buffer) {
        if (buffer == null) {
            buffer = ByteBuffer.allocate(data.length);
        } else {
            buffer.rewind();
        }
        Rc4Library.INSTANCE.RC4(key, len, data, buffer);
        return buffer;
    }

    private static ByteBuffer decrypt(final RC4_KEY key, final NativeLong len, final byte[] data, final ByteBuffer buffer) {
        return encrypt(key, len, data, buffer);
    }

}
