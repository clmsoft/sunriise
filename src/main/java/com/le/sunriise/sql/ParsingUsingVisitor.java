package com.le.sunriise.sql;

import java.io.IOException;
import java.io.StringReader;
import java.util.Iterator;
import java.util.List;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.statement.select.Select;

import org.apache.log4j.Logger;

public class ParsingUsingVisitor {
    private static final Logger log = Logger.getLogger(ParsingUsingVisitor.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        CCJSqlParserManager pm = new CCJSqlParserManager();
        String sql = "select SEC.szSymbol, SP.* from SP inner join SEC on SP.hsec = SEC.hsec where SEC.szSymbol = 'MFST' ";
        try {
            try {
                net.sf.jsqlparser.statement.Statement statement = pm.parse(new StringReader(sql));
                if (statement instanceof Select) {
                    Select selectStatement = (Select) statement;
                    BaseSelectVisitor tablesNamesFinder = new BaseSelectVisitor();
                    tablesNamesFinder.accept(selectStatement);
                } else {
                    throw new IOException("Unsupported statement, sql=" + sql);
                }
            } catch (IOException e) {
                log.error(e);
            }
        } catch (JSQLParserException e) {
            log.error(e, e);
        }
    }

}
