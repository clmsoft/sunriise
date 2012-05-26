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