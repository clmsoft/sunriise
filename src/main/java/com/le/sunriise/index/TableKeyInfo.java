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
package com.le.sunriise.index;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.Table;

public class TableKeyInfo {
    private static final Logger log = Logger.getLogger(TableKeyInfo.class);

    private Table table;

    private List<String> primaryKeyColumns;

    public TableKeyInfo(Table table) throws IOException {
        this.table = table;

        findPrimaryKeyColumns();

        findForeignKeyColumns();
    }

    private void findForeignKeyColumns() throws IOException {
        List<Index> indexes = table.getIndexes();
        for (Index index : indexes) {
            if (index.isForeignKey() && index.getReference().isPrimaryTable()) {
                // load index/table from referenced table
                Index otherIndex = index.getReferencedIndex();
                Table otherTable = otherIndex.getTable();
                if ((index.getColumns().size() != 1) || (otherIndex.getColumns().size() != 1)) {
                    throw new IOException("ForeignKey must have exactly 1 column.");
                }
                String str1 = table.getName() + "." + index.getColumns().get(0).getName();
                String str2 = otherTable.getName() + "." + otherIndex.getColumns().get(0).getName();
                log.info(str1 + " - " + str2);
            }
        }
    }

    private void findPrimaryKeyColumns() {
        primaryKeyColumns = new ArrayList<String>();
        List<Index> indexes = table.getIndexes();
        for (Index index : indexes) {
            if (index.isPrimaryKey()) {
                List<ColumnDescriptor> columns = index.getColumns();
                for (ColumnDescriptor column : columns) {
                    primaryKeyColumns.add(column.getName());
                }
            }
        }
    }
}
