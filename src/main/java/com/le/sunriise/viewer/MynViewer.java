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
package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComponent;
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
import javax.swing.JViewport;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.MouseInputAdapter;
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
import javax.swing.text.DefaultEditorKit;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.beansbinding.Bindings;
import org.jdesktop.beansbinding.ELProperty;
import org.jdesktop.swingbinding.JListBinding;
import org.jdesktop.swingbinding.SwingBindings;

import app.MnyViewer;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.DataType;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.JavaInfo;
import com.le.sunriise.StopWatch;
import com.le.sunriise.export.ExportToContext;
import com.le.sunriise.index.IndexLookup;
import com.le.sunriise.model.bean.MnyViewerDataModel;
import com.le.sunriise.model.bean.TableListItem;

public class MynViewer {
    private static final Logger log = Logger.getLogger(MynViewer.class);

    private static final Preferences prefs = Preferences.userNodeForPackage(MynViewer.class);

    private static final Executor threadPool = Executors.newCachedThreadPool();

    public static final String TITLE_NO_OPENED_DB = "No opened db";

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

    JLabel rightStatusLabel;

    // private JTextField rightStatusLabel;

    private Map<String, Object> selectRowValues1 = null;
    private Map<String, Object> selectRowValues2 = null;

    private JMenu toolsMenu;

    private JMenu gotoColumnMenu;

    private int labelColumnIndex;

    private AtomicBoolean settingNewSorter = new AtomicBoolean(false);

    private JPopupMenu filterCcpPopupMenu;

    private final class MnyViewerOpenDbAction extends OpenDbAction {

        private MnyViewerOpenDbAction(Component locationRelativeTo, Preferences prefs, OpenedDb openedDb) {
            super(locationRelativeTo, prefs, openedDb);
        }

        @Override
        public void dbFileOpened(OpenedDb newOpenedDb, OpenDbDialog dialog) {
            if (newOpenedDb != null) {
                MynViewer.this.openedDb = newOpenedDb;
            }

            File dbFile = openedDb.getDbFile();
            if (dbFile != null) {
                getFrame().setTitle(dbFile.getAbsolutePath());
            } else {
                getFrame().setTitle(TITLE_NO_OPENED_DB);
            }

            List<TableListItem> tables = new ArrayList<TableListItem>();
            try {
                Database db = getDb();
                
                DatabaseUtils.logDbInfo(db);
                
                Set<String> tableNames = db.getTableNames();
                for (String tableName : tableNames) {
                    try {
                        Table table = db.getTable(tableName);
                        TableListItem tableListItem = new TableListItem();
                        tableListItem.setTable(table);
                        tables.add(tableListItem);
                    } catch (IOException e) {
                        log.warn(e);
                    }
                }
                
                boolean showSystemCatalog = true;
                if (showSystemCatalog) {
                    Table table = db.getSystemCatalog();
                    if (table != null) {
                        TableListItem tableListItem = new TableListItem();
                        tableListItem.setTable(table);
                        tables.add(tableListItem);
                    }
                }
            } catch (IOException e) {
                log.warn(e);
            }

            log.info("Found " + tables.size() + " tables.");

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
    }

    private final class GotoToColumnAction extends AbstractAction {
        private String columnName;

        private GotoToColumnAction(String name) {
            super(name);
            this.columnName = name;
        }

        @Override
        public void actionPerformed(ActionEvent event) {
            int row = 0;

            int rowIndex = table.getSelectedRow();
            if (rowIndex > 0) {
                // row = getRowIndex(rowIndex);
                row = rowIndex;
            }

            int column = 0;
            column = tableModel.getColumnIndex(columnName);

            log.info("GotoToColumnAction" + ", columnName=" + columnName + ", row=" + row + ", column=" + column);

            Rectangle aRect = table.getCellRect(row, column, true);

            // table.scrollRectToVisible(aRect);
            scrollToCenter(table, row, column);
        }

