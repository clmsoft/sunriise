package com.le.sunriise.md;

public class Category extends MnyObject implements Comparable<Category>{
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer classificationId;
    
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parent) {
        this.parentId = parent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(Category o) {
        return getId().compareTo(o.getId());
    }

    public Integer getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(Integer classificationId) {
        this.classificationId = classificationId;
    }
}
