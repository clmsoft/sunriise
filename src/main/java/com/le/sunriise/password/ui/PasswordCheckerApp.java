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
package com.le.sunriise.password.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
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
import com.le.sunriise.JavaInfo;
import com.le.sunriise.Launcher;
import com.le.sunriise.SunriiseBuildNumber;
import com.le.sunriise.model.bean.BruteForceCheckerModel;
import com.le.sunriise.model.bean.PasswordCheckerModel;
import com.le.sunriise.password.bruteforce.BruteForceStat;
import com.le.sunriise.password.bruteforce.CheckBruteForce;
import com.le.sunriise.password.bruteforce.GenBruteForce;
import com.le.sunriise.password.timing.Duration;

public class PasswordCheckerApp {
    private static final Logger log = Logger.getLogger(PasswordCheckerApp.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(PasswordCheckerApp.class);

    private JFrame frame;

    private JTextField textField;

    private JTextField textField_1;

    private PasswordCheckerModel dataModel = new PasswordCheckerModel();

    private BruteForceCheckerModel bruteForceDataModel = new BruteForceCheckerModel();

    private JSpinner spinner;

    private ExecutorService pool = Executors.newCachedThreadPool();
    private ScheduledExecutorService schedulers = Executors.newScheduledThreadPool(2);

    private JTextField textField_2;

    private JTextField textField_3;
    private JTextField textField_4;
    private JTextField textField_5;
    private JTextField textField_6;
    private JTextField txtNotImplementedYet;

    private JLabel[][] scoreboards;

    private JFileChooser sharedFileChooser;

    // private AtomicLong counter = new AtomicLong(0L);

    // private JFileChooser fc;

    private final class OpenWordListAction implements ActionListener {
        private JFileChooser fc = null;
        private JTextField textField;
        private FileFilter choosableFileFilter = null;

        public OpenWordListAction(JTextField textField, JFileChooser fc) {
            super();
            this.textField = textField;
            // XXX - create JFileChooser is slow
            if (log.isDebugEnabled()) {
                log.debug("> new JFileChooser");
            }
            // fc = new JFileChooser(new File("."));
            this.fc = fc;
            if (log.isDebugEnabled()) {
                log.debug("< new JFileChooser");
            }
            // fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            Component parent = getFrame();

            resetFileChooser(fc);

            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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

        private void resetFileChooser(JFileChooser fc) {
            if (fc == null) {
                return;
            }

            fc.resetChoosableFileFilters();

            if (choosableFileFilter != null) {
                fc.addChoosableFileFilter(choosableFileFilter);
            }
            fc.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
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
                    JavaInfo.logInfo();

                    log.info("> Starting PasswordCheckerApp");
                    PasswordCheckerApp window = new PasswordCheckerApp();
                    showMainFrame(window);

                    String buildNumber = SunriiseBuildNumber.getBuildnumber();
                    log.info("BuildNumber: " + buildNumber);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            private void showMainFrame(PasswordCheckerApp window) {
                JFrame mainFrame = window.getFrame();

                String title = "Mny Password Checker";
                mainFrame.setTitle(title);

                Dimension preferredSize = new Dimension(600, 400);
                mainFrame.setPreferredSize(preferredSize);

                mainFrame.pack();

                mainFrame.setLocationRelativeTo(null);

                mainFrame.setVisible(true);
                log.info(" setVisible to true");
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
        log.info("> initialize");

        setFrame(new JFrame());
        // getFrame().setBounds(100, 100, 600, 300);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);
                log.info("> windowClosing");
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                log.info("> windowClosed");
            }

        });

        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("User selects File -> Exit");
                System.exit(0);
            }
        });
        mnNewMenu.add(mntmNewMenuItem);

        Launcher.addHelpMenu(getFrame(), menuBar);

        // fc = new JFileChooser(new File("."));

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        getFrame().getContentPane().add(tabbedPane, BorderLayout.CENTER);

        JPanel wordListView = new JPanel();

        log.info("> Adding tab 'Word list'");
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

        log.info("> Creating sharedFileChooser");
        sharedFileChooser = new JFileChooser(".");
        log.info("< Creating sharedFileChooser");

        log.info("> Adding OpenMnyAction");
        JButton btnNewButton = new JButton("Open ...");
        btnNewButton.addActionListener(new OpenMnyAction(this.getFrame(), textField, sharedFileChooser) {
            @Override
            protected void setMnyFileName(String fileName) {
                dataModel.setMnyFileName(fileName);
                prefs.put("lastOpenedMnyFileName", fileName);

            }

        });
        wordListView.add(btnNewButton, "6, 2");

        JLabel lblNewLabel_1 = new JLabel("Word list");
        lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_1, "2, 4, right, default");

        textField_1 = new JTextField();
        wordListView.add(textField_1, "4, 4, fill, default");
        textField_1.setColumns(10);

        log.info("> Adding OpenWordListAction");
        JButton btnNewButton_1 = new JButton("Open ...");
        btnNewButton_1.addActionListener(new OpenWordListAction(textField_1, sharedFileChooser));
        wordListView.add(btnNewButton_1, "6, 4");

        // log.info("> 111");
        JLabel lblNewLabel_2 = new JLabel("Threads");
        lblNewLabel_2.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_2, "2, 6");

        // log.info("> 222");
        spinner = new JSpinner();
        spinner.setModel(new SpinnerNumberModel(new Integer(1), new Integer(1), null, new Integer(1)));
        wordListView.add(spinner, "4, 6");

        // log.info("> 333");
        JLabel lblNewLabel_3 = new JLabel("Status");
        lblNewLabel_3.setHorizontalAlignment(SwingConstants.TRAILING);
        wordListView.add(lblNewLabel_3, "2, 8, right, default");

        // log.info("> 444");
        textField_2 = new JTextField();
        textField_2.setEditable(false);
        wordListView.add(textField_2, "4, 8, fill, default");
        textField_2.setColumns(10);

        log.info("> Adding DictionarySearchAction");
        JButton btnNewButton_2 = new JButton("Start");
        final DictionarySearchAction action = new DictionarySearchAction(this, dataModel, btnNewButton_2);
        scheduleDictionaryStatusCommand(action);
        btnNewButton_2.addActionListener(action);
        wordListView.add(btnNewButton_2, "6, 10");

        JPanel bruteForceViewParent = new JPanel();
        bruteForceViewParent.setLayout(new BoxLayout(bruteForceViewParent, BoxLayout.PAGE_AXIS));

        log.info("> Adding tab 'Brute force'");
        tabbedPane.addTab("Brute force", null, bruteForceViewParent, null);

        JPanel bruteForceView = new JPanel();
        bruteForceViewParent.add(bruteForceView);
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
        btnNewButton_3.addActionListener(new OpenMnyAction(this.getFrame(), textField_3, sharedFileChooser) {
            @Override
            protected void setMnyFileName(String fileName) {
                bruteForceDataModel.setMnyFileName(fileName);
                prefs.put("lastOpenedMnyFileName", fileName);
            }
        });
        bruteForceView.add(btnNewButton_3, "6, 2");

        JLabel lblNewLabel_5 = new JLabel("Password length/mask");
        bruteForceView.add(lblNewLabel_5, "2, 4, right, default");

        textField_4 = new JTextField();
        bruteForceView.add(textField_4, "4, 4, fill, default");
        textField_4.setColumns(10);

        JLabel lblNewLabel_6 = new JLabel("Character set");
        bruteForceView.add(lblNewLabel_6, "2, 6, right, default");

        textField_5 = new JTextField();
        bruteForceView.add(textField_5, "4, 6, fill, default");
        textField_5.setColumns(10);

        JLabel lblNewLabel_8 = new JLabel("Context");
        bruteForceView.add(lblNewLabel_8, "2, 8, right, default");

        txtNotImplementedYet = new JTextField();
        txtNotImplementedYet.setText("Not implemented yet.");
        txtNotImplementedYet.setEnabled(false);
        bruteForceView.add(txtNotImplementedYet, "4, 8, fill, default");
        txtNotImplementedYet.setColumns(10);

        JButton btnNewButton_5 = new JButton("Open ...");
        btnNewButton_5.setEnabled(false);
        bruteForceView.add(btnNewButton_5, "6, 8");

        JLabel lblNewLabel_7 = new JLabel("Status");
        bruteForceView.add(lblNewLabel_7, "2, 10, right, default");

        textField_6 = new JTextField();
        textField_6.setEditable(false);
        bruteForceView.add(textField_6, "4, 10, fill, default");
        textField_6.setColumns(10);

        JButton btnNewButton_4 = new JButton("Start");
        StartBruteForceSearchAction bruteForceAction = new StartBruteForceSearchAction(btnNewButton_4, PasswordCheckerApp.this,
                bruteForceDataModel);
        btnNewButton_4.addActionListener(bruteForceAction);
        scheduleBruteForceStatusCommand(bruteForceAction);
        bruteForceView.add(btnNewButton_4, "6, 12");

        scoreboards = null;
        int rows = 3;
        int columns = 12;
        scoreboards = new JLabel[rows][];
        for (int i = 0; i < rows; i++) {
            scoreboards[i] = new JLabel[columns];
            for (int j = 0; j < 12; j++) {
                scoreboards[i][j] = new JLabel("00");
            }
        }
        JPanel scoreBoardView = new JPanel();
        bruteForceViewParent.add(scoreBoardView);
        scoreBoardView.setBorder(BorderFactory.createTitledBorder("Scoreboard"));
        scoreBoardView.setLayout(new GridLayout(3, 12));
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < columns; j++) {
                scoreBoardView.add(scoreboards[i][j]);
            }
        }

        if (dataModel != null) {
            String str = prefs.get("lastOpenedMnyFileName", null);
            if (str != null) {
                dataModel.setMnyFileName(str);
            }
        }

        if (bruteForceDataModel != null) {
            String str = prefs.get("lastOpenedMnyFileName", null);
            if (str != null) {
                bruteForceDataModel.setMnyFileName(str);
            }
        }

        log.info("> initDataBindings");
        initDataBindings();

        log.info("< initialize");
    }

    private void scheduleBruteForceStatusCommand(final StartBruteForceSearchAction action) {
        Runnable cmd = new Runnable() {
            private final AtomicBoolean running = action.getRunning();

            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("> bruteforce status scheduler ...");
                }
                if (!running.get()) {
                    return;
                }

                CheckBruteForce checker = action.getChecker();
                updateScoreboardsUI(checker, true);

                long delta = action.getElapsed();
                final Duration duration = new Duration(delta);

                final BruteForceStat stat = checker.getStat();
                String rateString = null;
                if (stat != null) {
                    if (stat.getSeconds().longValue() > 0) {
                        rateString = "(" + GenBruteForce.calcRate(stat) + "/sec)";
                    } else {
                        rateString = "(count=" + stat.getCount() + ")";
                    }
                }

                if (rateString == null) {
                    action.setStatus("Running ... " + duration.toString());
                } else {
                    action.setStatus("Running ... " + duration.toString() + ". " + rateString);
                }
            }

        };
        long period = 1L;
        long initialDelay = 1L;
        TimeUnit unit = TimeUnit.SECONDS;
        schedulers.scheduleAtFixedRate(cmd, initialDelay, period, unit);
    }

    private void scheduleDictionaryStatusCommand(final DictionarySearch action) {
        Runnable command = new Runnable() {
            private final AtomicBoolean running = action.getRunning();

            @Override
            public void run() {
                if (log.isDebugEnabled()) {
                    log.debug("> status scheduler ...");
                }
                if (!running.get()) {
                    return;
                }
                dataModel.setStatus("Running ... searched count " + action.getCounter());
            }
        };
        long period = 2L;
        long initialDelay = 1L;
        TimeUnit unit = TimeUnit.SECONDS;
        schedulers.scheduleAtFixedRate(command, initialDelay, period, unit);
    }

    JFrame getFrame() {
        return frame;
    }

    void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public ExecutorService getPool() {
        return pool;
    }

    public void setPool(ExecutorService pool) {
        this.pool = pool;
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
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty_1 = BeanProperty.create("mask");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_4 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_5 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty_1, textField_4,
                jTextFieldBeanProperty_4);
        autoBinding_5.bind();
        //
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty_2 = BeanProperty.create("alphabets");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_5 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_6 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty_2, textField_5,
                jTextFieldBeanProperty_5);
        autoBinding_6.bind();
        //
        BeanProperty<BruteForceCheckerModel, String> bruteForceCheckerModelBeanProperty_3 = BeanProperty.create("status");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_6 = BeanProperty.create("text");
        AutoBinding<BruteForceCheckerModel, String, JTextField, String> autoBinding_7 = Bindings.createAutoBinding(
                UpdateStrategy.READ_WRITE, bruteForceDataModel, bruteForceCheckerModelBeanProperty_3, textField_6,
                jTextFieldBeanProperty_6);
        autoBinding_7.bind();
    }

    public JLabel[][] getScoreboards() {
        return scoreboards;
    }

    protected void updateScoreboardsUI(CheckBruteForce checker, boolean invokeLater) {
        final BruteForceStat stat = checker.getStat();

        final char[] alphabets = checker.getAlphabets();

        if ((stat != null) && (alphabets != null)) {
            Runnable doRun = new Runnable() {
                @Override
                public void run() {
                    updateScoreboardsUI(stat, alphabets);
                }
            };
            if (invokeLater) {
                SwingUtilities.invokeLater(doRun);
            } else {
                doRun.run();
            }
        }
    }

    private void updateScoreboardsUI(final BruteForceStat stat, char[] alphabets) {
        JLabel[][] scoreboards = getScoreboards();
        int[] cursorIndex = stat.getCurrentCursorIndex();
        if ((cursorIndex == null) || (cursorIndex.length <= 0)) {
            return;
        }

        int i = 2;
        int columns = scoreboards[i].length;
        for (int j = 0; j < columns; j++) {
            if (j < cursorIndex.length) {
                scoreboards[i][j].setText("" + alphabets.length);
            } else {
                scoreboards[i][j].setText("");
            }
        }

        i = 1;
        for (int j = 0; j < columns; j++) {
            if (j < cursorIndex.length) {
                scoreboards[i][j].setText("" + cursorIndex[j]);
            } else {
                scoreboards[i][j].setText("");
            }
        }

        i = 0;
        for (int j = 0; j < columns; j++) {
            if (j < cursorIndex.length) {
                int index = cursorIndex[j];
                if ((alphabets != null) && (index >= 0) && (index < alphabets.length)) {
                    scoreboards[i][j].setText("" + alphabets[index]);
                } else {
                    scoreboards[i][j].setText("");
                }
            } else {
                scoreboards[i][j].setText("");
            }
        }
    }

}
