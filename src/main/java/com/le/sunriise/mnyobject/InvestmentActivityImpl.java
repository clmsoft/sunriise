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

public class InvestmentActivityImpl {
    public static final int TRANSFER_SHARES_OUT = 33;

    public static final int TRANSFER_SHARES_IN = 32;

    public static final int REINVEST_L_TERM_CG_DIST = 29;

    public static final int REINVEST_S_TERM_CG_DIST = 27;

    public static final int L_TERM_CAP_GAINS_DIST = 26;

    public static final int S_TERM_CAP_GAINS_DIST = 24;

    public static final int ADD_SHARES = 16;

    public static final int REMOVE_SHARES = 13;

    public static final int REINVEST_INTEREST = 10;

    public static final int REINVEST_DIVIDEND = 9;

    public static final int RETURN_OF_CAPITAL = 8;
    
    public static final int INTEREST = 4;
    
    public static final int DIVIDEND = 3;
    
    public static final int SELL = 2;
    
    public static final int BUY = 1;
    
    private final Integer flag;
    
    private String string;

    // Buy: act=1, grftt=18, grftf=0
    // Sell: act=2, grftt=18, grftf=0
    // Dividend: act=3, grftt=18, grftf=0
    // Interest: act=4, grftt=18, grftf=0
    // Return of Capital: act=8
    // Reinvest Dividend: act=9, grftt=16, grftf=0
    // Reinvest Interest: act=10, grftt=16, grftf=0
    // Remove Shares: act=13
    // Add Shares: act=12, grftt=16, grftf=0
    // S-Term Cap Gains Dist: act=24, grftt=18, grftf=0
    // L-Term Cap Gains Dist: act=26, grftt=18, grftf=0
    // Reinvest S-Term CG Dist: act=27, grftt=16, grftf=0
    // Reinvest L-Term CG Dist: act=29, grftt=16, grftf=0
    // Transfer Shares (in): act=32
    // Transfer Shares (out): act=33

    private boolean added = true;

    public InvestmentActivityImpl(Integer flag) {
        super();
        this.flag = flag;
        initialize();
    }

    public Integer getFlag() {
        return flag;
    }

    
    @Override
    public String toString() {
        return string;
    }

    private void initialize() {
        this.added = true;
        String str = "ACTIVITY_UNKNOWN";
        switch (flag) {
        case BUY:
            str = "Buy";
            break;
        case SELL:
            str = "Sell";
            this.added = false;
            break;
        case DIVIDEND:
            str = "Dividend";
            break;
        case INTEREST:
            str = "Interest";
            break;
        case RETURN_OF_CAPITAL:
            str = "Return of Capital";
            break;
        case REINVEST_DIVIDEND:
            str = "Reinvest Dividend";
            break;
        case REINVEST_INTEREST:
            str = "Reinvest Interest";
            break;
        case REMOVE_SHARES:
            str = "Remove Shares";
            this.added = false;
            break;
        case ADD_SHARES:
            str = "Add Shares";
            break;
        case S_TERM_CAP_GAINS_DIST:
            str = "S-Term Cap Gains Dist";
            break;
        case L_TERM_CAP_GAINS_DIST:
            str = "L-Term Cap Gains Dist";
            break;
        case REINVEST_S_TERM_CG_DIST:
            str = "Reinvest S-Term CG Dist";
            break;
        case REINVEST_L_TERM_CG_DIST:
            str = "Reinvest L-Term CG Dist";
            break;
        case TRANSFER_SHARES_IN:
            str = "Transfer Shares (in)";
            break;
        case TRANSFER_SHARES_OUT:
            str = "Transfer Shares (out)";
            this.added = false;
            break;
        default:
            str = "ACTIVITY_UNKNOWN";
            break;
        }

        this.string = str;
    }

    public boolean isAdded() {
        return added;
    }
}
