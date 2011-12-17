package com.le.sunriise.report;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class FilteredAccountVisitorCmd {
    private static final Logger log = Logger.getLogger(FilteredAccountVisitorCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        File propsFile = null;
        String password = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            propsFile = new File(args[1]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            propsFile = new File(args[1]);
            password = args[2];
        } else {
            Class<FilteredAccountVisitorCmd> clz = FilteredAccountVisitorCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny in.props [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("propsFile=" + propsFile);
        try {
            FilteredAccountVisitor cmd = new FilteredAccountVisitor();
            cmd.setFilterFile(propsFile);
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }
    }

}
