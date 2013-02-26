package com.le.sunriise.viewer;

import java.io.IOException;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PropertyMap;
import com.healthmarketscience.jackcess.Table;

public class DatabaseUtils {
    private static final Logger log = Logger.getLogger(DatabaseUtils.class);

    static void logDbInfo(Database db) {
        try {
            log.info("### (openned) db=" + db.getFile());

            log.info("  fileFormat=" + db.getFileFormat());
            // log.info("  format=" + db.getFormat());

            log.info("  timeZone=" + db.getTimeZone().getDisplayName());
            log.info("  charset=" + db.getCharset());

            log.info("  tableNames=" + db.getTableNames().size());

            log.info("  defaultCodePage=" + db.getDefaultCodePage());

            Table table = null;
            String label = null;

            log.info("  ");
            table = db.getSystemCatalog();
            label = "systemCatalog";
            logSystemTable(label, table);

            log.info("  ");
            Set<String> systemTableNames = db.getSystemTableNames();
            log.info("  systemTableNames=" + systemTableNames.size());
            int i = 0;
            int size = systemTableNames.size();
            for (String systemTableName : systemTableNames) {
                log.info("  " + "(" + i + "/" + size + ") " + "systemTableName=" + systemTableName);
                table = db.getSystemTable(systemTableName);
                label = "systemTable." + systemTableName;
                logSystemTable(label, table);
                
                i++;
            }

            // log.info("  columnOrder=" + db.getColumnOrder());

//            logPropertyMaps(db);

        } catch (IOException e) {
            log.warn(e);
        }
    }

    protected static void logSystemTable(String label, Table table) {
        if (table == null) {
            log.info("  " + label + "=" + table);
        } else {
            log.info("  " + label + "=" + table.getName());
            log.info("  " + label + ".rows=" + table.getRowCount());
            log.info("  " + label + ".columns=" + table.getColumnCount());
        }
    }

    protected static void logPropertyMaps(Database db) throws IOException {
        PropertyMap map = null;
        String label = null;

        map = db.getDatabaseProperties();
        label = "databaseProperties";
        logPropertyMap(label, map);

        map = db.getSummaryProperties();
        label = "summaryProperties";
        logPropertyMap(label, map);

        map = db.getUserDefinedProperties();
        label = "userDefinedProperties";
        logPropertyMap(label, map);
    }

    protected static void logPropertyMap(String label, PropertyMap map) {

        if (map != null) {
            log.info("  " + label + "=" + map.getSize());
            for (PropertyMap.Property prop : map) {
                log.info("  " + prop);
            }
        } else {
            log.info("  " + label + "=" + map);
        }
    }

}
