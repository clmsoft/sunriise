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

import com.le.sunriise.password.BackupFileUtils;

abstract class OpenMnyAction implements ActionListener {
    private static final Logger log = Logger.getLogger(OpenMnyAction.class);

    private JFileChooser fc = null;
    private JTextField textField;
    private Component parent;

    private MnyFileFilter choosableFileFilter;

    protected abstract void setMnyFileName(String fileName);

    private final class MnyFileFilter extends FileFilter {
        @Override
        public boolean accept(File f) {
            if (f.isDirectory()) {
                return true;
            }
    
            String name = f.getName();
            if (BackupFileUtils.isMnyFile(name)) {
                return true;
            }
    
            return false;
        }
    
        @Override
        public String getDescription() {
            String description = "*.mny - Money file";
            return description;
        }
    }
    
    public OpenMnyAction(Component parent, JTextField textField, JFileChooser fc) {
        super();
        this.parent = parent;
        this.textField = textField;
        // XXX - create a new JFileChooser could be slow
        if (log.isDebugEnabled()) {
            log.debug("> new JFileChooser");
        }
//        this.fc = new JFileChooser(new File("."));
        // fc = new JFileChooser();
        this.fc = fc;
        if (log.isDebugEnabled()) {
            log.debug("< new JFileChooser");
        }

        this.choosableFileFilter = new MnyFileFilter();
//        fc.addChoosableFileFilter(choosableFileFilter);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        resetFileChooser(fc);
        
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

    private void resetFileChooser(JFileChooser fc) {
        if (fc == null) {
            return;
        }
        
        if (choosableFileFilter != null) {
            fc.resetChoosableFileFilters();
            fc.addChoosableFileFilter(choosableFileFilter);
        }
        fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
    }
}