package com.le.sunriise.accountviewer;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractAccountViewerTableModel extends AbstractTableModel {

    private Account account;
    private MnyContext mnyContext;

    public AbstractAccountViewerTableModel(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public MnyContext getMnyContext() {
        return mnyContext;
    }

    public void setMnyContext(MnyContext mnyContext) {
        this.mnyContext = mnyContext;
    }
}
