package com.le.sunriise.viewer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Calendar;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.SpinnerDateModel;
import javax.swing.WindowConstants;
import javax.swing.border.EmptyBorder;
import javax.swing.table.TableCellEditor;

import org.apache.log4j.Logger;

import com.jgoodies.forms.factories.FormFactory;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

public class DateEditorDialog extends JDialog {
    private static final Logger log = Logger.getLogger(DateEditorDialog.class);

    private final JPanel contentPanel = new JPanel();

    private Component relativeTo;
    private JSpinner spinner;
    private TableCellEditor cellEditor;

    private boolean canceled = true;

    private Date originalValue;

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        try {
            DateEditorDialog dialog = new DateEditorDialog();
            dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
            dialog.setVisible(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Create the dialog.
     */
    public DateEditorDialog() {
        setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        addWindowListener(new WindowAdapter() {

            
            @Override
            public void windowClosing(WindowEvent e) {
                cancelEditing();
                super.windowClosing(e);
            }

        });
        setModalityType(ModalityType.APPLICATION_MODAL);
        // setBounds(100, 100, 450, 300);
        getContentPane().setLayout(new BorderLayout());
        contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
        getContentPane().add(contentPanel, BorderLayout.CENTER);
        contentPanel.setLayout(new FormLayout(new ColumnSpec[] { FormFactory.RELATED_GAP_COLSPEC, FormFactory.DEFAULT_COLSPEC, FormFactory.RELATED_GAP_COLSPEC,
                ColumnSpec.decode("default:grow"), FormFactory.RELATED_GAP_COLSPEC, }, new RowSpec[] { FormFactory.RELATED_GAP_ROWSPEC,
                FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, }));
        {
            JLabel lblNewLabel = new JLabel("Date/Time");
            contentPanel.add(lblNewLabel, "2, 2");
        }
        {
            SpinnerDateModel model = new SpinnerDateModel();
            spinner = new JSpinner(model);
            spinner.setEditor(new JSpinner.DateEditor(spinner, "MMM dd, yyyy HH:mm"));
            contentPanel.add(spinner, "4, 2");
        }
        {
            JButton btnNewButton = new JButton("Use current date");
            btnNewButton.addActionListener(new ActionListener() {
                
                public void actionPerformed(ActionEvent e) {
                    // date only, no time
                    Calendar cal = Calendar.getInstance();
                    cal.set(Calendar.HOUR_OF_DAY, 0);
                    cal.set(Calendar.MINUTE, 0);
                    cal.set(Calendar.SECOND, 0);
                    cal.set(Calendar.MILLISECOND, 0);
                    Date value = cal.getTime();
                    getSpinner().setValue(value);
                }
            });
            contentPanel.add(btnNewButton, "4, 4");
        }
        {
            JPanel buttonPane = new JPanel();
            buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
            getContentPane().add(buttonPane, BorderLayout.SOUTH);
            {
                JButton okButton = new JButton("OK");
                okButton.addActionListener(new ActionListener() {
                    
                    public void actionPerformed(ActionEvent e) {
                        DateEditorDialog.this.dispose();
                        stopEditing();
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
                        DateEditorDialog.this.dispose();
                        cancelEditing();
                    }
                });
                cancelButton.setActionCommand("Cancel");
                buttonPane.add(cancelButton);
            }
        }
    }

    public DateEditorDialog(Component relativeTo) {
        this();
        this.relativeTo = relativeTo;
    }

    public Component getRelativeTo() {
        return relativeTo;
    }

    public void setRelativeTo(Component relativeTo) {
        this.relativeTo = relativeTo;
    }

    public void setValue(Object value) {
        if (log.isDebugEnabled()) {
            log.debug("> setValue value=" + value.getClass().getName() + ", " + value);
        }
        Date date = null;

        if (value == null) {
            date = new Date();
        } else {
            if (value instanceof Date) {
                date = (Date) value;
            } else {
                date = new Date();
            }
        }
        this.originalValue = date;
        getSpinner().setValue(date);

        // textField.setText((value != null) ? value.toString() : "");
    }

    public JSpinner getSpinner() {
        return spinner;
    }

    public Object getValue() {
        Object value = getSpinner().getValue();
        if (canceled) {
            value = originalValue;
        }
        if (log.isDebugEnabled()) {
            log.debug("> getValue value=" + value.getClass().getName() + ", " + value);
        }

        return value;
    }

    public void startEditing() {
        if (log.isDebugEnabled()) {
            log.debug("> startEditing");
        }

        this.pack();
        if (relativeTo != null) {
            this.setLocationRelativeTo(relativeTo);
        }
        this.setVisible(true);
    }

    public void stopEditing() {
        if (log.isDebugEnabled()) {
            log.debug("> stopEditing");
        }

        canceled = false;
        if (cellEditor != null) {
            cellEditor.stopCellEditing();
        }
    }

    public void cancelEditing() {
        log.info("> cancelEditing");

        canceled = true;
        if (cellEditor != null) {
            cellEditor.cancelCellEditing();
        }

    }

    public TableCellEditor getCellEditor() {
        return cellEditor;
    }

    public void setCellEditor(TableCellEditor cellEditor) {
        this.cellEditor = cellEditor;
    }
}
