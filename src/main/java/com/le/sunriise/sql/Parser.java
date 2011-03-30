package com.le.sunriise.sql;

import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

import org.apache.log4j.Logger;

public class Parser {
    private static final Logger log = Logger.getLogger(Parser.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub
        CCJSqlParserManager pm = new CCJSqlParserManager();
        String sql = "select * from SP";
        try {
            net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
            if (statement instanceof Select) {
                Select selectStatement = (Select) statement;
                TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
                List tableList = tablesNamesFinder.getTableList(selectStatement);
                for (Iterator iter = tableList.iterator(); iter.hasNext();) {
                    String tableName = (String) iter.next();
                    log.info("tableName=" + tableName);

                }
            }
        } catch (JSQLParserException e) {
            log.error(e, e);
        }
    }

}
