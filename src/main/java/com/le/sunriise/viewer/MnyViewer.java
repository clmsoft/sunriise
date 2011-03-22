package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.ProgressMonitor;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.healthmarketscience.jackcess.ByteUtil;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PageChannel;
import com.healthmarketscience.jackcess.Table;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.DumpMsIsamDb;
import com.le.sunriise.ExportToMdb;
import com.le.sunriise.model.bean.DataModel;

public class MnyViewer {
    private static final Logger log = Logger.getLogger(OpenDbDialog.class);

    private JFrame frame;
    protected File dbFile;
    protected Database db;

    private DataModel dataModel = new DataModel();
    private JList list;
    private JTable table;
    private JTextField textField;

    private Pattern tableNamePattern = Pattern.compile("^(.*) \\([0-9]+\\)$");
    private JTextArea textArea;
    private JTextArea headerTextArea;

    private static final Executor threadPool = Executors.newCachedThreadPool();

    private final class ExportToCsvAction implements ActionListener {
        private JFileChooser fc = null;

        public void actionPerformed(ActionEvent event) {
            final Component source = (Component) event.getSource();
            if (fc == null) {
                fc = new JFileChooser(new File("."));
                fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
            }
            if (fc.showSaveDialog(source) == JFileChooser.CANCEL_OPTION) {
                return;
            }
            final File dir = fc.getSelectedFile();
            log.info("Export as CSV to directory=" + dir);
            if (source != null) {
                source.setEnabled(false);
            }
            Component parentComponent = (source != null) ? source : frame;
            Object message = "Exporting to CSV files ...";
            int min = 0;
            int max = 100;
            String note = "";

            final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, message, note, min, max);
            progressMonitor.setProgress(0);

