package com.le.sunriise.json;

import org.apache.log4j.Logger;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.schema.JsonSchema;

import com.le.sunriise.mnyobject.Category;

public class JsonSchemaCmd {
    private static final Logger log = Logger.getLogger(JsonSchemaCmd.class);

    /**
     * @param args
     */
    public static void main(String[] args) {
        ObjectMapper mapper = new ObjectMapper();

        JsonSchema jsonSchema = null;
        try {
            jsonSchema = mapper.generateJsonSchema(Category.class);
            System.out.println(jsonSchema.toString());
        } catch (JsonMappingException e) {
            log.warn(e);
        } finally {

        }

    }

}
