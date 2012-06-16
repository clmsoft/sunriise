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
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;

public abstract class AbstractBackgroundCommand implements ActionListener {
    private static final Logger log = Logger.getLogger(StartBruteForceSearchAction.class);

    private AtomicBoolean running = new AtomicBoolean(false);

    protected JButton button;

    private StopWatch stopWatch = null;

    public AbstractBackgroundCommand(JButton button) {
        super();
        this.button = button;
    }

    public AbstractBackgroundCommand() {
        super();
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        if (!isRunning()) {
            startBackground();
        } else {
            notifyBackgroundToStop();
        }
    }

    protected void startBackground() {
        log.info("Got START request.");

        if (!validate()) {
            log.warn("Failed to validate.");
            return;
        }
        Runnable command = createCommand();

        preExecute();
        executeCommand(command);
    }

    protected void notifyBackgroundToStop() {
        log.info("Got STOP request.");
    }

    protected abstract boolean validate();

    protected abstract void executeCommand(Runnable command);

    protected abstract Runnable createCommand();

    protected abstract void setStatus(String status);

    protected void preExecute() {
        toRunningState();
    }

    protected void toRunningState() {
        if (button != null) {
            button.setText("Stop");
        }
        this.getRunning().getAndSet(true);
        stopWatch = new StopWatch();
    }

    protected void toIdleState() {
        this.getRunning().getAndSet(false);
        if (button != null) {
            button.setText("Start");
        }
        stopWatch = null;
    }

    public boolean isRunning() {
        return this.getRunning().get();
    }

    public AtomicBoolean getRunning() {
        return running;
    }

    public void setRunning(AtomicBoolean running) {
        this.running = running;
    }

    protected void notifyResult(final Component parentComponent, final String result) {
        log.info("matchedPassword=" + result);
        Runnable doRun = new Runnable() {
            @Override
            public void run() {
                if (result == null) {
                    JOptionPane.showMessageDialog(parentComponent, "Result of last search: NO password found.", "Search Result",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    textArea.setColumns(30);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.append("Result of last search:\n");
                    textArea.append("found password=" + result);
                    textArea.setSize(textArea.getPreferredSize().width, 1);
                    JOptionPane.showMessageDialog(parentComponent, textArea, "Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        SwingUtilities.invokeLater(doRun);
    }

    public long getElapsed() {
        long elapsed = -1L;
        if (stopWatch != null) {
            elapsed = stopWatch.click(false);
        }
        return elapsed;
    }
}