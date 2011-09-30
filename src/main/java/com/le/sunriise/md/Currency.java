package com.le.sunriise.md;

import java.util.Map;

public class Currency extends MnyObject implements Comparable<Currency> {
    private Integer id;

    private String name;

    private String isoCode;

    @Override
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

    public static String getName(Map<Integer, Currency> currencies, Integer currencyId) {
        Currency currency = currencies.get(currencyId);
        if (currency != null) {
            return currency.getIsoCode();
        } else {
            return null;
        }
    }

}