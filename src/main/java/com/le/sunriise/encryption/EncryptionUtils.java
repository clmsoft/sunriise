package com.le.sunriise.encryption;

import java.nio.ByteBuffer;
import java.nio.charset.Charset;

import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.JetCryptCodecHandler;
import com.healthmarketscience.jackcess.MSISAMCryptCodecHandler;

public class EncryptionUtils {
    private static final int NEW_ENCRYPTION = 0x6;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;
    private static final int USE_SHA1 = 0x20;
    private static final int PASSWORD_LENGTH = 0x28;
    private static final int SALT_OFFSET = 0x72;
    private static final int CRYPT_CHECK_START = 0x2e9;
    private static final int ENCRYPTION_FLAGS_OFFSET = 0x298;

    public static byte[] createPasswordDigest(ByteBuffer buffer, String password, Charset charset) {
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

    public static Digest getDigest(ByteBuffer buffer) {
        Digest digest = (((buffer.get(ENCRYPTION_FLAGS_OFFSET) & USE_SHA1) != 0) ? new SHA1Digest() : new MD5Digest());
        return digest;
    }

    public static String getCodecHandlerName(ByteBuffer buffer) {
        if ((buffer.get(ENCRYPTION_FLAGS_OFFSET) & NEW_ENCRYPTION) != 0) {
            return MSISAMCryptCodecHandler.class.getName();
        }

        return JetCryptCodecHandler.class.getName();
    }

    public static byte[] concat(byte[] b1, byte[] b2) {
        byte[] out = new byte[b1.length + b2.length];
        System.arraycopy(b1, 0, out, 0, b1.length);
        System.arraycopy(b2, 0, out, b1.length, b2.length);
        return out;
    }

    public static byte[] getPasswordTestBytes(ByteBuffer buffer) {
        byte[] encrypted4BytesCheck = new byte[4];

        int cryptCheckOffset = ByteUtil.getUnsignedByte(buffer, SALT_OFFSET);
        buffer.position(CRYPT_CHECK_START + cryptCheckOffset);
        buffer.get(encrypted4BytesCheck);

        return encrypted4BytesCheck;
    }

    public static byte[] getDecrypted4BytesCheck(byte[] encrypted4BytesCheck, byte[] testEncodingKey) {
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

    public static byte[] getSalt(ByteBuffer buffer) {
        byte[] salt = new byte[8];
        buffer.position(SALT_OFFSET);
        buffer.get(salt);
        return salt;
    }
}
