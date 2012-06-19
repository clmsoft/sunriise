package com.le.sunriise.password.crypt;

import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.ShortBufferException;
import javax.crypto.spec.SecretKeySpec;

import org.apache.log4j.Logger;

public class JDKUtils {
    private static final Logger log = Logger.getLogger(JDKUtils.class);

    public static final byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {

        byte[] plainText = new byte[4];

        try {
            Cipher rc4 = Cipher.getInstance("ARCFOUR");
            SecretKeySpec secretKeySpec = new SecretKeySpec(key, "ARCFOUR");
            rc4.init(Cipher.DECRYPT_MODE, secretKeySpec);
            rc4.doFinal(ciphertext, 0, ciphertext.length, plainText, 0);
        } catch (NoSuchAlgorithmException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        } catch (NoSuchPaddingException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        } catch (InvalidKeyException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        } catch (ShortBufferException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        } catch (IllegalBlockSizeException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        } catch (BadPaddingException e) {
            log.error(e, e);
            throw new RuntimeException(e);
        }

        return plainText;
    }

    public static byte[] createDigestBytes(byte[] passwordBytes, boolean useSha1) {
            byte[] digestBytes = null;
    //        boolean useSha1 = headerPage.isUseSha1();
    
            MessageDigest digest = null;
    
            try {
                if (useSha1) {
                    digest = MessageDigest.getInstance("SHA-1");
                } else {
                    digest = MessageDigest.getInstance("MD5");
                }
                digest.update(passwordBytes, 0, passwordBytes.length);
                digestBytes = digest.digest();
            } catch (NoSuchAlgorithmException e) {
                log.error(e, e);
                throw new RuntimeException(e);
            }
    
            return digestBytes;
        }

}
