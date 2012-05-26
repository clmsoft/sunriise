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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.apache.log4j.Logger;

import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public final class UpdateFxDialog extends OpenDbDialog {
    private static final Logger log = Logger.getLogger(UpdateFxDialog.class);

    UpdateFxDialog(OpenedDb openedDb, List<String> recentOpenFileNames) {
        super(openedDb, "Update FX", recentOpenFileNames);
        getOkButton().setText("Update FX");
    }

    @Override
    protected boolean preHideDialog() {
        log.info("> preHideDialog, isCancel=" + isCancel());
        if (isCancel()) {
            return true;
        }

        String fxFileName = "fx.csv";
        File fxFile = new File(fxFileName);
        if (!fxFile.exists()) {
            JOptionPane.showMessageDialog(UpdateFxDialog.this, "Cannot find file " + fxFileName, "Missing file", JOptionPane.ERROR_MESSAGE);
            return false;
        }
        FxTable fxTable = new FxTable();
        try {
            fxTable.load(fxFile);
        } catch (IOException e) {
            String message = "Exception:\n" + e.toString();
            JOptionPane.showMessageDialog(UpdateFxDialog.this, message, "Cannot open " + fxFileName, JOptionPane.ERROR_MESSAGE);
            return false;
        }
        final List<String> messages = new ArrayList<String>();
        UpdateExchangeRates updater = new UpdateExchangeRates() {
            @Override
            protected void notifyUpdateExistingRate(Double rate, String hcrncFromStr, String hcrncFromName, String hcrncToStr, String hcrncToName,
                    Double newRate) {
                super.notifyUpdateExistingRate(rate, hcrncFromStr, hcrncFromName, hcrncToStr, hcrncToName, newRate);
                messages.add(hcrncFromStr + " -> " + hcrncToStr + ", " + newRate);
            }
        };
        try {
            updater.update(fxTable, getOpenedDb().getDb());
        } catch (Exception e) {
            String message = "Exception:\n" + e.toString();
            JOptionPane.showMessageDialog(UpdateFxDialog.this, message, "Failed to update", JOptionPane.ERROR_MESSAGE);
            return false;
        }

        StringBuilder dialogMessage = new StringBuilder();
        int max = 5;
        int i = 0;
        for (String message : messages) {
            dialogMessage.append(message + "\n");
            i++;
            if (i >= max) {
                dialogMessage.append("..." + "\n");
                break;
            }
        }
        JOptionPane.showMessageDialog(UpdateFxDialog.this, "Update succesfully:\n" + dialogMessage, "Update succesfully", JOptionPane.PLAIN_MESSAGE);

        return true;
    }
}