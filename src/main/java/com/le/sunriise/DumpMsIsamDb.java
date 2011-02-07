package com.le.sunriise;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import com.healthmarketscience.jackcess.CodecProvider;
import com.healthmarketscience.jackcess.Column;
import com.healthmarketscience.jackcess.CryptCodecProvider;
import com.healthmarketscience.jackcess.Database;
import com.healthmarketscience.jackcess.ExportFilter;
import com.healthmarketscience.jackcess.ExportUtil;
import com.healthmarketscience.jackcess.SimpleExportFilter;
import com.healthmarketscience.jackcess.Table;
import com.healthmarketscience.jackcess.query.Query;

public class DumpMsIsamDb {
	private static final Logger log = Logger.getLogger(DumpMsIsamDb.class);

	private File dbFile = null;
	private Database db = null;

	public DumpMsIsamDb(File inFile, String password) throws IOException {
		CodecProvider cryptCodecProvider = new CryptCodecProvider(password);
		boolean readOnly = true;
		boolean autoSync = true;
		Charset charset = null;
		TimeZone timeZone = null;
		this.dbFile = inFile;
		this.db = Database.open(inFile, readOnly, autoSync, charset, timeZone,
				cryptCodecProvider);
	}

	private void exportAll(File dir) throws IOException {
		ExportUtil.exportAll(db, dir, "csv", true);
	}

	private void writeToDir(File outDir) throws IOException {
		PrintWriter writer = null;
		try {
			File file = new File(outDir, "db.txt");
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			// writer.println("file=" + dbFile);
			// writer.println("fileFormat: " + db.getFileFormat());
			writer.println(db.toString());

			writer.println("");
			List<Query> queries = db.getQueries();
			writer.println("getQueries, " + queries.size());
			for (Query query : queries) {
				writer.println(query.toSQLString());
			}
			Set<String> tableNames = db.getTableNames();
			// writer.println("tableNames.size: " + tableNames.size());
			for (String tableName : tableNames) {
				try {
					log.info("tableName=" + tableName);
					writeTableInfo(tableName, outDir);
				} catch (IOException e) {
					log.warn("Cannot write table info for tableName="
							+ tableName);
				}
			}
		} finally {
			if (writer != null) {
				writer.close();
			}
			writer = null;
		}
	}

	private void writeTableInfo(String tableName, File outDir)
			throws IOException {
		File dir = new File(outDir, tableName);
		if ((!dir.exists()) && (!dir.mkdirs())) {
			throw new IOException("Cannot create directory, dir=" + dir);
		}

		Table table = db.getTable(tableName);

		File file = new File(dir, "table.txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.println(table.toString());
		} finally {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}

		writeColumnsInfo(table, dir);

		writeRowsInfo(table, dir);
	}

	private void writeColumnsInfo(Table table, File dir) throws IOException {
		// Table table = db.getTable(tableName);
		File columnsFile = null;
		// columnsFile = new File(dir, "columns.txt");
		if (columnsFile != null) {
			PrintWriter writer = null;
			try {
				writer = new PrintWriter(new BufferedWriter(new FileWriter(
						columnsFile)));
				writer.println("#name: " + table.getName());
				writer.println("#columns: " + table.getColumnCount());
				writer.println("");
				List<Column> columns = table.getColumns();
				for (Column column : columns) {
					writer.println(column.getName() + ","
							+ column.getType().toString());
				}
			} finally {
				if (writer != null) {
					writer.close();
					writer = null;
				}
			}
		}

		File columnsDir = new File(dir, "columns.d");
		if ((!columnsDir.exists()) && (!columnsDir.mkdirs())) {
			throw new IOException("Cannot create directory, dir=" + columnsDir);
		}
		List<Column> columns = table.getColumns();
		for (Column column : columns) {
			writeColumnsInfo(columnsDir, column);
		}
	}

	private void writeColumnsInfo(File columnsDir, Column column)
			throws IOException {
		File file = new File(columnsDir, column.getName() + ".txt");
		PrintWriter writer = null;
		try {
			writer = new PrintWriter(new BufferedWriter(new FileWriter(file)));
			writer.println(column.toString());
		} finally {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}
	}

	private void writeRowsInfo(Table table, File dir) throws IOException {
		File file = new File(dir, "rows.csv");
		BufferedWriter writer = null;
		try {
			writer = new BufferedWriter(new FileWriter(file));
			ExportFilter filter = SimpleExportFilter.INSTANCE;
			ExportUtil.exportWriter(db, table.getName(), writer, true,
					ExportUtil.DEFAULT_DELIMITER,
					ExportUtil.DEFAULT_QUOTE_CHAR, filter);
		} finally {
			if (writer != null) {
				writer.close();
				writer = null;
			}
		}

	}

	private void close() {
		if (db != null) {
			try {
				db.close();
			} catch (IOException e) {
				log.warn(e);
			} finally {
				db = null;
			}
		}
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String inFileName = null;
		String outDirName = null;
		String password = null;
		if (args.length == 2) {
			inFileName = args[0];
			outDirName = args[1];
			password = null;
		} else if (args.length == 3) {
			inFileName = args[0];
			outDirName = args[1];
			password = args[2];
		} else {
			Class clz = DumpMsIsamDb.class;
			System.out.println("Usage: " + clz.getName()
					+ " sample.mny outDir [password]");
			System.exit(1);
		}

		File inFile = new File(inFileName);
		File outDir = new File(outDirName);

		log.info("inFile=" + inFile);
		log.info("outDir=" + outDir);

		DumpMsIsamDb dbHelper = null;
		try {
			if (!inFile.exists()) {
				throw new IOException("File="
						+ inFile.getAbsoluteFile().getAbsolutePath()
						+ " does not exist.");
			}
			dbHelper = new DumpMsIsamDb(inFile, password);
			if ((!outDir.exists()) && (!outDir.mkdirs())) {
				throw new IOException("Cannot create directory, outDir="
						+ outDir);
			}
			dbHelper.writeToDir(outDir);
			// dbHelper.exportAll(outDir);
		} catch (IllegalStateException e) {
			// java.lang.IllegalStateException: Incorrect password provided
			log.error(e);
		} catch (IOException e) {
			log.error(e);
		} finally {
			if (dbHelper != null) {
				dbHelper.close();
			}
		}
		log.info("< DONE");
	}
}
