package com.le.sunriise.header;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rtextarea.RTextScrollPane;

import com.le.sunriise.Launcher;
import com.le.sunriise.SunriiseBuildNumber;
import com.le.sunriise.password.HeaderPagePasswordChecker;
import com.le.sunriise.viewer.CreateOpenedDbPlugin;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public class HeaderPageViewer {
    private static final Logger log = Logger.getLogger(HeaderPage.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(HeaderPageViewer.class);

    private JFrame frame;

    private OpenedDb openedDb = new OpenedDb();

    private RSyntaxTextArea headerPageInfoTextArea;

    private HeaderPage headerPage;

    private final class MyOpenDbAction extends OpenDbAction {

        private MyOpenDbAction(Component locationRelativeTo, Preferences prefs, OpenedDb openedDb) {
            super(locationRelativeTo, prefs, openedDb);
            setDisableReadOnlyCheckBox(true);
            setPlugin(createPlugin());
        }

        private CreateOpenedDbPlugin createPlugin() {
            return new CreateOpenedDbPlugin() {
                @Override
                public OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted) {
                    OpenedDb openedDb = null;

                    openedDb = new OpenedDb();
                    openedDb.setDbFile(new File(dbFileName));
                    String password = null;
                    if ((passwordChars != null) && (passwordChars.length > 0)) {
                        password = new String(passwordChars);
                    }
                    openedDb.setPassword(password);

                    return openedDb;
                }
            };
        }

        @Override
        public void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog) {
            if (newOpenedDb != null) {
                HeaderPageViewer.this.openedDb = newOpenedDb;
            }

            File dbFile = openedDb.getDbFile();
            try {
                headerPage = new HeaderPage(dbFile);
                HeaderPagePasswordChecker checker = new HeaderPagePasswordChecker(headerPage);
                String password = openedDb.getPassword();
                boolean passwordIsValid = checker.check(password);
                if (!passwordIsValid) {
                    log.warn("Invalid password.");
                }

                RSyntaxTextArea textArea = headerPageInfoTextArea;

                textArea.append("# *.mny fileName.\n");
                textArea.append("dbFile: " + openedDb.getDbFile().getAbsolutePath());
                textArea.append("\n");

                textArea.append("# If true, the specified password is valid.\n");
                textArea.append("passwordIsValid: " + passwordIsValid);
                textArea.append("\n");
                
                textArea.append("# Database format.\n");
                textArea.append("getJetFormat: " + headerPage.getJetFormat());
                textArea.append("\n");

                textArea.append("# Database page size.\n");
                textArea.append("getJetFormat.PAGE_SIZE: " + headerPage.getJetFormat().PAGE_SIZE);
                textArea.append("\n");

                textArea.append("# Database character set.\n");
                textArea.append("getCharset: " + headerPage.getCharset());
                textArea.append("\n");

                textArea.append("# 2001 should have oldEncrytion, 2002 on should have newEncryption.\n");
                textArea.append("isNewEncryption: " + headerPage.isNewEncryption());
                textArea.append("\n");

                if (!headerPage.isNewEncryption()) {
                    textArea.append("# For old encrytion, we can retrieve the embedded password directly.\n");
                    textArea.append("getEmbeddedDatabasePassword: " + headerPage.getEmbeddedDatabasePassword());
                    textArea.append("\n");
                }

                textArea.append("# Hash algo.\n");
                textArea.append("isUseSha1: " + headerPage.isUseSha1());
                textArea.append("\n");

                textArea.append("# Embedded salt value.\n");
                textArea.append("getSalt: " + HeaderPage.toHexString(headerPage.getSalt()));
                textArea.append("\n");

                textArea.append("getBaseSalt: " + HeaderPage.toHexString(headerPage.getBaseSalt()));
                textArea.append("\n");

                textArea.append("encrypted4BytesCheck: " + HeaderPage.toHexString(headerPage.getEncrypted4BytesCheck()));
                textArea.append("\n");

                textArea.append("testKey: " + HeaderPage.toHexString(checker.getTestKey()));
                textArea.append("\n");

                textArea.append("testBytes: " + HeaderPage.toHexString(checker.getTestBytes()));
                textArea.append("\n");

                textArea.append("decrypted4BytesCheck: " + HeaderPage.toHexString(checker.getDecrypted4BytesCheck()));
                textArea.append("\n");

                textArea.append("encodingKey: " + HeaderPage.toHexString(checker.getEncodingKey()));
                textArea.append("\n");

                textArea.setCaretPosition(0);
            } catch (IOException e) {
                log.error(e, e);
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
                    HeaderPageViewer window = new HeaderPageViewer();
                    showMainFrame(window);

                    String buildNumber = SunriiseBuildNumber.getBuildnumber();
                    log.info("BuildNumber: " + buildNumber);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            private void showMainFrame(HeaderPageViewer window) {
                JFrame mainFrame = window.getFrame();

                String title = "HeaderPage Viewer";
                String buildNumber = SunriiseBuildNumber.getBuildnumber();
                if (buildNumber != null) {
                    title = title + " - " + buildNumber;
                }
                mainFrame.setTitle(title);

                Dimension preferredSize = new Dimension(900, 500);
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
    public HeaderPageViewer() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());
        // getFrame().setBounds(100, 100, 450, 300);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getFrame().addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                super.windowClosing(e);

                if (openedDb != null) {
                    openedDb.close();
                    openedDb = null;
                }

                log.info("> windowClosing");
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                log.info("> windowClosed");
            }

        });

        initMainMenuBar();

        addViews(getFrame().getContentPane());
    }

    private void addViews(Container contentPane) {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        headerPageInfoTextArea = textArea;
        headerPageInfoTextArea.setEditable(false);
        view.add(sp, BorderLayout.CENTER);

        contentPane.add(view);
    }

    private void initMainMenuBar() {
        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (openedDb != null) {
                    openedDb.close();
                    openedDb = null;
                }
                log.info("User selects File -> Exit");
                System.exit(0);
            }
        });

        JMenuItem fileOpenMenuItem = new JMenuItem("Open");
        fileOpenMenuItem.addActionListener(new MyOpenDbAction(HeaderPageViewer.this.frame, prefs, openedDb));

        fileMenu.add(fileOpenMenuItem);

        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);

        Launcher.addHelpMenu(frame, menuBar);
    }

    private JFrame getFrame() {
        return frame;
    }

    private void setFrame(JFrame frame) {
        this.frame = frame;
    }

}
