package com.le.sunriise.password.crypt;

public class ARCFOUR {
    // Constructor, byte-array key.
    public ARCFOUR(byte[] key) {
        setKey(key);
    }

    // Key routines.

    private byte[] state = new byte[256];
    private int x, y;

    // / Set the key.
    public void setKey(byte[] key) {
        int index1;
        int index2;
        int counter;
        byte temp;

        for (counter = 0; counter < 256; ++counter)
            state[counter] = (byte) counter;
        x = 0;
        y = 0;
        index1 = 0;
        index2 = 0;
        for (counter = 0; counter < 256; ++counter) {
            index2 = (key[index1] + state[counter] + index2) & 0xff;
            temp = state[counter];
            state[counter] = state[index2];
            state[index2] = temp;
            index1 = (index1 + 1) % key.length;
        }
    }

    // / Encrypt a byte.
    public byte encrypt(byte clearText) {
        return (byte) (clearText ^ state[nextState()]);
    }

    // / Decrypt a byte.
    public byte decrypt(byte cipherText) {
        return (byte) (cipherText ^ state[nextState()]);
    }

    // / Encrypt some bytes.
    public void encrypt(byte[] clearText, int clearOff, byte[] cipherText, int cipherOff, int len) {
        for (int i = 0; i < len; ++i)
            cipherText[cipherOff + i] = (byte) (clearText[clearOff + i] ^ state[nextState()]);
    }

    // / Decrypt some bytes.
    public void decrypt(byte[] cipherText, int cipherOff, byte[] clearText, int clearOff, int len) {
        for (int i = 0; i < len; ++i)
            clearText[clearOff + i] = (byte) (cipherText[cipherOff + i] ^ state[nextState()]);
    }

    private int nextState() {
        byte temp;

        x = (x + 1) & 0xff;
        y = (y + state[x]) & 0xff;
        temp = state[x];
        state[x] = state[y];
        state[y] = temp;
        return (state[x] + state[y]) & 0xff;
    }
}
