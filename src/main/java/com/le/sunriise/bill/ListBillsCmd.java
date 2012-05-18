package com.le.sunriise.bill;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.Joiner;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.accountviewer.Account;
import com.le.sunriise.accountviewer.Frequency;
import com.le.sunriise.accountviewer.Transaction;
import com.le.sunriise.report.DefaultAccountVisitor;
import com.le.sunriise.viewer.OpenedDb;

public class ListBillsCmd {

    private static final Logger log = Logger.getLogger(ListBillsCmd.class);

    private static final class ListBillsVisitor extends DefaultAccountVisitor {
        @Override
        protected boolean acceptAccount(Account account) {
            return false;
        }

        @Override
        public void visit(OpenedDb openedDb) throws IOException {
            super.visit(openedDb);
            Database db = openedDb.getDb();
            if (db == null) {
                return;
            }
            Set<String> tableNames = db.getTableNames();
            for (String tableName : tableNames) {
                Table table = db.getTable(tableName);
                if (table == null) {
                    log.warn("Cannot get table=" + tableName);
                    continue;
                }

                if (!acceptTable(table)) {
                    continue;
                }
                visitTable(table);
            }
        }

        protected boolean acceptTable(Table table) {
            return true;
        }

        protected void visitTable(Table table) throws IOException {
            String tableName = table.getName();
            if(tableName.compareToIgnoreCase("bill") == 0) {
                parseBillTable(table);
            }
        }

        private void parseBillTable(Table table) throws IOException {
            Table trnTable = table.getDatabase().getTable("trn");
            Index trnToBillIndex = table.getForeignKeyIndex(trnTable);
//            log.info("trnToBillIndex=" + trnToBillIndex.getTable().getName());
            
            Index billToTrnIndex = trnTable.getForeignKeyIndex(table);
//            log.info("billToTrnIndex=" + billToTrnIndex.getTable().getName());
            
            // from bill to transaction
            Joiner joiner = Joiner.create(table, trnTable);
            Index fromIndex = joiner.getFromIndex();
            log.info("fromIndex=" + fromIndex.getName());
            log.info("fromIndex.table=" + fromIndex.getTable().getName());
            Index toIndex = joiner.getToIndex();
            log.info("toIndex=" + toIndex.getName());
            log.info("toIndex.table=" + toIndex.getTable().getName());

            Index trnIndex = trnTable.getIndex("AcctDtIdTrn");

            Cursor trnCursor = Cursor.createIndexCursor(trnTable, trnIndex);
            
            Cursor cursor = Cursor.createCursor(table);
            Map<String, Object> row = null;
            while((row = cursor.getNextRow()) != null) {
                Integer st= (Integer) row.get("st");
                
                Integer frq = (Integer) row.get("frq");
                Double cFrqInst = (Double) row.get("cFrqInst");
                Frequency frequency = new Frequency();
                frequency.setFrq(frq);
                frequency.setcFrqInst(cFrqInst);
                
                Integer lHtrn = (Integer) row.get("lHtrn");
                log.info("lHtrn=" + lHtrn);
//                Transaction transaction = getTransaction(lHtrn, trnCursor);
                Map<String, Object> fromRow = new HashMap<String, Object>();
//                fromRow.put("trn", lHtrn);
                fromRow.put("htrn", lHtrn);
                
                Map<String, Object> toRow = joiner.findFirstRow(fromRow);
                log.info(toRow);
                
                Integer hbillHead = (Integer) row.get("hbillHead");
                
//                        Columns:
//                                Name: (BILL) hbill
//                                Type: 0x4 (LONG)
//                                Number: 0
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) st
//                                Type: 0x4 (LONG)
//                                Number: 1
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) frq
//                                Type: 0x4 (LONG)
//                                Number: 2
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) cFrqInst
//                                Type: 0x7 (DOUBLE)
//                                Number: 3
//                                Length: 8
//                                Variable length: true
//
//                                Name: (BILL) dt
//                                Type: 0x8 (SHORT_DATE_TIME)
//                                Number: 4
//                                Length: 8
//                                Variable length: true
//
//                                Name: (BILL) iinstNextUnpaid
//                                Type: 0x4 (LONG)
//                                Number: 5
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) dtMax
//                                Type: 0x8 (SHORT_DATE_TIME)
//                                Number: 6
//                                Length: 8
//                                Variable length: true
//
//                                Name: (BILL) cInstMax
//                                Type: 0x4 (LONG)
//                                Number: 7
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) dtSerial
//                                Type: 0x8 (SHORT_DATE_TIME)
//                                Number: 8
//                                Length: 8
//                                Variable length: true
//
//                                Name: (BILL) itrnLink
//                                Type: 0x4 (LONG)
//                                Number: 9
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) hbillHead
//                                Type: 0x4 (LONG)
//                                Number: 10
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) iinst
//                                Type: 0x4 (LONG)
//                                Number: 11
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) itrn
//                                Type: 0x4 (LONG)
//                                Number: 12
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) lHtrn
//                                Type: 0x4 (LONG)
//                                Number: 13
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) iinstLastSkipped
//                                Type: 0x4 (LONG)
//                                Number: 14
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) cEstInst
//                                Type: 0x4 (LONG)
//                                Number: 15
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) cDaysAutoEnter
//                                Type: 0x4 (LONG)
//                                Number: 16
//                                Length: 4
//                                Variable length: true
//
//                                Name: (BILL) sguid
//                                Type: 0xf (GUID)
//                                Number: 17
//                                Length: 16
//                                Variable length: true
//
//                                Name: (BILL) fUpdated
//                                Type: 0x1 (BOOLEAN)
//                                Number: 18
//                                Length: 1
//                                Variable length: false

            }
        }

        private Transaction getTransaction(Integer lHtrn, Cursor trnCursor) throws IOException {
            log.info("> getTransaction, lHtrn=" + lHtrn);
            Transaction transation = null;
            
            trnCursor.reset();
            Map<String, Object> rowPattern = new HashMap<String, Object>();
            rowPattern.put("htrn", lHtrn);
            if (trnCursor.findFirstRow(rowPattern)) {
                transation = new Transaction();
                Map<String, Object> row = trnCursor.getCurrentRow();
                
            }
            
            log.info("< getTransaction, transation=" + transation);
            return transation;
        }
    }

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
            Class<ListBillsCmd> clz = ListBillsCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            DefaultAccountVisitor cmd = new ListBillsVisitor();
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }
}
