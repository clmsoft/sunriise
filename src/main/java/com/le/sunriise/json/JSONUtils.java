package com.le.sunriise.json;

import java.io.IOException;
import java.io.Writer;

import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.PrettyPrinter;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.ObjectWriter;
import org.codehaus.jackson.util.DefaultPrettyPrinter;

public class JSONUtils {
    public static void writeValue(Object value, Writer w, boolean prettyPrint) throws JsonGenerationException,
            JsonMappingException, IOException {
        ObjectMapper mapper = new ObjectMapper();
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
}
