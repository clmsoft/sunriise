package com.le.sunriise.accountviewer;

import java.util.Map;

public class Category extends MnyObject implements Comparable<Category> {
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer classificationId;
    private Integer level;

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

    
    public int compareTo(Category o) {
        return getId().compareTo(o.getId());
    }

    public Integer getClassificationId() {
        return classificationId;
    }

    public void setClassificationId(Integer classificationId) {
        this.classificationId = classificationId;
    }

    public static String getCategoryName(Integer categoryId, Map<Integer, Category> categories) {
        String sep = ":";
        int depth = 0;
        int maxDepth = 2;
        return getCategoryName(categoryId, categories, sep, depth, maxDepth);
    }

    public static String getCategoryName(Integer categoryId, Map<Integer, Category> categories, String sep, int depth, int maxDepth) {
        String categoryName = null;
        if (categoryId != null) {
            if (categories != null) {
                Category category = categories.get(categoryId);
                if ((category != null) && (category.getLevel() > 0)) {
                    Integer parentId = category.getParentId();
                    categoryName = category.getName();
                    depth = depth + 1;
                    if ((parentId != null) && ((maxDepth > 0) && (depth < maxDepth))) {
                        String parentName = getCategoryName(parentId, categories, sep, depth, maxDepth);
                        if (parentName != null) {
                            categoryName = parentName + sep + categoryName;
                        }
                    }
                }
            }
        }
        return categoryName;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }
}
