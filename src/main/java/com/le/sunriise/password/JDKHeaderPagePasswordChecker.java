/*******************************************************************************
 * Copyright (c) 2010 Hung Le
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
package com.le.sunriise.password;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.password.crypt.JDKUtils;

public class JDKHeaderPagePasswordChecker extends AbstractHeaderPagePasswordChecker {
    private static final Logger log = Logger.getLogger(JDKHeaderPagePasswordChecker.class);

    public JDKHeaderPagePasswordChecker(HeaderPage headerPage) throws IOException {
        super(headerPage);
    }

    @Override
    protected byte[] createDigestBytes(byte[] passwordBytes, boolean useSha1) {
        return JDKUtils.createDigestBytes(passwordBytes, useSha1);
    }

    @Override
    protected byte[] decryptUsingRC4(byte[] encrypted4BytesCheck, byte[] testKey) {
        boolean useJDK = true;
        if (useJDK) {
            return JDKUtils.decryptUsingRC4(encrypted4BytesCheck, testKey);
        } else {
            return decryptUsingLocalRC4(encrypted4BytesCheck, testKey);
        }
    }

    private byte[] decryptUsingLocalRC4(byte[] encrypted4BytesCheck, byte[] testKey) {
        byte[] decrypted4BytesCheck = new byte[4];
        return decrypted4BytesCheck;
    }

}
