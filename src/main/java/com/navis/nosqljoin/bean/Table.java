package com.navis.nosqljoin.bean;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Data
@NoArgsConstructor
public class Table<T> {

    private T id;

    private T primaryKey;

    private T aliasName;

    private T type;

    private T reKeyedTableName;

    private Set<RefTable<T>> joins;

    private T businessEntityName;
}
