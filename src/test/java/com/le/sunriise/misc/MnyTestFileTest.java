package com.le.sunriise.misc;

import java.io.File;

import org.junit.Assert;
import org.junit.Test;

public class MnyTestFileTest {

    @Test
    public void test() {
        for (MnyTestFile testFile : MnyTestFile.SAMPLE_FILES) {
            File file = new File(testFile.getFileName());
            Assert.assertNotNull(file);
            Assert.assertTrue(file.exists());
        }
    }

}
