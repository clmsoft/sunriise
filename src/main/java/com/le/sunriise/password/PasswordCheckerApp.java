package com.le.sunriise.password;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JButton;
import javax.swing.JSpinner;
import javax.swing.SwingConstants;
import javax.swing.JPanel;

public class PasswordCheckerApp {
    private static final Logger log = Logger.getLogger(PasswordCheckerApp.class);
    
    private JFrame frame;
    private JTextField textField;
    private JTextField textField_1;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    PasswordCheckerApp window = new PasswordCheckerApp();
                    window.frame.setVisible(true);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public PasswordCheckerApp() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        JPanel view = new JPanel();
        frame.getContentPane().add(view);
        
       view.setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.UNRELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.UNRELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,}));
        
        JLabel lblNewLabel = new JLabel("Money file");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel, "2, 2, right, default");
        
        textField = new JTextField();
        view.add(textField, "4, 2, fill, default");
        textField.setColumns(10);
        
        JButton btnNewButton = new JButton("Open ...");
        view.add(btnNewButton, "6, 2");
        
        JLabel lblNewLabel_1 = new JLabel("Word list");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel_1, "2, 4, right, default");
        
        textField_1 = new JTextField();
        view.add(textField_1, "4, 4, fill, default");
        textField_1.setColumns(10);
        
        JButton btnNewButton_1 = new JButton("Open ...");
        view.add(btnNewButton_1, "6, 4");
        
        JLabel lblNewLabel_2 = new JLabel("Threads");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel_2, "2, 6");
        
        JSpinner spinner = new JSpinner();
        view.add(spinner, "4, 6");
    }

}
