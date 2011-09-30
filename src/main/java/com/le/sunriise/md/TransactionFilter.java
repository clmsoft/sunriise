package com.le.sunriise.md;

import java.util.Map;

public interface TransactionFilter {

    boolean accept(Transaction transaction, Map<String, Object> row);

}
