package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Map;
import java.util.Set;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class NewRowDialog extends JDialog {
    private static final Logger log = Logger.getLogger(NewRowDialog.class);

    private final JPanel contentPanel = new JPanel();
    private boolean cancel;

    private Map<String, Object> data;

    private Set<Integer> uniqueColumnIndex;

    public static NewRowDialog showDialog(Map<String, Object> data, Set<Integer> uniqueColumnIndex, Component locationRealativeTo) {
        NewRowDialog dialog = new NewRowDialog(data, uniqueColumnIndex);
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setModalityType(ModalityType.APPLICATION_MODAL);
        dialog.pack();
        dialog.setLocationRelativeTo(locationRealativeTo);
        dialog.setVisible(true);
        return dialog;
    }

    /**
     * Create the dialog.
     * 
     * @param uniqueColumnIndex
     */
    public NewRowDialog(Map<String, Object> data, Set<Integer> uniqueColumnIndex) {
        setTitle("Set Unique Column Value");
        this.data = data;
        this.uniqueColumnIndex = uniqueColumnIndex;
        // setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        // RowSpec[] rowSpecs = new RowSpec[] {
        // FormFactory.RELATED_GAP_ROWSPEC,
        // FormFactory.DEFAULT_ROWSPEC,
        // FormFactory.RELATED_GAP_ROWSPEC,
        // FormFactory.DEFAULT_ROWSPEC,
        // FormFactory.RELATED_GAP_ROWSPEC,
        // FormFactory.DEFAULT_ROWSPEC,};
        RowSpec[] rowSpecs = new RowSpec[uniqueColumnIndex.size() * 2];
        for (int i = 0; i < uniqueColumnIndex.size(); i++) {
            rowSpecs[(i * 2) + 0] = FormFactory.RELATED_GAP_ROWSPEC;
            rowSpecs[(i * 2) + 1] = FormFactory.DEFAULT_ROWSPEC;
        }
        // log.info("rowSpecs=" + rowSpecs.length);
        // log.info("rowSpecs2=" + rowSpecs.length);
        // for(int i = 0; i < rowSpecs.length; i++) {
        // if (! rowSpecs[i].equals(rowSpecs2[i])) {
        // log.error("Not same, i=" + i);
        // }
        // }

        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, rowSpecs));

        addRows();

        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {

                    public void actionPerformed(ActionEvent e) {
                        cancel = false;
                        dispose();
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
                        cancel = true;
                        dispose();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }

    private void addRows() {
        int row = 0;
        int i = 0;
        for (Integer index : uniqueColumnIndex) {
            row = (i + 1) * 2;
            {
                JLabel lblNewLabel = new JLabel(getKey(data, index));
                contentPanel.add(lblNewLabel, "2, " + row + ", right, default");
            }
            {
                JTextField textField = new JTextField();
                contentPanel.add(textField, "4, " + row + ", fill, default");
                textField.setColumns(10);
                Object value = getValue(data, index);
                if (value != null) {
                    textField.setText(value.toString());
                }
            }
            i++;
        }
    }

    private Object getValue(Map<String, Object> data, Integer index) {
        int i = 0;
        for (Object value : data.values()) {
            if (i == index) {
                return value;
            }
            i++;
        }
        return null;
    }

    private String getKey(Map<String, Object> data, Integer index) {
        int i = 0;
        for (String key : data.keySet()) {
            if (i == index) {
                return key;
            }
            i++;
        }
        return null;
    }

    public boolean isCancel() {
        return cancel;
    }

}
