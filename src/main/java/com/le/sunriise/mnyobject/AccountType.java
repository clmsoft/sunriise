/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.mnyobject;

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
