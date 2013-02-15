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
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.poi.util.HexDump;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.JetFormat;
import com.le.sunriise.header.HeaderPage;

public abstract class AbstractHeaderPagePasswordChecker {
    private static final Logger log = Logger.getLogger(AbstractHeaderPagePasswordChecker.class);

    private static final int PASSWORD_LENGTH = 0x28;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;

    protected final HeaderPage headerPage;

    private byte[] encodingKey;

    private byte[] testKey;

    private byte[] testBytes;

    private byte[] decrypted4BytesCheck;

    public static boolean checkPassword(HeaderPage headerPage, String testPassword) {
        boolean matched = false;
        AbstractHeaderPagePasswordChecker checker = null;
        boolean useBouncycastle = true;
        try {
            if (useBouncycastle) {
                checker = new HeaderPagePasswordChecker(headerPage);
            } else {
                checker = new JDKHeaderPagePasswordChecker(headerPage);
            }
            if (log.isDebugEnabled()) {
                JetFormat jetFormat = checker.getJetFormat();
                log.debug("format=" + jetFormat);
                log.debug("databasePassword=" + checker.getDatabasePassword());
            }

            try {
                matched = false;
                matched = checker.check(testPassword);
                if (matched) {
                    matched = PasswordUtils.doubleCheck(headerPage, testPassword);
                }
            } catch (IllegalStateException e) {
                if (log.isDebugEnabled()) {
                    log.warn("Not a valid testPassword=" + testPassword);
                }
                matched = false;
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
        }

        if (matched) {
            if (checker != null) {
                byte[] bytes = null;
                bytes = checker.getEncodingKey();
                if (bytes == null) {
                    log.info("encodingKey=" + bytes);
                } else {
                    log.info("encodingKey.length=" + bytes.length + " (" + (bytes.length * 8) + ")");
                    log.info("    encodingKey=" + toHexString(bytes));
                }
                bytes = checker.getTestKey();
                if (bytes == null) {
                    log.info("testKey=" + bytes);
                } else {
                    log.info("testKey.length=" + bytes.length + " (" + (bytes.length * 8) + ")");
                    log.info("    testKey=" + toHexString(bytes));
                }
                bytes = checker.getTestBytes();
                if (bytes == null) {
                    log.info("testBytes=" + bytes);
                } else {
                    log.info("testBytes.length=" + bytes.length + " (" + (bytes.length * 8) + ")");
                    log.info("    testBytes=" + toHexString(bytes));
                }
            }
        }
        return matched;
    }

    private String getDatabasePassword() throws IOException {
        return getHeaderPage().getEmbeddedDatabasePassword();
    }

    private JetFormat getJetFormat() {
        return getHeaderPage().getJetFormat();
    }

    public AbstractHeaderPagePasswordChecker(HeaderPage headerPage) throws IOException {
        super();
        this.headerPage = headerPage;
    }

    public boolean check(String password) throws IOException {
        return check(password, getHeaderPage().getCharset());
    }

    /**
     * 
     * @param password
     * @param charset
     * @return
     * @throws IOException
     */
    private boolean check(String password, Charset charset) throws IOException {
        boolean result = false;
        // Notes: important fact: headerPage.isNewEncryption()
        if (headerPage.isNewEncryption()) {
            result = checkNewEncryption(headerPage, password, charset);
        } else {
            checkOldEncryption(headerPage, password, charset);
        }
        return result;
    }

    private void checkOldEncryption(HeaderPage headerPage, String password, Charset charset) throws IOException {
        throw new IOException("Old MSISAM dbs using jet-style encryption. " + "No password to check. "
                + "The embedded password is " + headerPage.getEmbeddedDatabasePassword());
    }

    private boolean checkNewEncryption(HeaderPage headerPage, String password, Charset charset) {
        // input password is first hash to get a digest (either sha1 or md5)
        byte[] passwordDigest = createPasswordDigest(password, headerPage.isUseSha1(), charset);
        if (log.isDebugEnabled()) {
            log.debug("passwordDigest=" + HexDump.toHex(passwordDigest));
        }
        // then a salt is append to the digest. This is is now known as the
        // testKey
        // Notes: important fact headerPage.getSalt()
        testKey = concat(passwordDigest, headerPage.getSalt());
        // Notes: important fact headerPage.getBaseSalt()
        testBytes = headerPage.getBaseSalt();

        // an embedded encrypted 4 bytes is retrieved from the db
        // (encrypted4BytesCheck)
        // decrypted4BytesCheck = f(encrypted4BytesCheck, testKey)
        // assert decrypted4BytesCheck == testBytes
        boolean rv = verifyPassword(headerPage, testKey, testBytes);

        // create final key
        encodingKey = concat(passwordDigest, testBytes);

        return rv;
    }

    private boolean verifyPassword(HeaderPage headerPage, byte[] testKey, byte[] testBytes) {
        // Notes: important fact headerPage.getBaseSalt()
        byte[] encrypted4BytesCheck = headerPage.getEncrypted4BytesCheck();
        if (isBlankKey(encrypted4BytesCheck)) {
            // no password?
            log.warn("Found blank encrypted4BytesCheck=" + toHexString(encrypted4BytesCheck));
            return true;
        }

        return verifyPassword(encrypted4BytesCheck, testKey, testBytes);
    }

    private boolean verifyPassword(byte[] encrypted4BytesCheck, byte[] testKey, byte[] testBytes) {
        this.decrypted4BytesCheck = decryptUsingRC4(encrypted4BytesCheck, testKey);

        if (!Arrays.equals(decrypted4BytesCheck, testBytes)) {
            // throw new IllegalStateException("Incorrect password provided");
            return false;
        } else {
            if (log.isDebugEnabled()) {
                log.debug("encrypted4BytesCheck=" + toHexString(encrypted4BytesCheck));
                log.debug("testKey=" + toHexString(testKey));
                log.debug("decrypted4BytesCheck=" + toHexString(decrypted4BytesCheck));
                log.debug("testBytes=" + toHexString(testBytes));
            }
            return true;
        }
    }

    /**
     * For a given ciphertext and key, decrypt using RC4.
     * 
     * @param ciphertext
     * @param key
     * @return a String plaintext
     */
    protected abstract byte[] decryptUsingRC4(byte[] ciphertext, byte[] key);

    public static String toHexString(byte[] bytes) {
        return HexDump.toHex(bytes);
    }

    private byte[] createPasswordDigest(String password, boolean useSha1, Charset charset) {
        boolean toUpperCase = true;
        byte[] passwordBytes = toPasswordBytes(password, charset, toUpperCase);
        byte[] passwordDigestBytes = createDigestBytes(passwordBytes, useSha1);

        if (log.isDebugEnabled()) {
            log.debug("PASSWORD_DIGEST_LENGTH=" + PASSWORD_DIGEST_LENGTH + ", " + (PASSWORD_DIGEST_LENGTH * 8));
        }
        // Truncate to 128 bit to match Max key length as per MSDN
        if (passwordDigestBytes.length != PASSWORD_DIGEST_LENGTH) {
            passwordDigestBytes = ByteUtil.copyOf(passwordDigestBytes, PASSWORD_DIGEST_LENGTH);
        }

        return passwordDigestBytes;
    }

    protected abstract byte[] createDigestBytes(byte[] bytes, boolean useSha1);

    private static byte[] toPasswordBytes(String password, Charset charset, boolean toUpperCase) {
        byte[] passwordBytes = new byte[PASSWORD_LENGTH];

        if (password != null) {
            String str = password;
            if (toUpperCase) {
                str = password.toUpperCase();
            }
            ByteBuffer buffer = encodeUncompressedText(str, charset);
            buffer.get(passwordBytes, 0, Math.min(passwordBytes.length, buffer.remaining()));
        }
        return passwordBytes;
    }

    private static byte[] concat(byte[] b1, byte[] b2) {
        byte[] out = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, out, 0, b1.length);
        System.arraycopy(b2, 0, out, b1.length, b2.length);
        return out;
    }

