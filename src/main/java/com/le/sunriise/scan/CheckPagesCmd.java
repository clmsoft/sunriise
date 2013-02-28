package com.le.sunriise.scan;

import java.io.IOException;

import org.apache.log4j.Logger;

import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.HeaderPagePasswordChecker;

public class CheckPagesCmd {
    private static final Logger log = Logger.getLogger(CheckPagesCmd.class);

    private DbFile dbFile;

    public CheckPagesCmd(String dbFileName, String password) throws IOException {
        this.dbFile = new DbFile(dbFileName, password);

    }

    private void check() throws IOException {
        log.info("dbFile=" + dbFile.getFile().getAbsolutePath());

        long fileLength = dbFile.getFile().length();
        log.info("fileLength=" + fileLength);

        log.info("getPageSize()=" + getPageSize());
        long pages = fileLength / getPageSize();
        long leftOverBytes = fileLength % getPageSize();
        log.info("pages=" + pages + "/" + leftOverBytes);

        HeaderPagePasswordChecker checker = new HeaderPagePasswordChecker(getHeaderPage());
        boolean passwordIsValid = checker.check(getPassword());
        if (!passwordIsValid) {
            log.warn("Invalid password.");
        } else {
            log.info("Valid password.");
        }

        PageScanner scanner = new PageScanner(dbFile, checker);
        scanner.scan();

    }

    private String getPassword() {
        return dbFile.getPassword();
    }

    private int getPageSize() {
        return dbFile.getHeaderPage().getJetFormat().PAGE_SIZE;
    }

    private HeaderPage getHeaderPage() {
        return dbFile.getHeaderPage();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String dbFileName = null;
        String password = null;

        if (args.length == 1) {
            dbFileName = args[0];
            password = null;
        } else if (args.length == 2) {
            dbFileName = args[0];
            password = args[1];
        } else {
            Class<CheckPagesCmd> clz = CheckPagesCmd.class;
            System.out.println("Usage: java " + clz.getName() + " *.mny [password]");
            System.exit(1);
        }

        CheckPagesCmd cmd = null;
        try {
            cmd = new CheckPagesCmd(dbFileName, password);
            cmd.check();
        } catch (IOException e) {
            log.error(e, e);
        }

    }
}
