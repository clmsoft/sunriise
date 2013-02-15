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

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.engines.RC4Engine;

import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.crypt.BouncyCastleUtils;

public class HeaderPagePasswordChecker extends AbstractHeaderPagePasswordChecker {
    private static final Logger log = Logger.getLogger(HeaderPagePasswordChecker.class);

    private RC4Engine engine;

    public HeaderPagePasswordChecker(HeaderPage headerPage) throws IOException {
        super(headerPage);

    }

    private final RC4Engine getEngine() {
        if (engine == null) {
            engine = new RC4Engine();
        }
        return engine;
    }

    @Override
    protected byte[] createDigestBytes(byte[] bytes, boolean useSha1) {
        return BouncyCastleUtils.createDigestBytes(bytes, useSha1);
    }

    @Override
    protected byte[] decryptUsingRC4(byte[] ciphertext, byte[] key) {
        RC4Engine engine = null;
        engine = getEngine();

        return BouncyCastleUtils.decryptUsingRC4(engine, ciphertext, key);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = null;
        String password = null;
        if (args.length == 1) {
            fileName = args[0];
            password = null;
        } else if (args.length == 2) {
            fileName = args[0];
            password = args[1];
        } else {
            Class<HeaderPagePasswordChecker> clz = HeaderPagePasswordChecker.class;
            System.out.println("Usage: java " + clz.getName() + " samples.mny [password]");
            System.exit(1);
        }

        try {
            File file = new File(fileName);
            log.info("file=" + file);
            HeaderPage headerPage = new HeaderPage(file);

            if (checkPassword(headerPage, password)) {
                log.info("OK password=" + password);
            } else {
                log.info("NOT OK password=" + password);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }
}
