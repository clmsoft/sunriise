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

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.ProgressMonitor;
import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

import com.le.sunriise.export.ExportToCsv;

public class ExportToCsvAction implements ActionListener {
    private static final Logger log = Logger.getLogger(ExportToCsvAction.class);
    
    /**
     * 
     */
    private final MynViewer mnyViewer;

    private JFileChooser fc = null;

    /**
     * @param mnyViewer
     */
    public ExportToCsvAction(MynViewer mnyViewer) {
        this.mnyViewer = mnyViewer;
    }

    private final class ExportToCsvTask implements Runnable {
        private final Component source;
        private final File dir;
        private final ProgressMonitor progressMonitor;

        private ExportToCsvTask(Component source, File dir, ProgressMonitor progressMonitor) {
            this.source = source;
            this.dir = dir;
            this.progressMonitor = progressMonitor;
        }

        
        @Override
        public void run() {
            Exception exception = null;
            try {
                ExportToCsv exporter = new ExportToCsv() {
                    private int maxTables = 0;

                    
                    @Override
                    protected void startExport(File outDir) {
                        super.startExport(outDir);
                        SwingUtilities.invokeLater(new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setProgress(progressMonitor.getMinimum());
                            }
                        });
                    }

                    
                    @Override
                    protected void endExport(File outDir) {
                        super.endExport(outDir);
                        SwingUtilities.invokeLater(new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setProgress(progressMonitor.getMaximum());
                            }
                        });
                    }

                    
                    @Override
                    protected void startExportTables(int size) {
                        super.startExportTables(size);
                        maxTables = size;
                    }

                    
                    @Override
                    protected boolean exportedTable(final String tableName, final int count) {
                        super.exportedTable(tableName, count);
                        if (progressMonitor.isCanceled()) {
                            return false;
                        }
                        SwingUtilities.invokeLater(new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setNote("Table: " + tableName);
                                progressMonitor.setProgress((count * 100) / maxTables);
                            }
                        });
                        return true;
                    }

                    
                    @Override
                    protected void endExportTables(int count) {
                        super.endExportTables(count);
                    }

                };
                exporter.setOpenedDb(ExportToCsvAction.this.mnyViewer.getOpenedDb());
                exporter.writeToDir(dir);
            } catch (IOException e) {
                log.error(e);
                exception = e;
            } finally {
                final Exception exception2 = exception;
                Runnable swingRun = new Runnable() {
                    
                    @Override
                    public void run() {
                        if (exception2 != null) {
                            Component parentComponent = source;
                            String message = exception2.toString();
                            String title = "Error export to CSV file";
                            JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
                        }
                        if (source != null) {
                            source.setEnabled(true);
                        }
                    }
                };
                SwingUtilities.invokeLater(swingRun);
                log.info("< Export CSV DONE");
            }
        }
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
        final File dir = fc.getSelectedFile();
        log.info("Export as CSV to directory=" + dir);
        if (source != null) {
            source.setEnabled(false);
        }
        Component parentComponent = this.mnyViewer.getFrame();
        Object message = "Exporting to CSV files ...";
        int min = 0;
        int max = 100;
        String note = "";

        final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, message, note, min, max);
        progressMonitor.setProgress(0);

        Runnable command = new ExportToCsvTask(source, dir, progressMonitor);
        MynViewer.getThreadPool().execute(command);
    }
}