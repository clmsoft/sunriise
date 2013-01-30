package com.le.sunriise.mnyobject;

import java.math.BigDecimal;

public interface SecurityHolding {
    public abstract Security getSecurity();
    
    public abstract void setSecurity(Security security);

    public abstract Double getQuantity();

    public abstract void setQuantity(Double quanity);

    public abstract BigDecimal getPrice();

    public abstract void setPrice(BigDecimal price);

    public abstract BigDecimal getMarketValue();

    public abstract void setMarketValue(BigDecimal marketValue);
}