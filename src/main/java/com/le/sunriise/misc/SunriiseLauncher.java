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

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.Box;
import java.awt.Component;

public class SunriiseLauncher {
    private static final Logger log = Logger.getLogger(SunriiseLauncher.class);
    
    private JFrame frmSunriiseLauncher;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    SunriiseLauncher window = new SunriiseLauncher();
                    showMainView(window);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            private void showMainView(SunriiseLauncher window) {
                window.frmSunriiseLauncher.pack();
                window.frmSunriiseLauncher.setLocationRelativeTo(null);
                window.frmSunriiseLauncher.setVisible(true);
            }
        });
    }

    /**
     * Create the application.
     */
    public SunriiseLauncher() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frmSunriiseLauncher = new JFrame();
        frmSunriiseLauncher.setTitle("Sunriise Launcher");
        frmSunriiseLauncher.setBounds(100, 100, 450, 300);
        frmSunriiseLauncher.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frmSunriiseLauncher.getContentPane().setLayout(new BoxLayout(frmSunriiseLauncher.getContentPane(), BoxLayout.X_AXIS));
        
        JButton btnNewButton = new JButton("Account Viewer");
        frmSunriiseLauncher.getContentPane().add(btnNewButton);
        
        Component horizontalStrut = Box.createHorizontalStrut(20);
        frmSunriiseLauncher.getContentPane().add(horizontalStrut);
        
        JButton btnNewButton_1 = new JButton("Table Viewer");
        frmSunriiseLauncher.getContentPane().add(btnNewButton_1);
    }

}
