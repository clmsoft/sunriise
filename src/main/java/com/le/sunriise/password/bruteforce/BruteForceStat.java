/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password.bruteforce;

import java.math.BigInteger;

public class BruteForceStat {
    private BigInteger count = BigInteger.ZERO;
    private BigInteger percentage = BigInteger.ZERO;
    private BigInteger seconds = BigInteger.ZERO;
    private String currentResult = null;
    private int[] currentCursorIndex = null;

    public BigInteger getPercentage() {
        return percentage;
    }

    public void setPercentage(BigInteger percentage) {
        this.percentage = percentage;
    }

    public BigInteger getCount() {
        return count;
    }

    public void setCount(BigInteger count) {
        this.count = count;
    }

    public BigInteger getSeconds() {
        return seconds;
    }

    public void setSeconds(BigInteger seconds) {
        this.seconds = seconds;
    }

    public String getCurrentResult() {
        return currentResult;
    }

    public void setCurrentResult(String currentResult) {
        this.currentResult = currentResult;
    }

    public int[] getCurrentCursorIndex() {
        return currentCursorIndex;
    }

    public void setCurrentCursorIndex(int[] currentCursorIndex) {
        this.currentCursorIndex = currentCursorIndex;
    }

}
