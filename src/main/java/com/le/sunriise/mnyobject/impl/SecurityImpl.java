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
import com.le.sunriise.mnyobject.Security;


public class SecurityImpl extends MnyObject implements Comparable<SecurityImpl>, Security {
    private Integer id;

    private String name;
    
    private String symbol;
    
    
    @Override
    public int compareTo(SecurityImpl o) {
        return getId().compareTo(o.getId());
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#getSymbol()
     */
    @Override
    public String getSymbol() {
        return symbol;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.Security#setSymbol(java.lang.String)
     */
    @Override
    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
