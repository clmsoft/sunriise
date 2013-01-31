/*******************************************************************************
 * Copyright (c) 2013 Hung Le
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
package com.le.sunriise.export;

import java.io.File;
import java.io.IOException;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.json.JSONUtils;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.report.DefaultAccountVisitor;
import com.le.sunriise.viewer.OpenedDb;

public class ExportToJSON extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(ExportToJSON.class);

    private class SafeCharMap {
        private char c1;
        private char c2;

        public SafeCharMap(char c1, char c2) {
            super();
            this.c1 = c1;
            this.c2 = c2;
        }

        public char getC1() {
            return c1;
        }

        public void setC1(char c1) {
            this.c1 = c1;
        }

        public char getC2() {
            return c2;
        }

        public void setC2(char c2) {
            this.c2 = c2;
        }
    }

    private File outDir;

    protected void startExport(File outDir) {
    }

    protected void endExport(File outDir) {
    }

    protected void exportMnyContext(File outDir) throws IOException {
        File outFile = null;
    
        if (outDir == null) {
            log.warn("outDir=null. Will SKIP exportMnyContext");
        }
        
        outFile = new File(outDir, "categories.json");
        JSONUtils.writeValue(this.mnyContext.getCategories().values(), outFile);
    
        outFile = new File(outDir, "payees.json");
        JSONUtils.writeValue(this.mnyContext.getPayees().values(), outFile);
    
        outFile = new File(outDir, "currencies.json");
        JSONUtils.writeValue(this.mnyContext.getCurrencies().values(), outFile);
    
        outFile = new File(outDir, "securities.json");
        JSONUtils.writeValue(this.mnyContext.getSecurities().values(), outFile);
    }

    protected void exportAccount(Account account, File outDir) throws IOException {
        File outFile = null;

        if (outDir == null) {
            log.warn("outDir=null. Will SKIP exportAccount");
        }
        
        outFile = new File(outDir, "account.json");
        JSONUtils.writeValue(account, outFile);
    
        outFile = new File(outDir, "transactions.json");
        JSONUtils.writeValue(account.getTransactions(), outFile);
    }

    @Override
    public void visitAccounts(List<Account> accounts) throws IOException {
        super.visitAccounts(accounts);

        exportMnyContext(outDir);
    }

    @Override
    public void visitAccount(Account account) throws IOException {
        super.visitAccount(account);

        String accountName = account.getName();
        log.info("> " + accountName);

        accountName = toSafeFileName(accountName);
        File d = new File(outDir, accountName + ".d");
        d.mkdirs();

        exportAccount(account, d);
    }

    private String toSafeFileName(String accountName) {
        StringBuilder sb = new StringBuilder();

        int len = accountName.length();
        for (int i = 0; i < len; i++) {
            char c = accountName.charAt(i);
            sb.append(toSafeFileNameChar(c));
        }
        return sb.toString();
    }

    private char toSafeFileNameChar(char c) {
        char safeC = c;

        if (c == ' ') {
            return '_';
        }
        if (c == '(') {
            return '_';
        }
        if (c == ')') {
            return '_';
        }
        if (c == '&') {
            return '_';
        }
        if (c == '\'') {
            return '_';
        }
        if (c == '’') {
            return '_';
        }
        if (c == '/') {
            return '_';
        }
        if (c == '\\') {
            return '_';
        }
        if (c == '>') {
            return '_';
        }
        if (c == '<') {
            return '_';
        }
        if (c == '|') {
            return '_';
        }
        if (c == '"') {
            return '_';
        }
        if (c == '*') {
            return '_';
        }
        if (c == '*') {
            return '_';
        }
        if (c == '{') {
            return '_';
        }
        if (c == '}') {
            return '_';
        }

        return safeC;
    }

    public void export(File dbFile, String password, File outDir) throws IOException {
        if (dbFile == null) {
            return;
        }

        this.outDir = outDir;
        startExport(outDir);
        try {
            visit(dbFile, password);
        } finally {
            endExport(outDir);
        }
    }

    public Database export(OpenedDb srcDb, File outDir) throws IOException {
        if (srcDb == null) {
            return null;
        }
        if (srcDb.getDb() == null) {
            return null;
        }
        this.outDir = outDir;
        startExport(outDir);

        try {
            _visit(srcDb);
        } finally {
            endExport(outDir);
        }

        return srcDb.getDb();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        File dbFile = null;
        String password = null;
        File outDir = null;

        if (args.length == 2) {
            dbFile = new File(args[0]);
            outDir = new File(args[1]);
        } else if (args.length == 3) {
            dbFile = new File(args[0]);
            password = args[1];
            outDir = new File(args[2]);
        } else {
            Class<ExportToJSON> clz = ExportToJSON.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password] outDir");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        log.info("outDir=" + outDir);

        outDir.mkdirs();
        try {
            ExportToJSON cmd = new ExportToJSON();
            cmd.export(dbFile, password, outDir);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }
}
