package com.le.sunriise.password;

import java.io.BufferedReader;

public class ReaderItem {
    private final BufferedReader reader;
    private final boolean endOfWork;

    public ReaderItem(BufferedReader reader, boolean endOfWork) {
        super();
        this.reader = reader;
        this.endOfWork = endOfWork;
    }

    public ReaderItem(boolean b) {
        this(null, b);
    }

    public ReaderItem(BufferedReader reader) {
        this(reader, false);
    }

    public BufferedReader getReader() {
        return reader;
    }

    public boolean isEndOfWork() {
        return endOfWork;
    }
}
