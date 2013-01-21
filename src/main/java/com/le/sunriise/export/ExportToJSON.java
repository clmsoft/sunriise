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
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
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

    @Override
    public void visitAccounts(List<Account> accounts) throws IOException {
        super.visitAccounts(accounts);

        File f = null;

        f = new File(outDir, "categories.json");
        writeValue(this.mnyContext.getCategories().values(), f);

        f = new File(outDir, "payees.json");
        writeValue(this.mnyContext.getPayees().values(), f);

        f = new File(outDir, "currencies.json");
        writeValue(this.mnyContext.getCurrencies().values(), f);

        f = new File(outDir, "securities.json");
        writeValue(this.mnyContext.getSecurities().values(), f);
    }

    @Override
    public void visitAccount(Account account) throws IOException {
        super.visitAccount(account);

        String accountName = account.getName();
        log.info("> " + accountName);

        accountName = toSafeFileName(accountName);
        File d = new File(outDir, accountName + ".d");
        d.mkdirs();

        File f = null;

        f = new File(d, "account.json");
        writeValue(account, f);

        f = new File(d, "transactions.json");
        writeValue(account.getTransactions(), f);
    }

    private void writeValue(Object value, File out) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            JSONUtils.writeValue(value, writer);
        } catch (JsonGenerationException e) {
            log.error(e, e);
        } catch (JsonMappingException e) {
            log.error(e, e);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    writer = null;
                }
            }
        }
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
