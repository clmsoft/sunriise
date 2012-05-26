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
package com.le.sunriise.misc;

//Busy JFrame sub-class.
//11/19/98 - Dan Syrstad (dsyrstad@vscorp.com)
//Substitute with your package name
//package your.package.name;
import java.awt.Cursor;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;

import javax.swing.JFrame;
import javax.swing.JPanel;

/** Extension of JFrame to provide a frame &quot;busy&quot; interface */
public class BusyJFrame extends JFrame {
    protected JPanel glass_pane;

    // ----------------------------------------------------------------------
    /**
     * Same as javax.swing.JFrame()
     */
    BusyJFrame() {
        super();
        initBusyJFrame();
    }

    // ----------------------------------------------------------------------
    /**
     * Same as javax.swing.JFrame(title)
     */
    BusyJFrame(String title) {
        super(title);
        initBusyJFrame();
    }

    // ----------------------------------------------------------------------
    /**
     * Do common constructor initialization
     */
    protected void initBusyJFrame() {
//        setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        // Create our own glass pane which says it manages focus. This is
        // part of the key to capturing keyboard events.
        glass_pane = new JPanel() {
            
            @Override
            public boolean isManagingFocus() {
                return true;
            }
        };
        // Add a no-op MouseAdapter so that we enable mouse events
        glass_pane.addMouseListener(new MouseAdapter() {
        });
        // Eat keystrokes so they don't go to other components
        glass_pane.addKeyListener(new KeyAdapter() {
            
            @Override
            public void keyPressed(KeyEvent e) {
                e.consume();
            }
        });
        glass_pane.setOpaque(false);
        this.setGlassPane(glass_pane);
    }

    // ----------------------------------------------------------------------
    /**
     * Sets the wait cursor on the frame and disables components.
     */
    public void setBusy(boolean busy) {
        if (busy) {
            // Setting the frame cursor AND glass pane cursor in this order
            // works around the Win32 problem where you have to move the mouse 1
            // pixel to get the Cursor to change.
            this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            glass_pane.setVisible(true);
            // Force glass pane to get focus so that we consume KeyEvents
            glass_pane.requestFocus();
            glass_pane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            // Turn off the close button
//            setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        } else {
            glass_pane.setCursor(Cursor.getDefaultCursor());
            glass_pane.setVisible(false);
            this.requestFocus();
            this.setCursor(Cursor.getDefaultCursor());
//            setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        }
    }
}
