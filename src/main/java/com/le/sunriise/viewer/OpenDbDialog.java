package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.List;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;
import org.jdesktop.beansbinding.AutoBinding.UpdateStrategy;
import org.jdesktop.beansbinding.BeanProperty;
import org.jdesktop.swingbinding.JComboBoxBinding;
import org.jdesktop.swingbinding.SwingBindings;

import com.healthmarketscience.jackcess.CodecProvider;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PageChannel;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.model.bean.OpenDbDialogDataModel;

public class OpenDbDialog extends JDialog {
    private static final Logger log = Logger.getLogger(OpenDbDialog.class);

    private final JPanel contentPanel = new JPanel();

    private boolean cancel = false;
    
    private JPasswordField passwordField;

    private JCheckBox readOnlyCheckBox;

    private Database db;

    private File dbFile;
    
    private JCheckBox encryptedCheckBox;
    
    private OpenDbDialogDataModel dataModel = new OpenDbDialogDataModel();
    private JComboBox dbFileNames;

    private File dbLockFile;

    public static OpenDbDialog showDialog(Database db, File dbFile, List<String> recentOpenFileNames, Component locationRealativeTo) {
        OpenDbDialog dialog = new OpenDbDialog(db, dbFile, recentOpenFileNames);
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setLocationRelativeTo(locationRealativeTo);
        dialog.setVisible(true);
        return dialog;
    }

