package com.le.sunriise.viewer;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.JetFormat.CodecType;
import com.healthmarketscience.jackcess.PageChannel;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.AccountUtil;
import com.le.sunriise.accountviewer.Transaction;
import com.le.sunriise.encryption.EncryptionUtils;
import com.le.sunriise.index.IndexLookup;

public class TableUtils {
    private static final Logger log = Logger.getLogger(TableUtils.class);
    
    public static String parseIndexInfo(Table table) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Index info");
        sb.append("\n");
        sb.append("\n");

        sb.append("Table: " + table.getName());
        sb.append("\n");
        sb.append("\n");

        List<Index> indexes = table.getIndexes();
        sb.append("# Index: (" + indexes.size() + ")");
        sb.append("\n");

        for (Index index : indexes) {
            IndexData indexData = index.getIndexData();
            sb.append("    type=" + indexData.getClass().getName());
            sb.append("\n");
            sb.append("    uniqueEntryCount=" + index.getUniqueEntryCount());
            sb.append("\n");
            // isUnique
            sb.append("    unique=" + index.isUnique());
            sb.append("\n");
            sb.append("    shouldIgnoreNulls=" + index.shouldIgnoreNulls());
            sb.append("\n");

            List<ColumnDescriptor> columns = index.getColumns();
            sb.append("    " + index.getName() + " (" + columns.size() + ")");
            sb.append("\n");
            for (ColumnDescriptor column : columns) {
                sb.append("        " + column.getColumn().getTable().getName() + "." + column.getColumn().getName());
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    public static String parseKeyInfo(Table table) throws IOException {
        StringBuilder sb = new StringBuilder();
    
        sb.append("Key info");
        sb.append("\n");
        sb.append("\n");
    
        sb.append("Table: " + table.getName());
        sb.append("\n");
        sb.append("\n");
    
        sb.append("# Primary keys:");
        sb.append("\n");
        IndexLookup indexLookup = new IndexLookup();
        for (Column column : table.getColumns()) {
            if (indexLookup.isPrimaryKeyColumn(column)) {
                sb.append("(PK) " + table.getName() + "." + column.getName() + ", " + indexLookup.getMax(column));
                sb.append("\n");
    
                List<Column> referencing = indexLookup.getReferencing(column);
                for (Column col : referencing) {
                    sb.append("    (referencing-FK) " + col.getTable().getName() + "." + col.getName());
                    sb.append("\n");
                }
            }
        }
        sb.append("\n");
    
        sb.append("# Foreign keys:");
        sb.append("\n");
        for (Column column : table.getColumns()) {
            List<Column> referenced = indexLookup.getReferencedColumns(column);
            for (Column col : referenced) {
                sb.append("(FK) " + table.getName() + "." + column.getName() + " -> " + col.getTable().getName() + "." + col.getName());
                sb.append("\n");
            }
        }
        sb.append("\n");
    
        return sb.toString();
    }

    public static String parseTableMetaData(Table table) {
        StringBuilder sb = new StringBuilder();
    
        int pageCount = table.getApproximateOwnedPageCount();
    
        sb.append("pageCount=" + pageCount);
        sb.append("\n");
    
        sb.append(table.toString());
    
        return sb.toString();
    }

    public static String parseHeaderInfo(Table table, OpenedDb openedDb) throws IOException {
        StringBuilder sb = new StringBuilder();
    
        sb.append("Header info");
        sb.append("\n");
        sb.append("\n");
    
        sb.append("Table: " + table.getName());
        sb.append("\n");
        sb.append("\n");
    
        Database db = table.getDatabase();
    
        PageChannel pageChannel = db.getPageChannel();
        ByteBuffer buffer = pageChannel.createPageBuffer();
        pageChannel.readPage(buffer, 0);
    
        JetFormat format = pageChannel.getFormat();
        sb.append("format=" + format.toString());
        sb.append("\n");
    
        CodecType msisam = CodecType.MSISAM;
        if (format.CODEC_TYPE == msisam) {
            EncryptionUtils.appendMSISAMInfo(buffer, openedDb.getPassword(), openedDb.getDb().getCharset(), sb);
        }
    
        // 0x00 4
        // ENGINE_NAME_OFFSET 0x04 15
        // OFFSET_VERSION 20 1
        // SALT_OFFSET 0x72 4
        // ENCRYPTION_FLAGS_OFFSET 0x298 1
    
        return sb.toString();
    }

    public static void visitAllRows(Table table) throws IOException {
        if (table == null) {
            return;
        }
    
        Cursor cursor = Cursor.createCursor(table);
        while (cursor.moveToNextRow()) {
            Map<String, Object> row = cursor.getCurrentRow();
            for (String key : row.keySet()) {
                Object value = row.get(key);
            }
        }
    }

    public static void calculateMonthlySummary(Account account) {
        List<Transaction> transactions = account.getTransactions();
        Date previousDate = null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");
    
        int rowIndex = 0;
    
        int entries = 0;
        BigDecimal monthlyBalance = new BigDecimal(0);
        for (Transaction transaction : transactions) {
            // if (transaction.isVoid()) {
            // rowIndex++;
            // continue;
            // }
            if (transaction.isRecurring()) {
                rowIndex++;
                continue;
            }
            entries++;
    
            Date date = transaction.getDate();
            if (previousDate != null) {
                Calendar cal = Calendar.getInstance();
    
                cal.setTime(previousDate);
                int previousMonth = cal.get(Calendar.MONTH);
    
                cal.setTime(date);
                int month = cal.get(Calendar.MONTH);
    
                if (month != previousMonth) {
                    log.info(dateFormatter.format(previousDate) + ", entries=" + entries + ", monthlyBalance=" + monthlyBalance + ", balance="
                            + AccountUtil.getRunningBalance(rowIndex - 1, account));
                    entries = 0;
                    monthlyBalance = new BigDecimal(0);
                }
            }
            previousDate = date;
    
            BigDecimal amount = transaction.getAmount();
            if (transaction.isVoid()) {
                amount = new BigDecimal(0);
            }
            if (amount == null) {
                amount = new BigDecimal(0);
            }
            monthlyBalance = monthlyBalance.add(amount);
    
            rowIndex++;
        }
    }

}
