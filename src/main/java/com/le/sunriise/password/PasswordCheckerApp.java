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
package com.le.sunriise.password;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.model.bean.PasswordCheckerModel;

public class PasswordCheckerApp {
    private static final Logger log = Logger.getLogger(PasswordCheckerApp.class);

    private JFrame frame;
    private JTextField textField;
    private JTextField textField_1;

    private PasswordCheckerModel dataModel = new PasswordCheckerModel();
    private JSpinner spinner;

    private final class StartSearchAction implements ActionListener {
        public void actionPerformed(ActionEvent e) {
            log.info(dataModel.getMnyFileName());
            log.info(dataModel.getWordListPath());
            log.info(dataModel.getThreads());
        }
    }

    private final class OpenMnyAction implements ActionListener {
        private JFileChooser fc = null;
        private JTextField textField;

        public OpenMnyAction(JTextField textField) {
            super();
            this.textField = textField;
            fc = new JFileChooser(new File("."));
            FileFilter filter = new FileFilter() {

                @Override
                public boolean accept(File f) {
                    if (f.isDirectory()) {
                        return true;
                    }
                    
                    String name = f.getName();
                    if (name.endsWith(".mny")) {
                        return true;
                    }
                    
                    return false;
                }

                @Override
                public String getDescription() {
                    String description = "*.mny - Money file";
                    return description;
                }
                
            };
            fc.addChoosableFileFilter(filter);
        }

        public void actionPerformed(ActionEvent e) {
            Component parent = frame;
            if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File path = fc.getSelectedFile();
            log.info("path=" + path);
            String str = path.getAbsolutePath();
            dataModel.setMnyFileName(str);
            if (textField != null) {
                textField.setCaretPosition(str.length());
            }
        }
    }

    private final class OpenWordListAction implements ActionListener {
        private JFileChooser fc = null;
        private JTextField textField;

        public OpenWordListAction(JTextField textField) {
            super();
            this.textField = textField;
            fc = new JFileChooser(new File("."));
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }

        public void actionPerformed(ActionEvent e) {
            Component parent = frame;
            if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File path = fc.getSelectedFile();
            log.info("path=" + path);
            String str = path.getAbsolutePath();
            dataModel.setWordListPath(str);
            if (textField != null) {
                textField.setCaretPosition(str.length());
            }
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
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

        JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        JPanel view = new JPanel();
        tabbedPane.addTab("Using word list", null, view, null);

        view.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        JLabel lblNewLabel = new JLabel("Money file");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel, "2, 2, right, default");

        textField = new JTextField();
        view.add(textField, "4, 2, fill, default");
        textField.setColumns(10);

        JButton btnNewButton = new JButton("Open ...");
        btnNewButton.addActionListener(new OpenMnyAction(textField));
        view.add(btnNewButton, "6, 2");

        JLabel lblNewLabel_1 = new JLabel("Word list");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel_1, "2, 4, right, default");

        textField_1 = new JTextField();
        view.add(textField_1, "4, 4, fill, default");
        textField_1.setColumns(10);

        JButton btnNewButton_1 = new JButton("Open ...");
        btnNewButton_1.addActionListener(new OpenWordListAction(textField_1));
        view.add(btnNewButton_1, "6, 4");

        JLabel lblNewLabel_2 = new JLabel("Threads");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.TRAILING);
        view.add(lblNewLabel_2, "2, 6");

        spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(new Integer(1), null, null, new Integer(1)));
        view.add(spinner, "4, 6");

        JButton btnNewButton_2 = new JButton("Start search");
        btnNewButton_2.addActionListener(new StartSearchAction());
        view.add(btnNewButton_2, "6, 8");

        JPanel panel = new JPanel();
        tabbedPane.addTab("Brute force", null, panel, null);
        initDataBindings();
    }

    protected void initDataBindings() {
        BeanProperty<PasswordCheckerModel, String> passwordCheckerModelBeanProperty = BeanProperty.create("mnyFileName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty = BeanProperty.create("text");
        AutoBinding<PasswordCheckerModel, String, JTextField, String> autoBinding = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, dataModel, passwordCheckerModelBeanProperty, textField, jTextFieldBeanProperty);
        autoBinding.bind();
        //
        BeanProperty<PasswordCheckerModel, String> passwordCheckerModelBeanProperty_1 = BeanProperty.create("wordListPath");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<PasswordCheckerModel, String, JTextField, String> autoBinding_1 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, dataModel, passwordCheckerModelBeanProperty_1, textField_1, jTextFieldBeanProperty_1);
        autoBinding_1.bind();
        //
        BeanProperty<PasswordCheckerModel, Integer> passwordCheckerModelBeanProperty_2 = BeanProperty.create("threads");
        BeanProperty<JSpinner, Object> jSpinnerBeanProperty = BeanProperty.create("value");
        AutoBinding<PasswordCheckerModel, Integer, JSpinner, Object> autoBinding_2 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, dataModel, passwordCheckerModelBeanProperty_2, spinner, jSpinnerBeanProperty);
        autoBinding_2.bind();
    }
}
