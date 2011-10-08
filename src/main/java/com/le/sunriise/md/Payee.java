package com.le.sunriise.md;

import java.util.Map;

public class Payee extends MnyObject implements Comparable<Payee>{
    private Integer id;

    private Integer parent;

    private String name;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParent() {
        return parent;
    }

    public void setParent(Integer parent) {
        this.parent = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Payee o) {
        return id.compareTo(o.getId());
    }

    static String getPayeeName(Integer payeeId, Map<Integer, Payee> payees) {
        if (payeeId == null) {
            return null;
        }
        if (payeeId < 0) {
            return null;
        }
        Payee payee = payees.get(payeeId);
        if (payee == null) {
            return null;
        }
        return payee.getName();
    }
}
