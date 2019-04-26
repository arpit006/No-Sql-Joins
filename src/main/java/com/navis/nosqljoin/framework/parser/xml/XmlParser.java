package com.navis.nosqljoin.framework.parser.xml;

import com.navis.nosqljoin.bean.Table;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Component
public class XmlParser<T> {

    public Map<String, Table<T>> parseXml(String inFileName) {
        XmlDefaultHandler defaultHandler = new XmlDefaultHandler();

        SAXParserFactory saxParserFactory = SAXParserFactory.newInstance();
        InputStream is = null;

        try {
            SAXParser saxParser = saxParserFactory.newSAXParser();
            is = getClass().getResourceAsStream(inFileName);
            saxParser.parse(is, defaultHandler);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            e.printStackTrace();
        } finally {
            try {
                if (is != null) {
                    is.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return defaultHandler.getTableMap();
    }

    /*public static void main(String[] args) {
        XmlParser xmlParser = new XmlParser();
        Map<String, Table> stringTableMap = xmlParser.parseXml("/entity_relationship_mapping.xml");
        for (Map.Entry<String, Table> val: stringTableMap.entrySet()) {
            System.out.println("******** \n"+val + "\n *******");
        }
    }*/
}
