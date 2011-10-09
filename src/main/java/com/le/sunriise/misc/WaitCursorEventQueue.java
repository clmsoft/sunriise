package com.le.sunriise.misc;

import java.awt.AWTEvent;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.EventQueue;
import java.awt.MenuComponent;
import java.awt.MenuContainer;
import java.awt.event.MouseEvent;

import javax.swing.SwingUtilities;

import org.apache.log4j.Logger;

public class WaitCursorEventQueue extends EventQueue {
    private static final Logger log = Logger.getLogger(WaitCursorEventQueue.class);
    
    public WaitCursorEventQueue(int delay) {
        this.delay = delay;
        waitTimer = new WaitCursorTimer();
        waitTimer.setDaemon(true);
        waitTimer.start();
    }

    
    @Override
    protected void dispatchEvent(AWTEvent event) {
        MouseEvent mouseEvent = null;
        if (event instanceof MouseEvent) {
            mouseEvent = (MouseEvent) event;
        }
        if ((mouseEvent != null) && (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)) {
            log.info("> dispatchEvent, event=" + event);
        }
        waitTimer.startTimer(event.getSource());
        try {
            super.dispatchEvent(event);
        } finally {
            if ((mouseEvent != null) && (mouseEvent.getID() == MouseEvent.MOUSE_CLICKED)) {
                log.info("< dispatchEvent, event=" + event);
            }
            waitTimer.stopTimer();
        }
    }

    private int delay;
    private WaitCursorTimer waitTimer;

    private class WaitCursorTimer extends Thread {

        synchronized void startTimer(Object source) {
            this.source = source;
            notify();
        }

        synchronized void stopTimer() {
            if (parent == null)
                interrupt();
            else {
                parent.setCursor(null);
                log.info("  YES setCursor to null");
                parent = null;
            }
        }

        
        @Override
        public synchronized void run() {
            while (true) {
                try {
                    // wait for notification from startTimer()
                    wait();

                    // wait for event processing to reach the threshold, or
                    // interruption from stopTimer()
                    wait(delay);
                    log.info(" timer exceeds ...");
                    
                    if (source instanceof Component)
                        parent = SwingUtilities.getRoot((Component) source);
                    else if (source instanceof MenuComponent) {
                        MenuContainer mParent = ((MenuComponent) source).getParent();
                        if (mParent instanceof Component)
                            parent = SwingUtilities.getRoot((Component) mParent);
                    }

                    log.info("parent=" + parent);
                    if (parent != null) {
                        log.info("parent.isShowing=" + parent.isShowing());
                    }
                    if (parent != null && parent.isShowing()) {
                        Cursor waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                        parent.setCursor(waitCursor);
                        log.info("  YES setCursor to waitCursor");
                    } else {
                        log.info("  NO setCursor");
                    }
                } catch (InterruptedException ie) {
                    if (log.isDebugEnabled()) {
                        log.warn(ie);
                    }
                }
            }
        }

        private Object source;
        private Component parent;
    }
}
