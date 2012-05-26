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
package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Relationship;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.viewer.OpenedDb;

public class GetRelationships {
    private static final Logger log = Logger.getLogger(GetRelationships.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        OpenedDb openedDb = null;
        String dbFileName = null;
        
        if (args.length == 1) {
            dbFileName = args[0];
        } else {
            Class<GetRelationships> clz = GetRelationships.class;
            System.out.println("Usage: java " + clz.getName() + " file.mny");
            System.exit(1);
        }
        
        File dbFile = new File(dbFileName);
        String password = null;
        log.info("dbFile=" + dbFile);
        try {
            openedDb = Utils.openDbReadOnly(dbFile, password);
            Database db = openedDb.getDb();
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
            if (openedDb != null) {
                try {
                    openedDb.close();
                } finally {
                    openedDb = null;
                }
            }
            log.info("< DONE");
        }
    }

}
