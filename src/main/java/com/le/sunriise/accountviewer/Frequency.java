package com.le.sunriise.accountviewer;

public class Frequency {
    /*
frq     cFrqInst        grftt   mMemo
-1              0       
0       1       2097152 only once
1       1       2097152 daily
2       1       2097152 weekly
2       0.5     2097152 every other week
3       2       2097152 twice a month
2       0.25    2097152 every four weeks
3       1       2097152 monthly
3       0.5     2097152 every other month
4       1       2097152 every three months
3       0.25    2097152 every four months
5       2       2097152 twice a year
5       1       2097152 yearly
5       0.5     2097152 every other year     
     */
    private Integer frq;

    private Double cFrqInst;

    public Integer getFrq() {
        return frq;
    }

    public void setFrq(Integer frq) {
        this.frq = frq;
    }

    public Double getcFrqInst() {
        return cFrqInst;
    }

    public void setcFrqInst(Double cFrqInst) {
        this.cFrqInst = cFrqInst;
    }

    public boolean isRecurring() {
        Double cFrqInst = getcFrqInst();
        if ((cFrqInst != null) && (cFrqInst > 0.0)) {
            return true;
        }
        return false;
    }
}
