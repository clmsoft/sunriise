package com.le.sunriise.viewer;

import java.awt.Component;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import javax.swing.AbstractCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

public class TableCellDateEditor extends AbstractCellEditor implements TableCellEditor {
    private static final Logger log = Logger.getLogger(TableCellDateEditor.class);

    private JComponent component = null;

    public TableCellDateEditor() {
        super();
//        JTextField textField = new JTextField();
        JButton button = new JButton();
        KeyListener keyListener = new KeyListener() {
            public void keyTyped(KeyEvent e) {
//                if (e.isAltDown()) {
//                    if (e.getKeyChar() == 'd') {
//                        Date now = new Date();
//                        ((JTextField) component).setText(now.toString());
//                    }
//                }
//                log.info("> keyTyped, e=" + e);
//                log.info("> keyTyped, controlDown=" + e.isControlDown());
//                log.info("> keyTyped, modifier=" + KeyEvent.getModifiersExText(e.getModifiersEx()) + ", char=" + e.getKeyChar());
            }

            public void keyReleased(KeyEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("> keyReleased");
                }
            }

            public void keyPressed(KeyEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("> keyPressed");
                }
            }
        };
//        textField.addKeyListener(keyListener);
//        this.component = textField;
        this.component = button;
    }

    public Object getCellEditorValue() {
//        String text = ((JTextField) component).getText();
        String text = ((JButton) component).getText();
        log.info("< getCellEditorValue, text=" + text);
        return text;
    }

    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        String text = null;
        if (value != null) {
            text = value.toString();
        }
//        ((JTextField) component).setText(text);
        ((JButton) component).setText(text);
        log.info("< getTableCellEditorComponent, text=" + text);
        return component;
    }

}
