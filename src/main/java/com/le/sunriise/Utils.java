package com.le.sunriise;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.CodecProvider;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.viewer.OpenedDb;

public class Utils {
    private static final Logger log = Logger.getLogger(Utils.class);

    private static String EMPTY_FILE = "empty-db.mdb";

    public static OpenedDb openDb(File dbFile, String password, boolean readOnly) throws IOException {
        return openDb(dbFile, password, readOnly, true);
    }

    public static OpenedDb openDbReadOnly(File dbFile, String password) throws IOException {
        return openDb(dbFile, password, true);
    }

    public static OpenedDb openDb(File inFile, String password) throws IOException {
        return openDb(inFile, password, false);
    }

    public static File lockDb(File dbFile) throws IOException {
        File parentDir = dbFile.getAbsoluteFile().getParentFile().getAbsoluteFile();
        String name = dbFile.getName();
        int i = name.lastIndexOf('.');
        if (i <= 0) {
            log.warn("Cannot lock dbFile=" + name + ". Cannot find suffix");
            return null;
        }
        name = name.substring(0, i);
        File lockFile = new File(parentDir, name + ".lrd");
        if (lockFile.exists()) {
            log.warn("Cannot lock dbFile=" + name + ". Lock file exists");
            return null;
        }

        if (lockFile.createNewFile()) {
            lockFile.deleteOnExit();
            return lockFile;
        } else {
            return null;
        }
    }

    public static OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted) throws IOException {
        String password = null;
        if ((passwordChars != null) && (passwordChars.length > 0)) {
            password = new String(passwordChars);
        }
        return openDb(dbFileName, password, readOnly, encrypted);
    }

    public static OpenedDb openDb(String dbFileName, String password, boolean readOnly, boolean encrypted) throws IOException {
        File dbFile = new File(dbFileName);
        return openDb(dbFile, password, readOnly, encrypted);
    }

    private static OpenedDb openDb(File dbFile, String password, boolean readOnly, boolean encrypted) throws IOException {
        OpenedDb openedDb = new OpenedDb();
        openedDb.setDbFile(dbFile);

        CodecProvider cryptCodecProvider = null;

        cryptCodecProvider = new CryptCodecProvider(password);
        if (!encrypted) {
            cryptCodecProvider = null;
        }

        try {
            if (log.isDebugEnabled()) {
                log.debug("> Database.open, dbFile=" + dbFile);
            }

            if ((!readOnly) && (isMnyFile(dbFile))) {
                File dbLockFile = null;
                if ((dbLockFile = lockDb(dbFile)) == null) {
                    throw new IOException("Cannot lock dbFile=" + dbFile);
                } else {
                    log.info("Created db lock file=" + dbLockFile);
                }
                openedDb.setDbLockFile(dbLockFile);
            }
            boolean autoSync = true;
            Charset charset = null;
            TimeZone timeZone = null;
            Database db = Database.open(dbFile, readOnly, autoSync, charset, timeZone, cryptCodecProvider);
            openedDb.setDb(db);
            openedDb.setPassword(password);
        } catch (UnsupportedOperationException e) {
            throw new IOException(e);
        } finally {
            if (log.isDebugEnabled()) {
                log.debug("< Database.open, dbFile=" + dbFile);
            }
        }

        return openedDb;
    }

    private static boolean isMnyFile(File dbFile) {
        String dbFileName = dbFile.getName();
        return dbFileName.endsWith(".mny");
    }

    public static Database createEmptyDb(InputStream emptyDbTemplateStream, File destFile) throws IOException {
        Utils.copyStreamToFile(emptyDbTemplateStream, destFile);

        boolean readOnly = false;
        boolean autoSync = true;
        Charset charset = null;
        TimeZone timeZone = null;
        CodecProvider provider = null;

        Database db = Database.open(destFile, readOnly, autoSync, charset, timeZone, provider);

        return db;
    }

    private static void copyStreamToFile(InputStream srcIn, File destFile) throws FileNotFoundException, IOException {
        OutputStream out = null;
        try {
            out = new BufferedOutputStream(new FileOutputStream(destFile));
            Utils.copyStream(srcIn, out);
        } finally {

            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    out = null;
                }
            }
        }
    }

    public static Database createEmptyDb(File file) throws IOException {
        Database db = null;
        InputStream in = null;
        try {
            String resource = Utils.EMPTY_FILE;
            in = Thread.currentThread().getContextClassLoader().getResourceAsStream(resource);
            if (in == null) {
                throw new IOException("Cannot find resource=" + resource);
            }
            in = new BufferedInputStream(in);
            db = createEmptyDb(in, file);
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    in = null;
                }
            }
        }
        return db;
    }

    private static void copyStream(InputStream in, OutputStream out) throws IOException {
        int bufSize = 1024;
        byte[] buffer = new byte[bufSize];
        int n = 0;
        while ((n = in.read(buffer, 0, bufSize)) != -1) {
            out.write(buffer, 0, n);
        }
    }

}
