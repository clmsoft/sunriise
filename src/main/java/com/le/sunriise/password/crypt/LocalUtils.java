package com.le.sunriise.password.crypt;

import org.apache.log4j.Logger;

public class LocalUtils {
    private static final Logger log = Logger.getLogger(LocalUtils.class);

    public static byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
        ARCFOUR rc4 = new ARCFOUR(key);
        byte[] cleartext = new byte[ciphertext.length];
        rc4.decrypt(ciphertext, 0, cleartext, 0, cleartext.length);
        return cleartext;
    }

}
