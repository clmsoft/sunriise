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
package com.le.sunriise.accountviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.fife.ui.rsyntaxtextarea.RSyntaxDocument;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;
import org.fife.ui.rsyntaxtextarea.TokenMaker;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.JavaInfo;
import com.le.sunriise.StopWatch;
import com.le.sunriise.SunriiseBuildNumber;
import com.le.sunriise.export.ExportToContext;
import com.le.sunriise.json.JSONUtils;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.mnyobject.AccountType;
import com.le.sunriise.mnyobject.Security;
import com.le.sunriise.mnyobject.SecurityHolding;
import com.le.sunriise.mnyobject.Transaction;
import com.le.sunriise.model.bean.AccountViewerDataModel;
import com.le.sunriise.qif.QifExportUtils;
import com.le.sunriise.viewer.ExportToJSONAction;
import com.le.sunriise.viewer.MyTableCellRenderer;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;
import com.le.sunriise.viewer.TableUtils;

public class AccountViewer {
    private static final Logger log = Logger.getLogger(AccountViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(AccountViewer.class);

    private JFrame frame;

    private OpenedDb openedDb = new OpenedDb();

    // private boolean dbReadOnly;

    private AccountViewerDataModel dataModel = new AccountViewerDataModel();

    private JList accountList;
    private JTable table;

    private MnyContext mnyContext = new MnyContext();

    private JLabel accountTypeLabel;
    private JLabel startingBalanceLabel;
    private JLabel endingBalanceLabel;

    private Account selectedAccount;

    private JTextArea accountInfoTextArea;
    private JTextArea accountJsonTextArea;

    private JTextArea transactionQifTextArea;
    private JTextArea transactionJsonTextArea;

    private Connection jdbcConn;

    private static final ExecutorService threadPool = Executors.newCachedThreadPool();

    private final class MyOpenDbAction extends OpenDbAction {

        private MyOpenDbAction(Component locationRelativeTo, Preferences prefs, OpenedDb openedDb) {
            super(locationRelativeTo, prefs, openedDb);
            setDisableReadOnlyCheckBox(true);
        }

        @Override
        public void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog) {
            if (newOpenedDb != null) {
                AccountViewer.this.openedDb = newOpenedDb;
            }

            File dbFile = openedDb.getDbFile();
            if (dbFile != null) {
                getFrame().setTitle(dbFile.getAbsolutePath());

                // String dbUrl = UcanaccessDriver.URL_PREFIX +
                // dbFile.getAbsolutePath() +
                // ";codecprovider=;password=";
                // try {
                // jdbcConn = DriverManager.getConnection(dbUrl);
                // log.info("jdbcConn=" + jdbcConn);
                // } catch (SQLException e) {
                // log.warn(e);
                // }
            } else {
                getFrame().setTitle(com.le.sunriise.viewer.MynViewer.TITLE_NO_OPENED_DB);
            }

            try {
                final List<Account> accounts = AccountUtil.initMnyContext(openedDb, mnyContext);

                AccountViewer.this.dataModel.setAccounts(accounts);

                Runnable doRun = new Runnable() {
                    @Override
                    public void run() {
                        Account account = null;
                        // clear out currently select account, if any
                        try {
                            accountSelected(null);
                        } catch (IOException e) {
                            log.warn(e);
                        }
                    }
                };
                SwingUtilities.invokeLater(doRun);
            } catch (IOException e) {
                log.warn(e);
            }
        }
    }

    /**
     * Create the application.
     */
    public AccountViewer() {
        initialize();
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
                if (openedDb != null) {
                    appClosed();
                }
            }

