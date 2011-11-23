package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.JetFormat;

public class MinPasswordChecker {
    private static final Logger log = Logger.getLogger(MinPasswordChecker.class);

    private RC4Engine engine;

    private static final int USE_SHA1 = 0x20;

    private static final int PASSWORD_LENGTH = 0x28;

    private static final int ENCRYPTION_FLAGS_OFFSET = 0x298;
    private static final int NEW_ENCRYPTION = 0x6;

    private static final int PASSWORD_DIGEST_LENGTH = 0x10;

    private static final int CRYPT_CHECK_START = 0x2e9;

    private static final int SALT_OFFSET = 0x72;
    private static final int SALT_LENGTH = 0x4;

    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    // private static final int INVALID_PAGE_NUMBER = -1;

    // private static final String CHARSET_PROPERTY_PREFIX =
    // "com.healthmarketscience.jackcess.charset.";

    private boolean newEncryption;

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
            HeaderPage headerPage = HeaderPage.newInstance(file);

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
        JetFormat jetFormat = getHeaderPage().getJetFormat();
        ByteBuffer buffer = getHeaderPage().getBuffer();
        return getDatabasePassword(buffer, jetFormat);
    }

    private JetFormat getJetFormat() {
        return getHeaderPage().getJetFormat();
    }

    public MinPasswordChecker(HeaderPage headerPage) throws IOException {
        super();
        this.headerPage = headerPage;

        ByteBuffer buffer = this.headerPage.getBuffer();

        if ((buffer.get(ENCRYPTION_FLAGS_OFFSET) & NEW_ENCRYPTION) != 0) {
            newEncryption = true;
            // checkNewEncryption(headerPage, password, charset);
        } else {
            newEncryption = false;
            // checkOldEncryption(buffer, password, charset);
        }
    }

    private boolean check(String password) throws IOException {
        return check(password, getHeaderPage().getCharset());
    }

    private boolean check(String password, Charset charset) throws IOException {
        boolean result = false;
        ByteBuffer buffer = headerPage.getBuffer();
        if (newEncryption) {
            result = checkNewEncryption(buffer, password, charset);
        } else {
            checkOldEncryption(buffer, password, charset);
        }
        return result;
    }

    private void checkOldEncryption(ByteBuffer buffer, String password, Charset charset) throws IOException {
        throw new IOException("Old MSISAM dbs using jet-style encryption. No password to check.");
    }

    private boolean checkNewEncryption(ByteBuffer buffer, String password, Charset charset) {
        byte[] pwdDigest = createPasswordDigest(buffer, password, charset);

        byte[] salt = new byte[8];
        buffer.position(SALT_OFFSET);
        buffer.get(salt);

        byte[] baseSalt = Arrays.copyOf(salt, SALT_LENGTH);

        byte[] testEncodingKey = concat(pwdDigest, salt);
        byte[] testBytes = baseSalt;
        return verifyPassword(buffer, testEncodingKey, testBytes);
    }

    private boolean verifyPassword(ByteBuffer buffer, byte[] testEncodingKey, byte[] testBytes) {
        RC4Engine engine = getEngine();
        engine.init(false, new KeyParameter(testEncodingKey));

        byte[] encrypted4BytesCheck = getPasswordTestBytes(buffer);
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

    /**
     * Reads and returns the header page (page 0) from the given pageChannel.
     */
    // protected ByteBuffer readHeaderPage() throws IOException {
    // ByteBuffer buffer = createPageBuffer();
    // readPage(buffer, 0);
    // return buffer;
    // }

    private static byte[] createPasswordDigest(ByteBuffer buffer, String password, Charset charset) {
        boolean useSha1 = (buffer.get(ENCRYPTION_FLAGS_OFFSET) & USE_SHA1) != 0;
        Digest digest = (useSha1 ? new SHA1Digest() : new MD5Digest());
        if (log.isDebugEnabled()) {
            log.debug("digest=" + digest.getAlgorithmName());
        }

        byte[] passwordBytes = new byte[PASSWORD_LENGTH];

        if (password != null) {
            ByteBuffer bb = encodeUncompressedText(password.toUpperCase(), charset);
            bb.get(passwordBytes, 0, Math.min(passwordBytes.length, bb.remaining()));
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

    private static byte[] getPasswordTestBytes(ByteBuffer buffer) {
        byte[] encrypted4BytesCheck = new byte[4];

        int cryptCheckOffset = ByteUtil.getUnsignedByte(buffer, SALT_OFFSET);
        buffer.position(CRYPT_CHECK_START + cryptCheckOffset);
        buffer.get(encrypted4BytesCheck);

        return encrypted4BytesCheck;
    }

    private static ByteBuffer encodeUncompressedText(CharSequence text, Charset charset) {
        CharBuffer cb = ((text instanceof CharBuffer) ? (CharBuffer) text : CharBuffer.wrap(text));
        return charset.encode(cb);
    }

    /**
     * @return A newly-allocated buffer that can be passed to readPage
     */
    // private ByteBuffer createPageBuffer() {
    // ByteBuffer buffer = null;
    // buffer = createBuffer(getFormat().PAGE_SIZE);
    //
    // return buffer;
    // }
    //
    // private ByteBuffer createBuffer(int size) {
    // return createBuffer(size, DEFAULT_BYTE_ORDER);
    // }
    //
    // private ByteBuffer createBuffer(int size, ByteOrder order) {
    // ByteBuffer buffer = ByteBuffer.allocate(size);
    // buffer.order(order);
    // return buffer;
    // }

    // private void readPage(ByteBuffer buffer, int pageNumber) throws
    // IOException {
    // long pageSize = (long) getFormat().PAGE_SIZE;
    // if (log.isDebugEnabled()) {
    // log.debug("readPage, pageNumber=" + pageNumber + ", pageSize=" +
    // pageSize);
    // }
    //
    // validatePageNumber(pageNumber);
    // if (log.isDebugEnabled()) {
    // log.debug("Reading in page " + Integer.toHexString(pageNumber));
    // }
    // buffer.clear();
    // int bytesRead = fileChannel.read(buffer, (long) pageNumber * pageSize);
    // buffer.flip();
    // if (bytesRead != getFormat().PAGE_SIZE) {
    // throw new IOException("Failed attempting to read " +
    // getFormat().PAGE_SIZE + " bytes from page " + pageNumber + ", only read "
    // + bytesRead);
    // }
    //
    // if (pageNumber == 0) {
    // // de-mask header (note, page 0 never has additional encoding)
    // applyHeaderMask(buffer);
    // } else {
    // this.codecHandler.decodePage(buffer, pageNumber);
    // }
    // }

    // private void validatePageNumber(int pageNumber) throws IOException {
    // int nextPageNumber = getNextPageNumber(fileChannel.size());
    // if ((pageNumber <= INVALID_PAGE_NUMBER) || (pageNumber >=
    // nextPageNumber)) {
    // throw new IllegalStateException("invalid page number " + pageNumber);
    // }
    // }

    // private int getNextPageNumber(long size) {
    // return (int) (size / getFormat().PAGE_SIZE);
    // }
    //
    // private void applyHeaderMask(ByteBuffer buffer) {
    // // de/re-obfuscate the header
    // byte[] headerMask = format.HEADER_MASK;
    // for (int idx = 0; idx < headerMask.length; ++idx) {
    // int pos = idx + format.OFFSET_MASKED_HEADER;
    // byte b = (byte) (buffer.get(pos) ^ headerMask[idx]);
    // buffer.put(pos, b);
    // }
    // }
    //
    // private static Charset getDefaultCharset(JetFormat format) {
    // String csProp = System.getProperty(CHARSET_PROPERTY_PREFIX + format);
    // if (log.isDebugEnabled()) {
    // log.debug("csProp=" + csProp);
    // }
    // if (csProp != null) {
    // csProp = csProp.trim();
    // if (csProp.length() > 0) {
    // return Charset.forName(csProp);
    // }
    // }
    //
    // // use format default
    // Charset cs = format.CHARSET;
    // if (log.isDebugEnabled()) {
    // log.debug("format.CHARSET=" + cs);
    // }
    // return cs;
    // }

    private String getDatabasePassword(ByteBuffer buffer, JetFormat jetFormat) throws IOException {
        // ByteBuffer buffer = takeSharedBuffer();
        try {
            // this.readPage(buffer, 0);

            byte[] pwdBytes = new byte[jetFormat.SIZE_PASSWORD];
            buffer.position(jetFormat.OFFSET_PASSWORD);
            buffer.get(pwdBytes);

            if (log.isDebugEnabled()) {
                log.debug("preMask pwdBytes=" + pwdBytes.length);
            }

            // de-mask password using extra password mask if necessary (the
            // extra
            // password mask is generated from the database creation date stored
            // in
            // the header)
            byte[] pwdMask = getPasswordMask(buffer, jetFormat);
            if (pwdMask != null) {
                for (int i = 0; i < pwdBytes.length; ++i) {
                    pwdBytes[i] ^= pwdMask[i % pwdMask.length];
                }
            }

            boolean hasPassword = false;
            for (int i = 0; i < pwdBytes.length; ++i) {
                if (pwdBytes[i] != 0) {
                    hasPassword = true;
                    break;
                }
            }

            if (!hasPassword) {
                return null;
            }

            log.info("postMask pwdBytes=" + pwdBytes.length);

            Charset charset = getHeaderPage().getCharset();
            log.info("charset=" + charset);
            String pwd = Column.decodeUncompressedText(pwdBytes, charset);

            // remove any trailing null chars
            int idx = pwd.indexOf('\0');
            if (idx >= 0) {
                pwd = pwd.substring(0, idx);
            }

            return pwd;
        } finally {
            // releaseSharedBuffer(buffer);
        }
    }

    static byte[] getPasswordMask(ByteBuffer buffer, JetFormat format) {
        // get extra password mask if necessary (the extra password mask is
        // generated from the database creation date stored in the header)
        int pwdMaskPos = format.OFFSET_HEADER_DATE;
        if (pwdMaskPos < 0) {
            return null;
        }

        buffer.position(pwdMaskPos);
        double dateVal = Double.longBitsToDouble(buffer.getLong());
        if (log.isDebugEnabled()) {
            log.debug("dateVal=" + dateVal);
        }

        byte[] pwdMask = new byte[4];
        ByteBuffer.wrap(pwdMask).order(DEFAULT_BYTE_ORDER).putInt((int) dateVal);

        return pwdMask;
    }

    public HeaderPage getHeaderPage() {
        return headerPage;
    }

}
