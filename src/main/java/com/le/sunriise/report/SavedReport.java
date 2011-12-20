package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class SavedReport {
    private static final Logger log = Logger.getLogger(SavedReport.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;

        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
        } else {
            Class<SavedReport> clz = SavedReport.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
            Database db = openedDb.getDb();
            Table table = db.getTable("Report Custom Pool");
            Cursor cursor = Cursor.createCursor(table);
            Iterator<Map<String, Object>> iterator = cursor.iterator();
            while (iterator.hasNext()) {
                Map<String, Object> values = iterator.next();
                String szTitle = (String) values.get("szTitle");
                log.info("szTitle=" + szTitle);
                String szFull = (String) values.get("szFull");
                log.info("    szFull=" + szFull);
                String szAls = (String) values.get("szAls");
                log.info("    szAls=" + szAls);
                byte[] rgbSRPT = (byte[]) values.get("rgbSRPT");
                log.info("    rgbSRPT=" + rgbSRPT.length);
                // log.info(new String(rgbSRPT, db.getCharset()));
                byte[] rgbMetaFile = (byte[]) values.get("rgbMetaFile");
                if (rgbMetaFile != null) {
                    log.info("    rgbMetaFile=" + rgbMetaFile.length);
//                  log.info(new String(rgbMetaFile));
                }
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

}
