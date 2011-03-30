package com.le.sunriise;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.TimeZone;

import com.healthmarketscience.jackcess.CodecProvider;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;

public class Utils {

    public static Database openDbReadOnly(File dbFile, String password) throws IOException {
        CodecProvider cryptCodecProvider = null;
        if (password == null) {
            cryptCodecProvider = new CryptCodecProvider();
        } else {
            cryptCodecProvider = new CryptCodecProvider(password);
        }
        boolean readOnly = true;
        boolean autoSync = true;
        Charset charset = null;
        TimeZone timeZone = null;

        return Database.open(dbFile, readOnly, autoSync, charset, timeZone, cryptCodecProvider);
    }

}
