package com.le.sunriise.accountviewer;

import java.io.IOException;
import java.io.Writer;

import javax.swing.JTextArea;

public class JTextAreaWriter extends Writer {

    private JTextArea textArea;
    private long maxCount = -1L;
    private long count = 0L;

    public JTextAreaWriter(JTextArea textArea, long maxCount) {
        super();
        this.textArea = textArea;
        this.maxCount = maxCount;
    }

    public JTextAreaWriter(JTextArea textArea) {
        this(textArea, -1L);
    }

    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        if (textArea == null) {
            return;
        }

        String str = new String(cbuf, off, len);
        count += str.length();

        if ((maxCount > 0) && (count < maxCount)) {
            textArea.append(str);
        } else {
            textArea.append(str);
        }
    }

    @Override
    public void flush() throws IOException {
    }

    @Override
    public void close() throws IOException {
    }

}
