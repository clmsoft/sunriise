package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
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
import javax.swing.SwingConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
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

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PageChannel;
import com.healthmarketscience.jackcess.Table;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.model.bean.DataModel;

public class MnyViewer {
    private static final Logger log = Logger.getLogger(MnyViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(MnyViewer.class);

    private static final Executor threadPool = Executors.newCachedThreadPool();

    private JFrame frame;
    private File dbFile;
    private Database db;

    private DataModel dataModel = new DataModel();
    private JList list;
    private JTable table;
    private MnyTableModel tableModel;

    private JTextField textField;

    private boolean dbReadOnly = true;

    private Pattern tableNamePattern = Pattern.compile("^(.*) \\([0-9]+\\)$");
    private JTextArea textArea;
    private JTextArea headerTextArea;

    private JMenuItem duplicateMenuItem;
    private JMenuItem deleteMenuItem;
    private JTextArea keyInfoTextArea;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MnyViewer window = new MnyViewer();
                    window.getFrame().setLocationRelativeTo(null);
                    window.getFrame().setVisible(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    /**
     * Create the application.
     */
    public MnyViewer() {
        initialize();
    }

    private String parseHeaderInfo(Table t) throws IOException {
        StringBuilder sb = new StringBuilder();

        sb.append("Header info");
        sb.append("\n");
        sb.append("\n");

        sb.append("Table: " + t.getName());
        sb.append("\n");
        sb.append("\n");
        
        Database db = t.getDatabase();

        PageChannel pageChannel = db.getPageChannel();
        ByteBuffer buffer = pageChannel.createPageBuffer();
        pageChannel.readPage(buffer, 0);

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

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());
        getFrame().setBounds(100, 100, 800, 600);
        getFrame().setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);

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
        mntmNewMenuItem_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Component component = (Component) event.getSource();
                Component locationRelativeTo = JOptionPane.getFrameForComponent(component);
                locationRelativeTo = MnyViewer.this.getFrame();
                List<String> recentOpenFileNames = new ArrayList<String>();
                int size = prefs.getInt("recentOpenFileNames_size", 0);
                size = Math.min(size, 10);
                for (int i = 0; i < size; i++) {
                    String value = prefs.get("recentOpenFileNames_" + i, null);
                    if (value != null) {
                        recentOpenFileNames.add(value);
                    }
                }
                OpenDbDialog dialog = OpenDbDialog.showDialog(getDb(), dbFile, recentOpenFileNames, locationRelativeTo);
                if (!dialog.isCancel()) {
                    setDb(dialog.getDb());
                    dbFile = dialog.getDbFile();
                    if (dbFile != null) {
                        getFrame().setTitle(dbFile.getAbsolutePath());
                    } else {
                        getFrame().setTitle("No opened db");
                    }
                    List<Table> tables = new ArrayList<Table>();
                    try {
                        Set<String> names = getDb().getTableNames();
                        for (String name : names) {
                            try {
                                Table t = getDb().getTable(name);
                                tables.add(t);
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
                    MnyViewer.this.dataModel.setTables(tables);

                    size = recentOpenFileNames.size();
                    size = Math.min(size, 10);
                    log.info("prefs: recentOpenFileNames_size=" + size);
                    prefs.putInt("recentOpenFileNames_size", size);
                    for (int i = 0; i < size; i++) {
                        log.info("prefs: recentOpenFileNames_" + i + ", value=" + recentOpenFileNames.get(i));
                        prefs.put("recentOpenFileNames_" + i, recentOpenFileNames.get(i));
                    }
                }
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
            public void valueChanged(ListSelectionEvent e) {
                if (e.getValueIsAdjusting()) {
                    return;
                }
                String selectedValue = (String) list.getSelectedValue();
                if (selectedValue == null) {
                    return;
                }
                if (selectedValue.length() <= 0) {
                    return;
                }
                // ABC (123)
                Matcher m = tableNamePattern.matcher(selectedValue);
                if (!m.matches()) {
                    log.error("Cannot parse tableName=" + selectedValue);
                    return;
                }
                String tableName = m.group(1);
                try {
                    final Table table = getDb().getTable(tableName);
                    dataModel.setTable(table);
                    dataModel.setTableName(tableName);
                    dataModel.setTableMetaData(table.toString());
                    dataModel.setHeaderInfo(parseHeaderInfo(table));
                    dataModel.setKeyInfo(parseKeyInfo(table));
                    tableModel = new MnyTableModel(table);
                    tableModel.setDbReadOnly(dbReadOnly);
                    dataModel.setTableModel(tableModel);
                } catch (IOException e1) {
                    log.error(e1);
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

        JScrollPane scrollPane_1 = new JScrollPane();
        tabbedPane.addTab("Rows", null, scrollPane_1, null);

        table = new JTable() {

            @Override
            public void setModel(TableModel dataModel) {
                super.setModel(dataModel);
                TableColumnModel columnModel = this.getColumnModel();
                int cols = columnModel.getColumnCount();

                MnyTableModel mnyTableModel = MnyViewer.this.tableModel;
                IndexLookup indexLookup = new IndexLookup();

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
//        table.setAutoCreateRowSorter(true);
        table.setDefaultRenderer(Date.class, new DefaultTableCellRenderer() {
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ");
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
            // private DateFormat formatter = new
            // SimpleDateFormat("yyyy/MM/dd");
            private DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy HH:mm");

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
        JPopupMenu popupMenu = new JPopupMenu();
        JMenuItem menuItem = null;
        menuItem = new JMenuItem(new AbstractAction("Duplicate") {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                duplicateRow(rowIndex);
            }
        });
        popupMenu.add(menuItem);
        this.duplicateMenuItem = menuItem;

        popupMenu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Delete") {
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                deleteRow(rowIndex);
            }
        });
        popupMenu.add(menuItem);
        this.deleteMenuItem = menuItem;

        MouseListener popupListener = new PopupListener(popupMenu);
        table.addMouseListener(popupListener);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        scrollPane_1.setViewportView(table);

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

        initDataBindings();
    }

    protected void deleteRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.deleteRow(rowIndex);
        }
    }

    protected void duplicateRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.duplicateRow(rowIndex, this.getFrame());
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
        this.db = db;
    }

    public Database getDb() {
        return db;
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

    protected void initDataBindings() {
        BeanProperty<DataModel, List<Table>> listOfTablesBeanProperty = BeanProperty.create("tables");
        JListBinding<Table, DataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, dataModel, listOfTablesBeanProperty, list);
        //
        ELProperty<Table, Object> tableEvalutionProperty = ELProperty.create("${name} (${rowCount})");
        jListBinding.setDetailBinding(tableEvalutionProperty);
        //
        jListBinding.bind();
        //
        BeanProperty<DataModel, TableModel> dataModelBeanProperty = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<DataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty,
                table, jTableEvalutionProperty);
        autoBinding.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_1 = BeanProperty.create("tableName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_1,
                textField, jTextFieldBeanProperty_1);
        autoBinding_2.bind();
        //
        ELProperty<DataModel, Object> dataModelEvalutionProperty = ELProperty.create("${tableMetaData}");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
        AutoBinding<DataModel, Object, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel,
                dataModelEvalutionProperty, textArea, jTextAreaBeanProperty);
        autoBinding_1.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_2 = BeanProperty.create("headerInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_2,
                headerTextArea, jTextAreaBeanProperty_1);
        autoBinding_3.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_3 = BeanProperty.create("keyInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_2 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextArea, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_3,
                keyInfoTextArea, jTextAreaBeanProperty_2);
        autoBinding_4.bind();
    }
}
