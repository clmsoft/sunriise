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

public class CryptlUtils {
    private static final Logger log = Logger.getLogger(CryptlUtils.class);

    public static byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
        ARCFOUR rc4 = new ARCFOUR(key);
        byte[] cleartext = new byte[ciphertext.length];
        rc4.decrypt(ciphertext, 0, cleartext, 0, cleartext.length);
        return cleartext;
    }

}