package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.Cursor;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javax.swing.SwingUtilities;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;

import com.le.sunriise.StopWatch;

final class MnyTableRowSorter extends TableRowSorter<TableModel> {
    private static final Logger log = Logger.getLogger(MnyTableRowSorter.class);

    private final MynViewer mynViewer;

    private final MnyTableModel tableModel;

    MnyTableRowSorter(MynViewer mynViewer, TableModel model, MnyTableModel tableModel) {
        super(model);
        this.mynViewer = mynViewer;
        this.tableModel = tableModel;
        setMaxSortKeys(1);
    }

    @Override
    public void setSortKeys(final List<? extends SortKey> sortKeys) {
        log.info("> setSortKeys, sortKeys.size=" + sortKeys.size());
        for (SortKey key : sortKeys) {
            log.info("  column=" + key.getColumn() + ", order=" + key.getSortOrder());
        }

        // JOptionPane.showConfirmDialog(frame, "setSortKeys");
        boolean background = true;
        log.info("Background sorting:  " + background);

        if (background) {
            int parties = 2;
            Runnable barrierAction = new Runnable() {
                @Override
                public void run() {
                    log.info("Background sorting thread is DONE.");
                }
            };
            final CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
            Runnable command = new Runnable() {
                @Override
                public void run() {
                    try {
                        parentSetSortKeys(sortKeys);
                        barrier.await();
                    } catch (InterruptedException e) {
                        log.warn(e);
                    } catch (BrokenBarrierException e) {
                        log.warn(e);
                    }
                }
            };

            Component parent = SwingUtilities.getRoot(this.mynViewer.frame);
            Cursor waitCursor = this.mynViewer.setWaitCursor(parent);
            try {
                MynViewer.getThreadPool().execute(command);

                barrier.await();
            } catch (InterruptedException e) {
                log.warn(e);
            } catch (BrokenBarrierException e) {
                log.warn(e);
            } finally {
                this.mynViewer.clearWaitCursor(parent, waitCursor);
            }
        } else {
            Component parent = SwingUtilities.getRoot(this.mynViewer.frame);
            Cursor waitCursor = this.mynViewer.setWaitCursor(parent);
            try {
                parentSetSortKeys(sortKeys);
            } finally {
                this.mynViewer.clearWaitCursor(parent, waitCursor);
            }
        }
    }

    private void parentSetSortKeys(List<? extends SortKey> sortKeys) {
        super.setSortKeys(sortKeys);
    }

    @Override
    public void toggleSortOrder(int column) {
        tableModel.setIsSorting(true);
        StopWatch stopWatch = new StopWatch();
        String columnName = tableModel.getColumnName(column);
        log.info("> toggleSortOrder, count=" + getViewRowCount() + ", column=" + column + ", columnName=" + columnName);
        try {
            // JOptionPane.showConfirmDialog(frame, "Hello");
            this.mynViewer.toggleSortOrderStarted(column);
            super.toggleSortOrder(column);
        } finally {
            tableModel.setIsSorting(false);
            long delta = stopWatch.click();
            this.mynViewer.rightStatusLabel.setText("sort: rows=" + getViewRowCount() + ", millisecond=" + delta);
            log.info("< toggleSortOrder, delta=" + delta);
        }
    }

    @Override
    public void sort() {
        StopWatch stopWatch = new StopWatch();
        log.info("> sort");

        String message = "### STARTING to sort " + " " + this.getViewRowCount() + "/" + this.getModelRowCount()
                + " ... please wait ...";
        log.info(message);

        // JOptionPane.showConfirmDialog(frame, message);

        // rightStatusLabel.setText(message);

        // Component parentComponent = MnyViewer.this.frame;
        // JOptionPane.showConfirmDialog(parentComponent, message);

        // Frame owner = MnyViewer.this.frame;
        // JDialog dialog = new JDialog(owner);
        // JPanel dialogMainView = new JPanel();
        // dialogMainView.setLayout(new BorderLayout());
        // JLabel dialogLabel = new JLabel(message);
        // dialogMainView.add(dialogLabel, BorderLayout.CENTER);
        // dialog.getContentPane().add(dialogMainView);
        // dialog.setModal(false);
        // dialog.pack();
        // dialog.setLocationRelativeTo(null);
        // dialog.show();

        // Component parent = SwingUtilities.getRoot(MynViewer.this.frame);
        // parent = MnyViewer.this.frame.getTopLevelAncestor();
        // Cursor waitCursor = setWaitCursor(parent);
        try {
            super.sort();
        } finally {
            // dialog.dispose();
            // clearWaitCursor(parent, waitCursor);

            final long delta = stopWatch.click();
            log.info("< sort, delta=" + delta);
        }
    }
}