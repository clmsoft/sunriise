package com.le.sunriise.accountviewer;

import java.math.BigDecimal;

public class SecurityHolding {
    private Integer id;

    private String name;

    private Double quanity;

    private BigDecimal price;

    private BigDecimal marketValue;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getQuanity() {
        return quanity;
    }

    public void setQuanity(Double quanity) {
        this.quanity = quanity;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public BigDecimal getMarketValue() {
        return marketValue;
    }

    public void setMarketValue(BigDecimal marketValue) {
        this.marketValue = marketValue;
    }
}
