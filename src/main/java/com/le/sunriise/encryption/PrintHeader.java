/*******************************************************************************
 * Copyright (c) 2010 Hung Le
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
package com.le.sunriise.encryption;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class PrintHeader {
    private static final Logger log = Logger.getLogger(PrintHeader.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File mdbFile = null;
        String password = null;

        if (args.length == 1) {
            mdbFile = new File(args[0]);
            password = null;
        } else if (args.length == 2) {
            mdbFile = new File(args[0]);
            password = args[1];
        } else {
            Class clz = PrintHeader.class;
            System.out.println("Usage: java " + clz.getName() + " mnyFile [password]");
            System.exit(1);
        }

        try {
            EncryptionUtils.parseHeader(mdbFile, password);
        } catch (IOException e) {
            log.error(e);
        }
    }

}
