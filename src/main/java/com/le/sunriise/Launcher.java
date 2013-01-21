/*******************************************************************************
 * Copyright (c) 2013 Hung Le
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
package com.le.sunriise;

import java.awt.EventQueue;

import javax.swing.JFrame;

import org.apache.log4j.Logger;
import javax.swing.JButton;

import app.AccountViewer;
import app.MnyViewer;
import app.PasswordCheckerApp;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.ColumnSpec;
import com.jgoodies.forms.layout.RowSpec;
import javax.swing.JLabel;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.factories.FormFactory;
import javax.swing.SwingConstants;
import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import javax.swing.Action;

public class Launcher {
    private static final Logger log = Logger.getLogger(Launcher.class);

    private JFrame frame;
    private final Action action = new SwingAction();
    private final Action action_1 = new SwingAction_1();
    private final Action action_2 = new SwingAction_2();

    /**
     * Launch the application.
     */
    public static void main(String[] args) {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try {
                    Launcher window = new Launcher();
                    showMainFrame(window);
                } catch (Exception e) {
                    log.error(e, e);
                }
            }

            protected void showMainFrame(Launcher window) {
                window.frame.pack();
                window.frame.setLocationRelativeTo(null);
                window.frame.setVisible(true);
            }
        });
    }

    /**
     * Create the application.
     */
    public Launcher() {
        initialize();
    }

    /**
     * Initialize the contents of the frame.
     */
    private void initialize() {
        frame = new JFrame();
        String buildNumber = BuildNumber.findBuilderNumber();
        if (buildNumber != null) {
            frame.setTitle("Sunriise Launcher - " + buildNumber);
        } else {
            frame.setTitle("Sunriise Launcher");
        }
        // frame.setBounds(100, 100, 450, 300);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.getContentPane().setLayout(
                new FormLayout(new ColumnSpec[] { ColumnSpec.decode("247px"), FormFactory.RELATED_GAP_COLSPEC,
                        FormFactory.DEFAULT_COLSPEC, }, new RowSpec[] { FormFactory.DEFAULT_ROWSPEC,
                        FormFactory.RELATED_GAP_ROWSPEC, FormFactory.DEFAULT_ROWSPEC, FormFactory.RELATED_GAP_ROWSPEC,
                        FormFactory.DEFAULT_ROWSPEC, }));

        JLabel lblNewJgoodiesLabel_3 = DefaultComponentFactory.getInstance().createLabel(
                "MS Money file viewer (view tables, rows ...)");
        lblNewJgoodiesLabel_3.setHorizontalAlignment(SwingConstants.RIGHT);
        frame.getContentPane().add(lblNewJgoodiesLabel_3, "1, 1");

        JButton btnNewButton_3 = new JButton("Launch");
        btnNewButton_3.setAction(action);
        frame.getContentPane().add(btnNewButton_3, "3, 1");

        JLabel lblNewJgoodiesLabel_4 = DefaultComponentFactory.getInstance().createLabel(
                "Accounts viewer (accounts, transactions ...)");
        lblNewJgoodiesLabel_4.setHorizontalAlignment(SwingConstants.RIGHT);
        frame.getContentPane().add(lblNewJgoodiesLabel_4, "1, 3");

        JButton btnNewButton_4 = new JButton("Launch");
        btnNewButton_4.setAction(action_1);
        frame.getContentPane().add(btnNewButton_4, "3, 3");

        JLabel lblNewJgoodiesLabel_5 = DefaultComponentFactory.getInstance().createLabel("Password tools");
        lblNewJgoodiesLabel_5.setHorizontalAlignment(SwingConstants.RIGHT);
        frame.getContentPane().add(lblNewJgoodiesLabel_5, "1, 5");

        JButton btnNewButton_5 = new JButton("Launch");
        btnNewButton_5.setAction(action_2);
        frame.getContentPane().add(btnNewButton_5, "3, 5");
    }

    private class SwingAction extends AbstractAction {
        public SwingAction() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch *.mny viewer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            MnyViewer.main(args);
            frame.setVisible(false);
        }
    }

    private class SwingAction_1 extends AbstractAction {
        public SwingAction_1() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch accounts viewer");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            AccountViewer.main(args);
            frame.setVisible(false);
        }
    }

    private class SwingAction_2 extends AbstractAction {
        public SwingAction_2() {
            putValue(NAME, "Launch");
            putValue(SHORT_DESCRIPTION, "Launch password tools");
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            String[] args = new String[0];
            PasswordCheckerApp.main(args);
            frame.setVisible(false);
        }
    }
}
