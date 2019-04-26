package com.navis.nosqljoin.framework.service;

import com.navis.nosqljoin.bean.BusinessEntity;
import com.navis.nosqljoin.bean.RefTable;
import com.navis.nosqljoin.bean.RekeyedObject;
import com.navis.nosqljoin.bean.Table;
import com.navis.nosqljoin.framework.parser.json.JsonParser;
import com.navis.nosqljoin.framework.parser.json.ParsedObject;
import com.navis.nosqljoin.framework.parser.xml.XmlParser;
import com.navis.nosqljoin.framework.repository.BaseRepository;
import com.navis.nosqljoin.framework.util.JsonUtil;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.search.fetch.subphase.FetchSourceContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Service
public class ServiceBase<T> {

    private final Logger _logger = LoggerFactory.getLogger(ServiceBase.class);

    private final JsonParser jsonParser;

    private final XmlParser<String> xmlParser;

    private final BaseRepository<String> baseRepository;

    private Map<String, BusinessEntity> businessEntityMap = new HashMap<>();

    private String EVENTTIMESTAMP = "eventTimestamp";

    private String CURRENTTIMESTAMP = "currentTimestamp";

    private Map<String, Table<String>> xmlParsedValueMap = new HashMap<>();

    public ServiceBase(JsonParser jsonParser, XmlParser<String> xmlParser, BaseRepository<String> baseRepository) {
        this.jsonParser = jsonParser;
        this.xmlParser = xmlParser;
        this.baseRepository = baseRepository;
    }

    @PostConstruct
    public void initializeXml() {
        xmlParsedValueMap = xmlParser.parseXml("/entity_relationship_mapping.xml");
        businessEntityMap.put("unit", new BusinessEntity("unit", "inv_unit", "gkey"));
    }

    public Map<String, Map<String, Object>> save(T t) {

        Map<String, Map<String, Object>> joinMap = new HashMap<>();

        ParsedObject parsedObject = jsonParser.parseJson(t);
        parsedObject.getValue().put(EVENTTIMESTAMP, parsedObject.getTimeStamp());
        parsedObject.getValue().put(CURRENTTIMESTAMP, new Date());

        String id = String.valueOf(parsedObject
                .getValue()
                .get(xmlParsedValueMap.get(parsedObject.getTableName()).getPrimaryKey()));

        IndexRequest indexRequest = new IndexRequest()
                .index(parsedObject.getTableName())
                .type(parsedObject.getTableName())
                .id(id)
                .source(parsedObject.getValue());

        String obj = baseRepository.save(indexRequest);

        _logger.info(obj);

        joinMap.put(parsedObject.getTableName(), parsedObject.getValue());

        boolean isTransform = getAllRequiredDocuments(
                xmlParsedValueMap.get(parsedObject.getTableName()).getJoins(),
                parsedObject.getTableName(),
                joinMap);

        if (!isTransform) {
            return Collections.emptyMap();
        }

        Map<String, Object> dateMap = new HashMap<>();
        dateMap.put(CURRENTTIMESTAMP, new Date());
        joinMap.put(CURRENTTIMESTAMP, dateMap);
        BusinessEntity businessEntity = businessEntityMap.get(
                xmlParsedValueMap.get(parsedObject.getTableName())
                        .getBusinessEntityName());

        if (xmlParsedValueMap.get(parsedObject.getTableName()).getReKeyedTableName() == null) {
            saveBusinessEntity(joinMap, businessEntity);
        } else {
            reindexBusinessEntity(parsedObject.getTableName(), joinMap);
        }
        return joinMap;
    }

