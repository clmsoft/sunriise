/*******************************************************************************
 * Copyright (c) 2010 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.mnyobject.impl;

import java.util.Map;

import com.le.sunriise.mnyobject.Category;
import com.le.sunriise.mnyobject.MnyObject;

public class CategoryImpl extends MnyObject implements Comparable<CategoryImpl>, Category {
    private Integer id;
    private Integer parentId;
    private String name;
    private Integer classificationId;
    private Integer level;

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#getId()
     */
    @Override
    public Integer getId() {
        return id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#setId(java.lang.Integer)
     */
    @Override
    public void setId(Integer id) {
        this.id = id;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#getParentId()
     */
    @Override
    public Integer getParentId() {
        return parentId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#setParentId(java.lang.Integer)
     */
    @Override
    public void setParentId(Integer parent) {
        this.parentId = parent;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#getName()
     */
    @Override
    public String getName() {
        return name;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#setName(java.lang.String)
     */
    @Override
    public void setName(String name) {
        this.name = name;
    }

    @Override
    public int compareTo(CategoryImpl o) {
        return getId().compareTo(o.getId());
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#getClassificationId()
     */
    @Override
    public Integer getClassificationId() {
        return classificationId;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * com.le.sunriise.mnyobject.Category#setClassificationId(java.lang.Integer)
     */
    @Override
    public void setClassificationId(Integer classificationId) {
        this.classificationId = classificationId;
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

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#getLevel()
     */
    @Override
    public Integer getLevel() {
        return level;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.le.sunriise.mnyobject.Category#setLevel(java.lang.Integer)
     */
    @Override
    public void setLevel(Integer level) {
        this.level = level;
    }
}