            @Override
            public void windowClosed(WindowEvent e) {
                super.windowClosed(e);
                log.info("> windowClosed");
            }

        });

        initMainMenuBar();

        JPanel leftComponent = createLeftComponent();
        JPanel rightComponent = createRightComponent();
        JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
        hSplitPane.setResizeWeight(0.30);
        getFrame().getContentPane().add(hSplitPane, BorderLayout.CENTER);
        hSplitPane.setDividerLocation(0.30);

        initDataBindings();
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
                appClosed();

                System.exit(0);
            }
        });

        JMenuItem fileOpenMenuItem = new JMenuItem("Open");
        fileOpenMenuItem.addActionListener(new MyOpenDbAction(AccountViewer.this.frame, prefs, openedDb));

        fileMenu.add(fileOpenMenuItem);

        JMenu mnNewMenu = new JMenu("Export");
        fileMenu.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("To *.json");
        ExportToContext exportToContext = new ExportToContext() {

            @Override
            public OpenedDb getSrcDb() {
                return getOpenedDb();
            }

            @Override
            public Component getParentComponent() {
                return getFrame();
            }
        };
        mntmNewMenuItem.addActionListener(new ExportToJSONAction(exportToContext));
        mnNewMenu.add(mntmNewMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
    }

    private JPanel createLeftComponent() {
        JPanel leftComponent = new JPanel();
        // leftComponent.setPreferredSize(new Dimension(80, -1));
        leftComponent.setLayout(new BorderLayout());

        leftComponent.setBorder(BorderFactory.createTitledBorder("Accounts"));

        JScrollPane scrollPane = new JScrollPane();
        leftComponent.add(scrollPane, BorderLayout.CENTER);

        accountList = new JList();
        accountList.addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                final Account account = (Account) accountList.getSelectedValue();
                if (account != null) {
                    try {
                        accountSelected(account);
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }

        });
        accountList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        accountList.setVisibleRowCount(-1);
        scrollPane.setViewportView(accountList);

        return leftComponent;
    }

    private JPanel createRightComponent() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        Component topComponent = createTopComponent();
        Component bottomComponent = createBottomComponent();

        JSplitPane vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topComponent, bottomComponent);
        view.add(vSplitPane, BorderLayout.CENTER);

        // vSplitPane.setResizeWeight(0.50);
        // vSplitPane.setDividerLocation(0.50);

        return view;
    }

    private Component createTopComponent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createTitledBorder("Account details"));

        tabbedPane.addTab("Transactions", createTransactionsView());

        tabbedPane.addTab("Account info", createAccountInfoView());

        tabbedPane.addTab("JSON", createAccountJsonView());

        return tabbedPane;
    }

    private Component createAccountJsonView() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        accountJsonTextArea = textArea;
        accountJsonTextArea.setEditable(false);
        view.add(sp, BorderLayout.CENTER);

        return view;
    }

    private Component createAccountInfoView() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PROPERTIES_FILE);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        accountInfoTextArea = textArea;
        accountInfoTextArea.setEditable(false);
        view.add(sp, BorderLayout.CENTER);

        return view;
    }

    private Component createTransactionsView() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        JPanel topStatusView = new JPanel();
        view.add(topStatusView, BorderLayout.NORTH);
        topStatusView.setLayout(new BorderLayout(0, 0));

        accountTypeLabel = new JLabel("");
        topStatusView.add(accountTypeLabel, BorderLayout.WEST);

        startingBalanceLabel = new JLabel("");
        updateStartingBalanceLabel(new BigDecimal(0.00), null);
        topStatusView.add(startingBalanceLabel, BorderLayout.EAST);

        JScrollPane scrollPane = new JScrollPane();
        view.add(scrollPane, BorderLayout.CENTER);

        table = new JTable() {

            @Override
            public void setModel(TableModel dataModel) {
                super.setModel(dataModel);

                TableColumnModel columnModel = this.getColumnModel();
                int cols = columnModel.getColumnCount();

                for (int i = 0; i < cols; i++) {
                    TableColumn column = columnModel.getColumn(i);
                    MyTableCellRenderer renderer = new MyTableCellRenderer(column.getCellRenderer());
                    column.setCellRenderer(renderer);
                }
            }
        };
        table.setDefaultRenderer(BigDecimal.class, new DefaultTableCellRenderer() {

            @Override
            public void setValue(Object value) {
                if (log.isDebugEnabled()) {
                    log.debug("cellRenderer: value=" + value + ", " + value.getClass().getName());
                }
                String renderedValue = ((value == null) || (selectedAccount == null)) ? "" : selectedAccount
                        .formatAmmount((BigDecimal) value);
                if (log.isDebugEnabled()) {
                    log.debug("cellRenderer: renderedValue=" + renderedValue);
                }

                setText(renderedValue);

                BigDecimal bigDecimal = (BigDecimal) value;
                if (bigDecimal.compareTo(BigDecimal.ZERO) < 0) {
                    setForeground(Color.RED);
                } else {
                    setForeground(Color.BLACK);
                }
            }
        });
        // table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {

            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                if (log.isDebugEnabled()) {
                    log.debug("Rows:");
                    for (int r : table.getSelectedRows()) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(" %d", r));
                        }
                    }
                    log.info(". Columns:");
                    for (int c : table.getSelectedColumns()) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format(" %d", c));
                        }
                    }
                }

                for (int r : table.getSelectedRows()) {
                    rowSelected(r);
                }
            }
        });
        scrollPane.setViewportView(table);

        JPanel bottomStatusView = new JPanel();
        view.add(bottomStatusView, BorderLayout.SOUTH);
        bottomStatusView.setLayout(new BorderLayout(0, 0));

        endingBalanceLabel = new JLabel("");
        updateEndingBalanceLabel(new BigDecimal(0.00), null);
        bottomStatusView.add(endingBalanceLabel, BorderLayout.EAST);

        return view;
    }

    private Component createBottomComponent() {
        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setBorder(BorderFactory.createTitledBorder("Transaction details"));

        // tabbedPane.addTab("QIF", createTransactionQif());

        tabbedPane.addTab("JSON", createTransactionJson());

        // tabbedPane.addTab("Transaction info", createTransactionInfoView());

        return tabbedPane;
    }

    private Component createTransactionJson() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        transactionJsonTextArea = textArea;
        transactionJsonTextArea.setEditable(false);
        view.add(sp, BorderLayout.CENTER);

        return view;
    }

    private Component createTransactionQif() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        RSyntaxTextArea textArea = new RSyntaxTextArea();
        textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
        RSyntaxDocument doc = (RSyntaxDocument) textArea.getDocument();
        TokenMaker tokenMaker = new QifTokenMaker();
        doc.setSyntaxStyle(tokenMaker);
        RTextScrollPane sp = new RTextScrollPane(textArea);
        transactionQifTextArea = textArea;
        transactionQifTextArea.setEditable(false);
        view.add(sp, BorderLayout.CENTER);

        return view;
    }

    private Component createTransactionInfoView() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());
        return view;
    }

    private Map<String, Object> getTransactionRow(OpenedDb openedDb, Integer id) throws IOException {
        if (openedDb == null) {
            return null;
        }
        Database db = openedDb.getDb();
        if (db == null) {
            return null;
        }
        Map<String, Object> row = null;
        String tableName = "TRN";
        Table table = db.getTable(tableName);
        Cursor cursor = null;
        cursor = Cursor.createCursor(table);
        Map<String, Object> rowPattern = new HashMap<String, Object>();
        rowPattern.put("htrn", id);
        if (cursor.findFirstRow(rowPattern)) {
            row = cursor.getCurrentRow();
        }
        return row;
    }

    private void updateEndingBalanceLabel(BigDecimal balance, Account account) {
        log.info("> updateEndingBalanceLabel, balance=" + balance + ", account=" + account);
        String label = "Ending balance: ";
        if ((balance != null) && (account != null)) {
            getEndingBalanceLabel().setText(label + account.formatAmmount(balance));
        } else {
            getEndingBalanceLabel().setText(label);
        }

    }

    private void updateStartingBalanceLabel(BigDecimal balance, Account account) {
        log.info("> updateStartingBalanceLabel, balance=" + balance + ", account=" + account);
        String label = "Starting balance: ";
        if ((balance != null) && (account != null)) {
            getStartingBalanceLabel().setText(label + account.formatAmmount(balance));
        } else {
            getStartingBalanceLabel().setText(label);
        }

    }

    private void setFrame(JFrame frame) {
        this.frame = frame;
    }

    private JFrame getFrame() {
        return frame;
    }

    protected void initDataBindings() {
        BeanProperty<AccountViewerDataModel, List<Account>> accountViewerDataModelBeanProperty = BeanProperty.create("accounts");
        JListBinding<Account, AccountViewerDataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ,
                dataModel, accountViewerDataModelBeanProperty, accountList);
        jListBinding.bind();
        //
        BeanProperty<AccountViewerDataModel, TableModel> accountViewerDataModelBeanProperty_1 = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<AccountViewerDataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(
                UpdateStrategy.READ, dataModel, accountViewerDataModelBeanProperty_1, table, jTableEvalutionProperty);
        autoBinding.bind();
    }

    private static void dump(ResultSet rs, String exName) throws SQLException {
        System.out.println("-------------------------------------------------");
        System.out.println();
        System.out.println(exName + " result:");
        System.out.println();
        while (rs.next()) {
            System.out.print("| ");
            int j = rs.getMetaData().getColumnCount();
            for (int i = 1; i <= j; ++i) {
                Object o = rs.getObject(i);
                System.out.print(o + " | ");
            }
            System.out.println();
            System.out.println();
        }
    }

    protected void accountSelected(final Account account) throws IOException {
        StopWatch stopWatch = new StopWatch();
        log.info("> accountSelected");
        selectedAccount = account;

        try {
            if (account != null) {
                log.info("select account=" + account);
                Database db = openedDb.getDb();

                AccountUtil.retrieveTransactions(db, account);

                jdbcDump();

                BigDecimal currentBalance = AccountUtil.calculateCurrentBalance(account);

                if (log.isDebugEnabled()) {
                    log.debug(account.getName() + ", " + account.getAccountType() + ", "
                            + AccountUtil.getCurrencyName(account.getCurrencyId(), mnyContext.getCurrencies()) + ", "
                            + account.getStartingBalance() + ", " + currentBalance + ", " + account.getAmountLimit());
                }

                // UI
                updateStartingBalanceLabel(account.getStartingBalance(), account);
                updateEndingBalanceLabel(currentBalance, account);

                getAccountTypeLabel().setText(account.getAccountType().toString());

                boolean calculateMonthlySummary = false;
                if (calculateMonthlySummary) {
                    TableUtils.calculateMonthlySummary(account);
                }
            }

            AbstractAccountViewerTableModel tableModel = null;

            if (account != null) {
                AccountType accountType = account.getAccountType();
                switch (accountType) {
                case INVESTMENT:
                    tableModel = new InvestmentTableModel(account);
                    Double marketValue = AccountUtil.calculateInvestmentBalance(account, mnyContext);
                    account.setCurrentBalance(new BigDecimal(marketValue));
                    updateEndingBalanceLabel(new BigDecimal(marketValue), account);
                    break;
                default:
                    if (log.isDebugEnabled()) {
                        log.warn("Skip handling unknown accountType=" + accountType);
                    }
                    break;
                }
            }

            if (tableModel == null) {
                tableModel = new DefaultAccountViewerTableModel(account);
            }

            tableModel.setMnyContext(mnyContext);

            dataModel.setTableModel(tableModel);

            updateAccountInfoPane(account);
            updateAccountJsonPane(account);

            if (transactionQifTextArea != null) {
                transactionQifTextArea.setText("");
            }
            transactionJsonTextArea.setText("");
        } finally {
            long delta = stopWatch.click();
            log.info("< accountSelected, delta=" + delta);
        }
    }

    protected void jdbcDump() {
        // JDBC
        if (jdbcConn != null) {
            Statement statement = null;
            try {
                statement = jdbcConn.createStatement();
                ResultSet rs = statement.executeQuery("select * from ACCT");
                dump(rs, "select * from ACCT");
            } catch (SQLException e) {
                log.warn(e);
            } finally {
                if (statement != null) {
                    try {
                        statement.close();
                    } catch (SQLException e) {
                        log.warn(e);
                    } finally {
                        statement = null;
                    }
                }
            }
        }
    }

    private void updateAccountJsonPane(Account account) {
        accountJsonTextArea.setText("");

        if (account == null) {
            return;
        }

        Writer writer = null;
        try {
            writer = new JTextAreaWriter(accountJsonTextArea);
            JSONUtils.writeValue(account, writer);
        } catch (IOException e) {
            log.warn(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    writer = null;
                }
            }
            accountJsonTextArea.setCaretPosition(0);
        }
    }

    private void updateAccountInfoPane(final Account account) {
        accountInfoTextArea.setText("");

        if (account == null) {
            return;
        }

        accountInfoTextArea.append("Name: " + account.getName() + "\n");
        accountInfoTextArea.append("ID: " + account.getId() + "\n");
        accountInfoTextArea.append("Account type: " + account.getAccountType().toString() + "\n");
        accountInfoTextArea.append("Number of transactions: " + account.getTransactions().size() + "\n");
        if (account.getAccountType() == AccountType.INVESTMENT) {
            accountInfoTextArea.append("Retirement: " + account.getRetirement().toString() + "\n");
        }
        accountInfoTextArea.append("Currency: " + AccountUtil.getCurrencyName(account.getCurrencyId(), mnyContext.getCurrencies())
                + "\n");
        accountInfoTextArea.append("Starting balance: " + account.formatAmmount(account.getStartingBalance()) + "\n");
        accountInfoTextArea.append("Ending balance: " + account.formatAmmount(account.getCurrentBalance()) + "\n");

        if (account.getAccountType() == AccountType.INVESTMENT) {
            accountInfoTextArea.append("# SecurityHolding" + "\n");
            List<SecurityHolding> securityHoldings = account.getSecurityHoldings();
            int count = 0;
            for (SecurityHolding securityHolding : securityHoldings) {
                Security security = securityHolding.getSecurity();
                StringBuilder sb = new StringBuilder();
                sb.append("SecurityHolding." + count++ + ": ");
                sb.append(security.getName());
                sb.append(", ");
                sb.append(account.formatSecurityQuantity(securityHolding.getQuantity()));
                sb.append(", ");
                sb.append(account.formatAmmount(securityHolding.getPrice()));
                sb.append(", ");
                sb.append(account.formatAmmount(securityHolding.getMarketValue()));
                sb.append("\n");

                accountInfoTextArea.append(sb.toString());
            }
        }

        Account relatedToAccount = account.getRelatedToAccount();
        if (relatedToAccount != null) {
            accountInfoTextArea.append("# RelatedToAccount" + "\n");
            if (account.getAccountType() == AccountType.INVESTMENT) {
                accountInfoTextArea.append("Cash account: " + relatedToAccount.getName() + "\n");
                accountInfoTextArea.append("Cash account balance: "
                        + relatedToAccount.formatAmmount(relatedToAccount.getCurrentBalance()) + "\n");
            } else {
                accountInfoTextArea.append("Related account: " + relatedToAccount.getName() + "\n");
                accountInfoTextArea.append("Related account balance: "
                        + relatedToAccount.formatAmmount(relatedToAccount.getCurrentBalance()) + "\n");
            }
        }

        if (account.getAccountType() == AccountType.CREDIT_CARD) {
            accountInfoTextArea.append("# Credit Card Info" + "\n");
            BigDecimal amountLimit = account.getAmountLimit();
            if (amountLimit == null) {
                amountLimit = new BigDecimal(0.0);
            }
            accountInfoTextArea.append("Credit card limit amount: "
                    + account.formatAmmount(new BigDecimal(Math.abs(amountLimit.doubleValue()))) + "\n");
        }

        accountInfoTextArea.setCaretPosition(0);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // try {
        // Class.forName("net.ucanaccess.jdbc.UcanaccessDriver");
        // } catch (ClassNotFoundException e1) {
        // log.error(e1);
        // System.exit(1);
        // }

        EventQueue.invokeLater(new Runnable() {

            @Override
            public void run() {
                try {
                    JavaInfo.logInfo();

                    log.info("> Starting AccountViewer");
                    AccountViewer window = new AccountViewer();
                    showMainFrame(window);

                    String buildNumber = SunriiseBuildNumber.getBuildnumber();
                    log.info("BuildNumber: " + buildNumber);
                    
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            protected void showMainFrame(AccountViewer window) {
                JFrame mainFrame = window.getFrame();

                String title = com.le.sunriise.viewer.MynViewer.TITLE_NO_OPENED_DB;
                mainFrame.setTitle(title);

                Dimension preferredSize = new Dimension(1000, 800);
                mainFrame.setPreferredSize(preferredSize);

                mainFrame.pack();

                mainFrame.setLocationRelativeTo(null);

                mainFrame.setVisible(true);
                log.info(" setVisible to true");
            }
        });
    }

    private JLabel getStartingBalanceLabel() {
        return startingBalanceLabel;
    }

    private JLabel getEndingBalanceLabel() {
        return endingBalanceLabel;
    }

    protected void rowSelected(int r) {
        int rowIndex = r;
        int columnIndex = 0;
        AbstractAccountViewerTableModel tableModel = (AbstractAccountViewerTableModel) dataModel.getTableModel();
        final Integer id = (Integer) tableModel.getValueAt(rowIndex, columnIndex);
        if (log.isDebugEnabled()) {
            log.debug("id=" + id);
        }
        final Account account = tableModel.getAccount();
        final List<Transaction> transactions = account.getTransactions();

        Runnable command = new Runnable() {

            @Override
            public void run() {
                logFlags(id);
                logDetails(id);
            }

            private void logDetails(Integer id) {
                // TODO: Better look up
                for (Transaction transaction : transactions) {
                    if (transaction.getId().equals(id)) {
                        log.info("Selected transactionId=" + transaction.getId());
                        logQif(transaction);
                        logJson(transaction);
                        break;
                    }
                }
            }

            private void logQif(Transaction transaction) {

                StringWriter stringWriter = new StringWriter();
                PrintWriter writer = null;
                try {
                    writer = new PrintWriter(stringWriter);
                    QifExportUtils.logQif(transaction, mnyContext, writer);
                    writer.flush();
                    final String qifStr = stringWriter.getBuffer().toString();

                    if (log.isDebugEnabled()) {
                        log.debug("Transaction QIF:");
                        log.debug("\n" + qifStr);
                    }

                    if (transactionQifTextArea != null) {
                        Runnable doRun = new Runnable() {

                            @Override
                            public void run() {
                                transactionQifTextArea.setText(qifStr);
                                transactionQifTextArea.setCaretPosition(0);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                    }
                } finally {
                    if (writer != null) {
                        writer.close();
                        writer = null;
                    }
                }
            }

            private void logJson(Transaction transaction) {
                try {
                    final String jsonStr = JSONUtils.valueToString(transaction);

                    if (log.isDebugEnabled()) {
                        log.debug("Transaction JSON:");
                        log.debug("\n" + jsonStr);
                    }

                    if (transactionJsonTextArea != null) {
                        Runnable doRun = new Runnable() {

                            @Override
                            public void run() {
                                transactionJsonTextArea.setText(jsonStr);
                                transactionJsonTextArea.setCaretPosition(0);
                            }
                        };
                        SwingUtilities.invokeLater(doRun);
                    }
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                }
            }

            private void logFlags(final Integer id) {
                try {
                    Map<String, Object> row = getTransactionRow(openedDb, id);
                    if (row != null) {
                        StringBuilder sb = new StringBuilder();
                        String[] keys = { "act", "grftt", "grftf" };
                        int i = 0;
                        for (String key : keys) {
                            if (i > 0) {
                                sb.append(", ");
                            }
                            sb.append(key + "=" + row.get(key));
                            i++;
                        }
                        log.info(sb.toString());
                    }
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        };
        threadPool.execute(command);
    }

    public JLabel getAccountTypeLabel() {
        return accountTypeLabel;
    }

    protected void appClosed() {
        if (openedDb != null) {
            openedDb.close();
        }
        openedDb = null;

        if (jdbcConn != null) {
            try {
                jdbcConn.close();
            } catch (SQLException e) {
                log.warn(e);
            }
            jdbcConn = null;
        }
    }

    private class SwingAction extends AbstractAction {
        public SwingAction() {
            putValue(NAME, "SwingAction");
            putValue(SHORT_DESCRIPTION, "Some short description");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
        }
    }

    public OpenedDb getOpenedDb() {
        return openedDb;
    }
}
