/*******************************************************************************
 * Copyright (c) 2012 Hung Le
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
package com.le.sunriise.password.ui;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.filechooser.FileFilter;

import org.apache.log4j.Logger;

abstract class OpenMnyAction implements ActionListener {
    private static final Logger log = Logger.getLogger(OpenMnyAction.class);

    private JFileChooser fc = null;
    private JTextField textField;
    private Component parent;

    protected abstract void setMnyFileName(String fileName);

    public OpenMnyAction(Component parent, JTextField textField) {
        super();
        this.parent = parent;
        this.textField = textField;
        fc = new JFileChooser(new File("."));
        FileFilter filter = new FileFilter() {

            @Override
            public boolean accept(File f) {
                if (f.isDirectory()) {
                    return true;
                }

                String name = f.getName();
                if (name.endsWith(".mny")) {
                    return true;
                }

                return false;
            }

            @Override
            public String getDescription() {
                String description = "*.mny - Money file";
                return description;
            }

        };
        fc.addChoosableFileFilter(filter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (fc.showOpenDialog(parent) != JFileChooser.APPROVE_OPTION) {
            return;
        }

        File path = fc.getSelectedFile();
        log.info("path=" + path);
        String str = path.getAbsolutePath();
        setMnyFileName(str);
        if (textField != null) {
            textField.setCaretPosition(str.length());
        }
    }
}