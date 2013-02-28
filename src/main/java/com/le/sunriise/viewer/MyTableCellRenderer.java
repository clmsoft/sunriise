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
/**
 * 
 */
package com.le.sunriise.viewer;

import java.awt.Color;
import java.awt.Component;
import java.math.BigDecimal;
import java.util.Date;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;

import org.apache.log4j.Logger;

public class MyTableCellRenderer extends DefaultTableCellRenderer {
    private static final Logger log = Logger.getLogger(MyTableCellRenderer.class);

    private final Color evenRowsColor;
    private final Color oddRowsColor;
    private TableCellRenderer parentCellRenderer = null;

    private MyTableCellRenderer(TableCellRenderer cellRenderer, Color evenRowsColor, Color oddRowsColor) {
        super();
        this.parentCellRenderer = cellRenderer;
        this.evenRowsColor = evenRowsColor;
        this.oddRowsColor = oddRowsColor;
    }

    public MyTableCellRenderer() {
        this(null);
    }

    public MyTableCellRenderer(TableCellRenderer cellRenderer) {
        this(cellRenderer, new Color(204, 255, 204), Color.WHITE);
    }

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row,
            int column) {
        Component rendererComponent = null;
        Class<? extends Object> valueClz = null;
        if (value != null) {
            valueClz = value.getClass();
            TableCellRenderer defaultRenderer = table.getDefaultRenderer(valueClz);

            if (defaultRenderer != null) {
                if (log.isDebugEnabled()) {
                    log.debug("clz=" + valueClz.getName() + ", row=" + row + ", column=" + column + ", defaultRenderer="
                            + defaultRenderer.getClass());
                }
                rendererComponent = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                if (log.isDebugEnabled()) {
                    log.debug("clz=" + valueClz.getName() + ", row=" + row + ", column=" + column + ", defaultRenderer="
                            + defaultRenderer);
                }
            }
        }

        if (rendererComponent == null) {
            if (parentCellRenderer != null) {
                rendererComponent = parentCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
                        column);
            } else {
                rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }

        // striped
        if (!isSelected) {
            if (row % 2 == 0) {
                rendererComponent.setBackground(evenRowsColor);
            } else {
                rendererComponent.setBackground(oddRowsColor);
            }
        }

        setCellHorizontalAlignment(rendererComponent, column, value);

        return rendererComponent;
    }

    protected void setCellHorizontalAlignment(Component rendererComponent, int column, Object value) {
        // right-align if it is a Date
        if ((value != null) && (value instanceof Date)) {
            if (rendererComponent instanceof JLabel) {
                JLabel label = (JLabel) rendererComponent;
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }
        if ((value != null) && (value instanceof BigDecimal)) {
            if (rendererComponent instanceof JLabel) {
                JLabel label = (JLabel) rendererComponent;
                label.setHorizontalAlignment(SwingConstants.RIGHT);
            }
        }
    }
}