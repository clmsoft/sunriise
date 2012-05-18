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

public abstract class AbstractHeaderPageOnlyPasswordChecker {
    private static final Logger log = Logger.getLogger(AbstractHeaderPageOnlyPasswordChecker.class);

    private static final int PASSWORD_LENGTH = 0x28;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;

    protected final HeaderPage headerPage;

    public static boolean checkPassword(HeaderPage headerPage, String password) {
        boolean matched = false;
        AbstractHeaderPageOnlyPasswordChecker checker = null;
        boolean useBouncycastle = true;
        try {
            if (useBouncycastle) {
                checker = new HeaderPageOnlyPasswordChecker(headerPage);
            } else {
                checker = new JDKHeaderPageOnlyPasswordChecker(headerPage);
            }
            if (log.isDebugEnabled()) {
                JetFormat jetFormat = checker.getJetFormat();
                log.debug("format=" + jetFormat);
                log.debug("databasePassword=" + checker.getDatabasePassword());
            }

            try {
                matched = false;
                matched = checker.check(password);
            } catch (IllegalStateException e) {
                if (log.isDebugEnabled()) {
                    log.warn("Not a valid password=" + password);
                }
                matched = false;
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
        }
        return matched;
    }

    private String getDatabasePassword() throws IOException {
        return getHeaderPage().getEmbeddedDatabasePassword();
    }

    private JetFormat getJetFormat() {
        return getHeaderPage().getJetFormat();
    }

    public AbstractHeaderPageOnlyPasswordChecker(HeaderPage headerPage) throws IOException {
        super();
        this.headerPage = headerPage;
    }

    private boolean check(String password) throws IOException {
        return check(password, getHeaderPage().getCharset());
    }

    private boolean check(String password, Charset charset) throws IOException {
        boolean result = false;
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
        byte[] passwordDigest = createPasswordDigest(headerPage, password, charset);
        if (log.isDebugEnabled()) {
            log.debug("passwordDigest=" + HexDump.toHex(passwordDigest));
        }
        byte[] testKey = concat(passwordDigest, headerPage.getSalt());
        byte[] testBytes = headerPage.getBaseSalt();
        return verifyPassword(headerPage, testKey, testBytes);
    }

    private boolean verifyPassword(HeaderPage headerPage, byte[] testKey, byte[] testBytes) {
        byte[] encrypted4BytesCheck = headerPage.getEncrypted4BytesCheck();
        if (isBlankKey(encrypted4BytesCheck)) {
            // no password?
            log.warn("Found blank encrypted4BytesCheck=" + toHexString(encrypted4BytesCheck));
            return true;
        }

        return verifyPassword(encrypted4BytesCheck, testKey, testBytes);
    }

    private boolean verifyPassword(byte[] encrypted4BytesCheck, byte[] testKey, byte[] testBytes) {
        byte[] decrypted4BytesCheck = decryptUsingRC4(encrypted4BytesCheck, testKey);

        if (!Arrays.equals(decrypted4BytesCheck, testBytes)) {
            // throw new IllegalStateException("Incorrect password provided");
            return false;
        } else {
            log.info("encrypted4BytesCheck=" + toHexString(encrypted4BytesCheck));
            log.info("testKey=" + toHexString(testKey));
            log.info("decrypted4BytesCheck=" + toHexString(decrypted4BytesCheck));
            log.info("testBytes=" + toHexString(testBytes));
            return true;
        }
    }

    protected abstract byte[] decryptUsingRC4(byte[] encrypted4BytesCheck, byte[] testKey);

    private String toHexString(byte[] bytes) {
        return HexDump.toHex(bytes);
    }

    private byte[] createPasswordDigest(HeaderPage headerPage, String password, Charset charset) {
        boolean toUpperCase = true;
        byte[] passwordBytes = toPasswordBytes(password, charset, toUpperCase);

        byte[] digestBytes = createDigestBytes(headerPage, passwordBytes);

        if (log.isDebugEnabled()) {
            log.debug("PASSWORD_DIGEST_LENGTH=" + PASSWORD_DIGEST_LENGTH + ", " + (PASSWORD_DIGEST_LENGTH * 8));
        }
        // Truncate to 128 bit to match Max key length as per MSDN
        if (digestBytes.length != PASSWORD_DIGEST_LENGTH) {
            digestBytes = ByteUtil.copyOf(digestBytes, PASSWORD_DIGEST_LENGTH);
        }

        return digestBytes;
    }

    protected abstract byte[] createDigestBytes(HeaderPage headerPage, byte[] passwordBytes);

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
    private static boolean isBlankKey(byte[] key) {
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

}
