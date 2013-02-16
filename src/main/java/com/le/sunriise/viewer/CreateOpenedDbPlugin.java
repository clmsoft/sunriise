package com.le.sunriise.viewer;

public interface CreateOpenedDbPlugin {

    OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted);

}
