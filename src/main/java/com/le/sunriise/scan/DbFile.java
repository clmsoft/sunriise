package com.le.sunriise.scan;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.JetFormat;
import com.le.sunriise.header.HeaderPage;
import com.le.sunriise.password.HeaderPagePasswordChecker;

public class DbFile {
    private static final Logger log = Logger.getLogger(DbFile.class);

    private String password;

    private String dbFileName;

    private File file;

    private HeaderPage headerPage;

    private JetFormat format;

    private int pageSize;

    private long fileLength;

    private long pages;

    private long leftOverBytes;

    private boolean passwordIsValid;

    public DbFile(String dbFileName, String password) throws IOException {
        super();
        this.dbFileName = dbFileName;
        this.password = password;
        parse();
    }

    public String getDbFileName() {
        return dbFileName;
    }

    public void setDbFileName(String dbFileName) {
        this.dbFileName = dbFileName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    private void parse() throws IOException {
        this.file = new File(dbFileName);

        this.headerPage = new HeaderPage(file);
        this.format = headerPage.getJetFormat();
        this.pageSize = format.PAGE_SIZE;
        this.fileLength = file.length();

        this.pages = fileLength / pageSize;
        this.leftOverBytes = fileLength % pageSize;

        HeaderPagePasswordChecker checker = new HeaderPagePasswordChecker(headerPage);
        this.passwordIsValid = checker.check(password);
    }

    public File getFile() {
        return file;
    }

    public void setFile(File dbFile) {
        this.file = dbFile;
    }

    public HeaderPage getHeaderPage() {
        return headerPage;
    }

    public void setHeaderPage(HeaderPage headerPage) {
        this.headerPage = headerPage;
    }

    public JetFormat getFormat() {
        return format;
    }

    public void setFormat(JetFormat format) {
        this.format = format;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getPages() {
        return pages;
    }

    public void setPages(long pages) {
        this.pages = pages;
    }

    public long getLeftOverBytes() {
        return leftOverBytes;
    }

    public void setLeftOverBytes(long leftOverBytes) {
        this.leftOverBytes = leftOverBytes;
    }

    public boolean isPasswordIsValid() {
        return passwordIsValid;
    }

    public void setPasswordIsValid(boolean passwordIsValid) {
        this.passwordIsValid = passwordIsValid;
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
            Class<DbFile> clz = DbFile.class;
            System.out.println("Usage: java " + clz.getName() + " *.mny [password]");
            System.exit(1);
        }

        DbFile dbFile = null;
        try {
            dbFile = new DbFile(dbFileName, password);
        } catch (IOException e) {
            log.error(e, e);
        }

    }

}
