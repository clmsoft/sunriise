package com.le.sunriise.header;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    private static final boolean CIPHER_DECRYPT_MODE = false;

    private String dbFileName;

    private String password;

    private int pageSize;

    private byte[] encodingKey;

    private RC4Engine engine;

    private Map<Byte, List<PageInfo>> knownTypePages = new HashMap<Byte, List<PageInfo>>();
    private List<PageInfo> unknownTypePages = null;

    private PageInfo[] encryptedPages = new PageInfo[MSISAM_MAX_ENCRYPTED_PAGE];

    public CheckPagesCmd(String dbFileName, String password) {
        this.dbFileName = dbFileName;
        this.password = password;

        initKnownTypesPagesMap();
        unknownTypePages = new ArrayList<PageInfo>();
    }

    protected void initKnownTypesPagesMap() {
        Byte key = null;
        List<PageInfo> value = null;

        key = PageTypes.DATA;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);

        key = PageTypes.INDEX_LEAF;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);

        key = PageTypes.INDEX_NODE;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);

        key = PageTypes.INVALID;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);

        key = PageTypes.TABLE_DEF;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);

        key = PageTypes.USAGE_MAP;
        value = new ArrayList<PageInfo>();
        knownTypePages.put(key, value);
    }

    private void check() throws IOException {
        File dbFile = new File(dbFileName);

        HeaderPage headerPage = null;
        headerPage = new HeaderPage(dbFile);
        this.pageSize = headerPage.getJetFormat().PAGE_SIZE;
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
        } else {
            log.info("Valid password.");
        }

        try {
            RandomAccessFile rFile = null;
            FileChannel fileChannel = null;
            try {
                rFile = new RandomAccessFile(dbFile, "r");
                rFile.seek(0L);
                fileChannel = rFile.getChannel();

                ByteBuffer buffer = ByteBuffer.allocate(pageSize);
                buffer.order(ByteOrder.LITTLE_ENDIAN);

                this.encodingKey = checker.getEncodingKey();
                this.engine = new RC4Engine();
                for (int pageNumber = 0; pageNumber < pages; pageNumber++) {
                    checkPage(fileChannel, buffer, pageNumber);
                }

            } finally {
                logPageInfoSummary();

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

    private void logPageInfoSummary() {
        log.info("UNKNOWN_TYPE_PAGE count=" + unknownTypePages.size());

        Set<Byte> keys = knownTypePages.keySet();
        for (byte key : keys) {
            List<PageInfo> pages = knownTypePages.get(key);
            log.info("KNOWN_TYPE_PAGE type=" + PageInfo.pageTypeToString(key) + ", count=" + pages.size());
        }

        for (int i = 0; i < encryptedPages.length; i++) {
            log.info("ENCRYPTED_PAGE, " + encryptedPages[i].toString());
        }
    }

    protected void checkPage(FileChannel fileChannel, ByteBuffer buffer, int pageNumber) throws IOException {
        // skip header page
        if (pageNumber == 0) {
            return;
        }

        buffer.clear();

        int position = pageNumber * pageSize;

        int bytesRead = fileChannel.read(buffer, position);
        if (bytesRead != pageSize) {
            log.error("bytesRead=" + bytesRead + " is not the same as pageSize=" + pageSize);
        }
        buffer.flip();

        decodePage(buffer, pageNumber, encodingKey, engine);

        byte pageType = buffer.get(0);

        if (pageNumber <= MSISAM_MAX_ENCRYPTED_PAGE) {
            encryptedPages[pageNumber - 1] = new PageInfo(pageNumber, pageType);
        }

        switch (pageType) {
        case PageTypes.DATA:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;
        case PageTypes.INDEX_LEAF:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;
        case PageTypes.INDEX_NODE:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;
        case PageTypes.INVALID:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;
        case PageTypes.TABLE_DEF:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;
        case PageTypes.USAGE_MAP:
            knownTypePages.get(pageType).add(new PageInfo(pageNumber, pageType));
            break;

        default:
            unknownTypePages.add(new PageInfo(pageNumber, (byte) -1));
            break;
        }
        return;
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

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;
        String password = null;

        if (args.length == 1) {
            dbFileName = args[0];
            password = null;
        } else if (args.length == 2) {
            dbFileName = args[0];
            password = args[1];
        } else {
            Class<CheckPagesCmd> clz = CheckPagesCmd.class;
            System.out.println("Usage: java " + clz.getName() + " *.mny [password]");
            System.exit(1);
        }

        CheckPagesCmd cmd = new CheckPagesCmd(dbFileName, password);
        try {
            cmd.check();
        } catch (IOException e) {
            log.error(e, e);
        }

    }
}
