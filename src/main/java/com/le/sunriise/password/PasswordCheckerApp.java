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
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
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
import com.le.sunriise.model.bean.BruteForceCheckerModel;
import com.le.sunriise.model.bean.PasswordCheckerModel;

public class PasswordCheckerApp {
    private static final Logger log = Logger.getLogger(PasswordCheckerApp.class);

    private JFrame frame;

    private JTextField textField;

    private JTextField textField_1;

    private PasswordCheckerModel dataModel = new PasswordCheckerModel();

    private BruteForceCheckerModel bruteForceDataModel = new BruteForceCheckerModel();

    private JSpinner spinner;

    private ExecutorService pool = Executors.newCachedThreadPool();
    private ScheduledExecutorService schedulers = Executors.newScheduledThreadPool(2);

    private JTextField textField_2;

    private AtomicBoolean running = new AtomicBoolean(false);
    private CheckDictionary checker = null;
    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    private JTextField textField_6;
    private JTextField textField_7;

    private final class StartBruteForceSearchAction implements ActionListener {
        private JButton button;

        public StartBruteForceSearchAction(JButton button) {
            this.button = button;
        }

        public void actionPerformed(ActionEvent event) {
            log.info(bruteForceDataModel.getMnyFileName());
            log.info(bruteForceDataModel.getAlphabets());
            log.info(bruteForceDataModel.getMask());
        }
    }

    private final class StartSearchAction implements ActionListener {
        private JButton button;
        private boolean closeChecker = false;
        private int lastCheckerThreads = 0;

        public StartSearchAction(JButton button) {
            this.button = button;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            if (running.get()) {
                stopCheck();
            } else {
                startCheck();
            }
        }

        private void startCheck() {
            String fileName = null;

            fileName = dataModel.getMnyFileName();
            if ((fileName == null) || (fileName.trim().length() <= 0)) {
                Component parentComponent = frame;
                int messageType = JOptionPane.WARNING_MESSAGE;
                String title = "Warning - bad input";
                Object message = new String("Invalid *.mny path=" + fileName);
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                return;
            } else {
                File file = new File(fileName);
                if (!file.exists()) {
                    Component parentComponent = frame;
                    int messageType = JOptionPane.WARNING_MESSAGE;
                    String title = "Warning - bad input";
                    Object message = new String("Invalid *.mny path=" + fileName);
                    JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                    return;
                }
            }

            fileName = dataModel.getWordListPath();
            if ((fileName == null) || (fileName.trim().length() <= 0)) {
                Component parentComponent = frame;
                int messageType = JOptionPane.WARNING_MESSAGE;
                String title = "Warning - bad input";
                Object message = new String("Invalid wordlist path=" + fileName);
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                return;
            } else {
                File file = new File(fileName);
                if (!file.exists()) {
                    Component parentComponent = frame;
                    int messageType = JOptionPane.WARNING_MESSAGE;
                    String title = "Warning - bad input";
                    Object message = new String("Invalid wordlist path=" + fileName);
                    JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
                    return;
                }
            }

            if (button != null) {
                button.setText("Stop");
            }
            running.getAndSet(true);

            if (checker != null) {
                if (dataModel.getThreads() > lastCheckerThreads) {
                    try {
                        checker.close();
                    } finally {
                        checker = null;
                    }
                }
            }
            lastCheckerThreads = dataModel.getThreads();
            if (checker == null) {
                log.info("Created new checker, threads=" + lastCheckerThreads);
                checker = new CheckDictionary(lastCheckerThreads);
            } else {
                checker.getCounter().getAndSet(0);
            }
            AtomicLong counter = checker.getCounter();
            dataModel.setStatus("Running ... seached " + counter.get());

            Runnable command = new Runnable() {
                @Override
                public void run() {
                    String matchedPassword = null;
                    try {
                        HeaderPage headerPage = new HeaderPage(new File(dataModel.getMnyFileName()));
                        matchedPassword = checker.check(headerPage, new File(dataModel.getWordListPath()));
                        notifyResult(matchedPassword);
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        if (closeChecker) {
                            if (checker != null) {
                                try {
                                    checker.close();
                                } finally {
                                    checker = null;
                                }
                            }
                        }
                        running.getAndSet(false);

                        if (button != null) {
                            final String str = matchedPassword;
                            Runnable doRun = new Runnable() {
                                @Override
                                public void run() {
                                    button.setText("Start");
                                    dataModel.setStatus("Idle - last result " + str);
                                }
                            };
                            SwingUtilities.invokeLater(doRun);
                        }
                    }
                }
            };
            pool.execute(command);
        }

