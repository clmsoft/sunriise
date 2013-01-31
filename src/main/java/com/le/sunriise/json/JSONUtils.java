/*******************************************************************************
 * Copyright (c) 2010 Hung Le
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
package com.le.sunriise.json;

import java.io.CharArrayWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;

import org.apache.log4j.Logger;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.PrettyPrinter;
import com.fasterxml.jackson.core.util.DefaultPrettyPrinter;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;

public class JSONUtils {
    private static final Logger log = Logger.getLogger(JSONUtils.class);
    
    public static void writeValue(Object value, Writer w, boolean prettyPrint) throws JsonGenerationException,
            JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true); // Jackson 1.2+
        if (prettyPrint) {
            PrettyPrinter pp = new DefaultPrettyPrinter();
            ObjectWriter objectWriter = mapper.writer(pp);
            objectWriter.writeValue(w, value);
        } else {
            mapper.writeValue(w, value);
        }
    }

    public static void writeValue(Object value, Writer w) throws JsonGenerationException, JsonMappingException, IOException {
        boolean prettyPrint = true;
        writeValue(value, w, prettyPrint);
    }

    public static void writeValue(Object value, File file) throws IOException {
        FileWriter writer = null;
        try {
            writer = new FileWriter(file);
            writeValue(value, writer);
        } catch (JsonGenerationException e) {
            throw new IOException(e);
        } catch (JsonMappingException e) {
            throw new IOException(e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    log.warn(e);
                } finally {
                    writer = null;
                }
            }
        }
    }

    public static String valueToString(Object value) throws IOException {
        String str = null;
        CharArrayWriter writer = new CharArrayWriter();
        try {
            try {
                JSONUtils.writeValue(value, writer);
            } catch (JsonGenerationException e) {
                throw new IOException(e);
            } catch (JsonMappingException e) {
                throw new IOException(e);
            }
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                    str = new String(writer.toCharArray());
                } finally {
                    writer = null;
                }
            }
        }
        return str;
    }

}