    private boolean getAllRequiredDocuments(Set<RefTable<String>> refTable,
                                            String parentTableName,
                                            Map<String, Map<String, Object>> result) {

        Map<String, String> refTypeMap = new HashMap<>();
        if (!refTable.isEmpty()) {
            MultiGetRequest multiGetRequest = new MultiGetRequest();
            for (RefTable<String> value : refTable) {
                refTypeMap.put(value.getRef(), value.getType());

                if (value.getType().equals("inner") && result.get(parentTableName).get(value.getParentColumn()) == null) {
                    return false;
                } else if (value.getType().equals("left")) {
                    if (result.get(parentTableName) == null || result.get(parentTableName).get(value.getParentColumn()) == null) {
                        continue;
                    }
                }
                MultiGetRequest.Item getRequest = new MultiGetRequest.Item(value.getRef(),
                        value.getRef(),
                        String.valueOf(result.get(parentTableName).get(value.getParentColumn())))
                        .fetchSourceContext(FetchSourceContext.FETCH_SOURCE);

                multiGetRequest.add(getRequest);
            }
            if (multiGetRequest.getItems().size() == 0) {
                return true;
            }

            Map<String, Map<String, Object>> availableValueMap = baseRepository.get(multiGetRequest);
            result.putAll(availableValueMap);


            for (RefTable<String> value : refTable) {
                if (result.containsKey(value.getRef()) && result.get(value.getRef()) != null) {
                    getAllRequiredDocuments(value.getJoin(), value.getRef(), result);
                } else if (result.get(value.getRef()) == null
                        && refTypeMap.containsKey(value.getRef())
                        && refTypeMap.get(value.getRef()).equals("inner")) {
                    return false;
                }
            }
        }
        return true;
    }

    private void saveBusinessEntity(Map<String, Map<String, Object>> joinMap,
                                    BusinessEntity businessEntity) {

        IndexRequest indexRequest = new IndexRequest()
                .index(businessEntity.getName())
                .type(businessEntity.getName())
                .id(String.valueOf(joinMap
                        .get(businessEntity.getPrimaryDocument())
                        .get(businessEntity.getPrimaryIdFieldName())))
                .source(joinMap);

        String obj1 = baseRepository.save(indexRequest);

        _logger.info("\nBusiness Entity\n" + obj1);
    }

    private void reindexBusinessEntity(String documentName,
                                       Map<String, Map<String, Object>> joinMap) {

        RekeyedObject reKeyedNameValueObject = getReKeyedIdValueAndDocumentName(documentName, joinMap);

        if (reKeyedNameValueObject.getReKeyedDocumentName() != null &&
                reKeyedNameValueObject.getReKeyedPrimaryKeyValue() != null) {

            String reKeyedIndexName = reKeyedNameValueObject.getReKeyedDocumentName();
            String documentId = String.valueOf(reKeyedNameValueObject.getReKeyedPrimaryKeyValue());

            IndexRequest indexRequest = new IndexRequest()
                    .index(reKeyedIndexName)
                    .type(reKeyedIndexName)
                    .id(documentId)
                    .source(joinMap);

            String s = baseRepository.save(indexRequest);
            _logger.info("Re Indexed Business Entity:: " + s);

            boolean isTransform = false;
            if (xmlParsedValueMap.get(reKeyedIndexName).getJoins().size() != 0) {
                isTransform = getAllRequiredDocuments(xmlParsedValueMap.get(reKeyedIndexName).getJoins(), reKeyedIndexName, joinMap);
            }
            if (xmlParsedValueMap.get(reKeyedIndexName).getReKeyedTableName() != null &&
                    getReKeyedIdValueAndDocumentName(reKeyedIndexName, joinMap).getReKeyedPrimaryKeyValue() != null) {
                reindexBusinessEntity(reKeyedIndexName, joinMap);
            }

            if (isTransform) {
                saveBusinessEntity(joinMap,
                        businessEntityMap.get(xmlParsedValueMap.get(documentName).getBusinessEntityName()));
            } else {
                _logger.info("\nBusiness Entity could not be formed... All the necessary entities are not present... \n" + JsonUtil.toJson(joinMap));
            }
        }
    }

    private RekeyedObject getReKeyedIdValueAndDocumentName(String documentName,
                                                           Map<String, Map<String, Object>> joinMap) {

        String reKeyedIndexName = xmlParsedValueMap.get(documentName).getReKeyedTableName();
        if (xmlParsedValueMap.get(reKeyedIndexName).getType().equals("rekeyed")) {
            String[] primaryDocumentObject = xmlParsedValueMap.get(reKeyedIndexName).getPrimaryKey().split("\\.");
            String indexName = primaryDocumentObject[0];
            String primaryDocId = primaryDocumentObject[1];

            return new RekeyedObject(reKeyedIndexName,
                    joinMap.get(indexName)
                            .get(primaryDocId));
        }
        return new RekeyedObject();
    }
}
