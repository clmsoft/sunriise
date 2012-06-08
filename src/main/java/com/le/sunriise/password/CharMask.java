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
