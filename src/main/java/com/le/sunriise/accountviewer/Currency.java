package com.le.sunriise.accountviewer;

import java.util.Map;

public class Currency extends MnyObject implements Comparable<Currency> {
    private Integer id;

    private String name;

    private String isoCode;

    
    public int compareTo(Currency o) {
        return id.compareTo(o.getId());
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

    public String getIsoCode() {
        return isoCode;
    }

    public void setIsoCode(String isoCode) {
        this.isoCode = isoCode;
    }

    public static String getName(Integer currencyId, Map<Integer, Currency> currencies) {
        Currency currency = currencies.get(currencyId);
        if (currency != null) {
            return currency.getIsoCode();
        } else {
            return null;
        }
    }

}
