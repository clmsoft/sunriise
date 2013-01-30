package com.le.sunriise.mnyobject.impl;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.mnyobject.Category;

public class CategoryImplUtil {
    private static final String TABLE_NAME = "CAT";

    private static final String COL_ID = "hcat";

    private static final String COL_NAME = "szFull";

    private static final String COL_CLASSIFICATION_ID = "hct";

    private static final String COL_PARENT_ID = "hcatParent";

    private static final String COL_LEVEL = "nLevel";

    public static Map<Integer, Category> getCategories(Database db) throws IOException {
        Map<Integer, Category> categories = new HashMap<Integer, Category>();

        String tableName = TABLE_NAME;
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        try {
            cursor = Cursor.createCursor(table);

            while (cursor.moveToNextRow()) {
                Map<String, Object> row = cursor.getCurrentRow();

                String name = (String) row.get(COL_NAME);
                if (name == null) {
                    continue;
                }
                if (name.length() == 0) {
                    continue;
                }

                addCategory(name, row, categories);
            }
        } finally {

        }

        return categories;
    }

    private static void addCategory(String name, Map<String, Object> row, Map<Integer, Category> categories) {
        Category category = new CategoryImpl();

        Integer hcat = (Integer) row.get(COL_ID);
        category.setId(hcat);

        Integer hcatParent = (Integer) row.get(COL_PARENT_ID);
        category.setParentId(hcatParent);

        category.setName(name);

        Integer hct = (Integer) row.get(COL_CLASSIFICATION_ID);
        category.setClassificationId(hct);

        Integer nLevel = (Integer) row.get(COL_LEVEL);
        category.setLevel(nLevel);

        categories.put(hcat, category);
    }
}
