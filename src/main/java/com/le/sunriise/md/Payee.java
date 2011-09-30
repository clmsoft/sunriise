package com.le.sunriise.md;

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
}