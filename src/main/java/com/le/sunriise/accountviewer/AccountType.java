package com.le.sunriise.accountviewer;

public enum AccountType {
    BANKING(0), CREDIT_CARD(1), CASH(2), ASSET(3), LIABILITY(4), INVESTMENT(5), LOAN(6), UNKNOWN(-1);

    private final int mnyType;

    AccountType(int mnyType) {
        this.mnyType = mnyType;
    }

    public static AccountType toAccountType(int mnyType) {
        switch (mnyType) {
        case 0:
            return BANKING;
        case 1:
            return CREDIT_CARD;
        case 2:
            return CASH;
        case 3:
            return ASSET;
        case 4:
            return LIABILITY;
        case 5:
            return INVESTMENT;
        case 6:
            return LOAN;
        default:
            return UNKNOWN;
        }
    }
}
