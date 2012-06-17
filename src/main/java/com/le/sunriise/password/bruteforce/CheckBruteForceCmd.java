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
package com.le.sunriise.password.bruteforce;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;


public class CheckBruteForceCmd {
    private static final Logger log = Logger.getLogger(CheckBruteForceUtils.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        int passwordLength = 5;
        String password = null;
    
        if (args.length == 2) {
            dbFile = new File(args[0]);
            passwordLength = Integer.valueOf(args[1]);
        } else {
            Class<CheckBruteForceUtils> clz = CheckBruteForceUtils.class;
            System.out.println("Usage: java " + clz.getName() + " sample.mny passwordLength");
            System.exit(1);
        }
        char[] mask = null;
        mask = new String("*****!").toCharArray();
    
        char[] alphabets = CheckBruteForceUtils.createAlphabets();
    
        log.info("dbFile=" + dbFile);
        log.info("passwordLength=" + passwordLength);
        log.info("mask=" + ((mask == null) ? null : new String(mask)));
        log.info("alphabets=" + new String(alphabets));
    
        try {
            password = CheckBruteForceUtils.checkUsingMask(dbFile, passwordLength, mask, alphabets);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("password=" + password);
            log.info("< DONE");
        }
    }

}
