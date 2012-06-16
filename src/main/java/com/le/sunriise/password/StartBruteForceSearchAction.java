/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password;

import java.awt.Component;
import java.io.File;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.le.sunriise.model.bean.BruteForceCheckerModel;

final class StartBruteForceSearchAction extends AbstractBackgroundCommand {
    private static final Logger log = Logger.getLogger(StartBruteForceSearchAction.class);

    private BruteForceCheckerModel dataModel;
    private final PasswordCheckerApp app;
    private CheckBruteForce checker = null;

    private final class BackgroundTask implements Runnable {
        @Override
        public void run() {
            setStatus("Running ... ");

            String password = null;
            try {
                HeaderPage headerPage = new HeaderPage(new File(dataModel.getMnyFileName()));

                String maskString = dataModel.getMask();
                char[] mask = maskString.toCharArray();
                int passwordLength = -1;
                try {
                    passwordLength = Integer.valueOf(maskString);
                    mask = null;
                } catch (NumberFormatException e) {
                    passwordLength = -1;
                }

                char[] alphabets = null;
                String alphabetsString = dataModel.getAlphabets();
                if ((alphabetsString != null) && alphabetsString.length() > 0) {
                    alphabets = alphabetsString.toCharArray();
                } else {
                    alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
                }

                if (checker != null) {
                    try {
                        checker.shutdown();
                    } finally {
                        checker = null;
                    }
                }
                try {
                    checker = new CheckBruteForce(headerPage, passwordLength, mask, alphabets);
                    checker.check();
                    password = checker.getPassword();
                    log.info("password=" + password);
                    notifyResult(app.getFrame(), password);
                } finally {
                    if (checker != null) {
                        try {
                            checker.shutdown();
                        } finally {
                            checker = null;
                        }
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            } finally {
                if (button != null) {
                    final String str = password;
                    Runnable doRun = new Runnable() {
                        @Override
                        public void run() {
                            toIdleState();
                            setStatus("Idle - last result " + str);
                        }
                    };
                    SwingUtilities.invokeLater(doRun);
                }
            }
        }
    }

    public StartBruteForceSearchAction(JButton button, PasswordCheckerApp app, BruteForceCheckerModel dataModel) {
        super(button);

        this.app = app;
        this.dataModel = dataModel;
    }

    @Override
    protected boolean validate() {
        String fileName = null;

        fileName = dataModel.getMnyFileName();
        if ((fileName == null) || (fileName.trim().length() <= 0)) {
            Component parentComponent = this.app.getFrame();
            int messageType = JOptionPane.WARNING_MESSAGE;
            String title = "Warning - bad input";
            Object message = new String("Invalid *.mny path=" + fileName);
            JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            return false;
        } else {
            File file = new File(fileName);
            if (!file.exists()) {
                Component parentComponent = this.app.getFrame();
                int messageType = JOptionPane.WARNING_MESSAGE;
                String title = "Warning - bad input";
                Object message = new String("Invalid *.mny path=" + fileName + ".\n File does not exist.");
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                return false;
            }
        }

        String maskString = dataModel.getMask();
        if ((maskString == null) || (maskString.trim().length() <= 0)) {
            Component parentComponent = this.app.getFrame();
            int messageType = JOptionPane.WARNING_MESSAGE;
            String title = "Warning - bad input";
            Object message = new String("Invalid mask value");
            JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            return false;
        }

        return true;
    }

    @Override
    protected Runnable createCommand() {
        Runnable command = new BackgroundTask();
        return command;
    }

    @Override
    protected void executeCommand(Runnable command) {
        this.app.getPool().execute(command);
    }

    @Override
    protected void notifyBackgroundToStop() {
        super.notifyBackgroundToStop();
        if (checker != null) {
            checker.setTerminate(true);
        }
    }

    @Override
    protected void setStatus(String status) {
        if (dataModel == null) {
            return;
        }
        dataModel.setStatus(status);
    }
}