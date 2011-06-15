package com.le.sunriise.model.bean;

import java.util.List;

public class OpenDbDialogDataModel {
    private List<String> recentOpenFileNames;

    public List<String> getRecentOpenFileNames() {
        return recentOpenFileNames;
    }

    public void setRecentOpenFileNames(List<String> recentOpenFileNames) {
        this.recentOpenFileNames = recentOpenFileNames;
    }
}
