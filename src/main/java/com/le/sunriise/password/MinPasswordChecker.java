package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.poi.util.HexDump;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.JetFormat;

public class MinPasswordChecker {
    private static final Logger log = Logger.getLogger(MinPasswordChecker.class);

    private RC4Engine engine;

    private static final int PASSWORD_LENGTH = 0x28;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;

    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private final HeaderPage headerPage;

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
            Class<MinPasswordChecker> clz = MinPasswordChecker.class;
            System.out.println("Usage: java " + clz.getName() + "samples.mny [password]");
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
        }
    }

    public static boolean checkPassword(HeaderPage headerPage, String password) {
        boolean matched = false;
        MinPasswordChecker checker = null;

        try {
            checker = new MinPasswordChecker(headerPage);
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

    public MinPasswordChecker(HeaderPage headerPage) throws IOException {
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
        throw new IOException("Old MSISAM dbs using jet-style encryption. " + "No password to check. " + "The embedded password is "
                + headerPage.getEmbeddedDatabasePassword());
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
        RC4Engine engine = getEngine();
        engine.init(false, new KeyParameter(testKey));

        byte[] encrypted4BytesCheck = headerPage.getPasswordTestBytes();
        if (isBlankKey(encrypted4BytesCheck)) {
            // no password?
            return false;
        }

        byte[] decrypted4BytesCheck = new byte[4];
        engine.processBytes(encrypted4BytesCheck, 0, encrypted4BytesCheck.length, decrypted4BytesCheck, 0);

        if (!Arrays.equals(decrypted4BytesCheck, testBytes)) {
            // throw new IllegalStateException("Incorrect password provided");
            return false;
        } else {
            return true;
        }
    }

    private final RC4Engine getEngine() {
        if (engine == null) {
            engine = new RC4Engine();
        }
        return engine;
    }

    private static byte[] createPasswordDigest(HeaderPage headerPage, String password, Charset charset) {
        boolean toUpperCase = true;
        byte[] passwordBytes = toPasswordBytes(password, charset, toUpperCase);

        boolean useSha1 = headerPage.isUseSha1();
        Digest digest = (useSha1 ? new SHA1Digest() : new MD5Digest());
        if (log.isDebugEnabled()) {
            log.debug("digest=" + digest.getAlgorithmName());
        }

        digest.update(passwordBytes, 0, passwordBytes.length);

        // Get digest value
        byte[] digestBytes = new byte[digest.getDigestSize()];
        digest.doFinal(digestBytes, 0);

        if (log.isDebugEnabled()) {
            log.debug("PASSWORD_DIGEST_LENGTH=" + PASSWORD_DIGEST_LENGTH + ", " + (PASSWORD_DIGEST_LENGTH * 8));
        }
        // Truncate to 128 bit to match Max key length as per MSDN
        if (digestBytes.length != PASSWORD_DIGEST_LENGTH) {
            digestBytes = ByteUtil.copyOf(digestBytes, PASSWORD_DIGEST_LENGTH);
        }

        return digestBytes;
    }

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
