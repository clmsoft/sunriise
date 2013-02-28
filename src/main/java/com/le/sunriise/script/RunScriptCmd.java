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
package com.le.sunriise.script;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class RunScriptCmd {
    private static final Logger log = Logger.getLogger(RunScriptCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File scriptFile = null;
        String password = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            scriptFile = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            scriptFile = new File(args[1]);
            password = args[2];
        } else {
            Class<RunScriptCmd> clz = RunScriptCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password] script.txt");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("scriptFile=" + scriptFile);
        try {
            RunScript.runScript(dbFile, scriptFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

}
