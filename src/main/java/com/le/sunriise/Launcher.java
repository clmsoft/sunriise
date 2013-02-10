/*******************************************************************************
 * Copyright (c) 2013 Hung Le
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
package com.le.sunriise;

import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import app.AccountViewer;
import app.MnyViewer;
import app.PasswordCheckerApp;

import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class Launcher {
    private static final Logger log = Logger.getLogger(Launcher.class);

    private static String VERSION;

    private JFrame frame;
    private final Action viewerAction = new SwingAction();
    private final Action accountViewerAction = new AccountViewerSwingAction();
    private final Action passwordToolAction = new PasswordToolSwingAction();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Launcher window = new Launcher();
                    showMainFrame(window);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            protected void showMainFrame(final Launcher window) {
                JFrame mainFrame = window.getFrame();

                Dimension preferredSize = new Dimension(400, 300);
                mainFrame.setPreferredSize(preferredSize);

                Launcher.VERSION = SunriiseBuildNumber.getBuildnumber();
                if (Launcher.VERSION != null) {
                    mainFrame.setTitle("Sunriise - " + Launcher.VERSION);
                } else {
                    mainFrame.setTitle("Sunriise");
                }
                log.info("BuildNumber: " + Launcher.VERSION);

                mainFrame.pack();

                mainFrame.setLocationRelativeTo(null);

                JMenuBar menuBar = new JMenuBar();
                mainFrame.setJMenuBar(menuBar);

                JMenu mnNewMenu = new JMenu("File");
                menuBar.add(mnNewMenu);

                JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
                mntmNewMenuItem.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        log.info("User selects File -> Exit");
                        System.exit(0);
                    }
                });
                mnNewMenu.add(mntmNewMenuItem);

                addHelpMenu(window.getFrame(), menuBar);

                mainFrame.setVisible(true);
            }
        });
    }

    /**
     * Create the application.
     */
    public Launcher() {
        initialize();
    }

    public static final void addHelpMenu(final JFrame frame, final JMenuBar menuBar) {
        JMenu helpMenu = new JMenu("Help");
        menuBar.add(helpMenu);
    
        JMenuItem homePageMenuItem = new JMenuItem("Home page");
        homePageMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.info("> Go to home page");
                String str = "https://code.google.com/p/sunriise/";
                URI uri = URI.create(str);
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException exception) {
                    log.error(exception, exception);
                }
            }
        });
        helpMenu.add(homePageMenuItem);
    
        JMenuItem wikiMenuItem = new JMenuItem("Documentation (Wiki)");
        wikiMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String str = "https://code.google.com/p/sunriise/w/list";
                URI uri = URI.create(str);
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException exception) {
                    log.error(exception, exception);
                }
            }
        });
        helpMenu.add(wikiMenuItem);
    
        JMenuItem issueMenuItem = new JMenuItem("Log a bug");
        issueMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String str = "https://code.google.com/p/sunriise/issues/list";
                URI uri = URI.create(str);
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException exception) {
                    log.error(exception, exception);
                }
    
            }
        });
        helpMenu.add(issueMenuItem);
    
        JMenuItem groupMenuItem = new JMenuItem("Discussion group");
        groupMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String str = "https://groups.google.com/forum/?fromgroups#!forum/sunriise";
                URI uri = URI.create(str);
                try {
                    Desktop.getDesktop().browse(uri);
                } catch (IOException exception) {
                    log.error(exception, exception);
                }
    
            }
        });
        helpMenu.add(groupMenuItem);
    
        JSeparator separator = new JSeparator();
        helpMenu.add(separator);
    
        JMenuItem aboutMenuItem = new JMenuItem("About sunriise");
        aboutMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                log.info("> About sunriise");
    
                Component parentComponent = frame;
                Object message = createAboutContent();
                String title = "About sunriise";
                int messageType = JOptionPane.PLAIN_MESSAGE;
    
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            }
    
            protected RTextScrollPane createAboutContent() {
                int rows = 10;
                int cols = 60;
                RSyntaxTextArea textArea = new RSyntaxTextArea(rows, cols);
                textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
                RTextScrollPane scrollPane = new RTextScrollPane(textArea);
                textArea.setEditable(false);
    
                textArea.append("Build number: " + SunriiseBuildNumber.getBuildnumber() + "\n");
                textArea.append("\n");
                textArea.append("Directory: " + getCurrentDirectory() + "\n");
    
                File file = new File("sunriise-log.txt");
                if (file.exists()) {
                    textArea.append("Log file: " + file.getAbsoluteFile().getAbsolutePath() + "\n");
                } else {
                    textArea.append("Log file: " + "NOT_FOUND" + "\n");
                }
    
                textArea.append("\n");
                String key = null;
                key = "java.home";
                textArea.append(key + ": " + System.getProperty(key) + "\n");
                key = "java.version";
                textArea.append(key + ": " + System.getProperty(key) + "\n");
    
                return scrollPane;
            }
    
            protected String getCurrentDirectory() {
                return new File(".").getAbsoluteFile().getAbsolutePath();
            }
        });
        helpMenu.add(aboutMenuItem);
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());

        // frame.setBounds(100, 100, 450, 300);
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

        getFrame().getContentPane().setLayout(
                new FormLayout(new ColumnSpec[] { ColumnSpec.decode("right:90px"), FormFactory.RELATED_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, },
                        new RowSpec[] { FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));

        JButton btnNewButton_3 = new JButton();
        btnNewButton_3.setAction(viewerAction);
        getFrame().getContentPane().add(btnNewButton_3, "1, 3");

        JLabel lblNewJgoodiesLabel_3 = DefaultComponentFactory.getInstance().createLabel(
                "MS Money file viewer (view tables, rows ...)");
        lblNewJgoodiesLabel_3.setHorizontalAlignment(SwingConstants.LEFT);
        getFrame().getContentPane().add(lblNewJgoodiesLabel_3, "3, 3");

        JButton btnNewButton_4 = new JButton();
        btnNewButton_4.setAction(accountViewerAction);
        getFrame().getContentPane().add(btnNewButton_4, "1, 5");

        JLabel lblNewJgoodiesLabel_4 = DefaultComponentFactory.getInstance().createLabel(
                "Accounts viewer (accounts, transactions ...)");
        lblNewJgoodiesLabel_4.setHorizontalAlignment(SwingConstants.LEFT);
        getFrame().getContentPane().add(lblNewJgoodiesLabel_4, "3, 5");

        JButton btnNewButton_5 = new JButton();
        btnNewButton_5.setAction(passwordToolAction);
        getFrame().getContentPane().add(btnNewButton_5, "1, 7");

        JLabel lblNewJgoodiesLabel_5 = DefaultComponentFactory.getInstance().createLabel("Password tools");
        lblNewJgoodiesLabel_5.setHorizontalAlignment(SwingConstants.LEFT);
        getFrame().getContentPane().add(lblNewJgoodiesLabel_5, "3, 7");
    }

    private class SwingAction extends AbstractAction {
        public SwingAction() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch *.mny viewer");
            String iconResource1Name = "/images/icon_db.png";
            URL iconResource1 = Launcher.class.getResource(iconResource1Name);
            if (iconResource1 != null) {
                putValue(LARGE_ICON_KEY, new ImageIcon(iconResource1));
            } else {
                log.warn("Cannot find icon resource=" + iconResource1Name);
            }

        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            MnyViewer.main(args);
            getFrame().setVisible(false);
        }
    }

    private class AccountViewerSwingAction extends AbstractAction {
        public AccountViewerSwingAction() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch accounts viewer");
            String iconResource1Name = "/images/icon_money.png";
            URL iconResource1 = Launcher.class.getResource(iconResource1Name);
            if (iconResource1 != null) {
                putValue(LARGE_ICON_KEY, new ImageIcon(iconResource1));
            } else {
                log.warn("Cannot find icon resource=" + iconResource1Name);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            AccountViewer.main(args);
            getFrame().setVisible(false);
        }
    }

    private class PasswordToolSwingAction extends AbstractAction {
        public PasswordToolSwingAction() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch password tools");
            String iconResource1Name = "/images/icon_password.png";
            URL iconResource1 = Launcher.class.getResource(iconResource1Name);
            if (iconResource1 != null) {
                putValue(LARGE_ICON_KEY, new ImageIcon(iconResource1));
            } else {
                log.warn("Cannot find icon resource=" + iconResource1Name);
            }
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            PasswordCheckerApp.main(args);
            getFrame().setVisible(false);
        }
    }

    public JFrame getFrame() {
        return frame;
    }

    private void setFrame(JFrame frame) {
        this.frame = frame;
    }
}
