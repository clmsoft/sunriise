package com.le.sunriise.password;

import java.io.IOException;
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

public class JDKHeaderPageOnlyPasswordChecker extends AbstractHeaderPageOnlyPasswordChecker {
    private static final Logger log = Logger.getLogger(JDKHeaderPageOnlyPasswordChecker.class);

    public JDKHeaderPageOnlyPasswordChecker(HeaderPage headerPage) throws IOException {
        super(headerPage);
    }

    @Override
    protected byte[] createDigestBytes(HeaderPage headerPage, byte[] passwordBytes) {
        byte[] digestBytes = null;
        boolean useSha1 = headerPage.isUseSha1();

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

    @Override
    protected byte[] decryptUsingRC4(byte[] encrypted4BytesCheck, byte[] testKey) {
        byte[] decrypted4BytesCheck = new byte[4];

        try {
            Cipher rc4 = Cipher.getInstance("ARCFOUR");
            SecretKeySpec secretKeySpec = new SecretKeySpec(testKey, "ARCFOUR");
            rc4.init(Cipher.DECRYPT_MODE, secretKeySpec);
            rc4.doFinal(encrypted4BytesCheck, 0, encrypted4BytesCheck.length, decrypted4BytesCheck, 0);
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

        return decrypted4BytesCheck;
    }

}
