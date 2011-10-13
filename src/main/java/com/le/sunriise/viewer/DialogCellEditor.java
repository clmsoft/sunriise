package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class DialogCellEditor extends AbstractCellEditor implements TableCellEditor, ActionListener {
    private static final Logger log = Logger.getLogger(DialogCellEditor.class);

    private JButton button;
    private DateEditorDialog dialog;
    private static final String EDIT = "edit";

    public DialogCellEditor() {
        super();
        button = new JButton();
        button.setActionCommand(EDIT);
        button.addActionListener(this);
        button.setBorderPainted(false);

        this.dialog = new DateEditorDialog(button);
        this.dialog.setCellEditor(this);
    }

    
    @Override
    public Object getCellEditorValue() {
        log.info("> getCellEditorValue");
        return dialog.getValue();
    }

    
    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        dialog.setValue(value);
        return button;
    }

    
    @Override
    public void actionPerformed(ActionEvent e) {
        if (EDIT.equals(e.getActionCommand())) {
            // The user has clicked the cell, so
            // bring up the dialog.
            button.setText("* EDITING *");
            dialog.startEditing();
        }
    }
}
