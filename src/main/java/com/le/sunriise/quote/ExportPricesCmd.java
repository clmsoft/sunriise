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
package com.le.sunriise.quote;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;

public class ExportPricesCmd {
    private static final Logger log = Logger.getLogger(ExportPricesCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String inFileName = null;
        String outFileName = null;
        String password = null;

        if (args.length == 2) {
            inFileName = args[0];
            outFileName = args[1];
            password = null;
        } else if (args.length == 3) {
            inFileName = args[0];
            outFileName = args[1];
            password = args[2];
        } else {
            Class<ExportPricesCmd> clz = ExportPricesCmd.class;
            System.out.println("Usage: " + clz.getName() + " sample.mny out.csv [password]");
            System.exit(1);
        }

        File inFile = new File(inFileName);
        File outFile = new File(outFileName);

        log.info("inFile=" + inFile);
        log.info("outFile=" + outFile);

        if (!inFile.exists()) {
            log.error("File " + inFile + " does not exist.");
            System.exit(1);
        }

        try {
            ExportPrices cmd = new ExportPrices();
            cmd.export(inFile, outFile, password);
        } catch (IOException e) {
            log.error(e);
        } finally {
            log.info("< DONE");
        }

    }

}
