package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public abstract class OpenDbAction implements ActionListener {
    private static final Logger log = Logger.getLogger(OpenDbAction.class);
    
    private Component locationRelativeTo;

    private Preferences prefs;

    private OpenedDb openedDb;

    private boolean disableReadOnlyCheckBox = false;
    
    public OpenDbAction(Component locationRelativeTo, Preferences prefs, OpenedDb openedDb) {
        super();
        this.locationRelativeTo = locationRelativeTo;
        this.prefs = prefs;
        this.openedDb = openedDb;
    }

    
    @Override
    public void actionPerformed(ActionEvent event) {
        Component locationRelativeTo = getLocationRelativeTo();
        openDb(locationRelativeTo);
    }

    // private Component getLocationRelativeToXX(ActionEvent event) {
    // Component locationRelativeTo = null;
    // Component component = (Component) event.getSource();
    // locationRelativeTo = JOptionPane.getFrameForComponent(component);
    // locationRelativeTo = MnyViewer.this.getFrame();
    // return locationRelativeTo;
    // }

    private void openDb(Component locationRelativeTo) {
        List<String> recentOpenFileNames = OpenDbDialog.getRecentOpenFileNames(prefs);

        OpenDbDialog dialog = OpenDbDialog.showDialog(openedDb, recentOpenFileNames, locationRelativeTo, disableReadOnlyCheckBox);
        if (!dialog.isCancel()) {
            // setDb(dialog.getDb());
            // dbFile = dialog.getDbFile();
            openedDb = dialog.getOpenedDb();
            
            dbFileOpened(openedDb, dialog);

            OpenDbDialog.updateRecentOpenFileNames(recentOpenFileNames, prefs);
        }
    }

    public abstract void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog);

    public Component getLocationRelativeTo() {
        return locationRelativeTo;
    }

    public void setLocationRelativeTo(Component locationRelativeTo) {
        this.locationRelativeTo = locationRelativeTo;
    }

    public Preferences getPrefs() {
        return prefs;
    }

    public void setPrefs(Preferences prefs) {
        this.prefs = prefs;
    }

    public boolean isDisableReadOnlyCheckBox() {
        return disableReadOnlyCheckBox;
    }

    public void setDisableReadOnlyCheckBox(boolean disableReadOnlyCheckBox) {
        this.disableReadOnlyCheckBox = disableReadOnlyCheckBox;
    }
}