package com.le.sunriise.qif;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

import org.apache.log4j.Logger;

public class QifParser {
    private static final Logger log = Logger.getLogger(QifParser.class);

    private Map<String, Long> headers = new LinkedHashMap<String, Long>();

    /**
     * @param args
     */
    public static void main(String[] args) {
        File inFile = null;

        if (args.length != 1) {
            Class<QifParser> clz = QifParser.class;
            System.out.println("Usage: java " + clz.getName() + " file.qif");
            System.exit(1);
        }
        inFile = new File(args[0]);

        QifParser parser = new QifParser();
        log.info("> START inFile=" + inFile);
        try {
            parser.parse(inFile);
        } catch (IOException e) {
            log.error(e, e);
        } finally {
            log.info("< DONE");
        }

    }

    private void parse(File inFile) throws IOException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(inFile));
            parse(reader);
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

    }

    private void parse(BufferedReader reader) throws IOException {
        String line = null;
        boolean inRecord = false;

        while ((line = reader.readLine()) != null) {
            if (line.startsWith("!")) {
                Long count = headers.get(line);
                if (count == null) {
                    count = new Long(0L);
                    headers.put(line, count);
                }
                count = count + 1L;
                headers.put(line, count);
                if (inRecord) {
                    log.warn("See new header. Last record does not close properly");
                }
                inRecord = false;
            } else {
                if (line.startsWith("^")) {
                    inRecord = false;
                } else {

                }
            }
        }

        for (String header : headers.keySet()) {
            Long count = headers.get(header);
            log.info(header + ", " + count);
        }
    }

}
