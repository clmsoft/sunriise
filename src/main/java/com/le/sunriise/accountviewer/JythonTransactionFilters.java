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
package com.le.sunriise.accountviewer;

import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.python.util.PythonInterpreter;

import com.le.sunriise.mnyobject.Transaction;

public class JythonTransactionFilters {
    private static final Logger log = Logger.getLogger(JythonTransactionFilters.class);
    static {
        Properties props = new Properties();
        props.setProperty("python.path", "site/python" + ";" + "../com.le.sunriise.python/src");
        PythonInterpreter.initialize(System.getProperties(), props, new String[] { "" });
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        String packageName = "com.le.sunriise.python";
        String moduleName = "RecurringFilter";
        String className = "RecurringFilter";

        JythonTransactionFiltersFactory factory = new JythonTransactionFiltersFactory();
        TransactionFilter filter = factory.create();
        log.info("filter=" + filter);

        List<TransactionFilter> filters = getFilters(packageName);
        log.info("filters=" + filters);
        for (TransactionFilter f : filters) {
            log.info(f);
            Map<String, Object> row = null;
            Transaction transaction = new Transaction();
            transaction.isRecurring();
            log.info("    accept=" + f.accept(transaction, row));
        }
    }

    public static List<TransactionFilter> getFilters(String packageName) {
        log.info("> getFilters");
        PythonInterpreter interpreter = new PythonInterpreter();
        interpreter.exec("from " + packageName + "." + "Main" + " import *");
        interpreter.exec("main = Main()");
        interpreter.exec("filters = main.getFilters()");
        Class<List> clz = List.class;
        List<TransactionFilter> filters = interpreter.get("filters", clz);
        log.info("> getFilters, filters=" + filters);
        return filters;
    }
}
