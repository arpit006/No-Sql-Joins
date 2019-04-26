package com.navis.nosqljoin.framework.util;

import com.fasterxml.jackson.annotation.JsonInclude.Include;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Component
public class JsonUtil {

    private static ObjectMapper mapper = new ObjectMapper();

    static {
        mapper.configure(JsonParser.Feature.ALLOW_COMMENTS, true);
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);
        mapper.setSerializationInclusion(Include.NON_NULL);
    }

    private static ObjectMapper getObjectMapper() {
        return mapper;
    }

    public static String toJson(Object object) {
        String s = null;
        try {
            s = getObjectMapper().writeValueAsString(object);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return s;
    }

    public <T> T fromJson(Object inJson, TypeReference<T> inReference) {
        T t = null;
        try {
            t = getObjectMapper().readValue(inJson.toString(), new TypeReference<T>() {
            });
        } catch (IOException e) {
            e.printStackTrace();
        }
        return t;
    }
}
