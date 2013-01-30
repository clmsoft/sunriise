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

import java.math.BigDecimal;

import com.le.sunriise.mnyobject.Security;
import com.le.sunriise.mnyobject.SecurityHolding;

public class SecurityHoldingImpl implements SecurityHolding {
    private Security security;

    private Double quantity;

    private BigDecimal price;

    private BigDecimal marketValue;

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.SecurityHolding#getQuanity()
     */
    @Override
    public Double getQuantity() {
        return quantity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.SecurityHolding#setQuanity(java.lang.Double)
     */
    @Override
    public void setQuantity(Double quanity) {
        this.quantity = quanity;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.SecurityHolding#getPrice()
     */
    @Override
    public BigDecimal getPrice() {
        return price;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.SecurityHolding#setPrice(java.math.BigDecimal)
     */
    @Override
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.SecurityHolding#getMarketValue()
     */
    @Override
    public BigDecimal getMarketValue() {
        return marketValue;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.SecurityHolding#setMarketValue(java.math.BigDecimal
     * )
     */
    @Override
    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }

    @Override
    public Security getSecurity() {
        return security;
    }

    @Override
    public void setSecurity(Security security) {
        this.security = security;
    }
}
