package com.le.sunriise.graphviz;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.index.IndexLookup;

public class GenGraphViz {
    private static final Logger log = Logger.getLogger(GenGraphViz.class);

    public void gen(Table table, PrintWriter writer) throws IOException {
        log.info("### table=" + table.getName());
        IndexLookup indexLookup = new IndexLookup();

        List<Column> primaryKeys = new ArrayList<Column>();
        List<Column> foreignKeys = new ArrayList<Column>();
        for (Column column : table.getColumns()) {
            if (indexLookup.isPrimaryKeyColumn(column)) {
                // log.info("(PK) " + table.getName() + "." + column.getName() +
                // ", " + indexLookup.getMax(column));
                primaryKeys.add(column);
            }
            // List<Column> referencing = indexLookup.getReferencing(column);
            // for (Column col : referencing) {
            // log.info("(referencing-FK) " + col.getTable().getName() + "." +
            // col.getName());
            // }

            List<Column> referenced = indexLookup.getReferencedColumns(column);
            // for (Column col : referenced) {
            // log.info("(FK) " + table.getName() + "." + column.getName() +
            // " -> " + col.getTable().getName() + "." + col.getName());
            // }
            if ((referenced != null) && (referenced.size() > 0)) {
                foreignKeys.add(column);
            }
        }

        gen(table, primaryKeys, foreignKeys, writer);
    }

    private void gen(Table table, List<Column> primaryKeys, List<Column> foreignKeys, PrintWriter writer) {
        if (writer == null) {
            return;
        }

        writer.println("// http://graphviz-dev.appspot.com/");
        writer.println("digraph g {");
        writer.println("graph [ rankdir = \"LR\" ];");

        writer.println("node [ fontsize = \"16\" shape = \"record\" ];");

        writer.println("\"" + table.getName() + "\" [");
        writer.print("label = \"" + table.getName());
        for (Column column : primaryKeys) {
            writer.print("|");
            writer.print("<" + column.getName() + ">" + column.getName());
        }
        for (Column column : foreignKeys) {
            writer.print("|");
            writer.print("<" + column.getName() + ">" + column.getName());
        }
        writer.print("\"");
        writer.println();
        writer.println("];");

        writer.println("}");

    }
}
