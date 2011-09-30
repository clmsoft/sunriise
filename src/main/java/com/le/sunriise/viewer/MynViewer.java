package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
import javax.swing.event.RowSorterEvent;
import javax.swing.event.RowSorterEvent.Type;
import javax.swing.event.RowSorterListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.PageChannel;
import com.healthmarketscience.jackcess.Table;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.StopWatch;
import com.le.sunriise.encryption.EncryptionUtils;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.model.bean.MnyViewerDataModel;
import com.le.sunriise.model.bean.TableListItem;

public class MynViewer {
    private static final Logger log = Logger.getLogger(MynViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(MynViewer.class);

    private static final Executor threadPool = Executors.newCachedThreadPool();

    private JFrame frame;
    // private File dbFile;
    // private Database db;
    private OpenedDb openedDb = new OpenedDb();

    private MnyViewerDataModel dataModel = new MnyViewerDataModel();
    private JList list;
    private JTable table;
    private MnyTableModel tableModel;

    private JTextField textField;

    private boolean dbReadOnly = true;

    // private Pattern tableNamePattern =
    // Pattern.compile("^(.*) \\([0-9]+\\)$");
    private JTextArea textArea;
    private JTextArea headerTextArea;

    private JMenuItem duplicateMenuItem;
    private JMenuItem deleteMenuItem;
    private JTextArea keyInfoTextArea;

    private JTextArea indexInfoTextArea;
    private JTextField filterTextField;

    private TableRowSorter<TableModel> sorter;

    private boolean allowTableSorting = false;

    private JCheckBox filterOnSelectedColumnCheckBox;

    private JLabel rightStatusLabel;

    // private JTextField rightStatusLabel;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // EventQueue waitQueue = new WaitCursorEventQueue(500);
        // Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);

        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MynViewer window = new MynViewer();
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
    public MynViewer() {
        initialize();
    }

    private String parseHeaderInfo(Table table) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Header info");
        sb.append("\n");
        sb.append("\n");

        sb.append("Table: " + table.getName());
        sb.append("\n");
        sb.append("\n");

        Database db = table.getDatabase();

        PageChannel pageChannel = db.getPageChannel();
        ByteBuffer buffer = pageChannel.createPageBuffer();
        pageChannel.readPage(buffer, 0);

        JetFormat format = pageChannel.getFormat();
        sb.append("format=" + format.toString());
        sb.append("\n");

        if (format.CODEC_TYPE == format.CODEC_TYPE.MSISAM) {
            EncryptionUtils.appendMSISAMInfo(buffer, openedDb.getPassword(), openedDb.getDb().getCharset(), sb);
        }

        // 0x00 4
        // ENGINE_NAME_OFFSET 0x04 15
        // OFFSET_VERSION 20 1
        // SALT_OFFSET 0x72 4
        // ENCRYPTION_FLAGS_OFFSET 0x298 1

        return sb.toString();
    }

    private String parseKeyInfo(Table t) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Key info");
        sb.append("\n");
        sb.append("\n");

        sb.append("Table: " + t.getName());
        sb.append("\n");
        sb.append("\n");

        sb.append("# Primary keys:");
        sb.append("\n");
        IndexLookup indexLookup = new IndexLookup();
        for (Column column : t.getColumns()) {
            if (indexLookup.isPrimaryKeyColumn(column)) {
                sb.append("(PK) " + t.getName() + "." + column.getName() + ", " + indexLookup.getMax(column));
                sb.append("\n");

                List<Column> referencing = indexLookup.getReferencing(column);
                for (Column col : referencing) {
                    sb.append("    (referencing-FK) " + col.getTable().getName() + "." + col.getName());
                    sb.append("\n");
                }
            }
        }
        sb.append("\n");

        sb.append("# Foreign keys:");
        sb.append("\n");
        for (Column column : t.getColumns()) {
            List<Column> referenced = indexLookup.getReferencedColumns(column);
            for (Column col : referenced) {
                sb.append("(FK) " + t.getName() + "." + column.getName() + " -> " + col.getTable().getName() + "." + col.getName());
                sb.append("\n");
            }
        }
        sb.append("\n");

