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

import java.awt.Color;
import java.awt.Component;

import javax.swing.JTable;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellRenderer;

final class MyTabletHeaderRenderer extends DefaultTableCellRenderer {
    private TableCellRenderer parentCellRenderer;
    private JTable table;
    private Color color;

    public MyTabletHeaderRenderer(JTable table, TableCellRenderer headerRenderer, Color color) {
        this.table = table;
        this.parentCellRenderer = headerRenderer;
        this.color = color;
    }

    
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
        Component rendererComponent = null;

        if (rendererComponent == null) {
            JTableHeader tableHeader = table.getTableHeader();
            if (tableHeader != null) {
                TableCellRenderer defaultRenderer = tableHeader.getDefaultRenderer();
                if (defaultRenderer != null) {
                    rendererComponent = defaultRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                }
            }
        }
        if (rendererComponent == null) {
            if (parentCellRenderer != null) {
                rendererComponent = parentCellRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            } else {
                rendererComponent = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            }
        }
        rendererComponent.setForeground(color);

        return rendererComponent;
    }
}