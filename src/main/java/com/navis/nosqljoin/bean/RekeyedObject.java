package com.navis.nosqljoin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RekeyedObject {

    private String reKeyedDocumentName;

    private Object reKeyedPrimaryKeyValue;
}
