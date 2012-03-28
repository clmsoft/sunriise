package com.le.sunriise.tax;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class TaxInfoCmd {
    private static final Logger log = Logger.getLogger(TaxInfoCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;

        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
        } else {
            Class<TaxInfoCmd> clz = TaxInfoCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            TaxInfo cmd = new TaxInfo();
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

}
