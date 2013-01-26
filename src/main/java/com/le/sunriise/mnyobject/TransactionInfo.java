package com.le.sunriise.mnyobject;

public interface TransactionInfo {

    public abstract Integer getFlag();

    public abstract void setFlag(Integer flag);

    public abstract boolean isTransfer();

    public abstract boolean isTransferTo();

    public abstract boolean isInvestment();

    public abstract boolean isSplitParent();

    public abstract boolean isSplitChild();

    public abstract boolean isVoid();

}