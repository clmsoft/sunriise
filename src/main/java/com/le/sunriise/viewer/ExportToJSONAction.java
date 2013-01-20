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
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.export.ExportToContext;
import com.le.sunriise.export.ExportToJSON;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.Transaction;

public class ExportToJSONAction implements ActionListener {
    private static final Logger log = Logger.getLogger(ExportToJSONAction.class);

    /**
     * 
     */
    // private final MynViewer mnyViewer;
//    private Component parentComponent = null;
//    private OpenedDb srcDb = null;
    private ExportToContext exportToContext = null;
    
    private JFileChooser fc = null;

    private final class ExportToJSONTask implements Runnable {
        private final ProgressMonitor progressMonitor;
        private final File destDir;
        private final Component source;

        private ExportToJSONTask(ProgressMonitor progressMonitor, File destDir, Component source) {
            this.progressMonitor = progressMonitor;
            this.destDir = destDir;
            this.source = source;
        }

        @Override
        public void run() {
            // OpenedDb srcDb = null;
            Database destDb = null;
            Exception exception = null;

            try {
                // srcDb = ExportToJSONAction.this.mnyViewer.getOpenedDb();
                ExportToJSON exporter = new ExportToJSON() {
                    private String accountName = "";
                    private int count = 0;
                    private int maxAccounts = 0;

                    @Override
                    public void visitAccounts(List<Account> accounts) throws IOException {
                        maxAccounts = accounts.size();
                        super.visitAccounts(accounts);
                    }

                    @Override
                    protected void startExport(File outDir) {
                        accountName = "";
                        count = 0;
                        maxAccounts = 0;
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                progressMonitor.setProgress(progressMonitor.getMinimum());
                            }
                        });
                        super.startExport(outDir);
                    }

                    @Override
                    protected void endExport(File outDir) {
                        if (progressMonitor.isCanceled()) {
                            return;
                        }
                        SwingUtilities.invokeLater(new Runnable() {

                            @Override
                            public void run() {
                                progressMonitor.setProgress(progressMonitor.getMaximum());
                            }
                        });
                        super.endExport(outDir);
                    }

                    @Override
                    public void visitAccount(Account account) throws IOException {
                        if (progressMonitor.isCanceled()) {
                            return;
                        }
                        accountName = account.getName();
                        count++;
                        SwingUtilities.invokeLater(new Runnable() {
                            @Override
                            public void run() {
                                progressMonitor.setNote("Account: " + accountName);
                                progressMonitor.setProgress((count * 100) / maxAccounts);
                            }
                        });
                        super.visitAccount(account);
                    }

                    @Override
                    public void visitTransaction(Transaction transaction) throws IOException {
                        if (progressMonitor.isCanceled()) {
                            return;
                        }
                        super.visitTransaction(transaction);
                    }
                };
                destDb = exporter.export(exportToContext.getSrcDb(), destDir);
                // XXX - don't close it
                destDb = null;
            } catch (IOException e) {
                log.error(e);
                exception = e;
            } finally {
                if (destDb != null) {
                    try {
                        destDb.close();
                    } catch (IOException e1) {
                        log.warn(e1);
                    } finally {
                        destDb = null;
                    }
                }
                log.info("< DONE, exported to file=" + destDir);
                final Exception exception2 = exception;
                Runnable doRun = new Runnable() {

                    @Override
                    public void run() {
                        if (exception2 != null) {
                            Component parentComponent = JOptionPane.getFrameForComponent(source);
                            String message = exception2.toString();
                            String title = "Error export to *.json file";
                            JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
                        }
                        if (source != null) {
                            source.setEnabled(true);
                        }
                    }
                };
                SwingUtilities.invokeLater(doRun);
            }
        }
    }

    public ExportToJSONAction(ExportToContext exportToContext) {
        this.exportToContext = exportToContext;
    }

    @Override
    public void actionPerformed(ActionEvent event) {
        final Component source = (Component) event.getSource();
        if (fc == null) {
            fc = new JFileChooser(new File("."));
            fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        }
        if (fc.showSaveDialog(source) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File destFile = fc.getSelectedFile();
        log.info("> Export as *.json to file=" + destFile);

        source.setEnabled(false);
        // Component parentComponent = this.mnyViewer.getFrame();
        Object message = "Exporting to *.json ...";
        String note = "";
        int min = 0;
        int max = 100;
        final ProgressMonitor progressMonitor = new ProgressMonitor(exportToContext.getParentComponent(), message, note, min, max);
        progressMonitor.setProgress(0);

        Runnable command = new ExportToJSONTask(progressMonitor, destFile, source);
        MynViewer.getThreadPool().execute(command);
    }
}