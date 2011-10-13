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

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.export.ExportToMdb;

public class ExportToMdbAction implements ActionListener {
    private static final Logger log = Logger.getLogger(ExportToMdbAction.class);
    
    /**
     * 
     */
    private final MynViewer mnyViewer;

    private JFileChooser fc = null;

    /**
     * @param mnyViewer
     */
    public ExportToMdbAction(MynViewer mnyViewer) {
        this.mnyViewer = mnyViewer;
    }

    private final class ExportToMdbTask implements Runnable {
        private final ProgressMonitor progressMonitor;
        private final File destFile;
        private final Component source;

        private ExportToMdbTask(ProgressMonitor progressMonitor, File destFile, Component source) {
            this.progressMonitor = progressMonitor;
            this.destFile = destFile;
            this.source = source;
        }

        
        @Override
        public void run() {
            OpenedDb srcDb = null;
            Database destDb = null;
            Exception exception = null;

            try {
                srcDb = ExportToMdbAction.this.mnyViewer.getOpenedDb();
                ExportToMdb exporter = new ExportToMdb() {
                    private int progressCount = 0;
                    private int maxCount = 0;
                    private String currentTable = null;
                    private int maxRows;

                    
                    @Override
                    protected void startCopyTables(int maxCount) {
                        if (progressMonitor.isCanceled()) {
                            return;
                        }
                        this.maxCount = maxCount;
                        Runnable doRun = new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setProgress(0);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                    }

                    
                    @Override
                    protected void endCopyTables(int count) {
                        Runnable doRun = new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setProgress(100);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                    }

                    
                    @Override
                    protected boolean startCopyTable(String name) {
                        super.startCopyTable(name);

                        if (progressMonitor.isCanceled()) {
                            return false;
                        }
                        this.currentTable = name;
                        Runnable doRun = new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setNote("Table: " + currentTable);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                        return true;
                    }

                    
                    @Override
                    protected void endCopyTable(String name) {
                        progressCount++;
                        Runnable doRun = new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setProgress((progressCount * 100) / maxCount);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                    }

                    
                    @Override
                    protected boolean startAddingRows(int max) {
                        if (progressMonitor.isCanceled()) {
                            return false;
                        }
                        this.maxRows = max;
                        return true;
                    }

                    
                    @Override
                    protected boolean addedRow(int count) {
                        if (progressMonitor.isCanceled()) {
                            return false;
                        }
                        final String str = " (Copying rows: " + ((count * 100) / this.maxRows) + "%" + ")";
                        Runnable doRun = new Runnable() {
                            
                            @Override
                            public void run() {
                                progressMonitor.setNote("Table: " + currentTable + str);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                        return true;
                    }

                    
                    @Override
                    protected void endAddingRows(int count, long delta) {
                        super.endAddingRows(count, delta);
                    }

                };
                destDb = exporter.export(srcDb, destFile);
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
                log.info("< DONE, exported to file=" + destFile);
                final Exception exception2 = exception;
                Runnable doRun = new Runnable() {
                    
                    @Override
                    public void run() {
                        if (exception2 != null) {
                            Component parentComponent = JOptionPane.getFrameForComponent(source);
                            String message = exception2.toString();
                            String title = "Error export to *.mdb file";
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

    
    @Override
    public void actionPerformed(ActionEvent event) {
        final Component source = (Component) event.getSource();
        if (fc == null) {
            fc = new JFileChooser(new File("."));
            fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
        }
        if (fc.showSaveDialog(source) == JFileChooser.CANCEL_OPTION) {
            return;
        }
        final File destFile = fc.getSelectedFile();
        log.info("Export as *.mdb to file=" + destFile);

        source.setEnabled(false);
        Component parentComponent = this.mnyViewer.getFrame();
        Object message = "Exporting to *.mdb ...";
        String note = "";
        int min = 0;
        int max = 100;
        final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, message, note, min, max);
        progressMonitor.setProgress(0);

        Runnable command = new ExportToMdbTask(progressMonitor, destFile, source);
        MynViewer.getThreadPool().execute(command);
    }
}