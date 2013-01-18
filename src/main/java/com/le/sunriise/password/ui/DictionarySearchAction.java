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
package com.le.sunriise.password.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import com.le.sunriise.model.bean.PasswordCheckerModel;

final class DictionarySearchAction extends DictionarySearch implements ActionListener {
    private final PasswordCheckerApp app;
    private final PasswordCheckerModel dataModel;
    private final JButton button;

    public DictionarySearchAction(PasswordCheckerApp app, PasswordCheckerModel dataModel, JButton button) {
        super();
        this.app = app;
        this.dataModel = dataModel;
        this.button = button;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (this.getRunning().get()) {
            stopCheck();
        } else {
            Integer nThreads = dataModel.getThreads();
            File headerPageFile = new File(dataModel.getMnyFileName());
            File candidatesPath = new File(dataModel.getWordListPath());
            startCheck(nThreads, headerPageFile, candidatesPath);
        }
    }

    @Override
    protected boolean validateInputs() {
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
                Object message = new String("Invalid *.mny path=" + fileName);
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                return false;
            }
        }

        fileName = dataModel.getWordListPath();
        if ((fileName == null) || (fileName.trim().length() <= 0)) {
            Component parentComponent = this.app.getFrame();
            int messageType = JOptionPane.WARNING_MESSAGE;
            String title = "Warning - bad input";
            Object message = new String("Invalid wordlist path=" + fileName);
            JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            return false;
        } else {
            File file = new File(fileName);
            if (!file.exists()) {
                Component parentComponent = this.app.getFrame();
                int messageType = JOptionPane.WARNING_MESSAGE;
                String title = "Warning - bad input";
                Object message = new String("Invalid wordlist path=" + fileName);
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                return false;
            }
        }

        return true;
    }

    @Override
    protected void preStart() {
        if (button != null) {
            button.setText("Stop");
        }
    }

    @Override
    protected void postStart(String matchedPassword) {
        if (button != null) {
            final String str = matchedPassword;
            Runnable doRun = new Runnable() {
                @Override
                public void run() {
                    button.setText("Start");
                    dataModel.setStatus("Idle - last result " + str);
                }
            };
            SwingUtilities.invokeLater(doRun);
        }
    }

    @Override
    protected void notifyResult(final String matchedPassword) {
        super.notifyResult(matchedPassword);

        final Component parentComponent = app.getFrame();

        Runnable command = new Runnable() {
            @Override
            public void run() {
                // Component parentComponent = app.getFrame();
                if (matchedPassword == null) {
                    JOptionPane.showMessageDialog(parentComponent, "Result of last search: NO password found.", "Search Result",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    textArea.setColumns(30);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.append("Result of last search:\n");
                    textArea.append("found password=" + matchedPassword);
                    textArea.setSize(textArea.getPreferredSize().width, 1);
                    JOptionPane.showMessageDialog(parentComponent, textArea, "Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        SwingUtilities.invokeLater(command);
    }

    @Override
    protected void logStatus(String message) {
        dataModel.setStatus(message);
    }

    @Override
    protected void runCommand(Runnable command) {
        this.app.getPool().execute(command);
    }
}