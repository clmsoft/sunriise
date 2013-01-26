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
package com.le.sunriise.mnyobject.impl;

import com.le.sunriise.mnyobject.TransactionInfo;

public class TransactionInfoImpl implements TransactionInfo {
    // TRN.grftt bits
    private Integer flag = 0;

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#getFlag()
     */
    @Override
    public Integer getFlag() {
        return flag;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#setFlag(java.lang.Integer)
     */
    @Override
    public void setFlag(Integer flag) {
        if (flag == null) {
            flag = 0;
        }
        this.flag = flag;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isTransfer()
     */
    @Override
    public boolean isTransfer() {
        // bit 1 == transfer
        int mask = (1 << 1);
        return (flag & mask) == mask;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isTransferTo()
     */
    @Override
    public boolean isTransferTo() {
        // bit 2 == transfer to
        int mask = (1 << 2);
        return (flag & mask) == mask;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isInvestment()
     */
    @Override
    public boolean isInvestment() {
        // bit 4 == investment trn (need to figure out how to tell what
        // kind--other grftt bits?)
        int mask = (1 << 4);
        return (flag & mask) == mask;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isSplitParent()
     */
    @Override
    public boolean isSplitParent() {
        // bit 5 == split parent
        int mask = (1 << 5);
        return (flag & mask) == mask;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isSplitChild()
     */
    @Override
    public boolean isSplitChild() {
        // bit 6 == split child
        int mask = (1 << 6);
        return (flag & mask) == mask;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.TransactionInfo#isVoid()
     */
    @Override
    public boolean isVoid() {
        // bit 8 == void"
        int mask = (1 << 8);
        return (flag & mask) == mask;
    }
}
