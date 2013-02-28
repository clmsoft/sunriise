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

import com.le.sunriise.mnyobject.MnyObject;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.mnyobject.TransactionSplit;

public class TransactionSplitImpl extends MnyObject implements TransactionSplit {
    private Integer parentId;

    private Integer rowId;

    private Transaction transaction;

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.TransactionSplit#getParentId()
     */
    @Override
    public Integer getParentId() {
        return parentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.TransactionSplit#setParentId(java.lang.Integer)
     */
    @Override
    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.TransactionSplit#getRowId()
     */
    @Override
    public Integer getRowId() {
        return rowId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.TransactionSplit#setRowId(java.lang.Integer)
     */
    @Override
    public void setRowId(Integer rowId) {
        this.rowId = rowId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.TransactionSplit#getTransaction()
     */
    @Override
    public Transaction getTransaction() {
        return transaction;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.TransactionSplit#setTransaction(com.le.sunriise
     * .mnyobject.TransactionImpl)
     */
    @Override
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