        private void stopCheck() {
            log.info("Got STOP request.");
            if (checker != null) {
                checker.stop();
            }
        }
    }

    private abstract class OpenMnyAction implements ActionListener {
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

        @Override
        public void actionPerformed(ActionEvent e) {
            Component parent = frame;
            if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
                return;
            }

            File path = fc.getSelectedFile();
            log.info("path=" + path);
            String str = path.getAbsolutePath();
            setMnyFileName(str);
            if (textField != null) {
                textField.setCaretPosition(str.length());
            }
        }

        protected abstract void setMnyFileName(String fileName);
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

        @Override
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
                    showMainFrame(window);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            private void showMainFrame(PasswordCheckerApp window) {
                String title = "Mny Password Checker";
                window.frame.setTitle(title);
                // window.frame.pack();
                window.frame.setLocationRelativeTo(null);
                window.frame.setVisible(true);
            }
        });
    }

    /**
     * Create the application.
     */
    public PasswordCheckerApp() {
        Runnable command = new Runnable() {
            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("> status scheduler ...");
                }
                if (!running.get()) {
                    return;
                }

                if (checker == null) {
                    return;
                }

                AtomicLong counter = checker.getCounter();
                dataModel.setStatus("Running ... seached " + counter.get());
            }
        };
        long period = 2L;
        long initialDelay = 1L;
        TimeUnit unit = TimeUnit.SECONDS;
        schedulers.scheduleAtFixedRate(command, initialDelay, period, unit);

        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);
        JPanel wordListView = new JPanel();
        tabbedPane.addTab("Word list", null, wordListView, null);

        wordListView.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.UNRELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        JLabel lblNewLabel = new JLabel("Money file");
        lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel, "2, 2, right, default");

        textField = new JTextField();
        wordListView.add(textField, "4, 2, fill, default");
        textField.setColumns(10);

        JButton btnNewButton = new JButton("Open ...");
        btnNewButton.addActionListener(new OpenMnyAction(textField) {
            @Override
            protected void setMnyFileName(String fileName) {
                dataModel.setMnyFileName(fileName);
            }

        });
        wordListView.add(btnNewButton, "6, 2");

        JLabel lblNewLabel_1 = new JLabel("Word list");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_1, "2, 4, right, default");

        textField_1 = new JTextField();
        wordListView.add(textField_1, "4, 4, fill, default");
        textField_1.setColumns(10);

        JButton btnNewButton_1 = new JButton("Open ...");
        btnNewButton_1.addActionListener(new OpenWordListAction(textField_1));
        wordListView.add(btnNewButton_1, "6, 4");

        JLabel lblNewLabel_2 = new JLabel("Threads");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_2, "2, 6");

        spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        wordListView.add(spinner, "4, 6");

        JLabel lblNewLabel_3 = new JLabel("Status");
        lblNewLabel_3.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_3, "2, 8, right, default");

        textField_2 = new JTextField();
        textField_2.setEditable(false);
        wordListView.add(textField_2, "4, 8, fill, default");
        textField_2.setColumns(10);

        JButton btnNewButton_2 = new JButton("Start");
        btnNewButton_2.addActionListener(new StartSearchAction(btnNewButton_2));
        wordListView.add(btnNewButton_2, "6, 10");

        JPanel bruteForceView = new JPanel();
        tabbedPane.addTab("Brute force", null, bruteForceView, null);
        bruteForceView.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        JLabel lblNewLabel_4 = new JLabel("Money file");
        bruteForceView.add(lblNewLabel_4, "2, 2, right, default");

        textField_3 = new JTextField();
        bruteForceView.add(textField_3, "4, 2, fill, default");
        textField_3.setColumns(10);

        JButton btnNewButton_3 = new JButton("Open ...");
        btnNewButton_3.addActionListener(new OpenMnyAction(textField_3) {
            @Override
            protected void setMnyFileName(String fileName) {
                bruteForceDataModel.setMnyFileName(fileName);
            }
        });
        bruteForceView.add(btnNewButton_3, "6, 2");

        JLabel lblNewLabel_5 = new JLabel("Character set");
        bruteForceView.add(lblNewLabel_5, "2, 4, right, default");

        textField_4 = new JTextField();
        bruteForceView.add(textField_4, "4, 4, fill, default");
        textField_4.setColumns(10);

        JLabel lblNewLabel_6 = new JLabel("Password length/mask");
        bruteForceView.add(lblNewLabel_6, "2, 6, right, default");

        textField_5 = new JTextField();
        bruteForceView.add(textField_5, "4, 6, fill, default");
        textField_5.setColumns(10);

        JLabel lblNewLabel_8 = new JLabel("Context");
        bruteForceView.add(lblNewLabel_8, "2, 8, right, default");

        textField_7 = new JTextField();
        textField_7.setEnabled(false);
        bruteForceView.add(textField_7, "4, 8, fill, default");
        textField_7.setColumns(10);

        JButton btnNewButton_5 = new JButton("Open ...");
        btnNewButton_5.setEnabled(false);
        bruteForceView.add(btnNewButton_5, "6, 8");

        JLabel lblNewLabel_7 = new JLabel("Status");
        bruteForceView.add(lblNewLabel_7, "2, 10, right, default");

        textField_6 = new JTextField();
        bruteForceView.add(textField_6, "4, 10, fill, default");
        textField_6.setColumns(10);

        JButton btnNewButton_4 = new JButton("Start");
        btnNewButton_4.addActionListener(new StartBruteForceSearchAction(btnNewButton_4));
        bruteForceView.add(btnNewButton_4, "6, 12");
        initDataBindings();
    }

    protected void notifyResult(final String matchedPassword) {
        log.info("matchedPassword=" + matchedPassword);
        Runnable doRun = new Runnable() {
            @Override
            public void run() {
                Component parentComponent = frame;
                if (matchedPassword == null) {
                    JOptionPane.showMessageDialog(parentComponent, "Result of last search: NO password found.", "Search Result",
                            JOptionPane.WARNING_MESSAGE);
                } else {
                    JTextArea textArea = new JTextArea();
                    textArea.setEditable(false);
                    textArea.setColumns(30);
                    textArea.setLineWrap(true);
                    textArea.setWrapStyleWord(true);
                    textArea.append("Result of last search:\n");
                    textArea.append("found password=" + matchedPassword);
                    textArea.setSize(textArea.getPreferredSize().width, 1);
                    JOptionPane.showMessageDialog(parentComponent, textArea, "Search Result", JOptionPane.INFORMATION_MESSAGE);
                }
            }
        };
        SwingUtilities.invokeLater(doRun);
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
        //
        BeanProperty<PasswordCheckerModel, String> passwordCheckerModelBeanProperty_3 = BeanProperty.create("status");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_2 = BeanProperty.create("text");
        AutoBinding<PasswordCheckerModel, String, JTextField, String> autoBinding_3 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, dataModel, passwordCheckerModelBeanProperty_3, textField_2, jTextFieldBeanProperty_2);
        autoBinding_3.bind();
        //
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty = BeanProperty.create("mnyFileName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_3 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_4 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty, textField_3,
                jTextFieldBeanProperty_3);
        autoBinding_4.bind();
        //
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty_1 = BeanProperty.create("alphabets");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_5 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty_1, textField_4,
                jTextFieldBeanProperty_4);
        autoBinding_5.bind();
        //
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty_2 = BeanProperty.create("mask");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_5 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_6 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty_2, textField_5,
                jTextFieldBeanProperty_5);
        autoBinding_6.bind();
    }
}