        return sb.toString();
    }

    private String parseIndexInfo(Table t) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Index info");
        sb.append("\n");
        sb.append("\n");

        sb.append("Table: " + t.getName());
        sb.append("\n");
        sb.append("\n");

        List<Index> indexes = t.getIndexes();
        sb.append("# Index: (" + indexes.size() + ")");
        sb.append("\n");

        for (Index index : indexes) {
            IndexData indexData = index.getIndexData();
            sb.append("    type=" + indexData.getClass().getName());
            sb.append("\n");
            sb.append("    uniqueEntryCount=" + index.getUniqueEntryCount());
            sb.append("\n");
            // isUnique
            sb.append("    unique=" + index.isUnique());
            sb.append("\n");
            sb.append("    shouldIgnoreNulls=" + index.shouldIgnoreNulls());
            sb.append("\n");

            List<ColumnDescriptor> columns = index.getColumns();
            sb.append("    " + index.getName() + " (" + columns.size() + ")");
            sb.append("\n");
            for (ColumnDescriptor column : columns) {
                sb.append("        " + column.getColumn().getTable().getName() + "." + column.getColumn().getName());
                sb.append("\n");
            }
            sb.append("\n");
        }
        sb.append("\n");

        return sb.toString();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());
        getFrame().setBounds(100, 100, 800, 600);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);
        getFrame().setTitle("No opened db");
        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (getDb() != null) {
                    try {
                        getDb().close();
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        setDb(null);
                    }
                }
                System.exit(1);
            }
        });

        JMenuItem mntmNewMenuItem_1 = new JMenuItem("Open");
        mntmNewMenuItem_1.addActionListener(new OpenDbAction(MynViewer.this.getFrame(), prefs, openedDb) {
            @Override
            public void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog) {
                if (newOpenedDb != null) {
                    MynViewer.this.openedDb = newOpenedDb;
                }

                File dbFile = openedDb.getDbFile();
                if (dbFile != null) {
                    getFrame().setTitle(dbFile.getAbsolutePath());
                } else {
                    getFrame().setTitle("No opened db");
                }

                List<TableListItem> tables = new ArrayList<TableListItem>();
                try {
                    Set<String> names = getDb().getTableNames();
                    for (String name : names) {
                        try {
                            Table t = getDb().getTable(name);
                            TableListItem tableListItem = new TableListItem();
                            tableListItem.setTable(t);
                            tables.add(tableListItem);
                        } catch (IOException e) {
                            log.warn(e);
                        }
                    }
                } catch (IOException e) {
                    log.warn(e);
                }

                dbReadOnly = dialog.getReadOnlyCheckBox().isSelected();

                if (duplicateMenuItem != null) {
                    duplicateMenuItem.setEnabled(!dbReadOnly);
                }
                if (deleteMenuItem != null) {
                    deleteMenuItem.setEnabled(!dbReadOnly);
                }
                MynViewer.this.dataModel.setTables(tables);
                clearDataModel(MynViewer.this.dataModel);
            }

        });
        mnNewMenu.add(mntmNewMenuItem_1);

        JMenu mnNewMenu_1 = new JMenu("Export DB");
        mnNewMenu.add(mnNewMenu_1);

        JMenuItem mntmNewMenuItem_3 = new JMenuItem("To CSV");
        mntmNewMenuItem_3.addActionListener(new ExportToCsvAction(this));
        mnNewMenu_1.add(mntmNewMenuItem_3);

        JMenuItem mntmNewMenuItem_4 = new JMenuItem("To *.mdb");
        mntmNewMenuItem_4.addActionListener(new ExportToMdbAction(this));
        mnNewMenu_1.add(mntmNewMenuItem_4);

        JSeparator separator_1 = new JSeparator();
        mnNewMenu.add(separator_1);
        mnNewMenu.add(mntmNewMenuItem);

        JPanel statusPane = new JPanel();
        frame.getContentPane().add(statusPane, BorderLayout.SOUTH);
        statusPane.setLayout(new BorderLayout(0, 0));

        rightStatusLabel = new JLabel("...");
        // rightStatusLabel = new JTextField(50);
        // rightStatusLabel.setEditable(false);
        int alignment = SwingConstants.RIGHT;
        rightStatusLabel.setHorizontalAlignment(alignment);
        statusPane.add(rightStatusLabel, BorderLayout.EAST);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.33);
        splitPane.setDividerLocation(0.33);
        getFrame().getContentPane().add(splitPane, BorderLayout.CENTER);

        JPanel leftView = new JPanel();
        leftView.setPreferredSize(new Dimension(80, -1));
        splitPane.setLeftComponent(leftView);
        leftView.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane = new JScrollPane();
        leftView.add(scrollPane);

        list = new JList();
        list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        list.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                try {
                    TableListItem item = (TableListItem) list.getSelectedValue();
                    if (item != null) {
                        final Table table = item.getTable();
                        String tableName = table.getName();
                        log.info("> new table is selected, table=" + tableName);

                        dataModel.setTable(table);
                        dataModel.setTableName(tableName);
                        dataModel.setTableMetaData(parseTableMetaData(table));
                        dataModel.setHeaderInfo(parseHeaderInfo(table));
                        dataModel.setKeyInfo(parseKeyInfo(table));
                        dataModel.setIndexInfo(parseIndexInfo(table));

                        if (log.isDebugEnabled()) {
                            log.debug("clearing old filter text ...");
                        }
                        filterTextField.setText("");
                        if (tableModel != null) {
                            try {
                                tableModel.close();
                            } finally {
                                tableModel = null;
                            }
                        }

                        if (log.isDebugEnabled()) {
                            log.debug("creating new tableModel ...");
                        }
                        tableModel = new MnyTableModel(table);
                        tableModel.setDbReadOnly(dbReadOnly);

                        toggleTableSorting();

                        if (log.isDebugEnabled()) {
                            log.debug("setting new tableModel ...");
                        }
                        dataModel.setTableModel(tableModel);
                        rightStatusLabel.setText("open table=" + table.getName());
                    }
                } catch (IOException e) {
                    log.error(e);
                }
            }
        });
        list.setVisibleRowCount(-1);
        scrollPane.setViewportView(list);

        JPanel panel = new JPanel();
        splitPane.setRightComponent(panel);
        panel.setLayout(new BorderLayout(0, 0));

        JPanel panel_1 = new JPanel();
        panel.add(panel_1, BorderLayout.NORTH);
        // panel_1.setPreferredSize(new Dimension(100, 100));
        panel_1.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

        JLabel lblNewLabel = new JLabel("Table Name");
        panel_1.add(lblNewLabel, "2, 2, right, default");

        textField = new JTextField();
        textField.setEditable(false);
        panel_1.add(textField, "4, 2, fill, default");
        textField.setColumns(10);

        JTabbedPane tabbedPane = new JTabbedPane(SwingConstants.TOP);
        panel.add(tabbedPane, BorderLayout.CENTER);
        JPopupMenu tablePopupMenu = new JPopupMenu();
        JMenuItem menuItem = null;
        menuItem = new JMenuItem(new AbstractAction("Duplicate") {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                duplicateRow(rowIndex);
            }
        });
        tablePopupMenu.add(menuItem);
        this.duplicateMenuItem = menuItem;

        tablePopupMenu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                deleteRow(rowIndex);
            }
        });
        tablePopupMenu.add(menuItem);
        this.deleteMenuItem = menuItem;

        tablePopupMenu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Copy Column") {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                int columnIndex = table.getSelectedColumn();
                copyColumn(rowIndex, columnIndex);
            }
        });
        tablePopupMenu.add(menuItem);

        MouseListener tablePopupListener = new PopupListener(tablePopupMenu);

        JPanel panel_6 = new JPanel();
        tabbedPane.addTab("Rows", null, panel_6, null);
        panel_6.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_6.add(scrollPane_1);

        table = new JTable() {

            @Override
            public void setModel(TableModel dataModel) {
                super.setModel(dataModel);
                TableColumnModel columnModel = this.getColumnModel();
                int cols = columnModel.getColumnCount();

                MnyTableModel mnyTableModel = MynViewer.this.tableModel;
                // IndexLookup indexLookup = new IndexLookup();

                for (int i = 0; i < cols; i++) {
                    TableColumn column = columnModel.getColumn(i);
                    MyTableCellRenderer renderer = new MyTableCellRenderer(column.getCellRenderer());
                    column.setCellRenderer(renderer);

                    if (mnyTableModel.columnIsDateType(i)) {
                        if (log.isDebugEnabled()) {
                            log.debug("columnIsDateType, i=" + i);
                        }
                        // TableCellEditor cellEditor = new
                        // TableCellDateEditor();
                        // TableCellEditor cellEditor = new
                        // DatePickerTableEditor();
                        TableCellEditor cellEditor = new DialogCellEditor();
                        column.setCellEditor(cellEditor);
                    }

                    if (mnyTableModel.isPrimaryKeyColumn(i)) {
                        TableCellRenderer headerRenderer = new MyTabletHeaderRenderer(table, column.getHeaderRenderer(), Color.RED);
                        column.setHeaderRenderer(headerRenderer);
                    }
                    if (mnyTableModel.isForeignKeyColumn(i)) {
                        TableCellRenderer headerRenderer = new MyTabletHeaderRenderer(table, column.getHeaderRenderer(), Color.BLUE);
                        column.setHeaderRenderer(headerRenderer);
                    }
                }
            }

        };
        // table.setAutoCreateRowSorter(true);
        // RowSorter<TableModel> sorter = new
        // TableRowSorter<TableModel>(tableModel);
        // table.setRowSorter(sorter);

        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy/MM/dd");
            private DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm");

            @Override
            public void setValue(Object value) {
                if (log.isDebugEnabled()) {
                    log.debug("cellRenderer: value=" + value + ", " + value.getClass().getName());
                }
                if (formatter == null) {
                    formatter = DateFormat.getDateInstance();
                }
                String renderedValue = (value == null) ? "" : formatter.format(value);
                if (log.isDebugEnabled()) {
                    log.debug("cellRenderer: renderedValue=" + renderedValue);
                }

                setText(renderedValue);
            }
        });
        table.addMouseListener(tablePopupListener);

        // TODO: try to install our mouselistener first
        // insertListenerToHead();

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane_1.setViewportView(table);

        JPanel panel_7 = new JPanel();
        panel_7.setBorder(new EmptyBorder(3, 3, 3, 3));
        panel_6.add(panel_7, BorderLayout.SOUTH);
        panel_7.setLayout(new BoxLayout(panel_7, BoxLayout.LINE_AXIS));

        JLabel lblNewLabel_1 = new JLabel("Filter Text");
        panel_7.add(lblNewLabel_1);

        JPopupMenu filterPopupMenu = new JPopupMenu();
        JCheckBoxMenuItem checkBoxMenuItem = new JCheckBoxMenuItem(new AbstractAction("Enable Sorting/Filtering") {
            public void actionPerformed(ActionEvent e) {
                AbstractButton aButton = (AbstractButton) e.getSource();
                boolean selected = aButton.getModel().isSelected();
                if (selected == allowTableSorting) {
                    // no change
                    log.warn("No change in allowTableSorting=" + allowTableSorting);
                    return;
                }

                allowTableSorting = selected;

                toggleTableSorting();
            }
        });
        checkBoxMenuItem.setSelected(allowTableSorting);
        filterPopupMenu.add(checkBoxMenuItem);
        lblNewLabel_1.addMouseListener(new PopupListener(filterPopupMenu));

        Component horizontalStrut = Box.createHorizontalStrut(5);
        panel_7.add(horizontalStrut);

        filterTextField = new JTextField();
        filterTextField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                JTextField tf = (JTextField) event.getSource();
                String text = tf.getText();

                RowFilter<Object, Object> rf = null;
                if ((text == null) || (text.length() <= 0)) {
                    rf = null;
                } else {
                    // If current expression doesn't parse, don't update.
                    try {
                        if (filterOnSelectedColumnCheckBox.isSelected()) {
                            int selectedColumn = table.getSelectedColumn();
                            if (selectedColumn >= 0) {
                                log.info("filter for text=" + text + ", selectedColumn=" + selectedColumn);
                                rf = RowFilter.regexFilter(text, selectedColumn);
                            } else {
                                log.info("filter for text=" + text);
                                rf = RowFilter.regexFilter(text);
                            }
                        } else {
                            log.info("filter for text=" + text);
                            rf = RowFilter.regexFilter(text);
                        }
                    } catch (java.util.regex.PatternSyntaxException e) {
                        log.warn(e);
                        return;
                    }
                }
                if (sorter != null) {
                    StopWatch stopwatch = new StopWatch();
                    int preViewRowCount = sorter.getViewRowCount();
                    try {
                        log.info("> setRowFilter");
                        sorter.setRowFilter(rf);
                    } finally {
                        long delta = stopwatch.click();
                        int postViewRowCount = sorter.getViewRowCount();
                        rightStatusLabel.setText("filter: rows=" + postViewRowCount + "/" + preViewRowCount + ", millisecond=" + delta);
                        log.info("< setRowFilter, delta=" + delta);
                    }
                }
            }
        });
        panel_7.add(filterTextField);
        filterTextField.setColumns(10);

        Component horizontalStrut_1 = Box.createHorizontalStrut(5);
        panel_7.add(horizontalStrut_1);

        filterOnSelectedColumnCheckBox = new JCheckBox("on selected column");
        filterOnSelectedColumnCheckBox.setSelected(true);
        panel_7.add(filterOnSelectedColumnCheckBox);

        toggleTableSorting();

        JPanel panel_2 = new JPanel();
        tabbedPane.addTab("Meta Data", null, panel_2, null);
        panel_2.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_2 = new JScrollPane();
        panel_2.add(scrollPane_2, BorderLayout.CENTER);

        textArea = new JTextArea();
        scrollPane_2.setViewportView(textArea);

        JPanel panel_3 = new JPanel();
        tabbedPane.addTab("Header", null, panel_3, null);
        panel_3.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_3 = new JScrollPane();
        panel_3.add(scrollPane_3, BorderLayout.CENTER);

        headerTextArea = new JTextArea();
        scrollPane_3.setViewportView(headerTextArea);

        JPanel panel_4 = new JPanel();
        tabbedPane.addTab("Keys", null, panel_4, null);
        panel_4.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_4 = new JScrollPane();
        panel_4.add(scrollPane_4, BorderLayout.CENTER);

        keyInfoTextArea = new JTextArea();
        scrollPane_4.setViewportView(keyInfoTextArea);

        JPanel panel_5 = new JPanel();
        tabbedPane.addTab("Indexes", null, panel_5, null);
        panel_5.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_5 = new JScrollPane();
        panel_5.add(scrollPane_5);

        indexInfoTextArea = new JTextArea();
        scrollPane_5.setViewportView(indexInfoTextArea);

        initDataBindings();
    }

    protected String parseTableMetaData(Table table) {
        StringBuilder sb = new StringBuilder();

        int pageCount = table.getApproximateOwnedPageCount();

        sb.append("pageCount=" + pageCount);
        sb.append("\n");

        sb.append(table.toString());

        return sb.toString();
    }

    private void insertListenerToHead() {
        final JTableHeader tableHeader = table.getTableHeader();
        MouseListener[] mouseListeners = tableHeader.getMouseListeners();
        if (mouseListeners != null) {
            insertListenerToHead(tableHeader, mouseListeners);
        }
    }

    private void insertListenerToHead(final JTableHeader tableHeader, MouseListener[] mouseListeners) {
        for (MouseListener mouseListener : mouseListeners) {
            tableHeader.removeMouseListener(mouseListener);
        }
        MouseListener l = null;
        l = new MouseInputAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() % 2 == 1 && SwingUtilities.isLeftMouseButton(e)) {
                    JTable table = tableHeader.getTable();
                    RowSorter sorter;
                    if (table != null && (sorter = table.getRowSorter()) != null) {
                        int columnIndex = tableHeader.columnAtPoint(e.getPoint());
                        if (columnIndex != -1) {
                            columnIndex = table.convertColumnIndexToModel(columnIndex);
                            // sorter.toggleSortOrder(columnIndex);
                            log.info("> mouseClicked to sort");
                        }
                    }
                }
            }

        };
        tableHeader.addMouseListener(l);
        for (MouseListener mouseListener : mouseListeners) {
            tableHeader.addMouseListener(mouseListener);
        }
    }

    protected void clearDataModel(MnyViewerDataModel dataModel) {
        dataModel.setHeaderInfo("");
        dataModel.setIndexInfo("");
        dataModel.setKeyInfo("");
        dataModel.setTable(null);
        dataModel.setTableMetaData("");
        dataModel.setTableModel(new AbstractTableModel() {
            public Object getValueAt(int rowIndex, int columnIndex) {
                return null;
            }

            public int getRowCount() {
                return 0;
            }

            public int getColumnCount() {
                return 0;
            }
        });
        dataModel.setTableName("");
        // dataModel.setTables(null);

    }

    protected void copyColumn(int rowIndex, int columnIndex) {
        if (tableModel != null) {
            tableModel.copyColumn(table.convertRowIndexToModel(rowIndex), columnIndex);
        }
    }

    protected void deleteRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.deleteRow(table.convertRowIndexToModel(rowIndex));
        }
    }

    protected void duplicateRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.duplicateRow(table.convertRowIndexToModel(rowIndex), this.getFrame());
        }
    }

    public static Class getColumnJavaClass(Column column) {
        Class clz = null;
        DataType type = column.getType();
        if (type == DataType.BOOLEAN) {
            clz = Boolean.class;
        } else if (type == DataType.BYTE) {
            clz = Byte.class;
        } else if (type == DataType.INT) {
            clz = Integer.class;
        } else if (type == DataType.LONG) {
            clz = Long.class;
        } else if (type == DataType.DOUBLE) {
            clz = Double.class;
        } else if (type == DataType.FLOAT) {
            clz = Float.class;
        } else if (type == DataType.SHORT_DATE_TIME) {
            clz = Date.class;
        } else if (type == DataType.BINARY) {
            clz = byte[].class;
        } else if (type == DataType.TEXT) {
            clz = String.class;
        } else if (type == DataType.MONEY) {
            clz = BigDecimal.class;
        } else if (type == DataType.OLE) {
            clz = byte[].class;
        } else if (type == DataType.MEMO) {
            clz = String.class;
        } else if (type == DataType.NUMERIC) {
            clz = BigDecimal.class;
        } else if (type == DataType.GUID) {
            clz = String.class;
        } else if ((type == DataType.UNKNOWN_0D) || (type == DataType.UNKNOWN_11)) {
            clz = Object.class;
        } else {
            clz = Object.class;
            log.warn("Unrecognized data type: " + type);
        }
        return clz;
    }

    public void setDb(Database db) {
        openedDb.setDb(db);
    }

    public Database getDb() {
        return openedDb.getDb();
    }

    public void setFrame(JFrame frame) {
        this.frame = frame;
    }

    public JFrame getFrame() {
        return frame;
    }

    public static Executor getThreadPool() {
        return threadPool;
    }

    public OpenedDb getOpenedDb() {
        return openedDb;
    }

    public void setOpenedDb(OpenedDb openedDb) {
        this.openedDb = openedDb;
    }

    protected void initDataBindings() {
        BeanProperty<MnyViewerDataModel, List<TableListItem>> listOfTablesBeanProperty = BeanProperty.create("tables");
        JListBinding<TableListItem, MnyViewerDataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, dataModel, listOfTablesBeanProperty,
                list);
        jListBinding.bind();
        //
        BeanProperty<MnyViewerDataModel, TableModel> dataModelBeanProperty = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<MnyViewerDataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty,
                table, jTableEvalutionProperty);
        autoBinding.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_1 = BeanProperty.create("tableName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_1,
                textField, jTextFieldBeanProperty_1);
        autoBinding_2.bind();
        //
        ELProperty<MnyViewerDataModel, Object> dataModelEvalutionProperty = ELProperty.create("${tableMetaData}");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, Object, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel,
                dataModelEvalutionProperty, textArea, jTextAreaBeanProperty);
        autoBinding_1.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_2 = BeanProperty.create("headerInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_2,
                headerTextArea, jTextAreaBeanProperty_1);
        autoBinding_3.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_3 = BeanProperty.create("keyInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_2 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_3,
                keyInfoTextArea, jTextAreaBeanProperty_2);
        autoBinding_4.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_4 = BeanProperty.create("indexInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_3 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_4,
                indexInfoTextArea, jTextAreaBeanProperty_3);
        autoBinding_5.bind();
    }

    private TableRowSorter<TableModel> createTableRowSorter(MnyTableModel tableModel) {
        TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(tableModel) {
            @Override
            public void toggleSortOrder(int column) {
                StopWatch stopWatch = new StopWatch();
                log.info("> toggleSortOrder, count=" + getViewRowCount() + ", column=" + column);
                try {
                    // JOptionPane.showConfirmDialog(MnyViewer.this.frame,
                    // "Hello");
                    super.toggleSortOrder(column);
                } finally {
                    long delta = stopWatch.click();
                    rightStatusLabel.setText("sort: rows=" + getViewRowCount() + ", millisecond=" + delta);
                    log.info("< toggleSortOrder, delta=" + delta);
                }
            }

            @Override
            public void sort() {
                StopWatch stopWatch = new StopWatch();
                log.info("> sort");

                String message = "### STARTING to sort " + " " + this.getViewRowCount() + "/" + this.getModelRowCount() + " ... please wait ...";
                log.info(message);

                // rightStatusLabel.setText(message);

                // Component parentComponent = MnyViewer.this.frame;
                // JOptionPane.showConfirmDialog(parentComponent, message);

                // Frame owner = MnyViewer.this.frame;
                // JDialog dialog = new JDialog(owner);
                // JPanel dialogMainView = new JPanel();
                // dialogMainView.setLayout(new BorderLayout());
                // JLabel dialogLabel = new JLabel(message);
                // dialogMainView.add(dialogLabel, BorderLayout.CENTER);
                // dialog.getContentPane().add(dialogMainView);
                // dialog.setModal(false);
                // dialog.pack();
                // dialog.setLocationRelativeTo(null);
                // dialog.show();

                Component parent = SwingUtilities.getRoot(MynViewer.this.frame);
                // parent = MnyViewer.this.frame.getTopLevelAncestor();
                Cursor waitCursor = null;
                if ((parent != null) && (parent.isShowing())) {
                    waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
                    log.info("YES setCursor=" + waitCursor);
                    parent.setCursor(waitCursor);
                } else {
                    log.info("NO setCursor=" + waitCursor);
                }
                try {
                    super.sort();
                } finally {
                    // dialog.dispose();
                    if (waitCursor != null) {
                        log.info("YES CLEAR setCusror");
                        parent.setCursor(null);
                    } else {
                        log.info("NO CLEAR setCusror");
                    }
                    final long delta = stopWatch.click();
                    log.info("< sort, delta=" + delta);
                }
            }

        };
        RowSorterListener listener = new RowSorterListener() {
            private long startTime = -1L;

            public void sorterChanged(RowSorterEvent event) {
                log.info("> sorterChanged");
                Type type = event.getType();
                log.info("  " + type);
                switch (type) {
                case SORT_ORDER_CHANGED:
                    startTime = System.currentTimeMillis();
                    break;
                case SORTED:
                    if (startTime > 0) {
                        long now = System.currentTimeMillis();
                        long delta = now - startTime;
                        startTime = -1L;
                        log.info("sorterChanged, delta=" + delta);
                    }
                    break;
                default:
                    break;
                }
            }
        };
        sorter.addRowSorterListener(listener);
        return sorter;
    }

    private void toggleTableSorting() {
        if (allowTableSorting) {
            filterTextField.setEnabled(true);
            filterTextField.setText("");
            filterOnSelectedColumnCheckBox.setEnabled(true);

            if (tableModel != null) {
                log.info("creating new sorter ...");
                sorter = createTableRowSorter(tableModel);

                log.info("setting new sorter ...");
                MynViewer.this.table.setRowSorter(sorter);
            }
        } else {
            filterTextField.setEnabled(false);
            filterTextField.setText("Filter is disable");
            filterOnSelectedColumnCheckBox.setEnabled(false);

            if (tableModel != null) {
                sorter = null;
                MynViewer.this.table.setRowSorter(sorter);
            }
        }
    }
}