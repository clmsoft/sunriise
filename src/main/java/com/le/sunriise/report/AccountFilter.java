package com.le.sunriise.report;

import com.le.sunriise.accountviewer.Account;

public interface AccountFilter {
    public boolean accept(Account account);
}
