package com.le.sunriise;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.JetFormat;

public class Password {
    private static final Logger log = Logger.getLogger(Password.class);
    private FileChannel fileChannel;

    public Password(File file, boolean readOnly) throws FileNotFoundException {
        String mode = (readOnly ? "r" : "rw");
        this.fileChannel = new RandomAccessFile(file, mode).getChannel();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File file = null;
        String newPassword = null;

        if (args.length == 1) {
            file = new File(args[0]);
        } else if (args.length == 2) {
            file = new File(args[0]);
            newPassword = args[1];
        } else {
            Class<Password> clz = Password.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny [newPassword]");
            System.exit(1);
        }
        log.info("> file=" + file.getAbsolutePath());
        Password password = null;
        try {
            boolean readOnly = true;
            if (newPassword != null) {
                readOnly = false;
            }
            password = new Password(file, readOnly);
            if (readOnly) {
                String pw = password.getPassword();
                log.info("password=" + pw);
            } else {
                password.setPassword(newPassword);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (password != null) {
                password.close();
            }
        }
    }

    private void setPassword(String newPassword) {
        // TODO Auto-generated method stub

    }

    private String getPassword() throws IOException {
        String password = null;

        ByteBuffer buffer = getPageZero();

        byte[] pwdBytes = getPasswordByteArray(buffer);

        // de-mask password using extra password mask if necessary (the extra
        // password mask is generated from the database creation date stored in
        // the header)
        byte[] pwdMask = getPasswordMask(buffer, getFormat());
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

        String pwd = Column.decodeUncompressedText(pwdBytes, getCharset());

        // remove any trailing null chars
        int idx = pwd.indexOf('\0');
        if (idx >= 0) {
            pwd = pwd.substring(0, idx);
        }

        password = pwd;
        return password;
    }

    private Charset getCharset() {
        return Database.getDefaultCharset(getFormat());
    }

    private byte[] getPasswordByteArray(ByteBuffer buffer) {
        byte[] pwdBytes = new byte[getFormat().SIZE_PASSWORD];
        buffer.position(getFormat().OFFSET_PASSWORD);
        buffer.get(pwdBytes);
        return pwdBytes;
    }

    private ByteBuffer getPageZero() throws IOException {
        ByteBuffer buffer = null;
        int pageNumber = 0;

        ByteOrder order = ByteOrder.LITTLE_ENDIAN;
        int size = getFormat().PAGE_SIZE;
        buffer = ByteBuffer.allocate(size).order(order);

        buffer.clear();
        int bytesRead = fileChannel.read(buffer, (long) pageNumber * (long) size);
        if (bytesRead != size) {
            throw new IOException("Failed to read " + size + " bytes");
        }
        buffer.flip();

        applyHeaderMask(buffer);

        return buffer;
    }

    /**
     * Applies the XOR mask to the database header in the given buffer.
     */
    private void applyHeaderMask(ByteBuffer buffer) {
        // de/re-obfuscate the header
        byte[] headerMask = getFormat().HEADER_MASK;
        for (int idx = 0; idx < headerMask.length; ++idx) {
            int pos = idx + getFormat().OFFSET_MASKED_HEADER;
            byte b = (byte) (buffer.get(pos) ^ headerMask[idx]);
            buffer.put(pos, b);
        }
    }

    private JetFormat getFormat() {
        return JetFormat.VERSION_MSISAM;
    }

    private void close() {
        if (this.fileChannel != null) {
            try {
                this.fileChannel.close();
            } catch (IOException e) {
                log.warn(e);
            } finally {
                this.fileChannel = null;
            }
        }
    }

    /**
     * Returns the password mask retrieved from the given header page and
     * format, or {@code null} if this format does not use a password mask.
     */
    private static byte[] getPasswordMask(ByteBuffer buffer, JetFormat format) {
        // get extra password mask if necessary (the extra password mask is
        // generated from the database creation date stored in the header)
        int pwdMaskPos = format.OFFSET_HEADER_DATE;
        if (pwdMaskPos < 0) {
            return null;
        }

        buffer.position(pwdMaskPos);
        double dateVal = Double.longBitsToDouble(buffer.getLong());

        byte[] pwdMask = new byte[4];
        ByteOrder defaultByteOrder = ByteOrder.LITTLE_ENDIAN;
        ByteBuffer.wrap(pwdMask).order(defaultByteOrder).putInt((int) dateVal);

        return pwdMask;
    }
}
