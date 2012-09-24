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
package com.le.sunriise.encryption;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.Arrays;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.JetCryptCodecHandler;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.JetFormat.CodecType;
import com.healthmarketscience.jackcess.MSISAMCryptCodecHandler;
import com.healthmarketscience.jackcess.PageChannel;

public class EncryptionUtils {
    private static final int NEW_ENCRYPTION = 0x6;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;
    private static final int USE_SHA1 = 0x20;
    private static final int PASSWORD_LENGTH = 0x28;
    private static final int SALT_OFFSET = 0x72;
    private static final int CRYPT_CHECK_START = 0x2e9;
    private static final int ENCRYPTION_FLAGS_OFFSET = 0x298;

    public static final String CHARSET_PROPERTY_PREFIX =
        "com.le.sunriise.charset.";
    
    private static byte[] createPasswordDigest(ByteBuffer buffer, String password, Charset charset) {
        Digest digest = (((buffer.get(ENCRYPTION_FLAGS_OFFSET) & USE_SHA1) != 0) ? new SHA1Digest() : new MD5Digest());

        byte[] passwordBytes = new byte[PASSWORD_LENGTH];

        if (password != null) {
            ByteBuffer bb = Column.encodeUncompressedText(password.toUpperCase(), charset);
            bb.get(passwordBytes, 0, Math.min(passwordBytes.length, bb.remaining()));
        }

        digest.update(passwordBytes, 0, passwordBytes.length);

        // Get digest value
        byte[] digestBytes = new byte[digest.getDigestSize()];
        digest.doFinal(digestBytes, 0);

        // Truncate to 128 bit to match Max key length as per MSDN
        if (digestBytes.length != PASSWORD_DIGEST_LENGTH) {
            digestBytes = ByteUtil.copyOf(digestBytes, PASSWORD_DIGEST_LENGTH);
        }

        return digestBytes;
    }

    private static Digest getDigest(ByteBuffer buffer) {
        Digest digest = (((buffer.get(ENCRYPTION_FLAGS_OFFSET) & USE_SHA1) != 0) ? new SHA1Digest() : new MD5Digest());
        return digest;
    }

    private static String getCodecHandlerName(ByteBuffer buffer) {
        if ((buffer.get(ENCRYPTION_FLAGS_OFFSET) & NEW_ENCRYPTION) != 0) {
            return MSISAMCryptCodecHandler.class.getName();
        }

        return JetCryptCodecHandler.class.getName();
    }

