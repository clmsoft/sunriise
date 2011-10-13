package com.le.sunriise.accountviewer;

public class Security extends MnyObject implements Comparable<Security> {
    private Integer id;

    private String name;
    
    private String symbol;
    
    
    @Override
    public int compareTo(Security o) {
        return getId().compareTo(o.getId());
    }

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

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

}
