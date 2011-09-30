package com.le.sunriise.excel;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;

public class MoneyLinkExcelParser {
    private static final Logger log = Logger.getLogger(MoneyLinkExcelParser.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String fileName = null;
        if (args.length != 1) {
            Class<MoneyLinkExcelParser> clz = MoneyLinkExcelParser.class;
            System.out.println("Usage: java " + clz.getName() + " moneyLink-out.xls");
            System.exit(1);
        }

        fileName = args[0];
        InputStream input = null;
        try {
            log.info("> fileName=" + fileName);
            input = new BufferedInputStream(new FileInputStream(new File(fileName)));
            HSSFWorkbook workBook = new HSSFWorkbook(input);
            int n = workBook.getNumberOfSheets();
            for (int i = 0; i < n; i++) {
                // Accounts
                // TNX-number
                // Investment_Balances
                // Investment_TXN
                // Investment_Lots
                // Investment_Prices
                HSSFSheet sheet = workBook.getSheetAt(i);
                log.info("  sheet=" + sheet.getSheetName());
            }
            readAccounts(workBook);
            readTXNs(workBook);
            readInvestmentBalances(workBook);
            readInvestmentTXN(workBook);
            readInvestmentLots(workBook);
            readInvestmentPrices(workBook);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("> DONE");
            if (input != null) {
                try {
                    input.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    input = null;
                }
            }
        }

    }

    private static List<HSSFSheet> getSheets(HSSFWorkbook workBook, String sheetName, int max) {
        log.info("> getSheets, sheetName=" + sheetName);
        List<HSSFSheet> sheets = new ArrayList<HSSFSheet>();
        HSSFSheet sheet = workBook.getSheet(sheetName);
        if (sheet != null) {
            sheets.add(sheet);
        } else {
            for (int i = 0; i < max; i++) {
                sheet = workBook.getSheet(sheetName + "-" + (i + 1));
                if (sheet != null) {
                    sheets.add(sheet);
                } else {
                    break;
                }
            }
        }
        return sheets;
    }

    private static void readAccounts(HSSFWorkbook workBook) {
        List<Account> accounts = new ArrayList<Account>();

        String sheetName = "Accounts";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);
        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                Account account = Account.toAccount(sheetName, row);
                accounts.add(account);
            }
        }

        log.info("Parsed " + accounts.size() + " accounts.");
    }

    private static void readTXNs(HSSFWorkbook workBook) {
        List<Transaction> txns = new ArrayList<Transaction>();

        String sheetName = "TXN";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);
        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                Transaction txn = Transaction.toTransaction(sheetName, row);
                txns.add(txn);
            }
        }
        log.info("Parsed " + txns.size() + " transactions.");
    }

    private static void readInvestmentBalances(HSSFWorkbook workBook) {
        List<InvestmentBalance> balances = new ArrayList<InvestmentBalance>();

        String sheetName = "Investment_Balances";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);
        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                InvestmentBalance balance = InvestmentBalance.toInvestmentBalance(sheetName, row);
                balances.add(balance);
            }
        }
        log.info("Parsed " + balances.size() + " investment balances.");
    }

    private static void readInvestmentTXN(HSSFWorkbook workBook) {
        List<InvestmentTxn> investmentTxns = new ArrayList<InvestmentTxn>();

        String sheetName = "Investment_TXN";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);
        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                InvestmentTxn investmentTxn = InvestmentTxn.toInvestmentTxn(sheetName, row);
                investmentTxns.add(investmentTxn);
            }
        }
        log.info("Parsed " + investmentTxns.size() + " investment transactions.");
    }

    private static void readInvestmentLots(HSSFWorkbook workBook) {
        List<InvestmentLot> investmentLots = new ArrayList<InvestmentLot>();
        
        String sheetName = "Investment_Lots";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);
        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                InvestmentLot investmentLot = InvestmentLot.toInvestmentLot(sheetName, row);
                investmentLots.add(investmentLot);
            }
        }
        log.info("Parsed " + investmentLots.size() + " investment lots.");
    }

    private static void readInvestmentPrices(HSSFWorkbook workBook) {
        List<InvestmentPrice> investmentPrices = new ArrayList<InvestmentPrice>();
        
        String sheetName = "Investment_Prices";
        int max = 10;
        List<HSSFSheet> sheets = getSheets(workBook, sheetName, max);

        for (HSSFSheet sheet : sheets) {
            for (Row row : sheet) {
                InvestmentPrice investmentPrice = InvestmentPrice.toInvestmentPrice(sheetName, row);
                investmentPrices.add(investmentPrice);
            }
        }
        log.info("Parsed " + investmentPrices.size() + " investment prices.");
    
    }
}
