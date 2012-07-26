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
package com.le.sunriise.password.bruteforce;

import java.math.BigInteger;

import org.apache.log4j.Logger;

public class GenBruteForceCmd {
    private static final Logger log = Logger.getLogger(GenBruteForceCmd.class);

    public static void main(String[] args) {
        int passwordLength = 5;
        char[] mask = null;
        mask = new String("*****").toCharArray();
        char[] alphabets = GenBruteForce.genChars('a', 'c');
        GenBruteForce gen = new GenBruteForce(passwordLength, mask, alphabets);
    
        long actual = gen.generate();
    
        BigInteger expected = GenBruteForce.calculateExpected(passwordLength, alphabets.length);
    
        log.info("actual=" + actual + ", expected=" + expected);
    
    }

}