    private static byte[] concat(byte[] b1, byte[] b2) {
        byte[] out = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, out, 0, b1.length);
        System.arraycopy(b2, 0, out, b1.length, b2.length);
        return out;
    }

    private static byte[] getPasswordTestBytes(ByteBuffer buffer) {
        byte[] encrypted4BytesCheck = new byte[4];

        int cryptCheckOffset = ByteUtil.getUnsignedByte(buffer, SALT_OFFSET);
        buffer.position(CRYPT_CHECK_START + cryptCheckOffset);
        buffer.get(encrypted4BytesCheck);

        return encrypted4BytesCheck;
    }

    private static byte[] getDecrypted4BytesCheck(byte[] encrypted4BytesCheck, byte[] testEncodingKey) {
        RC4Engine engine = new RC4Engine();
        // decrypt
        engine.init(false, new KeyParameter(testEncodingKey));

        // byte[] encrypted4BytesCheck = getPasswordTestBytes(buffer);
        byte[] decrypted4BytesCheck = new byte[4];
        engine.processBytes(encrypted4BytesCheck, 0, encrypted4BytesCheck.length, decrypted4BytesCheck, 0);

        return decrypted4BytesCheck;
        // if (!Arrays.equals(decrypted4BytesCheck, testBytes)) {
        // throw new IllegalStateException("Incorrect password provided");
        // }
    }

    private static byte[] getSalt(ByteBuffer buffer) {
        byte[] salt = new byte[8];
        buffer.position(SALT_OFFSET);
        buffer.get(salt);
        return salt;
    }

    /**
     * Package visible only to support unit tests via
     * DatabaseTest.openChannel().
     * 
     * @param mdbFile
     *            file to open
     * @param readOnly
     *            true if read-only
     * @return a FileChannel on the given file.
     * @exception FileNotFoundException
     *                if the mode is <tt>"r"</tt> but the given file object does
     *                not denote an existing regular file, or if the mode begins
     *                with <tt>"rw"</tt> but the given file object does not
     *                denote an existing, writable regular file and a new
     *                regular file of that name cannot be created, or if some
     *                other error occurs while opening or creating the file
     */
    private static FileChannel openChannel(final File mdbFile, final boolean readOnly) throws FileNotFoundException {
        final String mode = (readOnly ? "r" : "rw");
        return new RandomAccessFile(mdbFile, mode).getChannel();
    }

    public static void parseHeader(final File mdbFile, final String password) throws IOException {
        boolean readOnly = true;
        FileChannel channel = null;
        try {
            channel = openChannel(mdbFile, readOnly);
            PageChannel pageChannel = null;
            try {
                boolean closeChannel = true;
                JetFormat format = JetFormat.getFormat(channel);
                boolean autoSync = true;

                pageChannel = new PageChannel(channel, closeChannel, format, autoSync);

                Charset charset = getDefaultCharset(format);

                ByteBuffer buffer = pageChannel.createPageBuffer();
                pageChannel.readPage(buffer, 0);

                StringBuilder sb = new StringBuilder();
                if (format.CODEC_TYPE == CodecType.MSISAM) {
                    EncryptionUtils.appendMSISAMInfo(buffer, password, charset, sb);
                }
                System.out.println(sb.toString());
            } finally {
                if (pageChannel != null) {
                    pageChannel.close();
                    pageChannel = null;
                }
            }
            
        } finally {
            if (channel != null) {
                try {
                    channel.close();
                } finally {
                    channel = null;
                }
            }
        }
    }

    public static void appendMSISAMInfo(ByteBuffer buffer, String password, Charset charset, StringBuilder sb) {
        sb.append("codecHandlerName=" + getCodecHandlerName(buffer));
        sb.append("\n");

        Digest digest = getDigest(buffer);
        sb.append("digest=" + digest.getAlgorithmName());
        sb.append("\n");
        sb.append("\n");

        byte[] salt = getSalt(buffer);
        sb.append("salt=" + ByteUtil.toHexString(salt));
        sb.append("\n");

        // String password = openedDb.getPassword();
        // Charset charset = openedDb.getDb().getCharset();
        byte[] pwdDigest = createPasswordDigest(buffer, password, charset);
        sb.append("pwdDigest=" + ByteUtil.toHexString(pwdDigest));
        sb.append("\n");

        final int SALT_LENGTH = 0x4;
        byte[] baseSalt = Arrays.copyOf(salt, SALT_LENGTH);
        byte[] testEncodingKey = concat(pwdDigest, salt);
        sb.append("testEncodingKey=" + ByteUtil.toHexString(testEncodingKey));
        sb.append("\n");

        byte[] encrypted4BytesCheck = getPasswordTestBytes(buffer);
        sb.append("encrypted4BytesCheck=" + ByteUtil.toHexString(encrypted4BytesCheck));
        sb.append("\n");

        byte[] decrypted4BytesCheck = getDecrypted4BytesCheck(encrypted4BytesCheck, testEncodingKey);
        sb.append("decrypted4BytesCheck=" + ByteUtil.toHexString(decrypted4BytesCheck));
        sb.append(" / ");

        byte[] testBytes = baseSalt;
        sb.append("testBytes=" + ByteUtil.toHexString(testBytes));
        sb.append("\n");
    }

    /**
     * Returns the default Charset for the given JetFormat. This may or may not
     * be platform specific, depending on the format, but can be overridden
     * using a system property composed of the prefix
     * {@value #CHARSET_PROPERTY_PREFIX} followed by the JetFormat version to
     * which the charset should apply, e.g.
     * {@code "com.healthmarketscience.jackcess.charset.VERSION_3"}.
     */
    private static Charset getDefaultCharset(JetFormat format) {
        String csProp = System.getProperty(CHARSET_PROPERTY_PREFIX + format);
        if (csProp != null) {
            csProp = csProp.trim();
            if (csProp.length() > 0) {
                return Charset.forName(csProp);
            }
        }

        // use format default
        return format.CHARSET;
    }
}
