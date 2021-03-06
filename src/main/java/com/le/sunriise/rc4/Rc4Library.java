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
package com.le.sunriise.rc4;

import java.nio.ByteBuffer;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import com.sun.jna.NativeLong;
import com.sun.jna.Pointer;

/**
 * JNA Wrapper for library <b>rc4</b><br>
 * This file was autogenerated by <a
 * href="http://jnaerator.googlecode.com/">JNAerator</a>,<br>
 * a tool written by <a href="http://ochafik.com/">Olivier Chafik</a> that <a
 * href="http://code.google.com/p/jnaerator/wiki/CreditsAndLicense">uses a few
 * opensource projects.</a>.<br>
 * For help, please visit <a
 * href="http://nativelibs4java.googlecode.com/">NativeLibs4Java</a> , <a
 * href="http://rococoa.dev.java.net/">Rococoa</a>, or <a
 * href="http://jna.dev.java.net/">JNA</a>.
 */
public interface Rc4Library extends Library {
    // public static final String JNA_LIBRARY_NAME = "rc4";
    // /usr/lib/libssl.so
    public static final String JNA_LIBRARY_NAME = "ssl";
    public static final NativeLibrary JNA_NATIVE_LIB = NativeLibrary.getInstance(Rc4Library.JNA_LIBRARY_NAME);
    public static final Rc4Library INSTANCE = (Rc4Library) Native.loadLibrary(Rc4Library.JNA_LIBRARY_NAME, Rc4Library.class);

    // /usr/lib/libcrypto.so
    // public static final NativeLibrary JNA_NATIVE_LIB2 =
    // NativeLibrary.getInstance("crypto");
    /**
     * Original signature : <code>char* RC4_options()</code><br>
     * <i>native declaration : /usr/include/openssl/rc4.h:77</i>
     */
    String RC4_options();

    /**
     * Original signature :
     * <code>void RC4_set_key(RC4_KEY*, int, const unsigned char*)</code><br>
     * <i>native declaration : /usr/include/openssl/rc4.h:81</i><br>
     * 
     * @deprecated use the safer methods
     *             {@link #RC4_set_key(com.le.sunriise.rc4.RC4_KEY, int, byte[])}
     *             and
     *             {@link #RC4_set_key(com.le.sunriise.rc4.RC4_KEY, int, com.sun.jna.Pointer)}
     *             instead
     */
    @Deprecated
    void RC4_set_key(RC4_KEY key, int len, Pointer data);

    /**
     * Original signature :
     * <code>void RC4_set_key(RC4_KEY*, int, const unsigned char*)</code><br>
     * <i>native declaration : /usr/include/openssl/rc4.h:81</i>
     */
    void RC4_set_key(RC4_KEY key, int len, byte data[]);

    /**
     * Original signature :
     * <code>void RC4(RC4_KEY*, unsigned long, const unsigned char*, unsigned char*)</code>
     * <br>
     * <i>native declaration : /usr/include/openssl/rc4.h:82</i><br>
     * 
     * @deprecated use the safer methods
     *             {@link #RC4(com.le.sunriise.rc4.RC4_KEY, com.sun.jna.NativeLong, byte[], java.nio.ByteBuffer)}
     *             and
     *             {@link #RC4(com.le.sunriise.rc4.RC4_KEY, com.sun.jna.NativeLong, com.sun.jna.Pointer, com.sun.jna.Pointer)}
     *             instead
     */
    @Deprecated
    void RC4(RC4_KEY key, NativeLong len, Pointer indata, Pointer outdata);

    /**
     * Original signature :
     * <code>void RC4(RC4_KEY*, unsigned long, const unsigned char*, unsigned char*)</code>
     * <br>
     * <i>native declaration : /usr/include/openssl/rc4.h:82</i>
     */
    void RC4(RC4_KEY key, NativeLong len, byte indata[], ByteBuffer outdata);
}
