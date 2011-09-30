package com.le.sunriise.excel;

import org.apache.poi.ss.usermodel.Row;

public class InvestmentTxn {

    public static InvestmentTxn toInvestmentTxn(String sheetName, Row row) {
        InvestmentTxn investmentTxn = new InvestmentTxn();
        return investmentTxn;
    }

}
