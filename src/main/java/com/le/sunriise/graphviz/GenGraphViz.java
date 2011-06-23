package com.le.sunriise.graphviz;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.Table;
import com.le.sunriise.index.IndexLookup;

public class GenGraphViz {
    private static final Logger log = Logger.getLogger(GenGraphViz.class);

    private class KeyInfo {
        private Column key;
        private List<Column> links;

        public Column getKey() {
            return key;
        }

        public void setKey(Column key) {
            this.key = key;
        }

        public List<Column> getLinks() {
            return links;
        }

        public void setLinks(List<Column> links) {
            this.links = links;
        }
    }

    private Map<String, Set<String>> tablesData = new TreeMap<String, Set<String>>();
    private List<KeyInfo> primaryKeys = new ArrayList<KeyInfo>();
    private List<KeyInfo> foreignKeys = new ArrayList<KeyInfo>();

    public void gen(Table table, PrintWriter writer) throws IOException {
        if (writer == null) {
            return;
        }

        tablesData.clear();
        primaryKeys.clear();
        foreignKeys.clear();

        log.info("### table=" + table.getName());
        IndexLookup indexLookup = new IndexLookup();
        for (Column column : table.getColumns()) {
            KeyInfo primaryKeyInfo = null;
            if (indexLookup.isPrimaryKeyColumn(column)) {
                primaryKeyInfo = new KeyInfo();
                primaryKeyInfo.setKey(column);
            }
            if (primaryKeyInfo != null) {
                // list of columns in other table that link to this primary key
                List<Column> referencing = indexLookup.getReferencing(column);
                primaryKeyInfo.setLinks(referencing);
                primaryKeys.add(primaryKeyInfo);
            }

            KeyInfo foreignKeyInfo = null;
            List<Column> referenced = indexLookup.getReferencedColumns(column);
            if ((referenced != null) && (referenced.size() > 0)) {
                foreignKeyInfo = new KeyInfo();
                foreignKeyInfo.setKey(column);
                foreignKeyInfo.setLinks(referenced);
                foreignKeys.add(foreignKeyInfo);
            }
        }

        collectTables();

        genHeader(writer);
        try {
            genNodes(table, writer);
        } finally {
            genFooter(writer);
        }
    }

    public void gen(Table table, File outFile) throws IOException {
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(new BufferedWriter(new FileWriter(outFile)));
            gen(table, writer);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } finally {
                    if (writer != null) {
                        writer = null;
                    }
                }
            }
        }
    }

    private void collectTables() {
        for (KeyInfo keyInfo : primaryKeys) {
            Column key = keyInfo.getKey();
            addColumn(key);
            List<Column> links = keyInfo.getLinks();
            for (Column link : links) {
                addColumn(link);
            }
        }
        for (KeyInfo keyInfo : foreignKeys) {
            Column key = keyInfo.getKey();
            addColumn(key);
            List<Column> links = keyInfo.getLinks();
            for (Column link : links) {
                addColumn(link);
            }
        }
    }

    private void addColumn(Column key) {
        String tableName = key.getTable().getName();
        String columnName = key.getName();
        Set<String> columns = tablesData.get(tableName);
        if (columns == null) {
            columns = new TreeSet<String>();
            tablesData.put(tableName, columns);
        }
        columns.add(columnName);
    }

    private void genHeader(PrintWriter writer) {
        writer.println("// Online graphviz chart generator: http://graphviz-dev.appspot.com/");
        writer.println("// Generated on: " + new Date());
        writer.println("digraph g {");
        writer.println("  graph [ rankdir = \"LR\" ];");

        writer.println("  node [ fontsize = \"16\" shape = \"record\" ];");
    }

    private void genNodes(Table table, PrintWriter writer) {
        genMainTableNode(table, writer);

        genOtherTableNodes(table, writer);
    }

    private void genMainTableNode(Table table, PrintWriter writer) {
        String tableName = table.getName();

        writer.println();
        writer.println("  // Main table: " + tableName);
        writer.println("  // primary key(s): " + primaryKeys.size());
        writer.println("  // foreign key(s): " + foreignKeys.size());

        genNode(tableName, writer);
    }

    private void genNode(String tableName, PrintWriter writer) {
        Set<String> columns = tablesData.get(tableName);
        writer.println("  \"" + tableName + "\" [");
        writer.print("    label = \"" + tableName);
        for (String column : columns) {
            writer.print("|");
            writer.print("<" + column + ">" + column);
        }
        writer.print("\"");
        writer.println();
        writer.println("  ];");

    }

    private void genOtherTableNodes(Table table, PrintWriter writer) {
        String mainTableName = table.getName();

        writer.println();
        writer.println("  // other tables");

        for (String tableName : tablesData.keySet()) {
            if (tableName.equals(mainTableName)) {
                continue;
            }
            genNode(tableName, writer);
        }

        // "node0":f0 -> "node1":f0
        writer.println();
        writer.println("  // primary key(s)");
        String edgePort = " [headport=c,tailport=c] ";
        edgePort = " ";
        for (KeyInfo keyInfo : primaryKeys) {
            Column key = keyInfo.getKey();
            List<Column> links = keyInfo.getLinks();
            for (Column link : links) {
                // writer.println("  // " + getColumnFullName(link) + " -> " +
                // getColumnFullName(key));
                writer.println("  " + getColumnGraphVizName(link) + " -> " + getColumnGraphVizName(key) + edgePort + ";");
            }
        }

        writer.println();
        writer.println("  // foreign key(s)");
        for (KeyInfo keyInfo : foreignKeys) {
            Column key = keyInfo.getKey();
            List<Column> links = keyInfo.getLinks();
            for (Column link : links) {
                // writer.println("  // " + getColumnFullName(key) + " -> " +
                // getColumnFullName(link));
                writer.println("   " + getColumnGraphVizName(key) + " -> " + getColumnGraphVizName(link) + edgePort + ";");
            }
        }
    }

    private String getColumnGraphVizName(Column column) {
        StringBuilder sb = new StringBuilder();

        sb.append("\"");
        sb.append(column.getTable().getName());
        sb.append("\"");

        sb.append(":");
        sb.append(column.getName());

        return sb.toString();

    }

    private String getColumnFullName(Column key) {
        return key.getTable().getName() + "." + key.getName();
    }

    private void genFooter(PrintWriter writer) {
        writer.println("}");
    }
}
