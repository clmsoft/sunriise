package com.le.sunriise.export;

import java.awt.Component;

import com.le.sunriise.viewer.OpenedDb;

public interface ExportToContext {

    Component getParentComponent();

    OpenedDb getSrcDb();

}
