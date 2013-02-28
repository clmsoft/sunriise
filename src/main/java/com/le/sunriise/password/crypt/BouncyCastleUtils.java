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

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

public class BouncyCastleUtils {
    private static final Logger log = Logger.getLogger(BouncyCastleUtils.class);

    public static byte[] decryptUsingRC4(RC4Engine engine, byte[] ciphertext, byte[] key) {
        // RC4Engine engine = null;
        // engine = getEngine();
        // engine = new RC4Engine();

        boolean forEncryption = false;
        if (log.isDebugEnabled()) {
            log.debug("key.length=" + key.length + ", " + (key.length * 8));
        }
        engine.init(forEncryption, new KeyParameter(key));

        byte[] plaintext = new byte[4];
        engine.processBytes(ciphertext, 0, ciphertext.length, plaintext, 0);
        return plaintext;
    }

    public static byte[] createDigestBytes(byte[] bytes, boolean useSha1) {
        // boolean useSha1 = headerPage.isUseSha1();
        Digest digest = (useSha1 ? new SHA1Digest() : new MD5Digest());
        if (log.isDebugEnabled()) {
            log.debug("digest=" + digest.getAlgorithmName());
        }

        digest.update(bytes, 0, bytes.length);

        // Get digest value
        byte[] digestBytes = new byte[digest.getDigestSize()];
        digest.doFinal(digestBytes, 0);
        return digestBytes;
    }

}
