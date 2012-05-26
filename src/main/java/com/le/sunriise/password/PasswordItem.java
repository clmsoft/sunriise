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
package com.le.sunriise.password;

public class PasswordItem {
    private final String password;

    private final boolean endOfWork;

    public PasswordItem(String password, boolean endOfWork) {
        super();
        this.password = password;
        this.endOfWork = endOfWork;
    }

    public PasswordItem(String line) {
        this(line, false);
    }

    public PasswordItem(boolean b) {
        this(null, b);
    }

    public String getPassword() {
        return password;
    }

    public boolean isEndOfWork() {
        return endOfWork;
    }
}
