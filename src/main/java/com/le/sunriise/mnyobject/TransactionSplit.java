package com.le.sunriise.mnyobject;

public interface TransactionSplit {

    public abstract Integer getParentId();

    public abstract void setParentId(Integer parentId);

    public abstract Integer getRowId();

    public abstract void setRowId(Integer rowId);

    public abstract Transaction getTransaction();

    public abstract void setTransaction(Transaction transaction);

}