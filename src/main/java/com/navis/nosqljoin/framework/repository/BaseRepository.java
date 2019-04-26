package com.navis.nosqljoin.framework.repository;

import com.navis.nosqljoin.framework.service.ServiceBase;
import com.navis.nosqljoin.framework.util.JsonUtil;
import org.apache.http.HttpHost;
import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author <a href="mailto:arpit.srivastava@navis.com">Arpit Srivastava</a>
 */
@Repository
public class BaseRepository<T> {

    private RestHighLevelClient CLIENT;

    private final Logger _logger = LoggerFactory.getLogger(ServiceBase.class);


    public BaseRepository() {
        CLIENT = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("10.43.131.186", 9200, "http"),
                        new HttpHost("10.43.131.186", 9300, "http")
                ));
    }

    public T save(IndexRequest indexRequest) {
        IndexResponse indexResponse = null;
        ActionListener<IndexResponse> listener = new ActionListener<IndexResponse>() {
            @Override
            public void onResponse(IndexResponse indexResponse) {
                _logger.info("Entity saved into elasticsearch... at index... " + indexResponse.getIndex());
                _logger.info("Running on Thread :: -> " + Thread.currentThread().getName());
            }

            @Override
            public void onFailure(Exception e) {
                _logger.info("OOPS!. This entity could not be saved into elasticsearch....");
            }
        };
        CLIENT.indexAsync(indexRequest, RequestOptions.DEFAULT, listener);
        return (T) JsonUtil.toJson(indexResponse);
    }

    public Map<String, Map<String, Object>> get(MultiGetRequest getRequest) {
        MultiGetResponse getResponse = null;
        try {
            getResponse = CLIENT.mget(getRequest, RequestOptions.DEFAULT);
            _logger.info("Running on thread :: ---> " + Thread.currentThread().getName());
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (getResponse == null) {
            return null;
        }
        return Arrays.stream(getResponse.getResponses())
                .map(MultiGetItemResponse::getResponse)
                .collect(HashMap::new, (m, v) -> m.put(v.getIndex(), v.getSource()), HashMap::putAll);
    }
}
