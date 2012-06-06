package com.le.sunriise.password;

import java.io.File;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.ObjectMapper;
import org.junit.Assert;
import org.junit.Test;

public class GenBruteForceContextTest {
    private static final Logger log = Logger.getLogger(GenBruteForceContextTest.class);

    @Test
    public void testReadWrite() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        GenBruteForceContext context = new GenBruteForceContext();
        File dst = File.createTempFile("context", ".json");

        log.info("file=" + dst);
        dst.deleteOnExit();

        mapper.writeValue(dst, context); // where 'dst' can be File,
                                         // OutputStream or Writer

        GenBruteForceContext context2 = mapper.readValue(dst, GenBruteForceContext.class);
        Assert.assertNotNull(context2);

        Assert.assertTrue(context2.equals(context));
        
        GenBruteForceContext context3 = new GenBruteForceContext(context2);
        Assert.assertTrue(context3.equals(context2));
        
        context3.setBuffer(null);
        Assert.assertTrue(! context3.equals(context2));
        
        context2.setBuffer(null);
        Assert.assertTrue(context3.equals(context2));

    }

}
