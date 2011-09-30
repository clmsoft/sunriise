package com.le.sunriise.md;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.google.gwt.dev.util.collect.HashMap;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.StopWatch;
import com.le.sunriise.model.bean.AccountViewerDataModel;
import com.le.sunriise.viewer.MyTableCellRenderer;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;
import javax.swing.JLabel;

public class AccountViewer {
    private static final Logger log = Logger.getLogger(AccountViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(AccountViewer.class);

    private JFrame frame;

    private OpenedDb openedDb = new OpenedDb();

    private boolean dbReadOnly;

    private AccountViewerDataModel dataModel = new AccountViewerDataModel();
    private JList list;
    private JTable table;

    private MnyContext mnyContext = new MnyContext();

    private JLabel startingBalanceLabel;
    private JLabel endingBalanceLabel;

    private Account selectedAccount;

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
                Database db = openedDb.getDb();
                mnyContext.setDb(db);

                Map<Integer, Payee> payees = AccountUtil.getPayees(db);
                mnyContext.setPayees(payees);

                Map<Integer, Category> categories = AccountUtil.getCategories(db);
                mnyContext.setCategories(categories);

                Map<Integer, Currency> currencies = AccountUtil.getCurrencies(db);
                mnyContext.setCurrencies(currencies);

                Map<Integer, Security> securities = AccountUtil.getSecurities(db);
                mnyContext.setSecurities(securities);

                List<Account> accounts = AccountUtil.getAccounts(db);
                AccountUtil.setCurrencies(accounts, currencies);

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

        // NumberFormat formatter = NumberFormat.getInstance();
        // if (formatter instanceof DecimalFormat) {
        // amountFormatter = (DecimalFormat) formatter;
        // amountFormatter.applyPattern("#,###,##0.00;(#,###,##0.00)");
        // amountFormatter.setGroupingUsed(true);
        // }

        JPanel leftComponent = new JPanel();
        leftComponent.setPreferredSize(new Dimension(80, -1));
        leftComponent.setLayout(new BorderLayout());

        JPanel rightComponent = new JPanel();
        rightComponent.setLayout(new BorderLayout());

        JSplitPane hSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftComponent, rightComponent);

        JScrollPane scrollPane = new JScrollPane();
        leftComponent.add(scrollPane, BorderLayout.CENTER);

        list = new JList();
        list.addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                final Account account = (Account) list.getSelectedValue();
                if (account != null) {
                    try {
                        accountSelected(account);
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }

        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
        scrollPane.setViewportView(list);

        hSplitPane.setResizeWeight(0.3);
        getFrame().getContentPane().add(hSplitPane, BorderLayout.CENTER);
        hSplitPane.setDividerLocation(0.3);

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

        JPanel topComponent = new JPanel();
        topComponent.setLayout(new BorderLayout());

        JPanel bottomComponent = new JPanel();
        topComponent.setLayout(new BorderLayout());

        JSplitPane vSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, topComponent, bottomComponent);

        JScrollPane scrollPane_1 = new JScrollPane();
        topComponent.add(scrollPane_1, BorderLayout.CENTER);

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
        scrollPane_1.setViewportView(table);

        JPanel panel = new JPanel();
        topComponent.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));

        startingBalanceLabel = new JLabel("");
        updateStartingBalanceLabel(new BigDecimal(0.00), null);
        panel.add(startingBalanceLabel, BorderLayout.EAST);

        JPanel panel_1 = new JPanel();
        topComponent.add(panel_1, BorderLayout.SOUTH);
        panel_1.setLayout(new BorderLayout(0, 0));

        endingBalanceLabel = new JLabel("");
        updateEndingBalanceLabel(new BigDecimal(0.00), null);
        panel_1.add(endingBalanceLabel, BorderLayout.EAST);
        vSplitPane.setResizeWeight(0.66);
        rightComponent.add(vSplitPane, BorderLayout.CENTER);
        vSplitPane.setDividerLocation(0.66);
        initDataBindings();
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
                accountViewerDataModelBeanProperty, list);
        jListBinding.bind();
        //
        BeanProperty<AccountViewerDataModel, TableModel> accountViewerDataModelBeanProperty_1 = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<AccountViewerDataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel,
                accountViewerDataModelBeanProperty_1, table, jTableEvalutionProperty);
        autoBinding.bind();
    }

    private void accountSelected(final Account account) throws IOException {
        StopWatch stopWatch = new StopWatch();
        log.info("> accountSelected");
        selectedAccount = account;

        try {
            if (account != null) {
                log.info("select account=" + account);
                List<Transaction> transactions = AccountUtil.getTransactions(openedDb.getDb(), account);
                account.setTransactions(transactions);

                BigDecimal currentBalance = AccountUtil.calculateCurrentBalance(account);

                log.info(account.getName() + ", " + account.getAccountType() + ", " + Currency.getName(mnyContext.getCurrencies(), account.getCurrencyId())
                        + ", " + account.getStartingBalance() + ", " + currentBalance);

                updateStartingBalanceLabel(account.getStartingBalance(), account);
                updateEndingBalanceLabel(currentBalance, account);

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
                    updateEndingBalanceLabel(new BigDecimal(marketValue), account);
                    break;
                }
            }

            if (tableModel == null) {
                tableModel = new AccountViewerTableModel(account);
            }

            tableModel.setMnyContext(mnyContext);

            dataModel.setTableModel(tableModel);
        } finally {
            long delta = stopWatch.click();
            log.info("< accountSelected, delta=" + delta);
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

    private void rowSelected(int r) {
        int rowIndex = r;
        int columnIndex = 0;
        final Integer id = (Integer) dataModel.getTableModel().getValueAt(rowIndex, columnIndex);
        if (log.isDebugEnabled()) {
            log.debug("id=" + id);
        }
        Runnable command = new Runnable() {
            @Override
            public void run() {
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
}
