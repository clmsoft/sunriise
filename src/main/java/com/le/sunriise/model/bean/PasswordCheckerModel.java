package com.le.sunriise.model.bean;

public class PasswordCheckerModel {
    private String mnyFileName;
    private String wordListPath;
    private Integer threads = new Integer(1);
    
    public String getMnyFileName() {
        return mnyFileName;
    }

    public void setMnyFileName(String mnyFileName) {
        this.mnyFileName = mnyFileName;
    }

    public String getWordListPath() {
        return wordListPath;
    }

    public void setWordListPath(String wordListPath) {
        this.wordListPath = wordListPath;
    }

    public Integer getThreads() {
        return threads;
    }

    public void setThreads(Integer threads) {
        this.threads = threads;
    }
}
