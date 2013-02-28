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

import org.python.core.PyObject;
import org.python.util.PythonInterpreter;

public class JythonTransactionFiltersFactory {

    private PyObject pythonInstance;

    public JythonTransactionFiltersFactory() {
        PythonInterpreter interpreter = new PythonInterpreter();
        // String packageName = "filters.MyTransactionFilters";
        String moduleName = "RecurringFilter";
        String packageName = "com.le.sunriise.python";

        interpreter.exec("from " + packageName + "." + moduleName + " import *");

        // String className = "NoRecurringFilter";
        String className = moduleName;
        pythonInstance = interpreter.get(className);
    }

    public TransactionFilter create() {
        PyObject pyObject = pythonInstance.__call__();
        Class<TransactionFilter> javaClz = TransactionFilter.class;
        return (TransactionFilter) pyObject.__tojava__(javaClz);
    }

    // public static Object getJythonObject(String interfaceName, String
    // pathToJythonModule) {
    //
    // Object javaInt = null;
    // PythonInterpreter interpreter = new PythonInterpreter();
    // interpreter.execfile(pathToJythonModule);
    // String tempName =
    // pathToJythonModule.substring(pathToJythonModule.lastIndexOf("/") + 1);
    // tempName = tempName.substring(0, tempName.indexOf("."));
    // System.out.println(tempName);
    // String instanceName = tempName.toLowerCase();
    // String javaClassName = tempName.substring(0, 1).toUpperCase() +
    // tempName.substring(1);
    // String objectDef = "=" + javaClassName + "()";
    // interpreter.exec(instanceName + objectDef);
    // try {
    // Class JavaInterface = Class.forName(interfaceName);
    // javaInt = interpreter.get(instanceName).__tojava__(JavaInterface);
    // } catch (ClassNotFoundException ex) {
    // ex.printStackTrace(); // Add logging here
    // }
    //
    // return javaInt;
    // }
}
