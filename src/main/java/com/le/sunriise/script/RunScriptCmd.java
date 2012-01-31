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
