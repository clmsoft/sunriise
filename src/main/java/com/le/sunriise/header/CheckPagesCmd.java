package com.le.sunriise.header;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;

import org.apache.log4j.Logger;
import org.bouncycastle.crypto.engines.RC4Engine;
import org.bouncycastle.crypto.params.KeyParameter;

import com.healthmarketscience.jackcess.BaseCryptCodecHandler;
import com.healthmarketscience.jackcess.PageTypes;
import com.le.sunriise.password.HeaderPagePasswordChecker;

public class CheckPagesCmd {

    private static final Logger log = Logger.getLogger(CheckPagesCmd.class);

    private static final int MSISAM_MAX_ENCRYPTED_PAGE = 0xE;
    private static final int PASSWORD_DIGEST_LENGTH = 0x10;
    public static final boolean CIPHER_DECRYPT_MODE = false;

    /**
     * @param args
     */
    public static void main(String[] args) {
        HeaderPage headerPage = null;

        File dbFile = new File(args[0]);
        String password = null;
        try {
            headerPage = new HeaderPage(dbFile);
            int pageSize = headerPage.getJetFormat().PAGE_SIZE;
            long fileLength = dbFile.length();
            log.info("dbFile=" + dbFile.getAbsolutePath());
            log.info("fileLength=" + fileLength);

            log.info("pageSize=" + pageSize);

            long pages = fileLength / pageSize;
            long leftOverBytes = fileLength % pageSize;
            log.info("pages=" + pages + "/" + leftOverBytes);

            HeaderPagePasswordChecker checker = new HeaderPagePasswordChecker(headerPage);
            boolean passwordIsValid = checker.check(password);
            if (!passwordIsValid) {
                log.warn("Invalid password.");
            }

            RandomAccessFile rFile = null;
            FileChannel fileChannel = null;
            try {
                rFile = new RandomAccessFile(dbFile, "r");
                rFile.seek(0L);
                fileChannel = rFile.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(pageSize);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                byte[] _encodingKey = checker.getEncodingKey();
                RC4Engine engine = new RC4Engine();

                for (int pageNumber = 0; pageNumber < pages; pageNumber++) {
                    buffer.clear();
                    int position = pageNumber * pageSize;
                    int bytesRead = fileChannel.read(buffer, position);
                    if (bytesRead != pageSize) {
                        log.error("bytesRead=" + bytesRead + " is not the same as pageSize=" + pageSize);
                    }
                    buffer.flip();

                    decodePage(buffer, pageNumber, _encodingKey, engine);

                    if (pageNumber == 0) {
                        continue;
                    }

                    byte pageType = buffer.get(0);
                    switch (pageType) {
                    case PageTypes.DATA:
                        break;
                    case PageTypes.INDEX_LEAF:
                        break;
                    case PageTypes.INDEX_NODE:
                        break;
                    case PageTypes.INVALID:
                        break;
                    case PageTypes.TABLE_DEF:
                        break;
                    case PageTypes.USAGE_MAP:
                        break;

                    default:
                        if (pageNumber < 20) {
                            log.error("Not a valid pageType=" + pageType + ", pageNumber=" + pageNumber + "/"
                                    + MSISAM_MAX_ENCRYPTED_PAGE);
                        }
                        break;
                    }
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

        } catch (IOException e) {
            log.error(e, e);
        }

    }

    private static void decodePage(ByteBuffer buffer, int pageNumber, byte[] _encodingKey, RC4Engine engine) {
        // log.info("> decodePage, pageNumber=" + pageNumber);

        if (!isEncryptedPage(pageNumber)) {
            // log.info("< decodePage, NOT ENCODED pageNumber=" + pageNumber);
            // not encoded
            return;
        }

        byte[] key = BaseCryptCodecHandler.applyPageNumber(_encodingKey, PASSWORD_DIGEST_LENGTH, pageNumber);
        decodePage(buffer, new KeyParameter(key), engine);
        // log.info("< decodePage, pageNumber=" + pageNumber);
    }

    private static boolean isEncryptedPage(int pageNumber) {
        boolean rv = (pageNumber > 0) && (pageNumber <= MSISAM_MAX_ENCRYPTED_PAGE);
        // rv = (pageNumber > 0);
        return rv;
    }

    private static void decodePage(ByteBuffer buffer, KeyParameter params, RC4Engine engine) {
        engine.init(CIPHER_DECRYPT_MODE, params);

        byte[] array = buffer.array();
        engine.processBytes(array, 0, array.length, array, 0);
    }
}
