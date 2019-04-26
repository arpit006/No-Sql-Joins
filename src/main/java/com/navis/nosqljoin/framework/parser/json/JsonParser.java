package com.navis.nosqljoin.framework.parser.json;

import com.fasterxml.jackson.core.type.TypeReference;
import com.navis.nosqljoin.framework.util.JsonUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Component
public class JsonParser {

    private final Logger LOGGER = LoggerFactory.getLogger(JsonParser.class);

    private final JsonUtil jsonUtil;

    private JsonParser(JsonUtil jsonUtil) {
        this.jsonUtil = jsonUtil;

    }

    public ParsedObject parseJson(Object inJson) {
        Map<String, Object> jsonObject = jsonUtil.fromJson(inJson, new TypeReference<Map<String, Object>>() {
        });
        String tableName = (String) jsonObject.get("table");
        Long timeStamp = (Long) jsonObject.get("timestamp");
        Map<String, Serializable> afterObject = (Map<String, Serializable>) jsonObject.get("after");
        Map<String, Object> afterValue = new LinkedHashMap<>();
        if (afterObject != null) {
            List<Map<String, Serializable>> valueList = (List<Map<String, Serializable>>) afterObject.get("value");
            afterValue = convertToMap(valueList);
        }
        return ParsedObject
                .builder()
                .tableName(tableName)
                .timeStamp(timeStamp)
                .value(afterValue)
                .build();
    }

    private Map<String, Object> convertToMap(List<Map<String, Serializable>> inValueList) {
        if (inValueList != null && !inValueList.isEmpty()) {
            return inValueList.stream()
                    .collect(HashMap::new, (m, v) -> m.put((String) v.get("key"),
                            getSerializedValue((String) v.get("type"), v.get("value"))), HashMap::putAll);
        }
        return null;
    }

    private Object getSerializedValue(String jsonType, Object tempValue) {
        if (tempValue != null) {
            Serializable value = null;
            switch (jsonType) {
                case "int32":
                    value = (Integer) tempValue;
                    break;
                case "int64":
                    if (tempValue instanceof Integer) {
                        value = Long.valueOf((Integer) tempValue);
                    } else if (tempValue instanceof Long) {
                        value = (long) tempValue;
                    }
                    break;
                case "float32":
                    value = (Float) tempValue;
                    break;
                case "float64":
                    value = (Double) tempValue;
                    break;
                case "string":
                    value = (String) tempValue;
                    break;
                case "Timestamp":
                    if (tempValue instanceof Integer) {
                        Timestamp ts = new Timestamp(Long.valueOf((Integer) tempValue));
                        value = new Date(ts.getTime());
                    } else {
                        Timestamp ts = new Timestamp((Long) tempValue);
                        value = new Date(ts.getTime());
                    }
                    break;
                case "boolean":
                    value = (boolean) tempValue;
                    break;
                default:
                    LOGGER.error("Type not available for parsing" + jsonType);
            }
            return value;
        }
        return null;
    }

    /*public static void main(String[] args) {
        JsonParser jsonParser = new JsonParser();
        File file = new File("Z:\\BI Microservices\\poc-elastic\\nosqljoin\\src\\main\\resources\\Sample.json");
        String s = null;
        try {
            byte[] b = Files.readAllBytes(file.toPath());
            s = new String(b, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }

        ParsedObject parsedObject = jsonParser.parseJson(s);
        System.out.println(parsedObject.getTableName());
        System.out.println(parsedObject.getTimeStamp());
        parsedObject.getValue().entrySet().stream().forEach(t -> System.out.println(t.getKey()+ " - " + t.getValue()));

    }*/
}
