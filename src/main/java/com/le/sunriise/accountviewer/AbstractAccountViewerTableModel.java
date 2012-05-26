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
package com.le.sunriise.accountviewer;

import javax.swing.table.AbstractTableModel;

public abstract class AbstractAccountViewerTableModel extends AbstractTableModel {

    private Account account;
    private MnyContext mnyContext;

    public AbstractAccountViewerTableModel(Account account) {
        this.account = account;
    }

    public Account getAccount() {
        return account;
    }

    public MnyContext getMnyContext() {
        return mnyContext;
    }

    public void setMnyContext(MnyContext mnyContext) {
        this.mnyContext = mnyContext;
    }
}
