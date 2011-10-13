package com.le.sunriise.accountviewer;

import java.util.Map;

public class RecurringTransactionFilter implements TransactionFilter {

    
    @Override
    public boolean accept(Transaction transaction, Map<String, Object> row) {
        return !transaction.isRecurring();
    }

}
