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
