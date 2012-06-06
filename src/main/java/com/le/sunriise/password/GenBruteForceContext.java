package com.le.sunriise.password;

import java.util.Arrays;

public class GenBruteForceContext {
    private static final char[] DEFAULT_ALPHABETS = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
    private static final char[] DEFAULT_MASK = "********".toCharArray();

    private char[] buffer;
    private int bufferLen;

    private char[] mask;

    private int cursor;

    private char[] alphabets;
    private int alphabetsLen;

    private int[] currentCursorIndex;

    GenBruteForceContext(char[] buffer, int bufferLen, char[] mask, int cursor, char[] alphabets, int alphabetsLen) {
        this.buffer = buffer;
        this.bufferLen = bufferLen;

        this.mask = mask;

        this.cursor = cursor;

        this.alphabets = alphabets;
        this.alphabetsLen = alphabetsLen;
    }

    GenBruteForceContext(GenBruteForceContext fromContext) {
        this.buffer = fromContext.buffer;
        this.bufferLen = fromContext.bufferLen;

        this.mask = fromContext.mask;

        this.cursor = fromContext.cursor;

        this.alphabets = fromContext.alphabets;
        this.alphabetsLen = fromContext.alphabetsLen;

        this.currentCursorIndex = fromContext.getCurrentCursorIndex();
    }

    GenBruteForceContext(char[] mask, char[] alphabets) {
        this(-1, mask, alphabets);
    }

    GenBruteForceContext(int passwordLength, char[] mask, char[] alphabets) {
        if (passwordLength < 0) {
            if (mask != null) {
                passwordLength = mask.length;
            }
        }
        this.mask = mask;

        this.alphabets = alphabets;
        this.alphabetsLen = this.alphabets.length;

        buffer = new char[passwordLength + 1];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = '\0';
        }
        this.bufferLen = this.buffer.length;

        currentCursorIndex = new int[passwordLength];
        for (int i = 0; i < currentCursorIndex.length; i++) {
            currentCursorIndex[i] = 0;
        }
    }

    public GenBruteForceContext() {
        this(DEFAULT_MASK, DEFAULT_ALPHABETS);
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

    public boolean equals(GenBruteForceContext o) {
        if (!Arrays.equals(getBuffer(), o.getBuffer())) {
            return false;
        }

        if (!Arrays.equals(getMask(), o.getMask())) {
            return false;
        }

        if (!Arrays.equals(getAlphabets(), o.getAlphabets())) {
            return false;
        }

        if (!Arrays.equals(getCurrentCursorIndex(), o.getCurrentCursorIndex())) {
            return false;
        }

        if (getCursor() != o.getCursor()) {
            return false;
        }

        return true;
    }

    public char getAlphabet(int index) {
        return alphabets[index];
    }

}