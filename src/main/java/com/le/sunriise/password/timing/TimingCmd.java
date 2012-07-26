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
package com.le.sunriise.password.timing;

import org.apache.log4j.Logger;

public class TimingCmd {
    private static final Logger log = Logger.getLogger(TimingCmd.class);

    private static final int DEFAULT_MAX_ITERATIONS = 10000000;

    /**
     * @param args
     */
    public static void main(String[] args) {
        int maxIteration = DEFAULT_MAX_ITERATIONS;

        log.info("#");
        long digestDelta = DigestTimingCmd.doTiming(maxIteration);

        log.info("#");
        long rc4Delta = RC4TimingCmd.doTiming(maxIteration);

        log.info("#");
        long checkerDelta = PasswordCheckerTimingCmd.doTiming(maxIteration);

        log.info("#");
        log.info("digestDelta=" + digestDelta);
        log.info("rc4Delta=" + rc4Delta);
        long sum = (digestDelta + rc4Delta);
        log.info("sum=" + sum);
        
        long rate1 = maxIteration/(sum/1000L);
        log.info("  rate=" + rate1 + "/sec");

        log.info("checkerDelta=" + checkerDelta);
        long rate2 = maxIteration/(checkerDelta/1000L);
        log.info("  rate=" + rate2 + "/sec");
        
        long percentage = ((rate1 - rate2) * 100) / rate1;
        log.info("  percentage=" + percentage + "%");
    }

}
