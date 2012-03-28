package com.le.sunriise.tax;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Cursor;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.Utils;
import com.le.sunriise.viewer.OpenedDb;

public class TaxInfo {
    private class IncomeRate {
        private Double amountLow;
        private Double amountHigh;
        private Double rate;

        public Double getAmountLow() {
            return amountLow;
        }

        public void setAmountLow(Double amountLow) {
            this.amountLow = amountLow;
        }

        public Double getAmountHigh() {
            return amountHigh;
        }

        public void setAmountHigh(Double amountHigh) {
            this.amountHigh = amountHigh;
        }

        public Double getRate() {
            return rate;
        }

        public void setRate(Double rate) {
            this.rate = rate;
        }

        public String toString() {
            StringBuilder sb = new StringBuilder();

            sb.append(amountLow);
            sb.append(", ");

            if (amountHigh != null) {
                sb.append(amountHigh);
            } else {
                sb.append("");
            }
            sb.append(", ");

            if (rate != null) {
                sb.append(rate);
            } else {
                sb.append("");
            }
            // sb.append(", ");

            return sb.toString();
        }
    }

    private static final Logger log = Logger.getLogger(TaxInfo.class);

    public void visit(File dbFile, String password) throws IOException {
        OpenedDb openedDb = Utils.openDbReadOnly(dbFile, password);
        visit(openedDb);
    }

    public void visit(OpenedDb openedDb) throws IOException {
        Database db = openedDb.getDb();
        Set<String> tableNames = db.getTableNames();
        for (String tableName : tableNames) {
            Table table = db.getTable(tableName);
            if (!acceptTable(table)) {
                continue;
            }
            visit(table);
        }
    }

    public boolean acceptTable(Table table) {
        return true;
    }

    public void visit(Table table) {
        String tableName = table.getName();

        if (tableName.compareToIgnoreCase("TAXLINE") == 0) {
            Cursor cursor = Cursor.createCursor(table);
            Iterator<Map<String, Object>> rows = cursor.iterator();
            while (rows.hasNext()) {
                Map<String, Object> row = rows.next();
                // log.info(row);
            }
        } else if (tableName.compareToIgnoreCase("Tax Rate Custom Pool") == 0) {
            Cursor cursor = Cursor.createCursor(table);
            Iterator<Map<String, Object>> rows = cursor.iterator();
            while (rows.hasNext()) {
                Map<String, Object> row = rows.next();
                Integer lTaxYear = (Integer) row.get("lTaxYear");
                if (lTaxYear != null) {
                    if (lTaxYear == 2012) {
                        String szFull = (String) row.get("szFull");
                        // log.info(szFull);

                        // System.out.println("#");

                        IncomeRate incomeRate = null;
                        String separator = ", ";
                        for (int i = 0; i < 6; i++) {
                            incomeRate = new IncomeRate();

                            Double dRate = (Double) row.get("dRate" + (i + 1));
                            incomeRate.setRate(dRate);

                            // damtLow1
                            Double damtLow = (Double) row.get("damtLow" + (i + 1));
                            incomeRate.setAmountLow(damtLow);

                            // damtHigh1
                            Double damtHigh = (Double) row.get("damtHigh" + (i + 1));
                            incomeRate.setAmountHigh(damtHigh);

                            System.out.println(szFull + "." + "Income" + "_" + (i + 1) + separator + incomeRate);
                        }

                        // Long-Term Capital Gains: dRateCapGains
                        Double dRateCapGains = (Double) row.get("dRateCapGains");
                        System.out.println(szFull + "." + "Long-Term Capital Gains" + separator + dRateCapGains);

                        // Dividends: dRateDividends
                        Double dRateDividends = (Double) row.get("dRateDividends");
                        System.out.println(szFull + "." + "Dividends" + separator + dRateDividends);

                        // Standard Deduction: damtStdDed
                        Double damtStdDed = (Double) row.get("damtStdDed");
                        System.out.println(szFull + "." + "Standard Deduction" + separator + damtStdDed);

                        // Exemption Amount: damtStdEx
                        Double damtStdEx = (Double) row.get("damtStdEx");
                        System.out.println(szFull + "." + "Exemption Amount" + separator + damtStdEx);

                        // Exemption Cutoff: damtThreshExemp
                        Double damtThreshExemp = (Double) row.get("damtThreshExemp");
                        System.out.println(szFull + "." + "Exemption Cutoff" + separator + damtThreshExemp);

                        // Maximum Capital Loss: damtMaxCapLoss
                        Double damtMaxCapLoss = (Double) row.get("damtMaxCapLoss");
                        System.out.println(szFull + "." + "Maximum Capital Loss" + separator + damtMaxCapLoss);

                        // Blind : damtDedBlind
                        Double damtDedBlind = (Double) row.get("damtDedBlind");
                        System.out.println(szFull + "." + "Blind" + separator + damtDedBlind);

                        // Over 65: damtDedOver65
                        Double damtDedOver65 = (Double) row.get("damtDedOver65");
                        System.out.println(szFull + "." + "Over 65" + separator + damtDedOver65);

                        // Deduction Cutoff: damtThreshDed
                        Double damtThreshDed = (Double) row.get("damtThreshDed");
                        System.out.println(szFull + "." + "Deduction Cutoff" + separator + damtThreshDed);
                    }
                }
            }
        } else if (tableName.compareToIgnoreCase("Tax Scenario Custom Pool") == 0) {
            Cursor cursor = Cursor.createCursor(table);
            Iterator<Map<String, Object>> rows = cursor.iterator();
            while (rows.hasNext()) {
                Map<String, Object> row = rows.next();
                // log.info(row);
            }
        }

    }

}
