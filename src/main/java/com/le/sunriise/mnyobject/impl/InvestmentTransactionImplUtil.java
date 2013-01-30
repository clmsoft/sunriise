package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.mnyobject.InvestmentTransaction;

public class InvestmentTransactionImplUtil {

    private static final String TABLE_NAME = "TRN_INV";
    private static final String COL_ID = "htrn";
    private static final String COL_PRICE = "dPrice";
    private static final String COL_QUANTITY = "qty";
    
    public static InvestmentTransaction getInvestmentTransaction(Database db, Integer id) throws IOException {
        InvestmentTransaction investmentTransaction = new InvestmentTransactionImpl();
    
        String tableName = TABLE_NAME;
        Table table = db.getTable(tableName);
        Cursor cursor = Cursor.createCursor(table);
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put(COL_ID, id);
        if (cursor.findFirstRow(rowPattern)) {
            Map<String, Object> row = cursor.getCurrentRow();
            
            Double price = (Double) row.get(COL_PRICE);
            investmentTransaction.setPrice(price);
    
            Double quantity = (Double) row.get(COL_QUANTITY);
            investmentTransaction.setQuantity(quantity);
        }
    
        return investmentTransaction;
    }

}
