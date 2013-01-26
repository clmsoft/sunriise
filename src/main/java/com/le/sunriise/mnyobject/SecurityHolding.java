package com.le.sunriise.mnyobject;

import java.math.BigDecimal;

public interface SecurityHolding {

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Double getQuanity();

    public abstract void setQuanity(Double quanity);

    public abstract BigDecimal getPrice();

    public abstract void setPrice(BigDecimal price);

    public abstract BigDecimal getMarketValue();

    public abstract void setMarketValue(BigDecimal marketValue);

}