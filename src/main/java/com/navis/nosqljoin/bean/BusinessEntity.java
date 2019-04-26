package com.navis.nosqljoin.bean;

import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Data
@AllArgsConstructor
public class BusinessEntity {

    private String name;

    private String primaryDocument;

    private String primaryIdFieldName;
}
