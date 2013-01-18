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
package com.le.sunriise.report;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;

import com.le.sunriise.json.JSONUtils;
import com.le.sunriise.mnyobject.Account;

public class ListAccounts extends DefaultAccountVisitor {
    private static final Logger log = Logger.getLogger(ListAccounts.class);

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
            Class<ListAccounts> clz = ListAccounts.class;
            System.out.println("Usage: java " + clz.getName() + " in.mny [password]");
            System.exit(1);
        }

        log.info("dbFile=" + dbFile);
        try {
            ListAccounts cmd = new ListAccounts();
            cmd.visit(dbFile, password);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    @Override
    public void visitAccount(Account account) {
        log.info(account.getName());

        boolean toJSON = true;
        if (toJSON) {
            CharArrayWriter writer = new CharArrayWriter();
            try {
                JSONUtils.writeValue(account, writer);
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
                        System.out.println(new String(writer.toCharArray()));
                    } finally {
                        writer = null;
                    }
                }
            }
        }
    }
}
