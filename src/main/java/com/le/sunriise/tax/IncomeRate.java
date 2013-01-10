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

package com.le.sunriise.tax;

class IncomeRate {
    private Double amountLow;
    private Double amountHigh;
    private Double rate;

    public Double getAmountLow() {
        return amountLow;
    }

    public void setAmountLow(Double amountLow) {
        this.amountLow = amountLow;
    }

    public Double getAmountHigh() {
        return amountHigh;
    }

    public void setAmountHigh(Double amountHigh) {
        this.amountHigh = amountHigh;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();

        sb.append(amountLow);
        sb.append(", ");

        if (amountHigh != null) {
            sb.append(amountHigh);
        } else {
            sb.append("");
        }
        sb.append(", ");

        if (rate != null) {
            sb.append(rate);
        } else {
            sb.append("");
        }
        // sb.append(", ");

        return sb.toString();
    }
}