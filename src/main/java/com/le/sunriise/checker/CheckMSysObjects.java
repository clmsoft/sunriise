package com.le.sunriise.checker;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class CheckMSysObjects {
    private static final Logger log = Logger.getLogger(CheckMSysObjects.class);

    private String dbFileName;
    private String password;

    private OpenedDb openDb;

    public CheckMSysObjects(String dbFileName, String password) throws IOException {
        this.dbFileName = dbFileName;
        this.password = password;
        this.openDb = Utils.openDbReadOnly(new File(dbFileName), password);
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;
        String password = null;

        if (args.length == 1) {
            dbFileName = args[0];
        } else if (args.length == 2) {
            dbFileName = args[0];
            password = args[1];
        } else {
            Class clz = CheckMSysObjects.class;
            System.out.println("Usage: java " + clz.getName() + " *.mny [password]");
            System.exit(1);
        }

        CheckMSysObjects checker = null;
        try {
            checker = new CheckMSysObjects(dbFileName, password);
            checker.check();
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");

            if (checker != null) {
                checker.close();
                checker = null;
            }
        }
    }

    private void check() throws IOException {
        Database db = openDb.getDb();
        // Table table = db.getSystemCatalog();
        // if (table == null) {
        // throw new IOException("Cannot find systemCatalog");
        // }
    }

    private void close() {
        if (openDb != null) {
            openDb.close();
            openDb = null;
        }

    }
}
