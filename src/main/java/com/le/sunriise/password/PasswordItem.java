package com.le.sunriise.password;

public class PasswordItem {
    private final String password;

    private final boolean endOfWork;

    public PasswordItem(String password, boolean endOfWork) {
        super();
        this.password = password;
        this.endOfWork = endOfWork;
    }

    public PasswordItem(String line) {
        this(line, false);
    }

    public PasswordItem(boolean b) {
        this(null, b);
    }

    public String getPassword() {
        return password;
    }

    public boolean isEndOfWork() {
        return endOfWork;
    }
}
