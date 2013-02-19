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
package com.le.sunriise.backup;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

import org.apache.log4j.Logger;

public class BackupFileUtils {
    private static final Logger log = Logger.getLogger(BackupFileUtils.class);

    public static final String MNY_BACKUP_SUFFIX = ".mbf";

    public static final String MNY_SUFFIX = ".mny";

    // 4d 53 49 53 MSIS
    // 41 4d 20 44 AM D
    // 61 74 61 62 atab
    // 61 73 65     ase  
    // MSISAM Database
    // at location 0x4
    private static final byte[] MSISAM_MAGIC_HEADER = { 
        0x4d, 0x53, 0x49, 0x53, 
        0x41, 0x4d, 0x20, 0x44,
        0x61, 0x74, 0x61, 0x62, 
        0x61, 0x73, 0x65};

    public static final int findMagicHeader(File srcFile) throws IOException {
        int index = -1;
        FileChannel srcChannel = null;
        MappedByteBuffer mappedByteBuffer = null;
        try {
            srcChannel = new RandomAccessFile(srcFile, "r").getChannel();
            mappedByteBuffer = srcChannel.map(MapMode.READ_ONLY, 0, srcChannel.size());
            KMPMatch matcher = new KMPMatch(MSISAM_MAGIC_HEADER);
            byte[] data = new byte[4096 * 2];
            for (int i = 0; i < data.length; i++) {
                data[i] = mappedByteBuffer.get();
            }
            index = matcher.indexOf(data, 0, data.length);
            index -= 4;
        } finally {
            if (mappedByteBuffer != null) {
                mappedByteBuffer = null;
            }
            if (srcChannel != null) {
                try {
                    srcChannel.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    srcChannel = null;
                }
            }
        }
        return index;
    }

    public static File copyBackupFile(File srcFile, File destFile, long offset, long maxByteCount) throws IOException {
        log.info("> Copying file from srcFile=" + srcFile);
        log.info("    destFile=" + destFile);
        log.info("    offset=" + offset);
        log.info("    maxByteCount=" + maxByteCount);
        
        File newFile = destFile;

        long totalBytes = 0L;
        FileChannel srcChannel = null;
        FileChannel destChannel = null;
        try {
            srcChannel = new RandomAccessFile(srcFile, "r").getChannel();

            // Create channel on the destination
            destChannel = new RandomAccessFile(destFile, "rwd").getChannel();

            if (log.isDebugEnabled()) {
                log.debug("srcFile=" + srcFile);
                log.debug("  size=" + srcChannel.size());
                log.debug("destFile=" + destFile);
            }
            // Copy file contents from source to destination
            if (maxByteCount < 0) {
                maxByteCount = srcChannel.size();
                log.info("    maxByteCount 222=" + maxByteCount);
            }
            
            maxByteCount -= offset;
            if (log.isDebugEnabled()) {
                log.debug("offset=" + offset);
                log.debug("maxByteCount=" + maxByteCount);
            }

            while (maxByteCount > 0) {
                long count = srcChannel.transferTo(offset, maxByteCount, destChannel);
                totalBytes += count;
                if (log.isDebugEnabled()) {
                    log.debug("count=" + count);
                }
                maxByteCount -= count;
                offset += count;
            }
        } finally {
            log.info("< DONE copying file to destFile=" + destFile);
            log.info("  totalBytes=" + totalBytes);
            
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

    public static final boolean isMnyBackupFile(String name) {
        return name.endsWith(MNY_BACKUP_SUFFIX);
    }

    public static final boolean isMnyBackupFile(File file) {
        String dbFileName = file.getName();
        return isMnyBackupFile(dbFileName);
    }

    public static final boolean isMnyFile(String name) {
        return name.endsWith(MNY_SUFFIX);
    }

    public static boolean isMnyFile(File file) {
        String dbFileName = file.getName();
        return isMnyFile(dbFileName);
    }

    public static boolean isMnyFiles(String name) {
        return isMnyFile(name) || isMnyBackupFile(name);
    }

    public static final File createBackupAsTempFile(File dbFile, boolean deleteOnExit, long maxByteCount) throws IOException {
        File tempFile = File.createTempFile("sunriise", MNY_SUFFIX);
        if (deleteOnExit){
            tempFile.deleteOnExit();
        }
        long headerOffset = findMagicHeader(dbFile);
        log.info("headerOffset 111=" + headerOffset);
        if (headerOffset < 0) {
            headerOffset = 80; // compression header?
            log.info("No magic header, guessing headerOffset=" + headerOffset);
        }
        dbFile = copyBackupFile(dbFile, tempFile, headerOffset, maxByteCount);
        if (log.isDebugEnabled()) {
            log.debug("Temp converted backup file=" + dbFile);
        }
        return dbFile;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;
        try {
            File dbFile = new File(dbFileName);
            String fileName = dbFile.getName();
            if (isMnyBackupFile(fileName)) {
                dbFile = BackupFileUtils.createBackupAsTempFile(dbFile, true, -1L);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

}
