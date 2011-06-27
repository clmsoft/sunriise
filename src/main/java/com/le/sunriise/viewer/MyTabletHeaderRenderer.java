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