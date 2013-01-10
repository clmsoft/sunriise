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

class RateInfo {
    private String description;

    private String columnName;

    private Double rate;

    public RateInfo(String columnName, String description) {
        super();
        this.columnName = columnName;
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String key) {
        this.description = key;
    }

    public String getColumnName() {
        return columnName;
    }

    public void setColumnName(String value) {
        this.columnName = value;
    }

    public Double getRate() {
        return rate;
    }

    public void setRate(Double rate) {
        this.rate = rate;
    }
}