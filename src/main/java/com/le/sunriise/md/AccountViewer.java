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
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
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

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.model.bean.AccountViewerDataModel;
import com.le.sunriise.viewer.MyTableCellRenderer;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;
import javax.swing.BoxLayout;
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

    protected List<Account> accounts;

    protected Map<Integer, Payee> payees;

    protected Map<Integer, Category> categories;
    private JLabel startingBalanceLabel;
    private JLabel endingBalanceLabel;

    private DecimalFormat amountFormatter;

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
                accounts = AccountUtil.getAccounts(db);

                payees = AccountUtil.getPayees(db);

                categories = AccountUtil.getCategories(db);

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

        NumberFormat formatter = NumberFormat.getInstance();
        if (formatter instanceof DecimalFormat) {
            amountFormatter = (DecimalFormat) formatter;
            amountFormatter.applyPattern("#,###,##0.00;(#,###,##0.00)");
            amountFormatter.setGroupingUsed(true);
        }

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
                String renderedValue = (value == null) ? "" : amountFormatter.format(value);
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

        scrollPane_1.setViewportView(table);

        JPanel panel = new JPanel();
        topComponent.add(panel, BorderLayout.NORTH);
        panel.setLayout(new BorderLayout(0, 0));

        startingBalanceLabel = new JLabel("");
        updateStartingBalanceLabel(new BigDecimal(0.00));
        panel.add(startingBalanceLabel, BorderLayout.EAST);

        JPanel panel_1 = new JPanel();
        topComponent.add(panel_1, BorderLayout.SOUTH);
        panel_1.setLayout(new BorderLayout(0, 0));

        endingBalanceLabel = new JLabel("");
        updateEndingBalanceLabel(new BigDecimal(0.00));
        panel_1.add(endingBalanceLabel, BorderLayout.EAST);
        vSplitPane.setResizeWeight(0.66);
        rightComponent.add(vSplitPane, BorderLayout.CENTER);
        vSplitPane.setDividerLocation(0.66);
        initDataBindings();
    }

    private void updateEndingBalanceLabel(BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            getEndingBalanceLabel().setText("Ending balance: " + amountFormatter.format(bigDecimal));
        } else {
            getEndingBalanceLabel().setText("Ending balance: ");
        }

    }

    private void updateStartingBalanceLabel(BigDecimal bigDecimal) {
        if (bigDecimal != null) {
            getStartingBalanceLabel().setText("Starting balance: " + amountFormatter.format(bigDecimal));
        } else {
            getStartingBalanceLabel().setText("Starting balance: ");
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
        if (account != null) {
            log.info("select account=" + account);
            ExportAccountsToMd exporter = new ExportAccountsToMd();
            List<Transaction> transactions = AccountUtil.getTransactions(openedDb.getDb(), account);
            account.setTransactions(transactions);
            BigDecimal currentBalance = exporter.calculateCurrentBalance(account);
            log.info(account.getName() + ", " + account.getStartingBalance() + ", " + currentBalance);
            updateStartingBalanceLabel(account.getStartingBalance());
            updateEndingBalanceLabel(currentBalance);
            boolean calculateMonthlySummary = false;
            if (calculateMonthlySummary) {
                calculateMonthlySummary(account);
            }
        }

        AccountViewerTableModel tableModel = new AccountViewerTableModel(account);
        tableModel.setPayees(payees);
        tableModel.setCategories(categories);
        tableModel.setAccounts(accounts);

        dataModel.setTableModel(tableModel);
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

    protected JLabel getStartingBalanceLabel() {
        return startingBalanceLabel;
    }

    protected JLabel getEndingBalanceLabel() {
        return endingBalanceLabel;
    }
}
