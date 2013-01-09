package com.le.sunriise.viewer;

import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;

import org.apache.log4j.Logger;

final class MnyRowSorterListener implements RowSorterListener {
    private static final Logger log = Logger.getLogger(MnyRowSorterListener.class);

    private long startTime = -1L;

    @Override
    public void sorterChanged(RowSorterEvent event) {
        log.info("> sorterChanged");
        Type type = event.getType();
        log.info("  " + type);
        switch (type) {
        case SORT_ORDER_CHANGED:
            startTime = System.currentTimeMillis();
            break;
        case SORTED:
            if (startTime > 0) {
                long now = System.currentTimeMillis();
                long delta = now - startTime;
                startTime = -1L;
                log.info("sorterChanged, delta=" + delta);
            }
            break;
        default:
            break;
        }
    }
}