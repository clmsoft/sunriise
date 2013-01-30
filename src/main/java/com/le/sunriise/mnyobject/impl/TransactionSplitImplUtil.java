package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionSplit;

public class TransactionSplitImplUtil {

    public static TransactionSplit getTransactionSplit(Cursor splitCursor, Transaction transaction) throws IOException {
        splitCursor.reset();
        TransactionSplit transactionSplit = null;
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("htrn", transaction.getId());
        if (splitCursor.findFirstRow(rowPattern)) {
            Map<String, Object> row = splitCursor.getCurrentRow();
            Integer htrnParent = (Integer) row.get("htrnParent");
            Integer iSplit = (Integer) row.get("iSplit");
    
            transactionSplit = new TransactionSplitImpl();
            transactionSplit.setTransaction(transaction);
            transactionSplit.setParentId(htrnParent);
            transactionSplit.setRowId(iSplit);
        }
        return transactionSplit;
    }

}