    /**
     * @return {@code true} if the given bytes are all 0, {@code false}
     *         otherwise
     */
    public static boolean isBlankKey(byte[] key) {
        for (byte byteVal : key) {
            if (byteVal != 0) {
                return false;
            }
        }
        return true;
    }

    private static ByteBuffer encodeUncompressedText(CharSequence text, Charset charset) {
        CharBuffer cb = ((text instanceof CharBuffer) ? (CharBuffer) text : CharBuffer.wrap(text));
        return charset.encode(cb);
    }

    public HeaderPage getHeaderPage() {
        return headerPage;
    }

    public byte[] getEncodingKey() {
        return encodingKey;
    }

    public byte[] getTestKey() {
        return testKey;
    }

    public byte[] getTestBytes() {
        return testBytes;
    }

    public byte[] getDecrypted4BytesCheck() {
        return decrypted4BytesCheck;
    }

    public void setDecrypted4BytesCheck(byte[] decrypted4BytesCheck) {
        this.decrypted4BytesCheck = decrypted4BytesCheck;
    }

    public static void printChecker(AbstractHeaderPagePasswordChecker checker) {
        System.out.println("");
        System.out.println("testKey=" + HeaderPage.toHexString(checker.getTestKey()));
        System.out.println("testBytes=" + HeaderPage.toHexString(checker.getTestBytes()));

        System.out.println("");
        System.out.println("decrypted4BytesCheck=" + HeaderPage.toHexString(checker.getDecrypted4BytesCheck()));
        
        System.out.println("");
        System.out.println("encodingKey=" + HeaderPage.toHexString(checker.getEncodingKey()));
        
        System.out.println("");
    }

}
