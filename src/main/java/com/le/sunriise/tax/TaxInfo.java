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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class TaxInfo {
    private static final Logger log = Logger.getLogger(TaxInfo.class);

    private List<RateInfo> otherRates;

    private List<IncomeRate> incomeRates;

    private String szFull;

    private Integer lTaxYear;

    public TaxInfo() {
        super();
        otherRates = new ArrayList<RateInfo>();

        otherRates.add(new RateInfo("dRateCapGains", "Long-Term Capital Gains"));
        otherRates.add(new RateInfo("dRateDividends", "Dividends"));
        otherRates.add(new RateInfo("damtStdDed", "Standard Deduction"));
        otherRates.add(new RateInfo("damtStdEx", "Exemption Amount"));
        otherRates.add(new RateInfo("damtThreshExemp", "Exemption Cutoff"));
        otherRates.add(new RateInfo("damtMaxCapLoss", "Maximum Capital Loss"));
        otherRates.add(new RateInfo("damtDedBlind", "Blind"));
        otherRates.add(new RateInfo("damtDedOver65", "Over 65"));
        otherRates.add(new RateInfo("damtThreshDed", "Deduction Cutoff"));
    }

    public static List<TaxInfo> parse(File dbFile, String password) throws IOException {
        OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
        return parse(openedDb);
    }

    public static List<TaxInfo> parse(OpenedDb openedDb) throws IOException {
        List<TaxInfo> taxInfoList = new ArrayList<TaxInfo>();

        Database db = openedDb.getDb();
        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            Table table = db.getTable(tableName);
            if (!TaxInfo.acceptTable(table)) {
                continue;
            }
            TaxInfo.parse(table, taxInfoList);
        }

        return taxInfoList;
    }

    private static boolean acceptTable(Table table) {
        return true;
    }

    private static void parse(Table table, List<TaxInfo> taxInfoList) {
        String tableName = table.getName();
        if (tableName.compareToIgnoreCase("TAXLINE") == 0) {
            visitTable_TAXLINE(table);
        } else if (tableName.compareToIgnoreCase("Tax Rate Custom Pool") == 0) {
            visitTable_Tax_Rate_Custom_Pool(table, taxInfoList);
        } else if (tableName.compareToIgnoreCase("Tax Scenario Custom Pool") == 0) {
            visitTable_Tax_Scenario_Custom_Pool(table);
        }
    }

    private static void visitTable_Tax_Rate_Custom_Pool(Table table, List<TaxInfo> taxInfoList) {
        Cursor cursor = Cursor.createCursor(table);
        Iterator<Map<String, Object>> rows = cursor.iterator();
        while (rows.hasNext()) {
            Map<String, Object> row = rows.next();
            Integer lTaxYear = (Integer) row.get("lTaxYear");
            if (lTaxYear != null) {
                TaxInfo taxInfo = vitsitTaxYear(row, lTaxYear);
                taxInfoList.add(taxInfo);
            }
        }
    }

    private static TaxInfo vitsitTaxYear(final Map<String, Object> row, final Integer lTaxYear) {
        TaxInfo taxInfo = new TaxInfo();

        taxInfo.setSzFull((String) row.get("szFull"));
        taxInfo.setlTaxYear(lTaxYear);

        visitIncomeRate(row, taxInfo);

        visitOtherRates(row, taxInfo);

        return taxInfo;
    }

    private static void visitIncomeRate(final Map<String, Object> row, final TaxInfo taxInfo) {
        List<IncomeRate> incomeRates = new ArrayList<IncomeRate>();

        IncomeRate incomeRate = null;
        int max = 6;
        for (int i = 0; i < max; i++) {
            incomeRate = new IncomeRate();

            int index = i + 1;
            Double dRate = (Double) row.get("dRate" + index);
            incomeRate.setRate(dRate);

            // damtLow1
            Double damtLow = (Double) row.get("damtLow" + index);
            incomeRate.setAmountLow(damtLow);

            // damtHigh1
            Double damtHigh = (Double) row.get("damtHigh" + index);
            incomeRate.setAmountHigh(damtHigh);

            incomeRates.add(incomeRate);
        }
        taxInfo.setIncomeRates(incomeRates);
    }

    private static void visitOtherRates(final Map<String, Object> row, final TaxInfo taxInfo) {
        for (RateInfo otherRate : taxInfo.getOtherRates()) {
            Double rate = (Double) row.get(otherRate.getColumnName());
            otherRate.setRate(rate);
            // System.out.println(szFull + "." + columnName.getKey() + separator
            // + rate);
        }
    }

    private static void visitTable_Tax_Scenario_Custom_Pool(Table table) {
        Cursor cursor = Cursor.createCursor(table);
        Iterator<Map<String, Object>> rows = cursor.iterator();
        while (rows.hasNext()) {
            Map<String, Object> row = rows.next();
            if (log.isDebugEnabled()) {
                log.debug(row);
            }
        }
    }

    private static void visitTable_TAXLINE(Table table) {
        Cursor cursor = Cursor.createCursor(table);
        Iterator<Map<String, Object>> rows = cursor.iterator();
        while (rows.hasNext()) {
            Map<String, Object> row = rows.next();
            if (log.isDebugEnabled()) {
                log.debug(row);
            }
        }
    }

    public List<RateInfo> getOtherRates() {
        return otherRates;
    }

    public void setOtherRates(List<RateInfo> otherRates) {
        this.otherRates = otherRates;
    }

    public List<IncomeRate> getIncomeRates() {
        return incomeRates;
    }

    public void setIncomeRates(List<IncomeRate> incomeRates) {
        this.incomeRates = incomeRates;
    }

    public String getSzFull() {
        return szFull;
    }

    public void setSzFull(String szFull) {
        this.szFull = szFull;
    }

    public Integer getlTaxYear() {
        return lTaxYear;
    }

    public void setlTaxYear(Integer lTaxYear) {
        this.lTaxYear = lTaxYear;
    }

}
