package com.le.sunriise.md;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
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
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
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

import com.le.sunriise.model.bean.AccountViewerDataModel;
import com.le.sunriise.viewer.MyTableCellRenderer;
import com.le.sunriise.viewer.OpenDbAction;
import com.le.sunriise.viewer.OpenDbDialog;
import com.le.sunriise.viewer.OpenedDb;

public class AccountViewer {
    private static final Logger log = Logger.getLogger(AccountViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(AccountViewer.class);

    private JFrame frame;

    private OpenedDb openedDb = new OpenedDb();

    private boolean dbReadOnly;

    private AccountViewerDataModel dataModel = new AccountViewerDataModel();
    private JList list;
    private JTable table;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
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
        getFrame().setPreferredSize(new Dimension(800, 600));
        // frame.setBounds(100, 100, 450, 300);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

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
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                final Account account = (Account) list.getSelectedValue();
                if (account != null) {
                    try {
                        log.info("select account=" + account);
                        ExportAccountsToMd exporter = new ExportAccountsToMd();
                        List<Transaction> transactions = exporter.getTransactions(openedDb.getDb(), account);
                        account.setTransactions(transactions);
                        BigDecimal currentBalance = exporter.calculateCurrentBalance(account);
                        log.info(account.getName() + ", " + account.getStartingBalance() + ", " + currentBalance);

                        TableModel tableModel = new AccountViewerTableModel(account);
                        dataModel.setTableModel(tableModel);
                    } catch (IOException e) {
                        log.error(e);
                    }
                }
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.setVisibleRowCount(-1);
        scrollPane.setViewportView(list);

        hSplitPane.setResizeWeight(0.2);
        getFrame().getContentPane().add(hSplitPane, BorderLayout.CENTER);
        hSplitPane.setDividerLocation(0.2);

        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);

        JMenu fileMenu = new JMenu("File");
        menuBar.add(fileMenu);

        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });

        JMenuItem fileOpenMenuItem = new JMenuItem("Open");
        fileOpenMenuItem.addActionListener(new OpenDbAction(AccountViewer.this.frame, prefs, openedDb) {

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

                ExportAccountsToMd exporter = new ExportAccountsToMd();
                try {
                    List<Account> accounts = exporter.readAccounts(openedDb.getDb());
                    Comparator<Account> comparator = new Comparator<Account>() {
                        public int compare(Account o1, Account o2) {
                            return o1.getName().compareTo(o2.getName());
                        }
                    };
                    Collections.sort(accounts, comparator);
                    AccountViewer.this.dataModel.setAccounts(accounts);
                } catch (IOException e) {
                    log.warn(e);
                }
            }
        });

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

                // MnyTableModel mnyTableModel = MynViewer.this.tableModel;
                // IndexLookup indexLookup = new IndexLookup();

                for (int i = 0; i < cols; i++) {
                    TableColumn column = columnModel.getColumn(i);
                    MyTableCellRenderer renderer = new MyTableCellRenderer(column.getCellRenderer());
                    column.setCellRenderer(renderer);

                    // if (mnyTableModel.columnIsDateType(i)) {
                    // if (log.isDebugEnabled()) {
                    // log.debug("columnIsDateType, i=" + i);
                    // }
                    // // TableCellEditor cellEditor = new
                    // // TableCellDateEditor();
                    // // TableCellEditor cellEditor = new
                    // // DatePickerTableEditor();
                    // TableCellEditor cellEditor = new DialogCellEditor();
                    // column.setCellEditor(cellEditor);
                    // }

                    // if (mnyTableModel.isPrimaryKeyColumn(i)) {
                    // TableCellRenderer headerRenderer = new
                    // MyTabletHeaderRenderer(table, column.getHeaderRenderer(),
                    // Color.RED);
                    // column.setHeaderRenderer(headerRenderer);
                    // }
                    // if (mnyTableModel.isForeignKeyColumn(i)) {
                    // TableCellRenderer headerRenderer = new
                    // MyTabletHeaderRenderer(table, column.getHeaderRenderer(),
                    // Color.BLUE);
                    // column.setHeaderRenderer(headerRenderer);
                    // }
                }
            }

        };
        scrollPane_1.setViewportView(table);
        vSplitPane.setResizeWeight(0.66);
        rightComponent.add(vSplitPane, BorderLayout.CENTER);
        vSplitPane.setDividerLocation(0.66);
        initDataBindings();
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
}