            Runnable command = new Runnable() {
                public void run() {
                    Exception exception = null;
                    try {
                        DumpMsIsamDb exporter = new DumpMsIsamDb() {
                            private int maxTables = 0;
                            @Override
                            protected void startExport(File outDir) {
                                super.startExport(outDir);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        progressMonitor.setProgress(progressMonitor.getMinimum());
                                    }
                                });
                            }

                            @Override
                            protected void endExport(File outDir) {
                                super.endExport(outDir);
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        progressMonitor.setProgress(progressMonitor.getMaximum());
                                    }
                                });
                            }

                            @Override
                            protected void startExportTables(int size) {
                                super.startExportTables(size);
                                maxTables = size;
                            }

                            @Override
                            protected boolean exportedTable(final String tableName, final int count) {
                                super.exportedTable(tableName, count);
                                if (progressMonitor.isCanceled()) {
                                    return false;
                                }
                                SwingUtilities.invokeLater(new Runnable() {
                                    public void run() {
                                        progressMonitor.setNote("Table: " + tableName);
                                        progressMonitor.setProgress((count * 100) / maxTables);
                                    }
                                });
                                return true;
                            }

                            @Override
                            protected void endExportTables(int count) {
                                super.endExportTables(count);
                            }
                            
                            
                        };
                        exporter.setDb(db);
                        exporter.writeToDir(dir);
                    } catch (IOException e) {
                        log.error(e);
                        exception = e;
                    } finally {
                        final Exception exception2 = exception;
                        Runnable swingRun = new Runnable() {
                            public void run() {
                                if (exception2 != null) {
                                    Component parentComponent = source;
                                    String message = exception2.toString();
                                    String title = "Error export to CSV file";
                                    JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
                                }
                                if (source != null) {
                                    source.setEnabled(true);
                                }
                            }
                        };
                        SwingUtilities.invokeLater(swingRun);
                        log.info("< Export CSV DONE");
                    }
                }
            };
            threadPool.execute(command);
        }
    }

    private final class MnyTableModel extends AbstractTableModel {
        private final Table t;
        private int currentRow = 0;
        private Cursor c;
        private Map<String, Object> data = null;

        private MnyTableModel(Table t) throws IOException {
            this.t = t;
            this.c = Cursor.createCursor(t);
            this.c.reset();
            this.c.moveToNextRow();
        }

        public int getRowCount() {
            return t.getRowCount();
        }

        public int getColumnCount() {
            return t.getColumnCount();
        }

        public Object getValueAt(int rowIndex, int columnIndex) {
            int delta = rowIndex - currentRow;
            currentRow = rowIndex;
            try {
                if (delta == 0) {
                    if (data == null) {
                        data = c.getCurrentRow();
                    }
                } else if (delta < 0) {
                    c.movePreviousRows(-delta);
                    data = c.getCurrentRow();
                } else {
                    c.moveNextRows(delta);
                    data = c.getCurrentRow();
                }
            } catch (IOException e) {
                log.warn(e);
            }

            // String label = "" + rowIndex + ", " + columnIndex + ", " + delta;
            Object value = data.get(getColumnName(columnIndex));
            if (value instanceof byte[]) {
                value = ByteUtil.toHexString((byte[]) value);
            }

            return value;
        }

        public String getColumnName(int column) {
            List<Column> cols = t.getColumns();
            return cols.get(column).getName();
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            public void run() {
                try {
                    MnyViewer window = new MnyViewer();
                    window.frame.setLocationRelativeTo(null);
                    window.frame.setVisible(true);
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

        sb.append("Table: " + t.getName());

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

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        frame.setBounds(100, 100, 800, 600);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JMenuBar menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                if (db != null) {
                    try {
                        db.close();
                    } catch (IOException e) {
                        log.warn(e);
                    } finally {
                        db = null;
                    }
                }
                System.exit(1);
            }
        });

        JMenuItem mntmNewMenuItem_1 = new JMenuItem("Open");
        mntmNewMenuItem_1.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent event) {
                Component component = (Component) event.getSource();
                OpenDbDialog dialog = OpenDbDialog.showDialog(JOptionPane.getFrameForComponent(component), db, dbFile);
                if (!dialog.isCancel()) {
                    db = dialog.getDb();
                    dbFile = dialog.getDbFile();
                    if (dbFile != null) {
                        frame.setTitle(dbFile.getAbsolutePath());
                    } else {
                        frame.setTitle("No opened db");
                    }
                    List<Table> tables = new ArrayList<Table>();
                    try {
                        Set<String> names = db.getTableNames();
                        for (String name : names) {
                            try {
                                Table t = db.getTable(name);
                                tables.add(t);
                            } catch (IOException e) {
                                log.warn(e);
                            }
                        }
                    } catch (IOException e) {
                        log.warn(e);
                    }
                    MnyViewer.this.dataModel.setTables(tables);
                }
            }
        });
        mnNewMenu.add(mntmNewMenuItem_1);

        JMenu mnNewMenu_1 = new JMenu("Export DB");
        mnNewMenu.add(mnNewMenu_1);

        JMenuItem mntmNewMenuItem_3 = new JMenuItem("To CSV");
        mntmNewMenuItem_3.addActionListener(new ExportToCsvAction());
        mnNewMenu_1.add(mntmNewMenuItem_3);

        JMenuItem mntmNewMenuItem_4 = new JMenuItem("To *.mdb");
        mntmNewMenuItem_4.addActionListener(new ActionListener() {
            private JFileChooser fc = null;

            public void actionPerformed(ActionEvent event) {
                final Component source = (Component) event.getSource();
                if (fc == null) {
                    fc = new JFileChooser(new File("."));
                    fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
                }
                if (fc.showSaveDialog(source) == JFileChooser.CANCEL_OPTION) {
                    return;
                }
                final File destFile = fc.getSelectedFile();
                log.info("Export as *.mdb to file=" + destFile);

                source.setEnabled(false);
                Component parentComponent = source;
                Object message = "Exporting to *.mdb ...";
                String note = "";
                int min = 0;
                int max = 100;
                final ProgressMonitor progressMonitor = new ProgressMonitor(parentComponent, message, note, min, max);
                progressMonitor.setProgress(0);

                Runnable command = new Runnable() {
                    public void run() {
                        Database srcDb = null;
                        Database destDb = null;
                        Exception exception = null;

                        try {
                            srcDb = db;
                            ExportToMdb exporter = new ExportToMdb() {
                                private int progressCount = 0;
                                private int maxCount = 0;
                                private String currentTable = null;
                                private int maxRows;

                                protected void startCopyTables(int maxCount) {
                                    if (progressMonitor.isCanceled()) {
                                        return;
                                    }
                                    this.maxCount = maxCount;
                                    Runnable doRun = new Runnable() {
                                        public void run() {
                                            progressMonitor.setProgress(0);
                                        }
                                    };
                                    SwingUtilities.invokeLater(doRun);
                                }

                                protected void endCopyTables(int count) {
                                    Runnable doRun = new Runnable() {
                                        public void run() {
                                            progressMonitor.setProgress(100);
                                        }
                                    };
                                    SwingUtilities.invokeLater(doRun);
                                }

                                protected boolean startCopyTable(String name) {
                                    super.startCopyTable(name);

                                    if (progressMonitor.isCanceled()) {
                                        return false;
                                    }
                                    this.currentTable = name;
                                    Runnable doRun = new Runnable() {
                                        public void run() {
                                            progressMonitor.setNote("Table: " + currentTable);
                                        }
                                    };
                                    SwingUtilities.invokeLater(doRun);
                                    return true;
                                }

                                protected void endCopyTable(String name) {
                                    progressCount++;
                                    Runnable doRun = new Runnable() {
                                        public void run() {
                                            progressMonitor.setProgress((progressCount * 100) / maxCount);
                                        }
                                    };
                                    SwingUtilities.invokeLater(doRun);
                                }

                                protected boolean startAddingRows(int max) {
                                    if (progressMonitor.isCanceled()) {
                                        return false;
                                    }
                                    this.maxRows = max;
                                    return true;
                                }

                                protected boolean addedRow(int count) {
                                    if (progressMonitor.isCanceled()) {
                                        return false;
                                    }
                                    final String str = " (Copying rows: " + ((count * 100) / this.maxRows) + "%" + ")";
                                    Runnable doRun = new Runnable() {
                                        public void run() {
                                            progressMonitor.setNote("Table: " + currentTable + str);
                                        }
                                    };
                                    SwingUtilities.invokeLater(doRun);
                                    return true;
                                }

                                protected void endAddingRows(int count, long delta) {
                                    super.endAddingRows(count, delta);
                                }

                            };
                            destDb = exporter.export(srcDb, destFile);
                        } catch (IOException e) {
                            log.error(e);
                            exception = e;
                        } finally {
                            if (destDb != null) {
                                try {
                                    destDb.close();
                                } catch (IOException e1) {
                                    log.warn(e1);
                                } finally {
                                    destDb = null;
                                }
                            }
                            log.info("< DONE, exported to file=" + destFile);
                            final Exception exception2 = exception;
                            Runnable doRun = new Runnable() {
                                public void run() {
                                    if (exception2 != null) {
                                        Component parentComponent = JOptionPane.getFrameForComponent(source);
                                        String message = exception2.toString();
                                        String title = "Error export to *.mdb file";
                                        JOptionPane.showMessageDialog(parentComponent, message, title, JOptionPane.ERROR_MESSAGE);
                                    }
                                    if (source != null) {
                                        source.setEnabled(true);
                                    }
                                }
                            };
                            SwingUtilities.invokeLater(doRun);
                        }
                    }
                };
                threadPool.execute(command);
            }
        });
        mnNewMenu_1.add(mntmNewMenuItem_4);

        JSeparator separator_1 = new JSeparator();
        mnNewMenu.add(separator_1);
        mnNewMenu.add(mntmNewMenuItem);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setResizeWeight(0.33);
        splitPane.setDividerLocation(0.33);
        frame.getContentPane().add(splitPane, BorderLayout.CENTER);

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
                    final Table t = db.getTable(tableName);
                    dataModel.setTable(t);
                    dataModel.setTableName(tableName);
                    dataModel.setTableMetaData(t.toString());
                    dataModel.setHeaderInfo(parseHeaderInfo(t));
                    AbstractTableModel model = new MnyTableModel(t);
                    dataModel.setTableModel(model);
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
        panel_1.setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.UNRELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.UNRELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,}));

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
                int cols = this.getColumnModel().getColumnCount();
                for (int i = 0; i < cols; i++) {
                    StripedTableRenderer renderer = new StripedTableRenderer();
                    this.getColumnModel().getColumn(i).setCellRenderer(renderer);
                }
            }

        };
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
        initDataBindings();
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
    }
}
