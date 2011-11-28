package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.apache.log4j.Logger;
import org.apache.poi.util.HexDump;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.JetFormat;

public class HeaderPage {
    private static final Logger log = Logger.getLogger(HeaderPage.class);

    private static final String CHARSET_PROPERTY_PREFIX = "com.healthmarketscience.jackcess.charset.";
    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private static final int ENCRYPTION_FLAGS_OFFSET = 0x298;
    private static final int NEW_ENCRYPTION = 0x6;
    private static final int USE_SHA1 = 0x20;

    private static final int SALT_OFFSET = 0x72;
    private static final int SALT_LENGTH = 0x4;
    private static final int CRYPT_CHECK_START = 0x2e9;

    private JetFormat jetFormat;
    private Charset charset;
    private ByteBuffer buffer;

    private boolean newEncryption;

    private String embeddedDatabasePassword = null;

    private boolean useSha1;

    private byte[] salt;

    private byte[] baseSalt;

    private byte[] passwordTestBytes;

    public HeaderPage() {
        super();
    }

    public HeaderPage(File dbFile) throws IOException {
        super();
        parse(dbFile);
    }

    private void parse(File dbFile) throws IOException {
        RandomAccessFile rFile = null;
        FileChannel fileChannel = null;
        try {
            rFile = new RandomAccessFile(dbFile, "r");
            rFile.seek(0L);
            fileChannel = rFile.getChannel();

            jetFormat = JetFormat.getFormat(fileChannel);

            charset = getDefaultCharset(jetFormat);

            buffer = readHeaderPage(jetFormat, fileChannel);

            if ((buffer.get(ENCRYPTION_FLAGS_OFFSET) & NEW_ENCRYPTION) != 0) {
                newEncryption = true;
            } else {
                newEncryption = false;
            }

            embeddedDatabasePassword = readEmbeddedDatabasePassword();

            if (newEncryption) {
                useSha1 = (buffer.get(ENCRYPTION_FLAGS_OFFSET) & USE_SHA1) != 0;

                salt = new byte[8];
                buffer.position(SALT_OFFSET);
                buffer.get(salt);

                baseSalt = Arrays.copyOf(salt, SALT_LENGTH);

                passwordTestBytes = readPasswordTestBytes(buffer);
            }
        } finally {
            if (fileChannel != null) {
                try {
                    fileChannel.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    fileChannel = null;
                }
            }

            if (rFile != null) {
                try {
                    rFile.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    rFile = null;
                }
            }
        }
    }

    private static Charset getDefaultCharset(JetFormat format) {
        String csProp = System.getProperty(CHARSET_PROPERTY_PREFIX + format);
        if (log.isDebugEnabled()) {
            log.debug("csProp=" + csProp);
        }
        if (csProp != null) {
            csProp = csProp.trim();
            if (csProp.length() > 0) {
                return Charset.forName(csProp);
            }
        }

        // use format default
        Charset cs = format.CHARSET;
        if (log.isDebugEnabled()) {
            log.debug("format.CHARSET=" + cs);
        }
        return cs;
    }

    private static ByteBuffer createBuffer(int size) {
        return createBuffer(size, DEFAULT_BYTE_ORDER);
    }

    private static ByteBuffer createBuffer(int size, ByteOrder order) {
        ByteBuffer buffer = ByteBuffer.allocate(size);
        buffer.order(order);
        return buffer;
    }

    private static ByteBuffer readHeaderPage(JetFormat jetFormat, FileChannel fileChannel) throws IOException {
        ByteBuffer buffer = createBuffer(jetFormat.PAGE_SIZE);
        readPage(buffer, jetFormat, fileChannel);
        return buffer;
    }

    private static void readPage(ByteBuffer buffer, JetFormat jetFormat, FileChannel fileChannel) throws IOException {
        int pageNumber = 0;
        long pageSize = jetFormat.PAGE_SIZE;
        if (log.isDebugEnabled()) {
            log.debug("readPage, pageNumber=" + pageNumber + ", pageSize=" + pageSize);
        }

        if (log.isDebugEnabled()) {
            log.debug("Reading in page " + Integer.toHexString(pageNumber));
        }
        buffer.clear();
        int bytesRead = fileChannel.read(buffer, pageNumber * pageSize);
        buffer.flip();
        if (bytesRead != jetFormat.PAGE_SIZE) {
            throw new IOException("Failed attempting to read " + jetFormat.PAGE_SIZE + " bytes from page " + pageNumber + ", only read " + bytesRead);
        }

        if (pageNumber == 0) {
            // de-mask header (note, page 0 never has additional encoding)
            applyHeaderMask(buffer, jetFormat);
        } else {
            throw new IOException("Cannot read non-header page, pageNumber=" + pageNumber);
        }
    }

    private static void applyHeaderMask(ByteBuffer buffer, JetFormat jetFormat) {
        // de/re-obfuscate the header
        byte[] headerMask = jetFormat.HEADER_MASK;
        for (int index = 0; index < headerMask.length; ++index) {
            int pos = index + jetFormat.OFFSET_MASKED_HEADER;
            byte b = (byte) (buffer.get(pos) ^ headerMask[index]);
            buffer.put(pos, b);
        }
    }

    public JetFormat getJetFormat() {
        return jetFormat;
    }

    public Charset getCharset() {
        return charset;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public boolean isNewEncryption() {
        return newEncryption;
    }

    private String readEmbeddedDatabasePassword() throws IOException {
        JetFormat jetFormat = getJetFormat();
        ByteBuffer buffer = getBuffer();
        return readEmbeddedDatabasePassword(buffer, jetFormat);
    }

    private String readEmbeddedDatabasePassword(ByteBuffer buffer, JetFormat jetFormat) throws IOException {
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

        if (log.isDebugEnabled()) {
            log.debug("postMask pwdBytes=" + pwdBytes.length);
        }

        Charset charset = getCharset();
        if (log.isDebugEnabled()) {
            log.info("charset=" + charset);
        }
        String password = Column.decodeUncompressedText(pwdBytes, charset);

        // remove any trailing null chars
        int idx = password.indexOf('\0');
        if (idx >= 0) {
            password = password.substring(0, idx);
        }

        return password;
    }

    private static byte[] getPasswordMask(ByteBuffer buffer, JetFormat format) {
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

    public String getEmbeddedDatabasePassword() {
        return embeddedDatabasePassword;
    }

    public boolean isUseSha1() {
        return useSha1;
    }

    public byte[] getSalt() {
        return salt;
    }

    public byte[] getBaseSalt() {
        return baseSalt;
    }

    private static byte[] readPasswordTestBytes(ByteBuffer buffer) {
        byte[] encrypted4BytesCheck = new byte[4];

        int cryptCheckOffset = ByteUtil.getUnsignedByte(buffer, SALT_OFFSET);
        buffer.position(CRYPT_CHECK_START + cryptCheckOffset);
        buffer.get(encrypted4BytesCheck);

        return encrypted4BytesCheck;
    }

    public byte[] getPasswordTestBytes() {
        return passwordTestBytes;
    }

    public static String toHexString(byte[] bytes) {
        if (bytes == null) {
            return null;
        }
        return HexDump.toHex(bytes);
    }
}