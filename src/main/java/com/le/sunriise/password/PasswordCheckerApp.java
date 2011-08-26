package com.le.sunriise.password;

import java.awt.Component;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.apache.log4j.Logger;

public class PasswordCheckerApp extends JFrame {
    private static final Logger log = Logger.getLogger(PasswordCheckerApp.class);

    protected void showMainView() {
        pack();
    
        setLocationRelativeTo(null);
    
        setVisible(true);
    }

    /**
     * Create the application.
     */
    public PasswordCheckerApp(String title) {
        super(title);
    
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    
        getContentPane().add(createMainView());
    }

    private Component createMainView() {
        JPanel view = new JPanel();
        return view;
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    String title = "PasswordChecker";
                    PasswordCheckerApp window = new PasswordCheckerApp(title);
                    window.showMainView();
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        });
    }

}
