package com.le.sunriise.scan;

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
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.PageTypes;
import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.header.PageInfo;
import com.le.sunriise.password.HeaderPagePasswordChecker;

public class PageScanner {
    private static final Logger log = Logger.getLogger(PageScanner.class);

    private DbFile dbFile;
    private HeaderPagePasswordChecker checker;

    private RandomAccessFile rFile;

    private FileChannel fileChannel;

    private byte[] encodingKey;

    private RC4Engine engine;

    private Map<Byte, List<PageInfo>> knownTypePages = new HashMap<Byte, List<PageInfo>>();

    private Map<Integer, TableDefPage> tableDefPageMap = new HashMap<Integer, TableDefPage>();

    private PageInfo[] encryptedPages = new PageInfo[MSISAM_MAX_ENCRYPTED_PAGE];

    private List<PageInfo> unknownTypePages = null;

    private static final boolean CIPHER_DECRYPT_MODE = false;

    private static final int PASSWORD_DIGEST_LENGTH = 0x10;

    private static final int MSISAM_MAX_ENCRYPTED_PAGE = 0xE;

    public PageScanner(DbFile dbFile, HeaderPagePasswordChecker checker) {
        super();
        this.dbFile = dbFile;
        this.checker = checker;

        initKnownTypesPagesMap();
        unknownTypePages = new ArrayList<PageInfo>();
    }

    public void scan() {
        try {
            try {
                openFileChannel(dbFile.getFile());

                ByteBuffer buffer = createPageBuffer();

                this.encodingKey = checker.getEncodingKey();
                this.engine = new RC4Engine();
                long pages = dbFile.getPages();
                for (int pageNumber = 0; pageNumber < pages; pageNumber++) {
                    scanPage(fileChannel, buffer, pageNumber);
                }

                checkTDefPages();

            } finally {
                logPageInfoSummary();

                closeFileChannel();
            }

        } catch (IOException e) {
            log.error(e, e);
        }
    }

    private void openFileChannel(File dbFile) throws IOException {
        rFile = new RandomAccessFile(dbFile, "r");
        rFile.seek(0L);
        fileChannel = rFile.getChannel();
    }

