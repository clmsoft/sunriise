package com.le.sunriise.password;

public class CharMask {

    private final String input;
    private char[] buffer;
    private boolean[] escape;

    public CharMask(String input) {
        super();
        this.input = input;
        parse(input);
    }

    private void parse(String str) {
        if (str == null) {
            return;
        }

        this.buffer = new char[str.length()];
        this.escape = new boolean[str.length()];
        int j = 0;
        boolean escapeState = false;
        for (int i = 0; i < str.length(); i++) {
            char c = str.charAt(i);
            if (c == '\\') {
                escapeState = true;
            } else {
                buffer[j] = c;
                escape[j] = escapeState;
                escapeState = false;
            }
        }
    }

    public char charAt(int index) {
        int count = buffer.length;
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return buffer[index];
    }

    public boolean isEscape(int index) {
        int count = escape.length;
        if ((index < 0) || (index >= count)) {
            throw new StringIndexOutOfBoundsException(index);
        }
        return escape[index];
    }

    public int getLength() {
        return buffer.length;
    }
}
