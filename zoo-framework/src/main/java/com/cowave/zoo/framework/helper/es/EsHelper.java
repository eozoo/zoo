package com.cowave.zoo.framework.helper.es;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.cowave.zoo.http.client.response.Response;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @author shanhuiming
 */
@Slf4j
@Component
@RequiredArgsConstructor
@ConditionalOnClass(ElasticsearchClient.class)
public class EsHelper {
    private final ElasticsearchClient client;

    @SneakyThrows
    public <T extends HitEntity> List<T> getByIds(String index, Collection<String> ids, Class<T> type) {
        var response = client.mget(request -> request.index(index).ids(new ArrayList<>(ids)), type);
        List<T> result = new ArrayList<>();
        response.docs().forEach(item -> {
            if (item.isResult() && item.result().found() && item.result().source() != null) {
                T entity = item.result().source();
                entity.setId(item.result().id());
                result.add(entity);
            }
        });
        return result;
    }

    @SneakyThrows
    public <T extends HitEntity> Response.Page<T> query(
            String index, Query query, int from, int size, String sortField, Class<T> type) {
        var response = client.search(request -> {
            request.index(index).query(query);
            if (from >= 0) {
                request.from(from);
            }
            if (size > 0) {
                request.size(size);
            }
            if (sortField != null) {
                request.sort(sort -> sort.field(field ->
                        field.field(sortField).order(SortOrder.Desc)));
            }
            return request;
        }, type);
        List<T> result = new ArrayList<>();
        for (Hit<T> hit : response.hits().hits()) {
            if (hit.source() != null) {
                hit.source().setId(hit.id());
                result.add(hit.source());
            }
        }
        long total = response.hits().total() == null ? result.size() : response.hits().total().value();
        return new Response.Page<>(result, total);
    }

    @SneakyThrows
    public void insert(String index, Object document) {
        client.index(request -> request.index(index).document(document));
    }

    @SneakyThrows
    public void deleteByQuery(String index, Query query, boolean refresh) {
        client.deleteByQuery(request -> request.index(index).query(query)
                .conflicts(co.elastic.clients.elasticsearch._types.Conflicts.Proceed)
                .refresh(refresh));
    }

    @SneakyThrows
    public boolean indexExist(String index) {
        return client.indices().exists(request -> request.index(index)).value();
    }

    @SneakyThrows
    public void indexCreate(String index, String mappingJson) {
        if (!indexExist(index)) {
            client.indices().create(request ->
                    request.index(index).withJson(new StringReader(mappingJson)));
        }
    }

    @SneakyThrows
    public void indexDelete(String index) {
        if (indexExist(index)) {
            client.indices().delete(request -> request.index(index));
        }
    }

    @SneakyThrows
    public void indexSetting(String index, int maxResultWindow) {
        if (indexExist(index)) {
            client.indices().putSettings(request -> request.index(index)
                    .settings(settings -> settings.maxResultWindow(maxResultWindow)));
        }
    }
}
