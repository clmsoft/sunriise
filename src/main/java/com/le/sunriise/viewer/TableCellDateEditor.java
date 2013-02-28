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
        // JTextField textField = new JTextField();
        JButton button = new JButton();
        KeyListener keyListener = new KeyListener() {

            @Override
            public void keyTyped(KeyEvent e) {
                // if (e.isAltDown()) {
                // if (e.getKeyChar() == 'd') {
                // Date now = new Date();
                // ((JTextField) component).setText(now.toString());
                // }
                // }
                // log.info("> keyTyped, e=" + e);
                // log.info("> keyTyped, controlDown=" + e.isControlDown());
                // log.info("> keyTyped, modifier=" +
                // KeyEvent.getModifiersExText(e.getModifiersEx()) + ", char=" +
                // e.getKeyChar());
            }

            @Override
            public void keyReleased(KeyEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("> keyReleased");
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (log.isDebugEnabled()) {
                    log.debug("> keyPressed");
                }
            }
        };
        // textField.addKeyListener(keyListener);
        // this.component = textField;
        this.component = button;
    }

    @Override
    public Object getCellEditorValue() {
        // String text = ((JTextField) component).getText();
        String text = ((JButton) component).getText();
        log.info("< getCellEditorValue, text=" + text);
        return text;
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        String text = null;
        if (value != null) {
            text = value.toString();
        }
        // ((JTextField) component).setText(text);
        ((JButton) component).setText(text);
        log.info("< getTableCellEditorComponent, text=" + text);
        return component;
    }

}
