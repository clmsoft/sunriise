package com.le.sunriise.mnyobject;

public interface Frequency {

    public abstract Integer getGrftt();

    public abstract void setGrftt(Integer grftt);

    public abstract Integer getFrq();

    public abstract void setFrq(Integer frq);

    public abstract Double getcFrqInst();

    public abstract void setcFrqInst(Double cFrqInst);

    public abstract boolean isRecurring();

    public abstract String getFrequencyString();

}