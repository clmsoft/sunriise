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
package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;

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
                long headerOffset = (long) findMagicHeader(dbFile);
                dbFile = copyBackupFile(dbFile, tempFile, headerOffset, headerOffset + 4096);
                log.info("Temp converted backup file=" + dbFile);
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

    // 0x00 0x01 0x00 0x00
    // 4d 53 49 53 MSIS
    // 42 4d 20 44 AM D
    // 61 74 61 62 atab
    // 61 73 65 00 ase
    // 01 00 00 00

    private static final byte[] MSISAM_MAGIC_HEADER = { 0x00, 0x01, 0x00, 0x00, 0x4d, 0x53, 0x49, 0x53, 0x41, 0x4d, 0x20, 0x44,
            0x61, 0x74, 0x61, 0x62, 0x61, 0x73, 0x65, 0x00, 0x01, 0x00, 0x00, 0x00 };

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
//                System.out.println("" + i + ", " + Integer.toHexString(data[i]));
            }
            index = matcher.indexOf(data, 0, data.length);
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

    public static File copyBackupFile(File srcFile, File destFile, long offset) throws IOException {
        return copyBackupFile(srcFile, destFile, offset, -1L);
    }

    public static File copyBackupFile(File srcFile, File destFile, long offset, long maxByteCount) throws IOException {
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
                long count = srcChannel.transferTo(offset, maxByteCount, destChannel);
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
