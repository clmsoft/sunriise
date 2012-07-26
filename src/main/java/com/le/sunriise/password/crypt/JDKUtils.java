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
