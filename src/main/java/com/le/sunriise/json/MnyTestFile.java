package com.le.sunriise.json;

import java.util.ArrayList;
import java.util.List;

class MnyTestFile {
    private String fileName;
    private String password;

    public static final List<MnyTestFile> sampleFiles = new ArrayList<MnyTestFile>(); 
    static {
        sampleFiles.add(new MnyTestFile("src/test/data/money2001-pwd.mny", "TEST12345"));
        sampleFiles.add(new MnyTestFile("src/test/data/money2002.mny", ""));
        sampleFiles.add(new MnyTestFile("src/test/data/money2004-pwd.mny", "123@abc!"));
        sampleFiles.add(new MnyTestFile("src/test/data/money2005-pwd.mny", "123@abc!"));
        sampleFiles.add(new MnyTestFile("src/test/data/money2008-pwd.mny", "Test12345"));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset01.mny", ""));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset02.mny", "12345678"));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset_401k.mny", ""));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset-sample-pwd-5.mny", "12@a!"));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset-sample-pwd-6.mny", "12@ab!"));
        sampleFiles.add(new MnyTestFile("src/test/data/sunset-sample-pwd.mny", "123@abc!"));
    }
    
    public MnyTestFile(String fileName, String password) {
        super();
        this.fileName = fileName;
        this.password = password;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

}