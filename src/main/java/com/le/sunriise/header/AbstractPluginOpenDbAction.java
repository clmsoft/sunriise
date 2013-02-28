package com.le.sunriise.header;

import java.awt.Component;
import java.io.File;
import java.util.prefs.Preferences;

import com.le.sunriise.viewer.CreateOpenedDbPlugin;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public abstract class AbstractPluginOpenDbAction extends OpenDbAction {

    public AbstractPluginOpenDbAction(Component locationRelativeTo, Preferences prefs, OpenedDb openedDb) {
        super(locationRelativeTo, prefs, openedDb);
        setDisableReadOnlyCheckBox(true);
        setPlugin(createPlugin());
    }

    protected CreateOpenedDbPlugin createPlugin() {
        return new CreateOpenedDbPlugin() {
            @Override
            public OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted) {
                OpenedDb openedDb = null;

                openedDb = new OpenedDb();
                openedDb.setDbFile(new File(dbFileName));
                String password = null;
                if ((passwordChars != null) && (passwordChars.length > 0)) {
                    password = new String(passwordChars);
                }
                openedDb.setPassword(password);

                return openedDb;
            }
        };
    }

    @Override
    public void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog) {
        dbFileOpened(newOpenedDb);
    }

    protected abstract void dbFileOpened(OpenedDb newOpenedDb);
}