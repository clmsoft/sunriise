/*******************************************************************************
 * Copyright (c) 2012 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
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
