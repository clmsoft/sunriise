package com.le.sunriise.mnyobject;

public interface Category {

    public abstract Integer getId();

    public abstract void setId(Integer id);

    public abstract Integer getParentId();

    public abstract void setParentId(Integer parent);

    public abstract String getName();

    public abstract void setName(String name);

    public abstract Integer getClassificationId();

    public abstract void setClassificationId(Integer classificationId);

    public abstract Integer getLevel();

    public abstract void setLevel(Integer level);

}