    /**
     * Create the dialog.
     * 
     * @param dbFile
     * @param db
     */
    public OpenDbDialog(final Database db, final File dbFile, final List<String> recentOpenFileNames) {
        setTitle("Open");
        // setModalityType(ModalityType.APPLICATION_MODAL);
        this.db = db;
        this.dbFile = dbFile;
        this.dataModel.setRecentOpenFileNames(recentOpenFileNames);
        // setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] {
                FormFactory.UNRELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"),
                FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC,
                FormFactory.UNRELATED_GAP_COLSPEC,},
            new RowSpec[] {
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC,}));
        {
            JLabel lblNewLabel = new JLabel("DB Filename");
            lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            contentPanel.add(lblNewLabel, "2, 2, right, default");
        }
        {
            JButton btnNewButton = new JButton("...");
            btnNewButton.addActionListener(new ActionListener() {
                private JFileChooser fc = null;

                public void actionPerformed(ActionEvent event) {
                    Component component = (Component) event.getSource();
                    if (fc == null) {
                        File currentDirectory = new File(".");
                        
                        String fileName = (String) dbFileNames.getSelectedItem();
                        if ((fileName != null) && (fileName.length() > 0)) {
                            File file = new File(fileName);
                            if (file.exists()) {
                                if (file.isDirectory()) {
                                    currentDirectory = file.getAbsoluteFile();
                                } else {
                                    currentDirectory = file.getParentFile().getAbsoluteFile();
                                }
                            }
                        }
                        fc = new JFileChooser(currentDirectory);
                    }
                    if (fc.showOpenDialog(JOptionPane.getFrameForComponent(component)) == JFileChooser.CANCEL_OPTION) {
                        return;
                    }
                    File selectedFile = fc.getSelectedFile();
                    selectedFile = selectedFile.getAbsoluteFile();
//                    String fileName= selectedFile.getName();
//                    if (fileName.endsWith(".mny")) {
//                        encryptedCheckBox.setSelected(true);
//                    } else {
//                        encryptedCheckBox.setSelected(false);
//                    }
                    dbFileNames.setSelectedItem(selectedFile.getAbsolutePath());
                    
                }
            });
            {
                dbFileNames = new JComboBox();
                dbFileNames.addItemListener(new ItemListener() {
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String fileName = (String) dbFileNames.getSelectedItem();
                            if (fileName == null) {
                                return;
                            }
                            if (fileName.length() <= 0) {
                                return;
                            }
                            if (fileName.endsWith(".mny")) {
                                encryptedCheckBox.setSelected(true);
                            } else {
                                encryptedCheckBox.setSelected(false);
                            }
                        } else {
                        }
                    }
                });
                dbFileNames.setEditable(true);
                contentPanel.add(dbFileNames, "4, 2, fill, default");
            }
            contentPanel.add(btnNewButton, "6, 2");
        }
        {
            JLabel lblNewLabel_1 = new JLabel("Password");
            lblNewLabel_1.setHorizontalAlignment(SwingConstants.TRAILING);
            contentPanel.add(lblNewLabel_1, "2, 4, right, default");
        }
        {
            passwordField = new JPasswordField();
            contentPanel.add(passwordField, "4, 4, fill, default");
        }
        {
            readOnlyCheckBox = new JCheckBox("Read only");
            readOnlyCheckBox.setSelected(true);
            contentPanel.add(readOnlyCheckBox, "4, 6");
        }
        {
            encryptedCheckBox = new JCheckBox("Encrypted");
            encryptedCheckBox.setSelected(true);
            contentPanel.add(encryptedCheckBox, "4, 8");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent event) {
                        String dbFileName = (String) dbFileNames.getSelectedItem();
                        if ((dbFileName == null) || (dbFileName.length() <= 0)) {
                            JOptionPane.showMessageDialog(dbFileNames, "Please enter a database filename.", "Missing database filename",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        try {
                            if (OpenDbDialog.this.db != null) {
                                try {
                                    OpenDbDialog.this.db.close();
                                } catch (IOException e) {
                                    log.warn(e);
                                } finally {
                                    OpenDbDialog.this.db = null;
                                    if (dbLockFile != null) {
                                        if (! dbLockFile.delete()) {
                                            log.warn("Could NOT delete db lock file=" + dbLockFile);
                                        }
                                    }
                                }
                            }
                            openDb(dbFileName, passwordField.getPassword(), readOnlyCheckBox.isSelected(), encryptedCheckBox.isSelected());
                        } catch (IOException e) {
                            log.error(e);
                            JOptionPane.showMessageDialog(dbFileNames, dbFileName + " \n" + e.toString(), "Error open db file", JOptionPane.ERROR_MESSAGE);
                            return;
                        }
                        File file = getDbFile();
                        if (file != null) {
                            List<String> list = dataModel.getRecentOpenFileNames();
                            if (list.contains(file.getAbsolutePath())) {
                                list.remove(file.getAbsolutePath());
                            }
                            list.add(0, file.getAbsolutePath());
                            if (log.isDebugEnabled()) {
                                log.debug(list);
                            }
                        }
                        setCancel(false);
                        // dispose();
                        setVisible(false);
                    }
                });
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                JButton cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent e) {
                        setCancel(true);
                        // dispose();
                        setVisible(false);
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }

        initDataBindings();
    }

    protected void openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted) throws IOException {
        CodecProvider cryptCodecProvider = null;
        String password = null;
        if ((passwordChars != null) && (passwordChars.length > 0)) {
            password = new String(passwordChars);
        }
        cryptCodecProvider = new CryptCodecProvider(password);
        if (!encrypted) {
            cryptCodecProvider = null;
        }
        boolean autoSync = true;
        Charset charset = null;
        TimeZone timeZone = null;
        this.dbFile = new File(dbFileName);
        try {
            log.info("> Database.open, dbFile=" + dbFile);
            if ((! readOnly) && (dbFileName.endsWith(".mny"))) {
                dbLockFile = null;
                if((dbLockFile = lockDb(dbFile)) == null) {
                    throw new IOException("Cannot lock dbFile=" + dbFileName);
                } else {
                    log.info("Created db lock file=" + dbLockFile);
                }
            }
            this.db = Database.open(dbFile, readOnly, autoSync, charset, timeZone, cryptCodecProvider);

            printEncryptionInfo();
        } finally {
            log.info("< Database.open, dbFile=" + dbFile);
        }
    }

    protected File lockDb(File dbFile) throws IOException {
        File parentDir = dbFile.getParentFile().getAbsoluteFile();
        String name = dbFile.getName();
        int i = name.lastIndexOf('.');
        if (i <= 0) {
            log.warn("Cannot lock dbFile=" + name + ". Cannot find suffix");
            return null;
        }
        name = name.substring(0, i);
        File lockFile = new File(parentDir, name + ".lrd");
        if (lockFile.exists()) {
            log.warn("Cannot lock dbFile=" + name + ". Lock file exists");
            return null;
        }
        
        if(lockFile.createNewFile()) {
            lockFile.deleteOnExit();
            return lockFile;
        } else {
            return null;
        }
    }

    private void printEncryptionInfo() {
        PageChannel pageChannel = db.getPageChannel();
        ByteBuffer buffer = pageChannel.createPageBuffer();
        int ENCRYPTION_FLAGS_OFFSET = 0x298;
        byte flag = buffer.get(ENCRYPTION_FLAGS_OFFSET);
        if (log.isDebugEnabled()) {
            log.debug("ENCRYPTION_FLAGS=0x" + String.format("%x", flag));
        }
        int NEW_ENCRYPTION = 0x6;
        if ((flag & NEW_ENCRYPTION) != 0) {
            if (log.isDebugEnabled()) {
                log.debug("NEW_ENCRYPTION - MSISAMCryptCodecHandler");
            }
        } else {
            if (log.isDebugEnabled()) {
                log.debug("OLD_ENCRYPTION - JetCryptCodecHandler");
            }
        }
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    public Database getDb() {
        return db;
    }

    public File getDbFile() {
        return dbFile;
    }

    public JCheckBox getReadOnlyCheckBox() {
        return readOnlyCheckBox;
    }

    protected void initDataBindings() {
        BeanProperty<OpenDbDialogDataModel, List<String>> openDbDialogDataModelBeanProperty = BeanProperty.create("recentOpenFileNames");
        JComboBoxBinding<String, OpenDbDialogDataModel, JComboBox> jComboBinding = SwingBindings.createJComboBoxBinding(UpdateStrategy.READ, dataModel, openDbDialogDataModelBeanProperty, dbFileNames);
        jComboBinding.bind();
    }
}
