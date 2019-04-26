package com.navis.nosqljoin.framework.parser.json;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Builder;
import lombok.Value;

import java.util.Map;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Value
@Builder(toBuilder = true)
@JsonDeserialize(builder = ParsedObject.ParsedObjectBuilder.class)
public class ParsedObject {
    
    String tableName;
    
    Long timeStamp;
    
    Map<String, Object> value;
}
