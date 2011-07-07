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
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
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
import org.bouncycastle.crypto.Digest;
import org.bouncycastle.crypto.digests.MD5Digest;
import org.bouncycastle.crypto.digests.SHA1Digest;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.healthmarketscience.jackcess.BigIndexData;
import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Index;
import com.healthmarketscience.jackcess.IndexData;
import com.healthmarketscience.jackcess.JetFormat;
import com.healthmarketscience.jackcess.IndexData.ColumnDescriptor;
import com.healthmarketscience.jackcess.PageChannel;
import com.healthmarketscience.jackcess.Table;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.encryption.EncryptionUtils;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.model.bean.DataModel;
import com.le.sunriise.model.bean.TableListItem;
import org.jdesktop.beansbinding.ObjectProperty;

public class MnyViewer {
    private static final Logger log = Logger.getLogger(MnyViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(MnyViewer.class);

    private static final Executor threadPool = Executors.newCachedThreadPool();

    private JFrame frame;
//    private File dbFile;
//    private Database db;
    private OpenedDb openedDb = new OpenedDb();
    
    private DataModel dataModel = new DataModel();
    private JList list;
    private JTable table;
    private MnyTableModel tableModel;

    private JTextField textField;

    private boolean dbReadOnly = true;

//    private Pattern tableNamePattern = Pattern.compile("^(.*) \\([0-9]+\\)$");
    private JTextArea textArea;
    private JTextArea headerTextArea;

    private JMenuItem duplicateMenuItem;
    private JMenuItem deleteMenuItem;
    private JTextArea keyInfoTextArea;

    private JTextArea indexInfoTextArea;

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
                    log.error(e, e);
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

        JetFormat format = pageChannel.getFormat();
        sb.append("format=" + format.toString());
        sb.append("\n");
        
        if (format.CODEC_TYPE == format.CODEC_TYPE.MSISAM) {
            sb.append("codecHandlerName=" + EncryptionUtils.getCodecHandlerName(buffer));
            sb.append("\n");

            Digest digest = EncryptionUtils.getDigest(buffer);
            sb.append("digest=" + digest.getAlgorithmName());
            sb.append("\n");
            sb.append("\n");
            
            byte[] salt = EncryptionUtils.getSalt(buffer);
            sb.append("salt=" + ByteUtil.toHexString(salt));
            sb.append("\n");
            
            String password = openedDb.getPassword();
            Charset charset = openedDb.getDb().getCharset();
            byte[] pwdDigest = EncryptionUtils.createPasswordDigest(buffer, password, charset);
            sb.append("pwdDigest=" + ByteUtil.toHexString(pwdDigest));
            sb.append("\n");
            
            final int SALT_LENGTH = 0x4;
            byte[] baseSalt = Arrays.copyOf(salt, SALT_LENGTH);
            byte[] testEncodingKey = EncryptionUtils.concat(pwdDigest, salt);
            sb.append("testEncodingKey=" + ByteUtil.toHexString(testEncodingKey));
            sb.append("\n");
            
            byte[] encrypted4BytesCheck = EncryptionUtils.getPasswordTestBytes(buffer);
            sb.append("encrypted4BytesCheck=" + ByteUtil.toHexString(encrypted4BytesCheck));
            sb.append("\n");
            
            byte[] decrypted4BytesCheck = EncryptionUtils.getDecrypted4BytesCheck(encrypted4BytesCheck, testEncodingKey);
            sb.append("decrypted4BytesCheck=" + ByteUtil.toHexString(decrypted4BytesCheck));
            sb.append(" / ");
            
            byte[] testBytes = baseSalt;
            sb.append("testBytes=" + ByteUtil.toHexString(testBytes));
            sb.append("\n");
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

        for(Index index : indexes) {
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
            for(ColumnDescriptor column: columns) {
                sb.append("        " + column.getColumn().getTable().getName() + "." +
                        column.getColumn().getName());
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
                OpenDbDialog dialog = OpenDbDialog.showDialog(openedDb, recentOpenFileNames, locationRelativeTo);
                if (!dialog.isCancel()) {
//                    setDb(dialog.getDb());
//                    dbFile = dialog.getDbFile();
                    openedDb = dialog.getOpendDb();
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
                    MnyViewer.this.dataModel.setTables(tables);

                    size = recentOpenFileNames.size();
                    size = Math.min(size, 10);
                    if (log.isDebugEnabled()) {
                        log.debug("prefs: recentOpenFileNames_size=" + size);
                    }
                    prefs.putInt("recentOpenFileNames_size", size);
                    for (int i = 0; i < size; i++) {
                        if (log.isDebugEnabled()) {
                            log.debug("prefs: recentOpenFileNames_" + i + ", value=" + recentOpenFileNames.get(i));
                        }
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
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                try {
                    TableListItem item = (TableListItem) list.getSelectedValue();
                    if (item != null) {
                        final Table table = item.getTable();
                        String tableName = table.getName();
                        dataModel.setTable(table);
                        dataModel.setTableName(tableName);
                        dataModel.setTableMetaData(table.toString());
                        dataModel.setHeaderInfo(parseHeaderInfo(table));
                        dataModel.setKeyInfo(parseKeyInfo(table));
                        dataModel.setIndexInfo(parseIndexInfo(table));

                        tableModel = new MnyTableModel(table);
                        tableModel.setDbReadOnly(dbReadOnly);

                        dataModel.setTableModel(tableModel);
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
        
        JPanel panel_5 = new JPanel();
        tabbedPane.addTab("Indexes", null, panel_5, null);
        panel_5.setLayout(new BorderLayout(0, 0));
        
        JScrollPane scrollPane_5 = new JScrollPane();
        panel_5.add(scrollPane_5);
        
        indexInfoTextArea = new JTextArea();
        scrollPane_5.setViewportView(indexInfoTextArea);

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
        BeanProperty<DataModel, List<TableListItem>> listOfTablesBeanProperty = BeanProperty.create("tables");
        JListBinding<TableListItem, DataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ, dataModel, listOfTablesBeanProperty, list);
        jListBinding.bind();
        //
        BeanProperty<DataModel, TableModel> dataModelBeanProperty = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<DataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty, table, jTableEvalutionProperty);
        autoBinding.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_1 = BeanProperty.create("tableName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_1, textField, jTextFieldBeanProperty_1);
        autoBinding_2.bind();
        //
        ELProperty<DataModel, Object> dataModelEvalutionProperty = ELProperty.create("${tableMetaData}");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
        AutoBinding<DataModel, Object, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelEvalutionProperty, textArea, jTextAreaBeanProperty);
        autoBinding_1.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_2 = BeanProperty.create("headerInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_2, headerTextArea, jTextAreaBeanProperty_1);
        autoBinding_3.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_3 = BeanProperty.create("keyInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_2 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextArea, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_3, keyInfoTextArea, jTextAreaBeanProperty_2);
        autoBinding_4.bind();
        //
        BeanProperty<DataModel, String> dataModelBeanProperty_4 = BeanProperty.create("indexInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_3 = BeanProperty.create("text");
        AutoBinding<DataModel, String, JTextArea, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ, dataModel, dataModelBeanProperty_4, indexInfoTextArea, jTextAreaBeanProperty_3);
        autoBinding_5.bind();
    }
}