    private void closeFileChannel() {
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

    private ByteBuffer createPageBuffer() {
        int capacity = getPageSize();
        return createPageBuffer(capacity);
    }

    private ByteBuffer createPageBuffer(int capacity) {
        ByteBuffer buffer = ByteBuffer.allocate(capacity);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer;
    }

    private int getPageSize() {
        return dbFile.getHeaderPage().getJetFormat().PAGE_SIZE;
    }

    private void scanPage(FileChannel fileChannel, ByteBuffer buffer, int pageNumber) throws IOException {
        readPage(fileChannel, pageNumber, buffer);

        // skip header page
        if (pageNumber == 0) {
            return;
        }

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
            parseTableDefPage(pageNumber, pageType, buffer);
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

    private void readPage(FileChannel fileChannel, int pageNumber, ByteBuffer buffer) throws IOException {
        buffer.clear();

        int position = pageNumber * getPageSize();

        int bytesRead = fileChannel.read(buffer, position);
        if (bytesRead != getPageSize()) {
            log.error("bytesRead=" + bytesRead + " is not the same as getPageSize()=" + getPageSize());
        }
        buffer.flip();

        decodePage(buffer, pageNumber, encodingKey, engine);
    }

    private ByteBuffer readPage(int pageNumber) throws IOException {
        ByteBuffer buffer = createPageBuffer();
        readPage(fileChannel, pageNumber, buffer);
        return buffer;
    }

    private void checkTDefPage(TableDefPage tDef, List<TableDefPage> children) throws IOException {
        log.info("pageNumber=" + tDef.getPageNumber());

        ByteBuffer buffer = readPage(tDef.getPageNumber());
        for (TableDefPage child : children) {
            ByteBuffer nextBuffer = readPage(child.getPageNumber());
            int offset = 8;
            buffer = concatBuffer(buffer, nextBuffer, offset);
        }
        int rowCount = buffer.getInt(getFormat().OFFSET_NUM_ROWS);
        log.info("  rowCount=" + rowCount);
        short columnCount = buffer.getShort(getFormat().OFFSET_NUM_COLS);
        log.info("  columnCount=" + columnCount);
        int logicalIndexCount = buffer.getInt(getFormat().OFFSET_NUM_INDEX_SLOTS);
        log.info("  logicalIndexCount=" + logicalIndexCount);
        int indexCount = buffer.getInt(getFormat().OFFSET_NUM_INDEXES);
        log.info("  indexCount=" + indexCount);
    }

    private void checkTDefPages() throws IOException {
        for (Integer key : tableDefPageMap.keySet()) {
            TableDefPage tDef = tableDefPageMap.get(key);
            if (tDef.isChild()) {
                continue;
            }
            List<TableDefPage> children = findChildren(tDef, tableDefPageMap);
            checkTDefPage(tDef, children);
        }
    }

    private TableDefPage parseTableDefPage(int pageNumber, byte pageType, ByteBuffer buffer) throws IOException {
        PageInfo pageInfo = new PageInfo(pageNumber, pageType);
        knownTypePages.get(pageType).add(pageInfo);

        TableDefPage tDef = tableDefPageMap.get(pageNumber);
        if (tDef == null) {
            tDef = new TableDefPage(pageNumber, buffer, getHeaderPage());
        } else {
            log.info("Already parse pageNumber=" + pageNumber);
        }
        tableDefPageMap.put(pageNumber, tDef);
        int nextPageNumber = tDef.getNextPageNumber();
        if (nextPageNumber > 0) {
            readPage(fileChannel, nextPageNumber, buffer);
            pageType = buffer.get(0);
            if (pageType != PageTypes.TABLE_DEF) {
                log.warn("Expected PageTypes.TABLE_DEF at pageNumber=" + nextPageNumber + ". Got pageType=" + pageType);
            } else {
                TableDefPage nextTDef = parseTableDefPage(nextPageNumber, pageType, buffer);
                nextTDef.setPreviousPageNumber(pageNumber);
            }
        }
        return tDef;
    }

    private JetFormat getFormat() {
        return dbFile.getHeaderPage().getJetFormat();
    }

    private ByteBuffer concatBuffer(ByteBuffer buffer, ByteBuffer nextBuffer, int offset) {
        ByteBuffer newBuffer = null;

        int capacity = buffer.capacity() + (nextBuffer.capacity() - offset);
        newBuffer = createPageBuffer(capacity);
        newBuffer.put(buffer);
        newBuffer.put(nextBuffer.array(), offset, (nextBuffer.capacity() - offset));

        newBuffer.flip();

        return newBuffer;
    }

    private HeaderPage getHeaderPage() {
        return dbFile.getHeaderPage();
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

        int count = 0;
        for (Integer key : tableDefPageMap.keySet()) {
            TableDefPage tDef = tableDefPageMap.get(key);
            if (tDef.isChild()) {
                continue;
            }
            List<TableDefPage> children = findChildren(tDef, tableDefPageMap);

            log.info(count + " TDEF [" + (children.size() + 1) + "] " + toString(tDef, children));
            count++;
        }
        log.info("Total tDef=" + count);
    }

    private String toString(TableDefPage parent, List<TableDefPage> children) {
        TableDefPage tDef = parent;
        StringBuilder sb = new StringBuilder();
        sb.append(tDef.getPageNumber());
        TableDefPage lastChild = tDef;
        for (TableDefPage child : children) {
            sb.append(" -> ");
            sb.append(child.getPageNumber());
            lastChild = child;
        }
        sb.append(" -> ");
        sb.append(lastChild.getNextPageNumber());
        return sb.toString();
    }

    private static boolean isEncryptedPage(int pageNumber) {
        boolean rv = (pageNumber > 0) && (pageNumber <= MSISAM_MAX_ENCRYPTED_PAGE);
        // rv = (pageNumber > 0);
        return rv;
    }

    private static List<TableDefPage> findChildren(TableDefPage tdef, Map<Integer, TableDefPage> tableDefPageMap) {
        List<TableDefPage> children = new ArrayList<TableDefPage>();
        while (tdef.getNextPageNumber() > 0) {
            tdef = tableDefPageMap.get(tdef.getNextPageNumber());
            if (tdef == null) {
                break;
            }
            children.add(tdef);
        }
        return children;
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

    private static void decodePage(ByteBuffer buffer, KeyParameter params, RC4Engine engine) {
        engine.init(CIPHER_DECRYPT_MODE, params);
        byte[] array = buffer.array();
        engine.processBytes(array, 0, array.length, array, 0);
    }

    public TableDefPage getPage(int i) {
        TableDefPage tDef = tableDefPageMap.get(i);
        return tDef;
    }

}
