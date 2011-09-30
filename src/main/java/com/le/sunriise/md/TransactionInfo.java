package com.le.sunriise.md;

public class TransactionInfo {
    // TRN.grftt bits
    private Integer flag = 0;

    public Integer getFlag() {
        return flag;
    }

    public void setFlag(Integer flag) {
        if (flag == null) {
            flag = 0;
        }
        this.flag = flag;
    }

    public boolean isTransfer() {
        // bit 1 == transfer
        int mask = (1 << 1);
        return (flag & mask) == mask;
    }

    public boolean isTransferTo() {
        // bit 2 == transfer to
        int mask = (1 << 2);
        return (flag & mask) == mask;
    }

    public boolean isInvestment() {
        // bit 4 == investment trn (need to figure out how to tell what
        // kind--other grftt bits?)
        int mask = (1 << 4);
        return (flag & mask) == mask;
    }

    public boolean isSplitParent() {
        // bit 5 == split parent
        int mask = (1 << 5);
        return (flag & mask) == mask;
    }

    public boolean isSplitChild() {
        // bit 6 == split child
        int mask = (1 << 6);
        return (flag & mask) == mask;
    }

    public boolean isVoid() {
        // bit 8 == void"
        int mask = (1 << 8);
        return (flag & mask) == mask;
    }
}
