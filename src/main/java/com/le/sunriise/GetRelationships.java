package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;

public class GetRelationships {
    private static final Logger log = Logger.getLogger(GetRelationships.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        Database db = null;
        String dbFileName = "C:/Users/Hung Le/Documents/Microsoft Money/2007/temp/My Money - Copy.mny";
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);
        try {
            db = Utils.openDbReadOnly(dbFile, password);
            Set<String> tableNames = db.getTableNames();
            String[] tableNameArray = new String[tableNames.size()];
            int i = 0;
            for (String tableName : tableNames) {
                tableNameArray[i++] = tableName;
            }
            for (i = 0; i < tableNameArray.length; i++) {
                for (int j = (i + 1); j < tableNameArray.length; j++) {
                    String tableName1 = tableNameArray[i];
                    String tableName2 = tableNameArray[j];
                    Table table1 = db.getTable(tableName1);
                    Table table2 = db.getTable(tableName2);
                    List<Relationship> relationships = db.getRelationships(table1, table2);
                    if (relationships.size() > 0) {
                        log.info(tableName1 + "/" + tableName2 + ", " + relationships.size());
                    }
                }
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (db != null) {
                try {
                    db.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    db = null;
                }
            }
            log.info("< DONE");
        }
    }

}
