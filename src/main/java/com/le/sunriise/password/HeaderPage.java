package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.JetFormat;

public class HeaderPage {
    private static final Logger log = Logger.getLogger(HeaderPage.class);

    private static final String CHARSET_PROPERTY_PREFIX = "com.healthmarketscience.jackcess.charset.";
    private static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.LITTLE_ENDIAN;

    private JetFormat jetFormat;
    private Charset charset;
    private ByteBuffer buffer;

    public static HeaderPage newInstance(File dbFile) throws IOException {
        HeaderPage headerPage = new HeaderPage();
        RandomAccessFile rFile = null;
        FileChannel fileChannel = null;
        try {
            rFile = new RandomAccessFile(dbFile, "r");
            rFile.seek(0L);
            fileChannel = rFile.getChannel();

            JetFormat jetFormat = JetFormat.getFormat(fileChannel);
            headerPage.setJetFormat(jetFormat);

            Charset charset = getDefaultCharset(jetFormat);
            headerPage.setCharset(charset);

            ByteBuffer buffer = readHeaderPage(jetFormat, fileChannel);
            headerPage.setBuffer(buffer);
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
        return headerPage;
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
        for (int idx = 0; idx < headerMask.length; ++idx) {
            int pos = idx + jetFormat.OFFSET_MASKED_HEADER;
            byte b = (byte) (buffer.get(pos) ^ headerMask[idx]);
            buffer.put(pos, b);
        }
    }

    public JetFormat getJetFormat() {
        return jetFormat;
    }

    public void setJetFormat(JetFormat jetFormat) {
        this.jetFormat = jetFormat;
    }

    public Charset getCharset() {
        return charset;
    }

    public void setCharset(Charset charset) {
        this.charset = charset;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public void setBuffer(ByteBuffer buffer) {
        this.buffer = buffer;
    }
}