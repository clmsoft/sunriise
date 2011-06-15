package com.le.sunriise.viewer;

import java.awt.event.MouseEvent;
import java.util.EventObject;

import javax.swing.DefaultCellEditor;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;

import org.apache.log4j.Logger;

public class DatePickerTableEditor extends DefaultCellEditor {
    private static final Logger log = Logger.getLogger(DatePickerTableEditor.class);

    // we need this as DefaultCellEditor has no default constructor.
    public DatePickerTableEditor() {
        super(new JTextField()); // not really relevant - sets a text field as
                                 // the editing default.
        setClickCountToStart(2);
    }

    // If the cell is editable and it's a mouse event, set up the date picker.
    // If you don't want any keyboard editing, return false if not a MouseEvent.
    public boolean isCellEditable(EventObject event) {
        boolean isEditable = super.isCellEditable(event);
        if (isEditable && event instanceof MouseEvent) {
            setupDatePicker();
        }
        return isEditable;
    }

    // Set the edit placeholder for the cell, and make the delegate our
    // DatePickerComponent
    // (the component that displays the DatePicker).
    private void setupDatePicker() {
        editorComponent = new JLabel("*** Editing ***");
        delegate = new DatePickerComponent(this);
    }

    // This component contains the actual date picker (represented here by a
    // dialog with a
    // text field and an OK button).
    private class DatePickerComponent extends EditorDelegate {

        private DatePickerTableEditor cellEditor; // reference to our cell
                                                  // editor so we can
        // tell it when we're finished.
        private DateEditorDialog datePicker;

        // The component constructor - stores the cell editor and creates the
        // date picker.
        public DatePickerComponent(DatePickerTableEditor cellEditor) {
            this.cellEditor = cellEditor;
            createDatePicker();
        }

        // Do whatever you need to create the date picker here.
        private void createDatePicker() {
            datePicker = new DateEditorDialog(editorComponent);
            datePicker.setCellEditor(cellEditor);
        }

        // Set the date to be edited into the date picker and display / edit it.
        public void setValue(Object value) {
            datePicker.setValue(value);
            datePicker.startEditing();
        }

        // Get the edited date out of the date picker and return it.
        public Object getCellEditorValue() {
            return datePicker.getValue();
        }
    }

    @Override
    public boolean stopCellEditing() {
        log.info("> stopEditing");
        boolean rv = super.stopCellEditing();
        if (editorComponent != null) {
            if (editorComponent instanceof JLabel) {
                ((JLabel) editorComponent).setText("stopEditing");
            }
        }
        log.info("  rv=" + rv);
        
        return rv;
    }

    @Override
    public void cancelCellEditing() {
        log.info("> cancelEditing");
        super.cancelCellEditing();
        if (editorComponent != null) {
            if (editorComponent instanceof JLabel) {
                ((JLabel) editorComponent).setText("cancelEditing");
            }
        }
    }

    @Override
    public void addCellEditorListener(CellEditorListener l) {
        log.info("> addCellEditorListener");
        super.addCellEditorListener(l);
    }

    @Override
    public void removeCellEditorListener(CellEditorListener l) {
        log.info("> removeCellEditorListener");
        super.removeCellEditorListener(l);
    }
    
    
}