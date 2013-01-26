package com.le.sunriise.mnyobject;

public interface Payee {

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract Integer getParent();

    public abstract void setParent(Integer parent);

    public abstract String getName();

    public abstract void setName(String name);

}