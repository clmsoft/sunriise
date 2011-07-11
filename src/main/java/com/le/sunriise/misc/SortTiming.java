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
