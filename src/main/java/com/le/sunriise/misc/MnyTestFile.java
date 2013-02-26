package com.le.sunriise.misc;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MnyTestFile {
    private String fileName;

    private String password;

    private String comment;

    public static final List<MnyTestFile> SAMPLE_FILES = new ArrayList<MnyTestFile>();
    static {
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/money2001-pwd.mny", "TEST12345"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/money2002.mny"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/money2004-pwd.mny", "123@abc!"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/money2005-pwd.mny", "123@abc!"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/money2008-pwd.mny", "Test12345"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset_401k.mny"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset-sample-pwd-5.mny", "12@a!"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset-sample-pwd-6.mny", "12@ab!"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset-sample-pwd.mny", "123@abc!"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset-sample.mbf"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset01.mny"));
        SAMPLE_FILES.add(new MnyTestFile("src/test/data/mny/sunset02.mny", "12345678"));
    }

    public MnyTestFile(String fileName, String password, String comment) {
        super();
        this.fileName = fileName;
        this.password = password;
        this.comment = comment;
    }

    public MnyTestFile(String fileName, String password) {
        this(fileName, password, null);
    }

    public MnyTestFile(String fileName) {
        this(fileName, null);
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

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
    
    public boolean isBackup() {
        return fileName.endsWith(".mbf");
    }

    public static MnyTestFile getSampleFile(String fileName) {
        for (MnyTestFile sampleFile : MnyTestFile.SAMPLE_FILES) {
            String fn = sampleFile.getFileName();
            if (fn == null) {
                continue;
            }
            if (fn.equals(fileName)) {
                return sampleFile;
            }
            File f = new File(fn);
            fn = f.getName();
            if (fn.equals(fileName)) {
                return sampleFile;
            }

        }
        return null;
    }
}