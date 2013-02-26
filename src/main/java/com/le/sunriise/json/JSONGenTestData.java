package com.le.sunriise.json;

import java.io.File;

import org.apache.log4j.Logger;

import com.le.sunriise.export.ExportToJSON;
import com.le.sunriise.misc.MnyTestFile;

public class JSONGenTestData {
    private static Logger log = Logger.getLogger(JSONGenTestData.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        String outputDirName = null;

        if (args.length == 1) {
            outputDirName = args[0];
        } else {
            outputDirName = "src/test/data/json";
        }
        File outputDir = new File(outputDirName);
        outputDir.mkdirs();

        log.info("outputDir=" + outputDir.getAbsolutePath());

        for (MnyTestFile file : MnyTestFile.SAMPLE_FILES) {
            try {
                exportFile(file, outputDir);
            } catch (Exception e) {
                log.error(e, e);
            }
        }
    }

    protected static void exportFile(MnyTestFile file, File outputDir) {
        String[] exportArgv = null;

        String password = file.getPassword();
        if (password != null) {
            password = password.trim();
            if (password.length() <= 0) {
                password = null;
            }
        }

        File f = new File(file.getFileName());
        File ff = new File(outputDir, f.getName());

        if (password == null) {
            exportArgv = new String[2];
            exportArgv[0] = file.getFileName();
            exportArgv[1] = ff.getAbsolutePath();
        } else {
            exportArgv = new String[3];
            exportArgv[0] = file.getFileName();
            exportArgv[1] = file.getPassword();
            exportArgv[2] = ff.getAbsolutePath();
        }

        ExportToJSON.main(exportArgv);
    }
}
