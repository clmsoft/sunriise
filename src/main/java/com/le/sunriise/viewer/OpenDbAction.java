package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import org.apache.log4j.Logger;

public abstract class OpenDbAction implements ActionListener {
    private static final Logger log = Logger.getLogger(OpenDbAction.class);
    
    private Component locationRelativeTo;

    private Preferences prefs;

    private OpenedDb openedDb;

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
        List<String> recentOpenFileNames = getRecentOpenFileNames(prefs);

        OpenDbDialog dialog = OpenDbDialog.showDialog(openedDb, recentOpenFileNames, locationRelativeTo);
        if (!dialog.isCancel()) {
            // setDb(dialog.getDb());
            // dbFile = dialog.getDbFile();
            openedDb = dialog.getOpendDb();
            
            dbFileOpened(openedDb, dialog);

            updateRecentOpenFileNames(recentOpenFileNames, prefs);
        }
    }

    public abstract void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog);

    private void updateRecentOpenFileNames(List<String> recentOpenFileNames, Preferences preferences) {
        int size;
        size = recentOpenFileNames.size();
        size = Math.min(size, 10);
        if (log.isDebugEnabled()) {
            log.debug("prefs: recentOpenFileNames_size=" + size);
        }
        preferences.putInt("recentOpenFileNames_size", size);
        for (int i = 0; i < size; i++) {
            if (log.isDebugEnabled()) {
                log.debug("prefs: recentOpenFileNames_" + i + ", value=" + recentOpenFileNames.get(i));
            }
            preferences.put("recentOpenFileNames_" + i, recentOpenFileNames.get(i));
        }
    }

    private List<String> getRecentOpenFileNames(Preferences preferences) {
        List<String> recentOpenFileNames = new ArrayList<String>();
        int size = preferences.getInt("recentOpenFileNames_size", 0);
        size = Math.min(size, 10);
        for (int i = 0; i < size; i++) {
            String value = prefs.get("recentOpenFileNames_" + i, null);
            if (value != null) {
                recentOpenFileNames.add(value);
            }
        }
        return recentOpenFileNames;
    }

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
}