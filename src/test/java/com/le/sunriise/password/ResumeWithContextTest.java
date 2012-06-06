package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Ignore;
import org.junit.Test;

public class ResumeWithContextTest {
    private final class AlarmTask implements Runnable {
        private CheckBruteForce checker;
        private long millis;

        public AlarmTask(CheckBruteForce checker, long millis) {
            this.checker = checker;
            this.millis = millis;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(millis);
                if (checker != null) {
                    checker.setTerminate(true);
                    log.info("Terminate checker.");
                }
            } catch (InterruptedException e) {
                log.warn(e);
            }
        }
    }

    private static final Logger log = Logger.getLogger(ResumeWithContextTest.class);

    @Ignore
    @Test
    public void testResumeWithContext() throws IOException {
        log.info("###");
        log.info("> START - testResumeWithContext");

        File contextFile = new File("bruteForceContext.json");
        contextFile.exists();
        if (!contextFile.delete()) {
            String message = "Cannot delete contextFile=" + contextFile;
            log.error(message);
            throw new IOException(message);
        }

        File dbFile = new File("src/test/data/sunset-sample-pwd.mny");
        // 123@ABC!
        String expectedPassword = "123@ABC!";
        char[] mask = "*******".toCharArray();
        char[] alphabets = GenBruteForce.ALPHABET_US_KEYBOARD_MNY;
        String password = null;

        int loops = 0;
        mask = "****ABC!".toCharArray();
        while (password == null) {
            CheckBruteForce checker = null;
            try {
                HeaderPage headerPage = new HeaderPage(dbFile);

                if (contextFile.exists()) {
                    ObjectMapper mapper = new ObjectMapper();
                    GenBruteForceContext context = mapper.readValue(contextFile, GenBruteForceContext.class);
                    Assert.assertNotNull(context);
                    log.info("### RESUME from context, "
                            + CheckBruteForce.printIntArray(context.getCurrentCursorIndex(), context.getAlphabets()));
                    log.info("### RESUME from context, cursor=" + context.getCursor());
                    checker = new CheckBruteForce(headerPage, context);
                } else {
                    log.info("### RESUME-NO");
                    int passwordLength = mask.length;
                    checker = new CheckBruteForce(headerPage, passwordLength, mask, alphabets);
                }

                AlarmTask cmd = new AlarmTask(checker, 60 * 1000L);
                Thread t = new Thread(cmd);
                t.setDaemon(true);
                t.start();

                checker.check();
                password = checker.getPassword();
                log.info("password=" + password);
            } finally {
                if (checker != null) {
                    try {
                        checker.shutdown();
                    } finally {
                        checker = null;
                    }
                }
                loops++;
                log.info("LOOP-ITERATION DONE, loops=" + loops);
            }
        }

        Assert.assertNotNull(password);
        Assert.assertTrue(password.compareToIgnoreCase(expectedPassword) == 0);
    }
}
