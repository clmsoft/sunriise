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
package com.le.sunriise.script;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

public class RunScriptUtils {
    private static final Logger log = Logger.getLogger(RunScriptUtils.class);

    public static List<RunScript> parseScriptFile(File scriptFile) throws IOException {
        List<RunScript> runScripts = new ArrayList<RunScript>();

        RunScript runScript = null;

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(scriptFile));
            String line = null;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 0) {
                    continue;
                }
                if (line.charAt(0) == '#') {
                    continue;
                }

                List<String> tokenList = new CommandLineParser(line).parseText();
                if (tokenList.size() <= 0) {
                    log.warn("Malformed command, str=" + line);
                    continue;
                }
                String[] tokens = new String[0];
                tokens = tokenList.toArray(tokens);

                if (tokens[0].compareToIgnoreCase("table") == 0) {
                    // table tableName
                    if (tokens.length <= 1) {
                        log.warn("Malformed command, str=" + line);
                        continue;
                    }
                    runScript = new RunScript();
                    runScripts.add(runScript);
                    runScript.setTableName(tokens[1]);
                } else if (line.startsWith("column ")) {
                    // column columnName columnValue
                    if (tokens.length <= 2) {
                        log.warn("Malformed command, str=" + line);
                        continue;
                    }
                    MatchColumn matchColumn = new MatchColumn();
                    matchColumn.setColumnName(tokens[1]);
                    matchColumn.setValue(tokens[2]);
                    runScript.getSelectColumns().add(matchColumn);
                } else if (tokens[0].compareToIgnoreCase("set") == 0) {
                    // set column abc 123
                    if (tokens.length <= 3) {
                        log.warn("Malformed command, str=" + line);
                        continue;
                    }
                    if (tokens[1].compareToIgnoreCase("column") == 0) {
                        MatchColumn matchColumn = new MatchColumn();
                        matchColumn.setColumnName(tokens[2]);
                        matchColumn.setValue(tokens[3]);
                        runScript.getSetColumns().add(matchColumn);
                    }
                } else {
                    log.warn("Malformed command, str=" + line);
                    continue;
                }
            }
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    reader = null;
                }
            }
        }

        return runScripts;
    }

}
