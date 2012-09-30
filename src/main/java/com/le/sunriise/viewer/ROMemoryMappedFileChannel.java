package com.le.sunriise.viewer;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class ROMemoryMappedFileChannel extends JackcessFileChannelAdapter {
    private static final Logger log = Logger.getLogger(ROMemoryMappedFileChannel.class);

    private File dbFile;
    private RandomAccessFile randomAccessFile;
    private FileChannel channel;
    private MappedByteBuffer mappedByteBuffer;

    public ROMemoryMappedFileChannel(File dbFile) throws IOException {
        super();
        this.dbFile = dbFile;
        this.randomAccessFile = new RandomAccessFile(dbFile, "r");
        this.channel = this.randomAccessFile.getChannel();
        this.mappedByteBuffer = this.channel.map(FileChannel.MapMode.READ_ONLY, 0, this.randomAccessFile.length());
    }

    @Override
    public long size() throws IOException {
        return mappedByteBuffer.limit();
    }

    @Override
    public void force(boolean metaData) throws IOException {
        if (mappedByteBuffer != null) {
            mappedByteBuffer.force();
        }
    }

    @Override
    public int read(ByteBuffer dst, long position) throws IOException {
        int n = 0;

        if (log.isDebugEnabled()) {
            log.debug("> read, position=" + position);
        }
        if (position < 0) {
            n = 0;
            if (log.isDebugEnabled()) {
                log.debug("< read, n=" + n);
            }
            return n;
        }

        if (position >= mappedByteBuffer.limit()) {
            n = -1;
            if (log.isDebugEnabled()) {
                log.debug("< read, n=" + n);
            }
            return n;
        }

        mappedByteBuffer.mark();
        try {
            mappedByteBuffer.position((int) position);
            n = transferAsMuchAsPossible(mappedByteBuffer, dst);
        } finally {
            mappedByteBuffer.reset();
        }
        if (log.isDebugEnabled()) {
            log.debug("< read, n=" + n);
        }
        return n;
    }

    @Override
    protected void implCloseChannel() throws IOException {
        log.info("> MemoryMappedFileChannel close");

        if (mappedByteBuffer != null) {
            mappedByteBuffer.force();
            mappedByteBuffer = null;
        }
        if (channel != null) {
            channel.close();
            channel = null;
        }
        if (randomAccessFile != null) {
            randomAccessFile.close();
            randomAccessFile = null;
        }
        dbFile = null;
    }

    private static int transferAsMuchAsPossible(ByteBuffer src, ByteBuffer dest) {
        int srcRemaining = src.remaining();
        int destRemaining = dest.remaining();
        if (log.isDebugEnabled()) {
            log.debug("> transferAsMuchAsPossible, srcRemaining=" + srcRemaining + ", destRemaining=" + destRemaining);
        }

        int nTransfer = Math.min(srcRemaining, destRemaining);
        if (log.isDebugEnabled()) {
            log.debug("> transferAsMuchAsPossible, nTransfer=" + nTransfer);
        }
        if (nTransfer > 0) {
            for (int i = 0; i < nTransfer; i++) {
                dest.put(src.get());
            }
        }
        return nTransfer;
    }

    public File getDbFile() {
        return dbFile;
    }

    public void setDbFile(File dbFile) {
        this.dbFile = dbFile;
    }

}
