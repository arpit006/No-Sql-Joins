package com.navis.nosqljoin.framework.parser.xml;

import com.navis.nosqljoin.bean.RefTable;
import com.navis.nosqljoin.bean.Table;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Stack;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
public class XmlDefaultHandler<T> extends DefaultHandler {

    private Stack<RefTable<String>> st;
    private Table<String> table;
    private Map<String, Table<T>> tableMap = new HashMap<>();
    private RefTable<String> refTable;
    private String businessEntity;

    @Override
    public void startElement(String uri,
                             String localName,
                             String qName,
                             Attributes attributes) throws SAXException {

        if (qName.equalsIgnoreCase("Entity")) {
            businessEntity = attributes.getValue("name");
        }

        if (qName.equalsIgnoreCase("table")) {
            table = new Table<>();
            st = new Stack<>();
            table.setId(attributes.getValue("id"));
            table.setPrimaryKey(attributes.getValue("primary-key"));
            table.setAliasName(attributes.getValue("alias-name"));
            table.setType(attributes.getValue("type"));
            table.setReKeyedTableName(attributes.getValue("rekeyed-table-name"));
            table.setJoins(new HashSet<>());
        }

        if (qName.equalsIgnoreCase("join")) {
            refTable = new RefTable<>();
            refTable.setRef(attributes.getValue("ref"));
            refTable.setType(attributes.getValue("type"));
            refTable.setParentColumn(attributes.getValue("parent-column"));
            refTable.setJoin(new HashSet<>());
            st.push(refTable);
        }
    }

    @Override
    public void endElement(String uri,
                           String localName,
                           String qName) throws SAXException {

        if (qName.equalsIgnoreCase("join")) {
            RefTable<String> temp = st.pop();
            if (st.isEmpty()) {
                table.getJoins().add(temp);
            } else {
                st.peek().getJoin().add(temp);
            }
        }

        if (qName.equalsIgnoreCase("table")) {
            table.setBusinessEntityName(businessEntity);
            tableMap.put(table.getId(), (Table<T>) table);
        }
    }

    protected Map<String, Table<T>> getTableMap() {
        return tableMap;
    }
}
