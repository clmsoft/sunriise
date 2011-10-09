package com.le.sunriise.accountviewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.ScrollPane;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

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
import javax.swing.JTextPane;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.apache.log4j.Logger;
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
import com.le.sunriise.StopWatch;
import com.le.sunriise.model.bean.AccountViewerDataModel;
import com.le.sunriise.qif.QifExportUtils;
import com.le.sunriise.viewer.MyTableCellRenderer;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public class AccountViewer {
    private static final Logger log = Logger.getLogger(AccountViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(AccountViewer.class);

    private JFrame frame;

    private OpenedDb openedDb = new OpenedDb();

//    private boolean dbReadOnly;

    private AccountViewerDataModel dataModel = new AccountViewerDataModel();
    private JList accountList;
    private JTable table;

    private MnyContext mnyContext = new MnyContext();

    private JLabel accountTypeLabel;
    private JLabel startingBalanceLabel;
    private JLabel endingBalanceLabel;

    private Account selectedAccount;

    private JTextPane accountInfoTextPane;

    private JTextArea transactionQifTextArea;

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
            } else {
                getFrame().setTitle("No opened db");
            }

            try {
                List<Account> accounts = AccountUtil.initMnyContext(openedDb, mnyContext);

                AccountViewer.this.dataModel.setAccounts(accounts);

                Runnable doRun = new Runnable() {
                    @Override
                    public void run() {
                        Account account = null;
                        // clear out currently select account, if any
                        try {
                            accountSelected(account);
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
        getFrame().setPreferredSize(new Dimension(1000, 800));
        // frame.setBounds(100, 100, 450, 300);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        initMainMenuBar();

        JPanel leftComponent = createLeftComponent();
        JPanel rightComponent = createRightComponent();
        JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);
        hSplitPane.setResizeWeight(0.3);
        getFrame().getContentPane().add(hSplitPane, BorderLayout.CENTER);
        hSplitPane.setDividerLocation(0.3);

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
                System.exit(0);
            }
        });

        JMenuItem fileOpenMenuItem = new JMenuItem("Open");
        fileOpenMenuItem.addActionListener(new MyOpenDbAction(AccountViewer.this.frame, prefs, openedDb));

        fileMenu.add(fileOpenMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
    }

    private JPanel createLeftComponent() {
        JPanel leftComponent = new JPanel();
        leftComponent.setPreferredSize(new Dimension(80, -1));
        leftComponent.setLayout(new BorderLayout());

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

        vSplitPane.setResizeWeight(0.66);
        view.add(vSplitPane, BorderLayout.CENTER);
        vSplitPane.setDividerLocation(0.66);

        return view;
    }

    private Component createTopComponent() {
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.addTab("Transactions", createTransactionsView());
        tabbedPane.addTab("Account info", createAccountInfoView());

        return tabbedPane;
    }

    private Component createAccountInfoView() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        accountInfoTextPane = new JTextPane();
        accountInfoTextPane.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(accountInfoTextPane);

        view.add(scrollPane, BorderLayout.CENTER);

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
                String renderedValue = ((value == null) || (selectedAccount == null)) ? "" : selectedAccount.formatAmmount((BigDecimal) value);
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
                        log.debug(String.format(" %d", r));
                    }
                    log.info(". Columns:");
                    for (int c : table.getSelectedColumns()) {
                        log.debug(String.format(" %d", c));
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
        JTabbedPane tabPane = new JTabbedPane();
        tabPane.addTab("Transaction info", createTransactionInfoView());
        tabPane.addTab("QIF", createTransactionQif());

        return tabPane;
    }

    private Component createTransactionQif() {
        JPanel view = new JPanel();
        view.setLayout(new BorderLayout());

        ScrollPane scrollPane = new ScrollPane();

        transactionQifTextArea = new JTextArea();
        transactionQifTextArea.setEditable(false);

        scrollPane.add(transactionQifTextArea);
        view.add(scrollPane, BorderLayout.CENTER);

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
        if (cursor.findRow(rowPattern)) {
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

    private void calculateMonthlySummary(Account account) {
        List<Transaction> transactions = account.getTransactions();
        Date previousDate = null;
        SimpleDateFormat dateFormatter = new SimpleDateFormat("MM/dd/yyyy");

        int rowIndex = 0;

        int entries = 0;
        BigDecimal monthlyBalance = new BigDecimal(0);
        for (Transaction transaction : transactions) {
            // if (transaction.isVoid()) {
            // rowIndex++;
            // continue;
            // }
            if (transaction.isRecurring()) {
                rowIndex++;
                continue;
            }
            entries++;

            Date date = transaction.getDate();
            if (previousDate != null) {
                Calendar cal = Calendar.getInstance();

                cal.setTime(previousDate);
                int previousMonth = cal.get(Calendar.MONTH);

                cal.setTime(date);
                int month = cal.get(Calendar.MONTH);

                if (month != previousMonth) {
                    log.info(dateFormatter.format(previousDate) + ", entries=" + entries + ", monthlyBalance=" + monthlyBalance + ", balance="
                            + AccountUtil.getRunningBalance(rowIndex - 1, account));
                    entries = 0;
                    monthlyBalance = new BigDecimal(0);
                }
            }
            previousDate = date;

            BigDecimal amount = transaction.getAmount();
            if (transaction.isVoid()) {
                amount = new BigDecimal(0);
            }
            if (amount == null) {
                amount = new BigDecimal(0);
            }
            monthlyBalance = monthlyBalance.add(amount);

            rowIndex++;
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
        JListBinding<Account, AccountViewerDataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, dataModel,
                accountViewerDataModelBeanProperty, accountList);
        jListBinding.bind();
        //
        BeanProperty<AccountViewerDataModel, TableModel> accountViewerDataModelBeanProperty_1 = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<AccountViewerDataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel,
                accountViewerDataModelBeanProperty_1, table, jTableEvalutionProperty);
        autoBinding.bind();
    }

    protected void accountSelected(final Account account) throws IOException {
        StopWatch stopWatch = new StopWatch();
        log.info("> accountSelected");
        selectedAccount = account;

        try {
            if (account != null) {
                log.info("select account=" + account);
                AccountUtil.getTransactions(openedDb.getDb(), account);

                BigDecimal currentBalance = AccountUtil.calculateCurrentBalance(account);

                log.info(account.getName() + ", " + account.getAccountType() + ", " + Currency.getName(account.getCurrencyId(), mnyContext.getCurrencies())
                        + ", " + account.getStartingBalance() + ", " + currentBalance + ", " + account.getAmountLimit());

                updateStartingBalanceLabel(account.getStartingBalance(), account);
                updateEndingBalanceLabel(currentBalance, account);

                getAccountTypeLabel().setText(account.getAccountType().toString());

                boolean calculateMonthlySummary = false;
                if (calculateMonthlySummary) {
                    calculateMonthlySummary(account);
                }
            }

            AbstractAccountViewerTableModel tableModel = null;

            if (account != null) {
                AccountType accountType = account.getAccountType();
                switch (accountType) {
                case INVESTMENT:
                    tableModel = new AccountViewerTableModel(account) {
                        @Override
                        public int getColumnCount() {
                            return super.getColumnCount() + 1;
                        }

                        @Override
                        public Object getValueAt(int rowIndex, int columnIndex) {
                            Object value = super.getValueAt(rowIndex, columnIndex);

                            List<Transaction> transactions = getAccount().getTransactions();
                            Transaction transaction = transactions.get(rowIndex);

                            switch (columnIndex) {
                            case 5:
                                value = transaction.getQuantity();
                                break;
                            case 6:
                                value = transaction.getPrice();
                                break;
                            case 7:
                                value = transaction.isVoid();
                                break;
                            }
                            return value;
                        }

                        @Override
                        public String getColumnName(int column) {
                            String columnName = super.getColumnName(column);

                            switch (column) {
                            case 2:
                                columnName = "Activity";
                                break;
                            case 3:
                                columnName = "Investment";
                                break;
                            case 5:
                                columnName = "Quantity";
                                break;
                            case 6:
                                columnName = "Price";
                                break;
                            case 7:
                                columnName = "Voided";
                                break;
                            }
                            return columnName;
                        }
                    };
                    Double marketValue = AccountUtil.calculateInvestmentBalance(account, mnyContext);
                    account.setCurrentBalance(new BigDecimal(marketValue));
                    updateEndingBalanceLabel(new BigDecimal(marketValue), account);
                    break;
                }
            }

            if (tableModel == null) {
                tableModel = new AccountViewerTableModel(account);
            }

            tableModel.setMnyContext(mnyContext);

            dataModel.setTableModel(tableModel);

            updateAccountInfoPane(account);
            
            transactionQifTextArea.setText("");
        } finally {
            long delta = stopWatch.click();
            log.info("< accountSelected, delta=" + delta);
        }
    }

    private void updateAccountInfoPane(Account account) {
        accountInfoTextPane.setText("");

        if (account == null) {
            return;
        }

        StyledDocument doc = accountInfoTextPane.getStyledDocument();

        // Define a keyword attribute
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
        // StyleConstants.setForeground(keyWord, Color.RED);
        StyleConstants.setBackground(keyWord, Color.YELLOW);
        StyleConstants.setBold(keyWord, true);

        try {
            insertKeyValueToStyleDocument("Number of transactions", "" + account.getTransactions().size(), doc, keyWord);
            insertKeyValueToStyleDocument("Name", account.getName(), doc, keyWord);
            insertKeyValueToStyleDocument("Account type", account.getAccountType().toString(), doc, keyWord);
            if (account.getAccountType() == AccountType.INVESTMENT) {
                insertKeyValueToStyleDocument("    Retirement", account.getRetirement().toString(), doc, keyWord);
            }
            insertKeyValueToStyleDocument("Currency", Currency.getName(account.getCurrencyId(), mnyContext.getCurrencies()), doc, keyWord);
            insertKeyValueToStyleDocument("Starting balance", account.formatAmmount(account.getStartingBalance()), doc, keyWord);
            insertKeyValueToStyleDocument("Ending balance", account.formatAmmount(account.getCurrentBalance()), doc, keyWord);
            if (account.getAccountType() == AccountType.INVESTMENT) {
                List<SecurityHolding> securityHolding = account.getSecurityHoldings();
                for (SecurityHolding sec : securityHolding) {
                    StringBuilder sb = new StringBuilder();
                    sb.append("  ");
                    sb.append(sec.getName());
                    sb.append(", ");
                    sb.append(account.formatSecurityQuantity(sec.getQuanity()));
                    sb.append(", ");
                    sb.append(account.formatAmmount(sec.getPrice()));
                    sb.append(", ");
                    sb.append(account.formatAmmount(sec.getMarketValue()));

                    insertKeyValueToStyleDocument(null, sb.toString(), doc, keyWord);
                }
            }

            Account relatedToAccount = account.getRelatedToAccount();
            if (relatedToAccount != null) {
                if (account.getAccountType() == AccountType.INVESTMENT) {
                    insertKeyValueToStyleDocument("Cash account", relatedToAccount.getName(), doc, keyWord);
                    insertKeyValueToStyleDocument("Cash account aalance", relatedToAccount.formatAmmount(relatedToAccount.getCurrentBalance()), doc, keyWord);
                } else {
                    insertKeyValueToStyleDocument("Related account", relatedToAccount.getName(), doc, keyWord);
                    insertKeyValueToStyleDocument("Related account balance", relatedToAccount.formatAmmount(relatedToAccount.getCurrentBalance()), doc, keyWord);
                }
            }

            if (account.getAccountType() == AccountType.CREDIT_CARD) {
                BigDecimal amountLimit = account.getAmountLimit();
                if (amountLimit == null) {
                    amountLimit = new BigDecimal(0.0);
                }
                insertKeyValueToStyleDocument("Limit amount", account.formatAmmount(new BigDecimal(Math.abs(amountLimit.doubleValue()))), doc, keyWord);
            }
        } catch (BadLocationException e) {
            log.warn(e);
        }
    }

    private void insertKeyValueToStyleDocument(String key, String value, StyledDocument doc, SimpleAttributeSet keyWord) throws BadLocationException {
        boolean newLine = true;
        insertKeyValueToStyleDocument(key, value, doc, keyWord, newLine);
    }

    private void insertKeyValueToStyleDocument(String key, String value, StyledDocument doc, SimpleAttributeSet keyWord, boolean newLine)
            throws BadLocationException {
        if (newLine) {
            doc.insertString(doc.getLength(), "\n", null);
        }
        if (key != null) {
            doc.insertString(doc.getLength(), key + ":", keyWord);
        }
        doc.insertString(doc.getLength(), " " + value, null);
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    AccountViewer window = new AccountViewer();
                    window.getFrame().pack();
                    window.getFrame().setLocationRelativeTo(null);
                    window.getFrame().setVisible(true);
                } catch (Exception e) {
                    log.error(e, e);
                }
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
                logQif(id);
            }

            private void logQif(Integer id) {
                // TODO: Better look up
                for (Transaction transaction : transactions) {
                    if (transaction.getId().equals(id)) {
                        log.info("Selected transactionId=" + transaction.getId());
                        StringWriter stringWriter = new StringWriter();
                        PrintWriter writer = null;
                        try {
                            writer = new PrintWriter(stringWriter);
                            QifExportUtils.logQif(transaction, mnyContext, writer);
                            writer.flush();
                            final String qifStr = stringWriter.getBuffer().toString();

                            log.info("Transaction QIF:");
                            log.info("\n" + qifStr);

                            if (transactionQifTextArea != null) {
                                Runnable doRun = new Runnable() {
                                    @Override
                                    public void run() {
                                        transactionQifTextArea.setText(qifStr);
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
                        break;
                    }
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
}
