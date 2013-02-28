package com.le.sunriise.scan;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridLayout;
import java.awt.LayoutManager;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.prefs.Preferences;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.apache.log4j.Logger;

import com.le.sunriise.JavaInfo;
import com.le.sunriise.Launcher;
import com.le.sunriise.SunriiseBuildNumber;
import com.le.sunriise.header.AbstractPluginOpenDbAction;
import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.HeaderPagePasswordChecker;
import com.le.sunriise.viewer.OpenedDb;

public class PagesViewer {
    private static final Logger log = Logger.getLogger(PagesViewer.class);
    private static final Preferences prefs = Preferences.userNodeForPackage(PagesViewer.class);

    private static final int DEFAULT_COLS = 50;
    private OpenedDb openedDb = new OpenedDb();

    private JFrame frame;
    private LayoutManager layout;
    private JPanel mainView;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    JavaInfo.logInfo();

                    log.info("> Starting PagesViewer");

                    PagesViewer window = new PagesViewer();
                    showMainFrame(window);

                    String buildNumber = SunriiseBuildNumber.getBuildnumber();
                    log.info("BuildNumber: " + buildNumber);

                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            protected void showMainFrame(PagesViewer window) {
                JFrame mainFrame = window.getFrame();

                String title = com.le.sunriise.viewer.MynViewer.TITLE_NO_OPENED_DB;
                mainFrame.setTitle(title);

                Dimension preferredSize = new Dimension(900, 700);
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
    public PagesViewer() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());
        // getFrame().setBounds(100, 100, 450, 300);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem fileOpenMenuItem = new JMenuItem("Open");
        fileOpenMenuItem.addActionListener(new AbstractPluginOpenDbAction(PagesViewer.this.frame, prefs, openedDb) {
            @Override
            protected void dbFileOpened(OpenedDb newOpenedDb) {
                try {
                    displayPages(newOpenedDb);
                } catch (IOException e) {
                    log.error(e, e);
                }
            }
        });
        fileMenu.add(fileOpenMenuItem);

        fileMenu.addSeparator();

        JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                log.info("User selects File -> Exit");
                System.exit(0);
            }
        });
        fileMenu.add(mntmNewMenuItem);

        Launcher.addHelpMenu(frame, menuBar);

        this.mainView = new JPanel();
        this.layout = new GridLayout(0, DEFAULT_COLS);
        this.mainView.setLayout(this.layout);
        frame.getContentPane().add(new JScrollPane(mainView));
    }

    protected void displayPages(OpenedDb newOpenedDb) throws IOException {
        String fileName = newOpenedDb.getDbFile().getAbsolutePath();
        getFrame().setTitle(fileName);

        DbFile dbFile = new DbFile(fileName, newOpenedDb.getPassword());
        long pages = dbFile.getPages();
        log.info("pages=" + pages);

        HeaderPage headerPage = dbFile.getHeaderPage();
        HeaderPagePasswordChecker checker = new HeaderPagePasswordChecker(headerPage);
        String password = dbFile.getPassword();
        boolean passwordIsValid = checker.check(password);
        if (!passwordIsValid) {
            log.warn("Invalid password.");
        } else {
            log.info("Valid password.");
        }
        PageScanner scanner = new PageScanner(dbFile, checker);
        scanner.scan();

        mainView.removeAll();
        log.info("> Starting adding ...");
        for (int i = 0; i < pages; i++) {
            JButton button = new JButton("" + (i + 1));
            TableDefPage tDef = scanner.getPage(i + 1);
            if (tDef != null) {
                button.setBackground(Color.RED);
            }
            this.mainView.add(button);
        }
        mainView.revalidate();
        log.info("< Ending adding ...");
    }

    private JFrame getFrame() {
        return frame;
    }

    private void setFrame(JFrame frame) {
        this.frame = frame;
    }

}
