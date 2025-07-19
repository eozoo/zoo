/*
 * Copyright (c) 2017～2025 Cowave All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0.txt
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific language governing permissions and limitations under the License.
 */
package com.cowave.commons.framework.helper.es;

import com.cowave.commons.client.http.asserts.HttpException;
import com.cowave.commons.client.http.asserts.HttpHintException;
import com.cowave.commons.client.http.response.Response;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.util.EntityUtils;
import org.apache.lucene.search.TotalHits;
import org.elasticsearch.action.admin.indices.delete.DeleteIndexRequest;
import org.elasticsearch.action.admin.indices.settings.put.UpdateSettingsRequest;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.MultiGetItemResponse;
import org.elasticsearch.action.get.MultiGetRequest;
import org.elasticsearch.action.get.MultiGetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.IndicesOptions;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.client.indices.CreateIndexRequest;
import org.elasticsearch.client.indices.GetIndexRequest;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.reindex.DeleteByQueryRequest;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.xcontent.XContentType;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static com.cowave.commons.client.http.constants.HttpCode.INTERNAL_SERVER_ERROR;

/**
 * @author shanhuiming
 */
@Slf4j
@ConditionalOnClass(RestHighLevelClient.class)
@RequiredArgsConstructor
@Component
public class EsHelper {

    private final RestHighLevelClient restHighLevelClient;

    private final ObjectMapper objectMapper;

    public <T extends HitEntity> List<T> getByIds(String index, Collection<String> idList, Class<T> clazz) {
        MultiGetRequest getRequest = new MultiGetRequest();
        idList.forEach(id -> getRequest.add(new MultiGetRequest.Item(index, id)));
        try {
            MultiGetResponse response = restHighLevelClient.mget(getRequest, RequestOptions.DEFAULT);
            List<T> list = new ArrayList<>();
            for (MultiGetItemResponse item : response.getResponses()) {
                if (item.getResponse().isExists()) {
                    T hitEntity = objectMapper.readValue(item.getResponse().getSourceAsString(), clazz);
                    hitEntity.setId(item.getId());
                    list.add(hitEntity);
                }
            }
            return list;
        } catch (Exception e) {
            log.error("ES query failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public SearchResponse query(String index, SearchSourceBuilder searchSourceBuilder) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        try {
            return restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES query failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public <T extends
            HitEntity> Response.Page<T> query(String index, SearchSourceBuilder searchSourceBuilder, Class<T> clazz) {
        SearchRequest searchRequest = new SearchRequest(index);
        searchRequest.source(searchSourceBuilder);
        searchRequest.indicesOptions(IndicesOptions.lenientExpandOpen());
        try {
            SearchResponse searchResponse = restHighLevelClient.search(searchRequest, RequestOptions.DEFAULT);
            if (searchResponse == null) {
                throw new HttpException(INTERNAL_SERVER_ERROR, "ES query failed, index=" + index);
            }

            SearchHits searchHits = searchResponse.getHits();
            if (searchHits == null) {
                return new Response.Page<>();
            }

            SearchHit[] hits = searchHits.getHits();
            List<T> list = new ArrayList<>();
            for (SearchHit hit : hits) {
                T hitEntity = objectMapper.readValue(hit.getSourceAsString(), clazz);
                hitEntity.setId(hit.getId());
                list.add(hitEntity);
            }

            TotalHits totalHits = searchHits.getTotalHits();
            return new Response.Page<>(list, totalHits != null ? totalHits.value : 0);
        } catch (Exception e) {
            log.error("ES query failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public Optional<String> query(String index, String dsl) {
        String path = String.format("/%s/_search?ignore_unavailable=true", index);
        Request request = new Request("GET", path);
        request.setJsonEntity(dsl);
        try {
            org.elasticsearch.client.Response response = restHighLevelClient.getLowLevelClient().performRequest(request);
            if (response == null || response.getEntity() == null) {
                return Optional.empty();
            }
            return Optional.of(EntityUtils.toString(response.getEntity(), StandardCharsets.UTF_8));
        } catch (Exception e) {
            log.error("ES query failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void insert(String index, Object data) {
        IndexRequest indexRequest = new IndexRequest(index);
        try {
            indexRequest.source(objectMapper.writeValueAsString(data), XContentType.JSON);
            restHighLevelClient.index(indexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES insert failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void update(String index, String id, Map<String, Object> dataMap) {
        UpdateRequest updateRequest = new UpdateRequest(index, id);
        updateRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        updateRequest.doc(dataMap);
        try {
            restHighLevelClient.update(updateRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES update failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void delete(String index, String id) {
        DeleteRequest deleteRequest = new DeleteRequest(index, id);
        deleteRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        try {
            restHighLevelClient.delete(deleteRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES delete failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void deleteByQuery(String index, QueryBuilder queryBuilder) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(queryBuilder);
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES delete failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void bulkDelete(String index, List<String> idList) {
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        for (String id : idList) {
            bulkRequest.add(new DeleteRequest(index, id));
        }
        try {
            restHighLevelClient.bulk(bulkRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES delete failed, index={}", index, e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public boolean indexExist(String index) {
        try {
            return restHighLevelClient.indices().exists(new GetIndexRequest(index), RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES failed", e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void indexCreate(String index, String mappingProperties) {
        if (indexExist(index)) {
            return;
        }

        CreateIndexRequest createIndexRequest = new CreateIndexRequest(index);
        createIndexRequest.source(mappingProperties, XContentType.JSON);
        try {
            restHighLevelClient.indices().create(createIndexRequest, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES failed", e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void indexDelete(String index) {
        try {
            if (indexExist(index)) {
                restHighLevelClient.indices().delete(new DeleteIndexRequest(index), RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            log.error("ES failed", e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void indexClean(String index) {
        DeleteByQueryRequest request = new DeleteByQueryRequest(index);
        request.setQuery(QueryBuilders.matchAllQuery());
        request.setConflicts("proceed");
        try {
            restHighLevelClient.deleteByQuery(request, RequestOptions.DEFAULT);
        } catch (Exception e) {
            log.error("ES failed", e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }

    public void indexSetting(String index, Settings.Builder builder) {
        try {
            if (indexExist(index)) {
                UpdateSettingsRequest settingsRequest = new UpdateSettingsRequest(index);
                settingsRequest.settings(builder);
                restHighLevelClient.indices().putSettings(settingsRequest, RequestOptions.DEFAULT);
            }
        } catch (Exception e) {
            log.error("ES failed", e);
            throw new HttpHintException(INTERNAL_SERVER_ERROR);
        }
    }
}