        public void scrollToCenter(JTable table, int rowIndex, int vColIndex) {
            if (!(table.getParent() instanceof JViewport)) {
                return;
            }
            JViewport viewport = (JViewport) table.getParent();

            // This rectangle is relative to the table where the
            // northwest corner of cell (0,0) is always (0,0).
            Rectangle rect = table.getCellRect(rowIndex, vColIndex, true);

            // The location of the view relative to the table
            Rectangle viewRect = viewport.getViewRect();

            // Translate the cell location so that it is relative
            // to the view, assuming the northwest corner of the
            // view is (0,0).
            rect.setLocation(rect.x - viewRect.x, rect.y - viewRect.y);

            // Calculate location of rect if it were at the center of view
            int centerX = (viewRect.width - rect.width) / 2;
            int centerY = (viewRect.height - rect.height) / 2;

            // Fake the location of the cell so that scrollRectToVisible
            // will move the cell to the center
            if (rect.x < centerX) {
                centerX = -centerX;
            }
            if (rect.y < centerY) {
                centerY = -centerY;
            }
            rect.translate(centerX, centerY);

            // Scroll the area into view.
            viewport.scrollRectToVisible(rect);
        }
    }

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        // EventQueue waitQueue = new WaitCursorEventQueue(500);
        // Toolkit.getDefaultToolkit().getSystemEventQueue().push(waitQueue);

        // String builNumber = BuildNumber.findBuilderNumber();

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    JavaInfo.logInfo();

