package com.navis.nosqljoin.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Data
@NoArgsConstructor
public class RefTable<T> {

    private T ref;

    private T type;

    private T parentColumn;

    private Set<RefTable<T>> join;
}
