package com.le.sunriise.quote;

import java.util.Date;

public class PriceInfo {
    private String stockSymbol;

    private Double price;

    private Date date;

    public PriceInfo(String stockSymbol, Double price) {
        super();
        this.stockSymbol = stockSymbol;
        this.price = price;
    }

    public String getStockSymbol() {
        return stockSymbol;
    }

    public void setStockSymbol(String stockSymbol) {
        this.stockSymbol = stockSymbol;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }
}
