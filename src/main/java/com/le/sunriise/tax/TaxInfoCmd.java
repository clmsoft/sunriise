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
package com.le.sunriise.tax;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

public class TaxInfoCmd {
    private static final Logger log = Logger.getLogger(TaxInfoCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;

        if (args.length == 1) {
            dbFile = new File(args[0]);
        } else if (args.length == 2) {
            dbFile = new File(args[0]);
            password = args[1];
        } else {
            Class<TaxInfoCmd> clz = TaxInfoCmd.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            List<TaxInfo> taxInfoList = TaxInfo.parse(dbFile, password);
            for (TaxInfo taxInfo : taxInfoList) {
                List<IncomeRate> incomeRates = taxInfo.getIncomeRates();
                int index = 1;
                for (IncomeRate incomeRate : incomeRates) {
                    printIncomeRate(taxInfo.getlTaxYear(), taxInfo.getSzFull(), index, incomeRate);
                    index++;
                }
                List<RateInfo> otherRates = taxInfo.getOtherRates();
                for (RateInfo otherRate : otherRates) {
                    printRateInfo(taxInfo.getlTaxYear(), taxInfo.getSzFull(), otherRate);
                }
            }
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    private static void printRateInfo(Integer getlTaxYear, String szFull, RateInfo otherRate) {
        StringBuilder sb = new StringBuilder();
        String sep = ",";

        sb.append("\"" + getlTaxYear + "\"");

        sb.append(sep);
        sb.append("\"" + szFull + "\"");

        sb.append(sep);
        sb.append("\"" + otherRate.getDescription() + "\"");

        sb.append(sep);
        sb.append("\"" + otherRate.getRate() + "\"");

        System.out.println(sb.toString());
    }

    private static void printIncomeRate(Integer getlTaxYear, String szFull, int index, IncomeRate incomeRate) {
        StringBuilder sb = new StringBuilder();
        String sep = ",";

        sb.append("\"" + getlTaxYear + "\"");

        sb.append(sep);
        sb.append("\"" + szFull + "\"");

        sb.append(sep);
        sb.append("\"" + "Income_" + index + "\"");

        sb.append(sep);
        sb.append(incomeRate.toString(sep, true, false));

        System.out.println(sb.toString());
    }
}
