package com.le.sunriise.accountviewer;

import java.util.Map;

public interface TransactionFilter {

    boolean accept(Transaction transaction, Map<String, Object> row);

}