                    log.info("> Starting MynViewer");
                    MynViewer window = new MynViewer();
                    showMainFrame(window);

                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            protected void showMainFrame(MynViewer window) {
                JFrame mainFrame = window.getFrame();

                String title = com.le.sunriise.viewer.MynViewer.TITLE_NO_OPENED_DB;
                mainFrame.setTitle(title);
                
                Dimension preferredSize = new Dimension(1000, 800);
                mainFrame.setPreferredSize(preferredSize);

                mainFrame.pack();
                
                mainFrame.setLocationRelativeTo(null);
                
                mainFrame.setVisible(true);
                log.info("setVisible to true");
            }
        });
    }

    /**
     * Create the application.
     */
    public MynViewer() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        setFrame(new JFrame());
        getFrame().setBounds(100, 100, 800, 600);
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

        JMenuBar menuBar = new JMenuBar();
        getFrame().setJMenuBar(menuBar);
        getFrame().setTitle(com.le.sunriise.viewer.MynViewer.TITLE_NO_OPENED_DB);
        JMenu mnNewMenu = new JMenu("File");
        menuBar.add(mnNewMenu);

        JMenuItem mntmNewMenuItem = new JMenuItem("Exit");
        mntmNewMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent event) {
                if (openedDb != null) {
                    appClosed();
                }
                System.exit(1);
            }
        });

        JMenuItem mntmNewMenuItem_1 = new JMenuItem("Open");
        mntmNewMenuItem_1.addActionListener(new MnyViewerOpenDbAction(MynViewer.this.getFrame(), prefs, openedDb));
        mnNewMenu.add(mntmNewMenuItem_1);

        JMenu mnNewMenu_1 = new JMenu("Export DB");
        mnNewMenu.add(mnNewMenu_1);

        JMenuItem mntmNewMenuItem_3 = new JMenuItem("To CSV");
        mntmNewMenuItem_3.addActionListener(new ExportToCsvAction(this));
        mnNewMenu_1.add(mntmNewMenuItem_3);

        JMenuItem mntmNewMenuItem_4 = new JMenuItem("To *.mdb");
        mntmNewMenuItem_4.addActionListener(new ExportToMdbAction(this));
        mnNewMenu_1.add(mntmNewMenuItem_4);

        JMenuItem mntmTojson = new JMenuItem("To *.json");
        mntmTojson.addActionListener(new ExportToJSONAction(new ExportToContext() {

            @Override
            public Component getParentComponent() {
                return getFrame();
            }

            @Override
            public OpenedDb getSrcDb() {
                return getOpenedDb();
            }

        }));
        mnNewMenu_1.add(mntmTojson);

        JSeparator separator_1 = new JSeparator();
        mnNewMenu.add(separator_1);
        mnNewMenu.add(mntmNewMenuItem);

        toolsMenu = new JMenu("Tools");
        menuBar.add(toolsMenu);
        JMenuItem scriptMenuItem = new JMenuItem(new AbstractAction("Run Script") {
            @Override
            public void actionPerformed(ActionEvent event) {
            }
        });
        toolsMenu.add(scriptMenuItem);

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
            @Override
            public void valueChanged(ListSelectionEvent event) {
                if (event.getValueIsAdjusting()) {
                    return;
                }
                try {
                    TableListItem item = (TableListItem) list.getSelectedValue();
                    if (item != null) {
                        tableSelected(item);
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
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.UNRELATED_GAP_COLSPEC, },
                new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, }));

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
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                duplicateRow(rowIndex);
            }
        });
        tablePopupMenu.add(menuItem);
        this.duplicateMenuItem = menuItem;

        tablePopupMenu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Delete") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                deleteRow(rowIndex);
            }
        });
        tablePopupMenu.add(menuItem);
        this.deleteMenuItem = menuItem;

        tablePopupMenu.addSeparator();

        menuItem = new JMenuItem(new AbstractAction("Copy Selected Cell Value") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                int columnIndex = table.getSelectedColumn();
                copyColumn(rowIndex, columnIndex);
            }
        });
        tablePopupMenu.add(menuItem);

        menuItem = new JMenuItem(new AbstractAction("Select Column as Label") {
            @Override
            public void actionPerformed(ActionEvent e) {
                int rowIndex = table.getSelectedRow();
                int columnIndex = table.getSelectedColumn();

                if (log.isDebugEnabled()) {
                    log.debug("Select Column as Label: rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
                }

                selectColumnAsLabel(rowIndex, columnIndex);
            }
        });
        tablePopupMenu.add(menuItem);

        gotoColumnMenu = new JMenu("Scroll to Column");
        tablePopupMenu.add(gotoColumnMenu);

        tablePopupMenu.addSeparator();
        JMenu diffMenu = new JMenu("Diff Two Rows");
        menuItem = new JMenuItem(new AbstractAction("Select as Row 1") {
            @Override
            public void actionPerformed(ActionEvent event) {
                int rowIndex = table.getSelectedRow();
                selectAsRow1ForDiff(rowIndex);
            }
        });
        diffMenu.add(menuItem);
        menuItem = new JMenuItem(new AbstractAction("Select as Row 2") {
            @Override
            public void actionPerformed(ActionEvent event) {
                int rowIndex = table.getSelectedRow();
                JMenuItem source = (JMenuItem) event.getSource();
                selectAsRow2ForDiff(rowIndex, source);
            }
        });
        diffMenu.add(menuItem);
        tablePopupMenu.add(diffMenu);

        MouseListener tablePopupListener = new PopupListener(tablePopupMenu);

        JPanel panel_6 = new JPanel();
        tabbedPane.addTab("Rows", null, panel_6, null);
        panel_6.setLayout(new BorderLayout(0, 0));

        JScrollPane scrollPane_1 = new JScrollPane();
        panel_6.add(scrollPane_1);

        table = new JTable() {

            @Override
            public void valueChanged(ListSelectionEvent e) {
                super.valueChanged(e);

                if (e.getValueIsAdjusting()) {
                    return;
                }

                if (settingNewSorter.get()) {
                    return;
                }

                ListSelectionModel model = (ListSelectionModel) e.getSource();
                int selectedIndex = model.getMinSelectionIndex();

                updateRowLabel(selectedIndex);
            }

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
        table.addMouseListener(new MouseAdapter() {

            @Override
            public void mousePressed(MouseEvent e) {
                super.mousePressed(e);

                Point point = e.getPoint();
                int rowIndex = table.rowAtPoint(point);
                int columnIndex = table.columnAtPoint(point);

                // if (labelColumnIndex >= 0) {
                // columnIndex = labelColumnIndex;
                // }
                // rowIndex = getRowIndex(rowIndex);
                // Object value = tableModel.getValueAt(rowIndex, columnIndex);
                // String text = value.toString();
                // rightStatusLabel.setText(text);
            }

        });

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
            @Override
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
            @Override
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

                        boolean background = true;
                        if (background) {
                            int parties = 2;
                            Runnable barrierAction = new Runnable() {
                                @Override
                                public void run() {
                                    log.info("Background filtering thread is DONE.");
                                }
                            };
                            final CyclicBarrier barrier = new CyclicBarrier(parties, barrierAction);
                            final RowFilter<Object, Object> rf2 = rf;

                            Runnable command = new Runnable() {
                                @Override
                                public void run() {
                                    try {
                                        sorter.setRowFilter(rf2);
                                        barrier.await();
                                    } catch (InterruptedException e) {
                                        log.warn(e);
                                    } catch (BrokenBarrierException e) {
                                        log.warn(e);
                                    }
                                }
                            };

                            Component parent = SwingUtilities.getRoot(MynViewer.this.frame);
                            Cursor waitCursor = setWaitCursor(parent);
                            try {
                                getThreadPool().execute(command);

                                barrier.await();
                            } catch (InterruptedException e) {
                                log.warn(e);
                            } catch (BrokenBarrierException e) {
                                log.warn(e);
                            } finally {
                                clearWaitCursor(parent, waitCursor);
                            }
                        } else {
                            sorter.setRowFilter(rf);
                        }
                    } finally {
                        long delta = stopwatch.click();
                        int postViewRowCount = sorter.getViewRowCount();
                        rightStatusLabel.setText("filter: rows=" + postViewRowCount + "/" + preViewRowCount + ", millisecond="
                                + delta);
                        log.info("< setRowFilter, delta=" + delta);
                    }
                }
            }
        });
        filterCcpPopupMenu = new JPopupMenu();
        populateCCPMenu(filterCcpPopupMenu);
        filterTextField.addMouseListener(new PopupListener(filterCcpPopupMenu) {

            @Override
            public void mousePressed(MouseEvent e) {
                if (!filterTextField.isEnabled()) {
                    return;
                }
                super.mousePressed(e);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (!filterTextField.isEnabled()) {
                    return;
                }

                super.mouseReleased(e);
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
            @Override
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
            @Override
            public Object getValueAt(int rowIndex, int columnIndex) {
                return null;
            }

            @Override
            public int getRowCount() {
                return 0;
            }

            @Override
            public int getColumnCount() {
                return 0;
            }
        });
        dataModel.setTableName("");
        // dataModel.setTables(null);

    }

    protected void copyColumn(int rowIndex, int columnIndex) {
        if (tableModel != null) {
            tableModel.copyColumn(getRowIndex(rowIndex), columnIndex);
        }
    }

    protected void selectColumnAsLabel(int rowIndex, int columnIndex) {
        setLabelColumnIndex(columnIndex);
    }

    private int getRowIndex(int rowIndex) {
        return table.convertRowIndexToModel(rowIndex);
    }

    protected void deleteRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.deleteRow(getRowIndex(rowIndex));
        }
    }

    protected void duplicateRow(int rowIndex) {
        if (tableModel != null) {
            tableModel.duplicateRow(getRowIndex(rowIndex), this.getFrame());
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
        JListBinding<TableListItem, MnyViewerDataModel, JList> jListBinding = SwingBindings.createJListBinding(UpdateStrategy.READ,
                dataModel, listOfTablesBeanProperty, list);
        jListBinding.bind();
        //
        BeanProperty<MnyViewerDataModel, TableModel> dataModelBeanProperty = BeanProperty.create("tableModel");
        ELProperty<JTable, Object> jTableEvalutionProperty = ELProperty.create("${model}");
        AutoBinding<MnyViewerDataModel, TableModel, JTable, Object> autoBinding = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelBeanProperty, table, jTableEvalutionProperty);
        autoBinding.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_1 = BeanProperty.create("tableName");
        BeanProperty<JTextField, String> jTextFieldBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextField, String> autoBinding_2 = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelBeanProperty_1, textField, jTextFieldBeanProperty_1);
        autoBinding_2.bind();
        //
        ELProperty<MnyViewerDataModel, Object> dataModelEvalutionProperty = ELProperty.create("${tableMetaData}");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, Object, JTextArea, String> autoBinding_1 = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelEvalutionProperty, textArea, jTextAreaBeanProperty);
        autoBinding_1.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_2 = BeanProperty.create("headerInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_1 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_3 = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelBeanProperty_2, headerTextArea, jTextAreaBeanProperty_1);
        autoBinding_3.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_3 = BeanProperty.create("keyInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_2 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_4 = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelBeanProperty_3, keyInfoTextArea, jTextAreaBeanProperty_2);
        autoBinding_4.bind();
        //
        BeanProperty<MnyViewerDataModel, String> dataModelBeanProperty_4 = BeanProperty.create("indexInfo");
        BeanProperty<JTextArea, String> jTextAreaBeanProperty_3 = BeanProperty.create("text");
        AutoBinding<MnyViewerDataModel, String, JTextArea, String> autoBinding_5 = Bindings.createAutoBinding(UpdateStrategy.READ,
                dataModel, dataModelBeanProperty_4, indexInfoTextArea, jTextAreaBeanProperty_3);
        autoBinding_5.bind();
    }

    private TableRowSorter<TableModel> createTableRowSorter(final MnyTableModel tableModel) {
        TableRowSorter<TableModel> sorter = new MnyTableRowSorter(this, tableModel, tableModel);
        RowSorterListener listener = new MnyRowSorterListener();
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

                setNewRowSorter(sorter);
            }
        } else {
            filterTextField.setEnabled(false);
            filterTextField.setText("Filter is disable");
            filterOnSelectedColumnCheckBox.setEnabled(false);

            if (tableModel != null) {
                sorter = null;
                setNewRowSorter(sorter);
            }
        }
    }

    private void setNewRowSorter(TableRowSorter<TableModel> sorter) {
        settingNewSorter.set(true);
        try {
            MynViewer.this.table.setRowSorter(sorter);
        } finally {
            settingNewSorter.set(false);
        }
    }

    private void selectAsRow1ForDiff(int rowIndex) {
        rowIndex = getRowIndex(rowIndex);

        log.info("Selected rowIndex=" + rowIndex + " as row #1");
        try {
            Map<String, Object> rowValues = tableModel.getRowValues(rowIndex);
            selectRowValues1 = rowValues;
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    private void selectAsRow2ForDiff(int rowIndex, JComponent source) {

        rowIndex = getRowIndex(rowIndex);

        log.info("Selected rowIndex=" + rowIndex + " as row #2");
        try {
            Map<String, Object> rowValues = tableModel.getRowValues(rowIndex);
            selectRowValues2 = rowValues;
            if (selectRowValues1 != null) {
                int n1 = selectRowValues1.size();
                int n2 = selectRowValues2.size();
                if (n1 != n2) {
                    log.warn("Two rows do not have same size!");
                    return;
                }
                List<DiffData> diffs = diffRowsValues(selectRowValues1, selectRowValues2);

                if (log.isDebugEnabled()) {
                    for (DiffData diff : diffs) {
                        if (diff.getValue1() instanceof byte[]) {
                            log.debug("DIFF columm=" + diff.getKey() + ", value1=" + "byte[]-instance" + ", value2="
                                    + "byte[]-instance");
                        } else {
                            log.debug("DIFF columm=" + diff.getKey() + ", value1=" + diff.getValue1() + ", value2="
                                    + diff.getValue2());
                        }
                    }
                }
                Component parentComponent = frame;
                final JTextArea textArea = new JTextArea();
                // textArea.setFont(new Font("Sans-Serif", Font.PLAIN, 10));
                textArea.setEditable(false);
                StringBuilder sb = new StringBuilder();
                for (DiffData diff : diffs) {
                    if (diff.getValue1() instanceof byte[]) {
                        sb.append("columm=" + diff.getKey() + ", value1=" + "byte[]-instance" + ", value2=" + "byte[]-instance");
                        sb.append("\n");
                        // sb.append("\n");
                    } else {
                        sb.append("columm=" + diff.getKey() + ", value1=" + diff.getValue1() + ", value2=" + diff.getValue2());
                        sb.append("\n");
                        // sb.append("\n");
                    }
                }
                textArea.setText(sb.toString());
                textArea.setCaretPosition(0);

                // stuff it in a scrollpane with a controlled size.
                JScrollPane scrollPane = new JScrollPane(textArea);
                scrollPane.setPreferredSize(new Dimension(350, 150));
                Object message = scrollPane;
                JOptionPane.showMessageDialog(parentComponent, message, "Diff Result", JOptionPane.INFORMATION_MESSAGE);
            } else {
                Component parentComponent = frame;
                String message = "Please select a first row";
                int messageType = JOptionPane.ERROR_MESSAGE;
                String title = "Missing required arguments";
                JOptionPane.showMessageDialog(parentComponent, message, title, messageType);
            }
        } catch (IOException e) {
            log.error(e, e);
        }
    }

    private List<DiffData> diffRowsValues(Map<String, Object> rowValues1, Map<String, Object> rowValues2) {
        List<DiffData> diffs = new ArrayList<DiffData>();
        for (String key : rowValues1.keySet()) {

            Object value1 = rowValues1.get(key);
            Object value2 = rowValues2.get(key);

            if (log.isDebugEnabled()) {
                log.debug("key=" + key + ", value1=" + value1 + ", value1=" + value2);
            }

            boolean same = rowValuesAreSame(value1, value2);

            if (!same) {
                DiffData diffData = new DiffData(key, value1, value2);
                diffs.add(diffData);
            }
        }
        return diffs;
    }

    private boolean rowValuesAreSame(Object value1, Object value2) {
        boolean same = false;
        if ((value1 == null) && (value2 == null)) {
            same = true;
        } else if (value1 == null) {
            same = false;
        } else if (value2 == null) {
            same = false;
        } else {
            if ((value1 instanceof Comparable) && (value2 instanceof Comparable)) {
                same = ((Comparable) value1).compareTo(value2) == 0;
            } else {
                same = value1.equals(value2);
            }
        }
        return same;
    }

    private void tableSelected(TableListItem item) throws IOException {
        final Table jackcessTable = item.getTable();
        String tableName = jackcessTable.getName();
        log.info("> new table is selected, table=" + tableName);

        dataModel.setTable(jackcessTable);
        dataModel.setTableName(tableName);
        dataModel.setTableMetaData(TableUtils.parseTableMetaData(jackcessTable));
        dataModel.setHeaderInfo(TableUtils.parseHeaderInfo(jackcessTable, openedDb));
        dataModel.setKeyInfo(TableUtils.parseKeyInfo(jackcessTable));
        dataModel.setIndexInfo(TableUtils.parseIndexInfo(jackcessTable));
        selectRowValues1 = null;
        selectRowValues2 = null;

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
        tableModel = new MnyTableModel(jackcessTable);
        tableModel.setDbReadOnly(dbReadOnly);

        toggleTableSorting();

        if (log.isDebugEnabled()) {
            log.debug("setting new tableModel ...");
        }
        dataModel.setTableModel(tableModel);

        updateGotoColumnMenu();
        setLabelColumnIndex(jackcessTable);

        rightStatusLabel.setText("open table=" + jackcessTable.getName());
    }

    private void setLabelColumnIndex(Table jackcessTable) {
        labelColumnIndex = -1;

        int index = findDefaulLlabelColumnIndex(jackcessTable);
        labelColumnIndex = index;

        setLabelColumnIndex(labelColumnIndex);
    }

    private int findDefaulLlabelColumnIndex(Table jackcessTable) {
        String[] columnNames = { "szFull", "szName" };
        int primaryKeyColumn = -1;
        int columnIndex = -1;
        if (jackcessTable != null) {
            IndexLookup indexLookup = new IndexLookup();
            for (Column column : jackcessTable.getColumns()) {
                if (indexLookup.isPrimaryKeyColumn(column)) {
                    if (primaryKeyColumn < 0) {
                        primaryKeyColumn = tableModel.getColumnIndex(column.getName());
                    }
                }
                if (columnNames != null) {
                    for (String name : columnNames) {
                        if (column.getName().compareTo(name) == 0) {
                            if (columnIndex < 0) {
                                columnIndex = column.getColumnIndex();
                            }
                        }
                    }
                }
            }
        }

        int index = -1;
        if (columnIndex >= 0) {
            index = columnIndex;
        }
        if (index < 0) {
            index = primaryKeyColumn;
        }
        if (index < 0) {
            index = 0;
        }
        return index;
    }

    private void setLabelColumnIndex(int columnIndex) {
        labelColumnIndex = columnIndex;
        log.info("setLabelColumnIndex, labelColumnIndex=" + labelColumnIndex);

        int rowIndex = table.getSelectedRow();
        log.info("setLabelColumnIndex, rowIndex=" + rowIndex);
        if (rowIndex >= 0) {
            updateRowLabel(rowIndex);
        }
    }

    private void updateGotoColumnMenu() {
        if (log.isDebugEnabled()) {
            log.debug("> updateGotoColumnMenu");
        }

        SortedSet<String> columnNames = new TreeSet<String>();

        int count = tableModel.getColumnCount();
        for (int i = 0; i < count; i++) {
            String columnName = tableModel.getColumnName(i);
            columnNames.add(columnName);
        }

        gotoColumnMenu.removeAll();
        if (count < 10) {
            for (String columnName : columnNames) {
                JMenuItem menuItem = new JMenuItem(new GotoToColumnAction(columnName));
                gotoColumnMenu.add(menuItem);
            }
        } else {
            JMenu parentMenu = null;
            int i = 0;
            for (String columnName : columnNames) {
                if ((i % 10) == 0) {
                    parentMenu = new JMenu(columnName + " ...");
                    gotoColumnMenu.add(parentMenu);
                }
                JMenuItem menuItem = new JMenuItem(new GotoToColumnAction(columnName));
                parentMenu.add(menuItem);
                i++;
            }
        }
        if (log.isDebugEnabled()) {
            log.debug("< updateGotoColumnMenu" + ", count=" + count);
        }
    }

    private void updateRowLabel(int rowIndex) {
        if (rowIndex < 0) {
            return;
        }

        int columnIndex = 0;
        if (labelColumnIndex >= 0) {
            columnIndex = labelColumnIndex;
        }
        rowIndex = getRowIndex(rowIndex);

        if (log.isDebugEnabled()) {
            log.debug("updateRowLabel: rowIndex=" + rowIndex + ", columnIndex=" + columnIndex);
        }

        if ((columnIndex >= 0) && (columnIndex >= 0)) {
            Object value = tableModel.getValueAt(rowIndex, columnIndex);
            String text = null;
            if (value != null) {
                text = value.toString();
            } else {
                text = "";
            }
            rightStatusLabel.setText(text);
        }
    }

    private void populateCCPMenu(JPopupMenu mainMenu) {
        JMenuItem menuItem = null;

        menuItem = new JMenuItem(new DefaultEditorKit.CutAction());
        menuItem.setText("Cut");
        menuItem.setMnemonic(KeyEvent.VK_T);
        mainMenu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.CopyAction());
        menuItem.setText("Copy");
        menuItem.setMnemonic(KeyEvent.VK_C);
        mainMenu.add(menuItem);

        menuItem = new JMenuItem(new DefaultEditorKit.PasteAction());
        menuItem.setText("Paste");
        menuItem.setMnemonic(KeyEvent.VK_P);
        mainMenu.add(menuItem);
    }

    Cursor setWaitCursor(Component parent) {
        Cursor waitCursor = null;
        if ((parent != null) && (parent.isShowing())) {
            waitCursor = Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR);
            log.info("YES setCursor=" + waitCursor);
            parent.setCursor(waitCursor);
        } else {
            log.info("NO setCursor=" + waitCursor);
        }
        return waitCursor;
    }

    void clearWaitCursor(Component parent, Cursor waitCursor) {
        if (waitCursor != null) {
            log.info("YES CLEAR setCusror");
            parent.setCursor(null);
        } else {
            log.info("NO CLEAR setCusror");
        }
    }

    protected void appClosed() {
        if (openedDb != null) {
            openedDb.close();
        }
        openedDb = null;
    }

    public void toggleSortOrderStarted(int column) {
        log.info("> toggleSortOrderStarted, column=" + column);
        tableModel.clearCache();
        tableModel.fillCacheForSorting(column);
    }
}
