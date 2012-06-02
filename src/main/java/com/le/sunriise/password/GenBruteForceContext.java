package com.le.sunriise.password;

public class GenBruteForceContext {
    private char[] buffer;
    private int bufferLen;
    private char[] mask;
    private int cursor;
    private char[] alphabets;
    private int alphabetsLen;

    private int[] currentCursorIndex;

    public GenBruteForceContext(char[] buffer, int bufferLen, char[] mask, int cursor, char[] alphabets, int alphabetsLen) {
        this.buffer = buffer;
        this.bufferLen = bufferLen;
        this.mask = mask;
        this.cursor = cursor;
        this.alphabets = alphabets;
        this.alphabetsLen = alphabetsLen;
    }

    public GenBruteForceContext(int passwordLength, char[] mask, char[] alphabets) {
        if (passwordLength < 0) {
            if (mask != null) {
                passwordLength = mask.length;
            }
        }        
        this.mask = mask;
        this.alphabets = alphabets;

        buffer = new char[passwordLength + 1];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = '\0';
        }

        currentCursorIndex = new int[passwordLength];
        for (int i = 0; i < currentCursorIndex.length; i++) {
            currentCursorIndex[i] = -1;
        }
    }

    public char[] getBuffer() {
        return buffer;
    }

    public void setBuffer(char[] buffers) {
        this.buffer = buffers;
    }

    public int getBufferLen() {
        return bufferLen;
    }

    public void setBufferLen(int bufferLen) {
        this.bufferLen = bufferLen;
    }

    public char[] getMask() {
        return mask;
    }

    public void setMask(char[] masks) {
        this.mask = masks;
    }

    public int getCursor() {
        return cursor;
    }

    public void setCursor(int cursor) {
        this.cursor = cursor;
    }

    public char[] getAlphabets() {
        return alphabets;
    }

    public void setAlphabets(char[] alphabets) {
        this.alphabets = alphabets;
    }

    public int getAlphabetsLen() {
        return alphabetsLen;
    }

    public void setAlphabetsLen(int alphabetsLen) {
        this.alphabetsLen = alphabetsLen;
    }

    public int[] getCurrentCursorIndex() {
        return currentCursorIndex;
    }

    public void setCurrentCursorIndex(int[] currentCursorIndex) {
        this.currentCursorIndex = currentCursorIndex;
    }
}