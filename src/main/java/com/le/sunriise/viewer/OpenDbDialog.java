package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.TimeZone;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.CodecProvider;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.PageChannel;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class OpenDbDialog extends JDialog {
    private static final Logger log = Logger.getLogger(OpenDbDialog.class);

    private final JPanel contentPanel = new JPanel();

    private boolean cancel = false;
    private JTextField textField;
    private JPasswordField passwordField;

    private JCheckBox readOnlyCheckBox;

    private Database db;

    private File dbFile;
    private JCheckBox encryptedCheckBox;

    public static OpenDbDialog showDialog(Component locationRealativeTo, Database db, File dbFile) {
        OpenDbDialog dialog = new OpenDbDialog(db, dbFile);
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
    public OpenDbDialog(Database db, File dbFile) {
        // setModalityType(ModalityType.APPLICATION_MODAL);
        this.db = db;
        this.dbFile = dbFile;
        // setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
        {
            JLabel lblNewLabel = new JLabel("DB Filename");
            lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            contentPanel.add(lblNewLabel, "2, 2, right, default");
        }
        {
            textField = new JTextField();
            contentPanel.add(textField, "4, 2, fill, default");
            textField.setColumns(25);
        }
        {
            JButton btnNewButton = new JButton("Open");
            btnNewButton.addActionListener(new ActionListener() {
                private JFileChooser fc = null;

                public void actionPerformed(ActionEvent event) {
                    Component component = (Component) event.getSource();
                    if (fc == null) {
                        File currentDirectory = new File(".");
                        String fileName = textField.getText();
                        ;
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
                    textField.setText(selectedFile.getAbsolutePath());
                }
            });
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
                        String dbFileName = textField.getText();
                        if ((dbFileName == null) || (dbFileName.length() <= 0)) {
                            JOptionPane.showMessageDialog(textField, "Please enter a database filename.", "Missing database filename",
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
                                }
                            }
                            openDb(dbFileName, passwordField.getPassword(), readOnlyCheckBox.isSelected(), encryptedCheckBox.isSelected());
                        } catch (IOException e) {
                            log.error(e);
                            JOptionPane.showMessageDialog(textField, dbFileName + " \n" + e.toString(), "Error open db file", JOptionPane.ERROR_MESSAGE);
                            return;
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

        if (this.dbFile != null) {
            textField.setText(dbFile.getAbsolutePath());
        }
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
            this.db = Database.open(dbFile, readOnly, autoSync, charset, timeZone, cryptCodecProvider);

            printEncryptionInfo();
        } finally {
            log.info("< Database.open, dbFile=" + dbFile);
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

}
