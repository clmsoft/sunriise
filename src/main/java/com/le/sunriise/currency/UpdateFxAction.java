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
package com.le.sunriise.currency;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.JFrame;

import org.apache.log4j.Logger;

import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public class UpdateFxAction implements ActionListener {
    private static final Logger log = Logger.getLogger(UpdateFxAction.class);

    private Component locationRelativeTo;
    private Preferences prefs;

    public UpdateFxAction(JFrame frame, Preferences prefs) {
        this.locationRelativeTo = frame;
        this.prefs = prefs;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        OpenedDb openedDb = null;
        try {
            List<String> recentOpenFileNames = OpenDbDialog.getRecentOpenFileNames(prefs);
            OpenDbDialog dialog = new UpdateFxDialog(openedDb, recentOpenFileNames);
            dialog.getReadOnlyCheckBox().setSelected(false);
            dialog.setHide(false);

            boolean disableReadOnlyCheckBox = true;
            OpenDbDialog.showDialog(dialog, locationRelativeTo, disableReadOnlyCheckBox);

            if (!dialog.isCancel()) {
                // setDb(dialog.getDb());
                // dbFile = dialog.getDbFile();
                openedDb = dialog.getOpenedDb();
                log.info("dbFile=" + openedDb.getDbFile());
                OpenDbDialog.updateRecentOpenFileNames(recentOpenFileNames, prefs);
            }
        } finally {
            if (openedDb != null) {
                openedDb.close();
            }
        }
    }

    public static void updateFx(Preferences prefs) {
        JFrame frame = null;

        UpdateFxAction action = new UpdateFxAction(frame, prefs);
        ActionEvent e = null;
        action.actionPerformed(e);
    }
}