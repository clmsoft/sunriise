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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.le.sunriise.model.bean.PasswordCheckerModel;

final class StartDictionarySearchAction implements ActionListener {
    private static final Logger log = Logger.getLogger(StartDictionarySearchAction.class);

    private final PasswordCheckerApp app;
    private JButton button;
    private boolean closeChecker = false;
    private int lastCheckerThreads = 0;
    private PasswordCheckerModel dataModel;
    private AtomicBoolean running = new AtomicBoolean(false);

    public StartDictionarySearchAction(PasswordCheckerApp app, PasswordCheckerModel dataModel, JButton button) {
        this.app = app;
        this.dataModel = dataModel;
        this.button = button;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (this.getRunning().get()) {
            stopCheck();
        } else {
            startCheck();
        }
    }

    private void startCheck() {
        if (!validateInputs()) {
            return;
        }

        if (button != null) {
            button.setText("Stop");
        }
        this.getRunning().getAndSet(true);

        if (this.app.getChecker() != null) {
            if (dataModel.getThreads() > lastCheckerThreads) {
                try {
                    this.app.getChecker().close();
                } finally {
                    this.app.setChecker(null);
                }
            }
        }
        lastCheckerThreads = dataModel.getThreads();
        if (this.app.getChecker() == null) {
            log.info("Created new checker, threads=" + lastCheckerThreads);
            this.app.setChecker(new CheckDictionary(lastCheckerThreads));
        } else {
            this.app.getChecker().getCounter().getAndSet(0);
        }
        AtomicLong counter = this.app.getChecker().getCounter();
        dataModel.setStatus("Running ... searched " + counter.get());

        Runnable command = new Runnable() {
            @Override
            public void run() {
                String matchedPassword = null;
                try {
                    HeaderPage headerPage = new HeaderPage(new File(dataModel.getMnyFileName()));
                    matchedPassword = StartDictionarySearchAction.this.app.getChecker().check(headerPage,
                            new File(dataModel.getWordListPath()));
                    notifyResult(app.getFrame(), matchedPassword);
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    if (closeChecker) {
                        if (StartDictionarySearchAction.this.app.getChecker() != null) {
                            try {
                                StartDictionarySearchAction.this.app.getChecker().close();
                            } finally {
                                StartDictionarySearchAction.this.app.setChecker(null);
                            }
                        }
                    }
                    StartDictionarySearchAction.this.getRunning().getAndSet(false);

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
            }
        };
        this.app.getPool().execute(command);
    }

    private boolean validateInputs() {
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

    private void stopCheck() {
        log.info("Got STOP request.");
        if (this.app.getChecker() != null) {
            this.app.getChecker().stop();
        }
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public void setRunning(AtomicBoolean running) {
        this.running = running;
    }

    protected void notifyResult(final Component parentComponent, final String matchedPassword) {
        log.info("matchedPassword=" + matchedPassword);
        Runnable doRun = new Runnable() {
            @Override
            public void run() {
//                Component parentComponent = app.getFrame();
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
        SwingUtilities.invokeLater(doRun);
    }
}