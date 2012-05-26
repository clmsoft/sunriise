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
package com.le.sunriise.misc;

import java.math.BigDecimal;
import java.util.Arrays;

import org.apache.log4j.Logger;

public class SortTiming {
    private static final Logger log = Logger.getLogger(SortTiming.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        int max = 100000;
        BigDecimal[] data = new BigDecimal[max];
        for (int i = 0; i < max; i++) {
            data[i] = new BigDecimal(i);
        }
        log.info("> Arrays.sort, max=" + max + ", type=" + data[0].getClass().getName());
        try {
            Arrays.sort(data);
        } finally {
            log.info("< Arrays.sort");
        }
    }

}
