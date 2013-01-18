/*******************************************************************************
 * Copyright (c) 2012 Hung Le
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307
 * USA
 *******************************************************************************/
package com.le.sunriise.password.bruteforce;

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
