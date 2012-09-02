package com.le.sunriise.viewer;

public class DiffData {

    private String key;

    private Object value1;

    private Object value2;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public Object getValue1() {
        return value1;
    }

    public void setValue1(Object value1) {
        this.value1 = value1;
    }

    public Object getValue2() {
        return value2;
    }

    public void setValue2(Object value2) {
        this.value2 = value2;
    }

    public DiffData(String key, Object value1, Object value2) {
        this.key = key;
        this.value1 = value1;
        this.value2 = value2;
    }
}
