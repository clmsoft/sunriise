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
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

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

import com.healthmarketscience.jackcess.Database;
import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.le.sunriise.Utils;
import com.le.sunriise.backup.BackupFileUtils;
import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.model.bean.OpenDbDialogDataModel;

public class OpenDbDialog extends JDialog {
    private static final Logger log = Logger.getLogger(OpenDbDialog.class);

    private final JPanel contentPanel = new JPanel();

    private boolean cancel = false;

    private JPasswordField passwordField;

    private JCheckBox readOnlyCheckBox;

    private JCheckBox encryptedCheckBox;

    private OpenDbDialogDataModel dataModel = new OpenDbDialogDataModel();
    private JComboBox dbFileNames;

    private OpenedDb openedDb;

    private JButton okButton;

    private JButton cancelButton;

    private boolean hide = true;

    private final class CancelAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            setCancel(true);

            if (preHideDialog()) {
                if (hide) {
                    setVisible(false);
                } else {
                    dispose();
                }
            }
        }
    }

    private final class OkAction implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            String dbFileName = (String) dbFileNames.getSelectedItem();
            if ((dbFileName == null) || (dbFileName.length() <= 0)) {
                JOptionPane.showMessageDialog(dbFileNames, "Please enter a database filename.", "Missing database filename",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (BackupFileUtils.isMnyBackupFile(dbFileName) && (!readOnlyCheckBox.isSelected())) {
                JOptionPane.showMessageDialog(dbFileNames, "Cannot open Money backup file write-mode.",
                        "Cannot open in write-mode", JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (BackupFileUtils.isMnyBackupFile(dbFileName)) {
                try {
                    File dbFile = new File(dbFileName);
                    dbFile = BackupFileUtils.createBackupAsTempFile(dbFile, true, -1L);
                    dbFileName = dbFile.getAbsolutePath();
                } catch (IOException e) {
                    log.error(e, e);
                }
            }
            try {
                if (OpenDbDialog.this.openedDb != null) {
                    OpenDbDialog.this.openedDb.close();
                }

                OpenDbDialog.this.openedDb = openDb(dbFileName, passwordField.getPassword(), readOnlyCheckBox.isSelected(),
                        encryptedCheckBox.isSelected());

                dbOpenedCallback();
            } catch (Exception e) {
                log.error(e, e);
                JOptionPane.showMessageDialog(dbFileNames, dbFileName + " \n" + e.toString(), "Error open db file",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            File file = OpenDbDialog.this.openedDb.getDbFile();
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

            if (preHideDialog()) {
                if (hide) {
                    setVisible(false);
                } else {
                    dispose();
                }
            }
        }

    }

    static OpenDbDialog showDialog(OpenedDb opendDb, List<String> recentOpenFileNames, Component locationRelativeTo,
            boolean disableReadOnlyCheckBox, final CreateOpenedDbPlugin plugin) {
        String title = null;
        OpenDbDialog dialog = new OpenDbDialog(opendDb, title, recentOpenFileNames) {
            @Override
            protected OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted)
                    throws IOException {
                if (plugin != null) {
                    return plugin.openDb(dbFileName, passwordChars, readOnly, encrypted);
                } else {
                    return super.openDb(dbFileName, passwordChars, readOnly, encrypted);
                }
            }
        };

        showDialog(dialog, locationRelativeTo, disableReadOnlyCheckBox);

        return dialog;
    }

    protected void dbOpenedCallback() {
        log.info("Opened dbFile=" + openedDb.getDbFile());
        log.info("    isMemoryMapped=" + openedDb.isMemoryMapped());
        
      Database db = openedDb.getDb();
      if (db.getSystemCatalog() == null) {
          // go into scanvenger mode
          try {
            HeaderPage headerPage = new HeaderPage(openedDb.getDbFile());
            int pageSize = headerPage.getJetFormat().PAGE_SIZE;
            
        } catch (IOException e) {
            
        }
      }
    }

    protected boolean preHideDialog() {
        return true;
    }

    public static void showDialog(OpenDbDialog dialog, Component locationRelativeTo, boolean disableReadOnlyCheckBox) {
        dialog.setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);

        if (disableReadOnlyCheckBox) {
            dialog.readOnlyCheckBox.setEnabled(false);
        } else {
            dialog.readOnlyCheckBox.setEnabled(true);
        }

        dialog.pack();
        dialog.setLocationRelativeTo(locationRelativeTo);
        dialog.setVisible(true);
    }

    /**
     * Create the dialog.
     */
    public OpenDbDialog(OpenedDb openedDb, String title, final List<String> recentOpenFileNames) {
        if ((title == null)) {
            title = "Open";
        }
        setTitle(title);
        // setModalityType(ModalityType.APPLICATION_MODAL);
        this.openedDb = openedDb;
        this.dataModel.setRecentOpenFileNames(recentOpenFileNames);
        // setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.UNRELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC,
                FormFactory.LABEL_COMPONENT_GAP_COLSPEC, ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC,
                FormFactory.DEFAULT_COLSPEC, FormFactory.UNRELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC,
                FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, }));
        {
            JLabel lblNewLabel = new JLabel("DB Filename");
            lblNewLabel.setHorizontalAlignment(SwingConstants.TRAILING);
            contentPanel.add(lblNewLabel, "2, 2, right, default");
        }
        {
            JButton btnNewButton = new JButton("...");
            btnNewButton.addActionListener(new ActionListener() {
                private JFileChooser fc = null;

                @Override
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
                    // String fileName= selectedFile.getName();
                    // if (fileName.endsWith(".mny")) {
                    // encryptedCheckBox.setSelected(true);
                    // } else {
                    // encryptedCheckBox.setSelected(false);
                    // }
                    dbFileNames.setSelectedItem(selectedFile.getAbsolutePath());

                }
            });
            {
                dbFileNames = new JComboBox();
                dbFileNames.addItemListener(new ItemListener() {

                    @Override
                    public void itemStateChanged(ItemEvent e) {
                        if (e.getStateChange() == ItemEvent.SELECTED) {
                            String fileName = (String) dbFileNames.getSelectedItem();
                            if (fileName == null) {
                                return;
                            }
                            if (fileName.length() <= 0) {
                                return;
                            }
                            if (BackupFileUtils.isMnyFiles(fileName)) {
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
            readOnlyCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("readOnlyCheckBox=" + readOnlyCheckBox.isSelected());
                    }
                }
            });
            readOnlyCheckBox.setSelected(true);
            contentPanel.add(readOnlyCheckBox, "4, 6");
        }
        {
            encryptedCheckBox = new JCheckBox("Encrypted (keep selected for *.mny file)");
            encryptedCheckBox.addItemListener(new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent e) {
                    if (log.isDebugEnabled()) {
                        log.debug("encryptedCheckBox=" + encryptedCheckBox.isSelected());
                    }
                }
            });
            encryptedCheckBox.setSelected(true);
            contentPanel.add(encryptedCheckBox, "4, 8");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                okButton = new JButton("OK");
                okButton.addActionListener(new OkAction());
                okButton.setActionCommand("OK");
                buttonPane.add(okButton);
                getRootPane().setDefaultButton(okButton);
            }
            {
                cancelButton = new JButton("Cancel");
                cancelButton.addActionListener(new CancelAction());
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }

        initDataBindings();
    }

    public boolean isCancel() {
        return cancel;
    }

    public void setCancel(boolean cancel) {
        this.cancel = cancel;
    }

    // public Database getDb() {
    // return opendDb.getDb();
    // }
    //
    // public File getDbFile() {
    // return opendDb.getDbFile();
    // }

    public JCheckBox getReadOnlyCheckBox() {
        return readOnlyCheckBox;
    }

    protected void initDataBindings() {
        BeanProperty<OpenDbDialogDataModel, List<String>> openDbDialogDataModelBeanProperty = BeanProperty
                .create("recentOpenFileNames");
        JComboBoxBinding<String, OpenDbDialogDataModel, JComboBox> jComboBinding = SwingBindings.createJComboBoxBinding(
                UpdateStrategy.READ, dataModel, openDbDialogDataModelBeanProperty, dbFileNames);
        jComboBinding.bind();
    }

    public OpenedDb getOpenedDb() {
        return openedDb;
    }

    public static void updateRecentOpenFileNames(List<String> recentOpenFileNames, Preferences preferences) {
        int size;
        size = recentOpenFileNames.size();
        size = Math.min(size, 10);
        if (log.isDebugEnabled()) {
            log.debug("prefs: recentOpenFileNames_size=" + size);
        }
        preferences.putInt("recentOpenFileNames_size", size);
        for (int i = 0; i < size; i++) {
            if (log.isDebugEnabled()) {
                log.debug("prefs: recentOpenFileNames_" + i + ", value=" + recentOpenFileNames.get(i));
            }
            preferences.put("recentOpenFileNames_" + i, recentOpenFileNames.get(i));
        }
    }

    public static List<String> getRecentOpenFileNames(Preferences prefs) {
        List<String> recentOpenFileNames = new ArrayList<String>();
        int size = prefs.getInt("recentOpenFileNames_size", 0);
        size = Math.min(size, 10);
        for (int i = 0; i < size; i++) {
            String value = prefs.get("recentOpenFileNames_" + i, null);
            if (value != null) {
                recentOpenFileNames.add(value);
            }
        }
        return recentOpenFileNames;
    }

    public JButton getOkButton() {
        return okButton;
    }

    public JButton getCancelButton() {
        return cancelButton;
    }

    public boolean isHide() {
        return hide;
    }

    public void setHide(boolean hide) {
        this.hide = hide;
    }

    protected OpenedDb openDb(String dbFileName, char[] passwordChars, boolean readOnly, boolean encrypted) throws IOException {
        return Utils.openDb(dbFileName, passwordChars, readOnly, encrypted);
    }
}
