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
package com.le.sunriise.export;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.healthmarketscience.jackcess.Database;
import com.le.sunriise.json.JSONUtils;
import com.le.sunriise.mnyobject.Account;
import com.le.sunriise.report.DefaultAccountVisitor;
import com.le.sunriise.viewer.OpenedDb;

public class ExportToJSON extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(ExportToJSON.class);

    private File outDir;

    protected void startExport(File outDir) {
    }

    protected void endExport(File outDir) {
    }

    @Override
    public void visitAccount(Account account) {
        String accountName = account.getName();
        log.info("> " + accountName);

        accountName = toSafeFileName(accountName);
        File out = new File(outDir, accountName + ".json");

        FileWriter writer = null;
        try {
            writer = new FileWriter(out);
            JSONUtils.writeValue(account, writer);
//            try {
//                Thread.sleep(1 * 1000L);
//            } catch (InterruptedException e) {
//                log.warn(e);
//            }
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
    
        return safeC;
    }

    public void export(File dbFile, String password, File outDir) throws IOException {
        this.outDir = outDir;
        startExport(outDir);
        try {
            visit(dbFile, password);
        } finally {
            endExport(outDir);
        }
    }

    public Database export(OpenedDb srcDb, File outDir) throws IOException {
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
