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

import com.le.sunriise.mnyobject.SecurityHolding;

public class SecurityHoldingImpl implements SecurityHolding {
    private Integer id;

    private String name;

    private Double quanity;

    private BigDecimal price;

    private BigDecimal marketValue;

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#getQuanity()
     */
    @Override
    public Double getQuanity() {
        return quanity;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#setQuanity(java.lang.Double)
     */
    @Override
    public void setQuanity(Double quanity) {
        this.quanity = quanity;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#getPrice()
     */
    @Override
    public BigDecimal getPrice() {
        return price;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#setPrice(java.math.BigDecimal)
     */
    @Override
    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#getMarketValue()
     */
    @Override
    public BigDecimal getMarketValue() {
        return marketValue;
    }

    /* (non-Javadoc)
     * @see com.le.sunriise.mnyobject.SecurityHolding#setMarketValue(java.math.BigDecimal)
     */
    @Override
    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }
}
