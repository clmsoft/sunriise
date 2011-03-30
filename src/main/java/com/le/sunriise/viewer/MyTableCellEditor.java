package com.le.sunriise.viewer;

import java.awt.Component;

import javax.swing.AbstractCellEditor;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class MyTableCellEditor extends AbstractCellEditor implements TableCellEditor {
    private static final Logger log = Logger.getLogger(MyTableCellEditor.class);

    private JComponent component = new JTextField();

    public Object getCellEditorValue() {
        String text = ((JTextField) component).getText();
        log.info("< getCellEditorValue, text=" + text);
        return text;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        String text = null;
        if (value != null) {
            text = value.toString();
        } 
        ((JTextField) component).setText(text);
        log.info("< getTableCellEditorComponent, text=" + text);
        return component;
    }

}
