package com.le.sunriise.mnyobject;

public interface InvestmentTransaction {

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract Double getPrice();

    public abstract void setPrice(Double price);

    public abstract Double getQuantity();

    public abstract void setQuantity(Double quantity);

}