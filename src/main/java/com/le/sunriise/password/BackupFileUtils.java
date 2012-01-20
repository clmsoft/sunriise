package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;

public class BackupFileUtils {
    private static final Logger log = Logger.getLogger(BackupFileUtils.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;
        File dbFile = null;

        try {
            dbFile = new File(dbFileName);
            String fileName = dbFile.getName();
            if (fileName.endsWith(".mbf")) {
                File tempFile = File.createTempFile("sunriise", ".mny");
                // tempFile.deleteOnExit();
                long headerOffset = 77;
                dbFile = copyBackupFile(dbFile, tempFile, headerOffset,
                        headerOffset + 4096);
                log.info("Temp converted backup file=" + dbFile);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    public static File copyBackupFile(File srcFile, File destFile, long offset)
            throws IOException {
        return copyBackupFile(srcFile, destFile, offset, -1L);
    }

    public static File copyBackupFile(File srcFile, File destFile, long offset,
            long maxByteCount) throws IOException {
        File newFile = destFile;

        FileChannel srcChannel = null;
        FileChannel destChannel = null;
        try {
            srcChannel = new RandomAccessFile(srcFile, "r").getChannel();

            // Create channel on the destination
            destChannel = new RandomAccessFile(destFile, "rwd").getChannel();

            if (log.isDebugEnabled()) {
                log.debug("srcFile=" + srcFile);
                log.debug("destFile=" + destFile);
            }
            // Copy file contents from source to destination
            if (maxByteCount < 0) {
                maxByteCount = srcChannel.size();
            }
            maxByteCount -= offset;
            if (log.isDebugEnabled()) {
                log.debug("offset=" + offset);
                log.debug("maxByteCount=" + maxByteCount);
            }

            while (maxByteCount > 0) {
                long count = srcChannel.transferTo(offset, maxByteCount,
                        destChannel);
                if (log.isDebugEnabled()) {
                    log.debug("count=" + count);
                }
                maxByteCount -= count;
                offset += count;
            }
        } finally {
            if (srcChannel != null) {
                try {
                    srcChannel.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    srcChannel = null;
                }
            }
            if (destChannel != null) {
                try {
                    destChannel.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    destChannel = null;
                }
            }
        }
        return newFile;
    }

}
